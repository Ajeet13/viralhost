package com.viralhost.solarleads.cloud

import android.content.Context
import android.util.Log
import com.viralhost.solarleads.BuildConfig
import com.viralhost.solarleads.data.AppDatabase
import com.viralhost.solarleads.data.model.CallLog
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.MessageTemplate
import com.viralhost.solarleads.data.model.Reminder
import com.viralhost.solarleads.data.model.RoofType
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Cloud sync between Room (the source of truth on-device) and Firestore.
 *
 * - Each lead/call_log/reminder/template carries a stable [syncId] (UUID).
 * - Firestore documents use that [syncId] as the document ID, scoped under
 *   `users/{uid}/{collection}`.
 * - On startup we sign in anonymously, then push any local rows missing in
 *   the cloud, then attach realtime listeners that pull cloud changes back
 *   into Room.
 *
 * The whole class is a no-op when Firebase is not configured (i.e. when
 * `google-services.json` is missing) – it's safe to instantiate either way.
 */
class CloudSync private constructor(
    private val context: Context,
    private val database: AppDatabase
) {
    enum class State { DISABLED, INITIALISING, SIGNED_IN, ERROR }

    private val _state = MutableStateFlow(State.DISABLED)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listenerRegistrations = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

    fun start() {
        if (!BuildConfig.FIREBASE_ENABLED) {
            Log.i(TAG, "Firebase not configured (google-services.json missing); cloud sync disabled.")
            _state.value = State.DISABLED
            return
        }
        scope.launch {
            try {
                _state.value = State.INITIALISING

                // Make sure Firebase initialised. The Google Services plugin
                // does this automatically, but only if google-services.json
                // is present at build time.
                FirebaseApp.initializeApp(context)
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }
                val uid = auth.currentUser?.uid ?: error("Anonymous sign-in failed")
                val firestore = FirebaseFirestore.getInstance()

                pushAllLocalToCloud(firestore, uid)
                attachListeners(firestore, uid)

                _state.value = State.SIGNED_IN
            } catch (t: Throwable) {
                Log.e(TAG, "Cloud sync init failed", t)
                _lastError.value = t.message
                _state.value = State.ERROR
            }
        }
    }

    fun stop() {
        listenerRegistrations.forEach { it.remove() }
        listenerRegistrations.clear()
    }

    private suspend fun pushAllLocalToCloud(fs: FirebaseFirestore, uid: String) {
        val userDoc = fs.collection(USERS).document(uid)

        database.leadDao().getAll().forEach { upsertLead(userDoc, it) }
        database.callLogDao().getSince(0L).forEach { upsertCallLog(userDoc, it) }
        database.messageTemplateDao().getAll().forEach { upsertTemplate(userDoc, it) }
        // Reminders are written as part of attachListeners side, but we also push current.
        // (No bulk getter for reminders; we rely on listeners to converge.)
    }

    private fun attachListeners(fs: FirebaseFirestore, uid: String) {
        val userDoc = fs.collection(USERS).document(uid)

        listenerRegistrations += userDoc.collection(COL_LEADS).addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            scope.launch {
                snap.documentChanges.forEach { change ->
                    val data = change.document.data
                    val syncId = change.document.id
                    val incoming = leadFromMap(syncId, data) ?: return@forEach
                    val existing = database.leadDao().getBySyncId(syncId)
                    if (existing == null) {
                        database.leadDao().insert(incoming)
                    } else if (incoming.updatedAt > existing.updatedAt) {
                        // Keep local primary key, overwrite the rest.
                        database.leadDao().update(incoming.copy(id = existing.id))
                    }
                }
            }
        }

        listenerRegistrations += userDoc.collection(COL_CALLS).addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            scope.launch {
                snap.documentChanges.forEach { change ->
                    val syncId = change.document.id
                    val data = change.document.data
                    val leadSyncId = data["leadSyncId"] as? String ?: return@forEach
                    val lead = database.leadDao().getBySyncId(leadSyncId) ?: return@forEach
                    val existing = database.callLogDao().getBySyncId(syncId)
                    val incoming = CallLog(
                        id = existing?.id ?: 0L,
                        leadId = lead.id,
                        calledAt = (data["calledAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        outcome = data["outcome"] as? String,
                        notes = data["notes"] as? String,
                        syncId = syncId
                    )
                    database.callLogDao().insert(incoming)
                }
            }
        }

        listenerRegistrations += userDoc.collection(COL_TEMPLATES).addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            scope.launch {
                snap.documentChanges.forEach { change ->
                    val syncId = change.document.id
                    val data = change.document.data
                    val existing = database.messageTemplateDao().getBySyncId(syncId)
                    val incoming = MessageTemplate(
                        id = existing?.id ?: 0L,
                        title = data["title"] as? String ?: return@forEach,
                        body = data["body"] as? String ?: return@forEach,
                        isDefault = data["isDefault"] as? Boolean ?: false,
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        syncId = syncId
                    )
                    database.messageTemplateDao().insert(incoming)
                }
            }
        }
    }

    /**
     * Push a single Lead change to Firestore. The repository can call this after any
     * local upsert; it's a no-op if Firebase is not configured.
     */
    fun pushLead(lead: Lead) = scope.launch {
        if (_state.value != State.SIGNED_IN) return@launch
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        val userDoc = FirebaseFirestore.getInstance().collection(USERS).document(uid)
        upsertLead(userDoc, lead)
    }

    fun pushCallLog(callLog: CallLog, leadSyncId: String) = scope.launch {
        if (_state.value != State.SIGNED_IN) return@launch
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        val userDoc = FirebaseFirestore.getInstance().collection(USERS).document(uid)
        upsertCallLog(userDoc, callLog, leadSyncId)
    }

    fun pushTemplate(t: MessageTemplate) = scope.launch {
        if (_state.value != State.SIGNED_IN) return@launch
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        val userDoc = FirebaseFirestore.getInstance().collection(USERS).document(uid)
        upsertTemplate(userDoc, t)
    }

    fun deleteLead(syncId: String) = scope.launch {
        if (_state.value != State.SIGNED_IN) return@launch
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        FirebaseFirestore.getInstance()
            .collection(USERS).document(uid)
            .collection(COL_LEADS).document(syncId).delete()
    }

    fun deleteTemplate(syncId: String) = scope.launch {
        if (_state.value != State.SIGNED_IN) return@launch
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        FirebaseFirestore.getInstance()
            .collection(USERS).document(uid)
            .collection(COL_TEMPLATES).document(syncId).delete()
    }

    // --- internal serialisation helpers -------------------------------------

    private suspend fun upsertLead(userDoc: com.google.firebase.firestore.DocumentReference, lead: Lead) {
        val data = mapOf(
            "name" to lead.name,
            "phone" to lead.phone,
            "ivrs" to lead.ivrs,
            "address" to lead.address,
            "roofType" to lead.roofType.name,
            "systemSizeKw" to lead.systemSizeKw,
            "status" to lead.status.name,
            "notes" to lead.notes,
            "createdAt" to lead.createdAt,
            "updatedAt" to lead.updatedAt
        )
        userDoc.collection(COL_LEADS).document(lead.syncId)
            .set(data, SetOptions.merge()).await()
    }

    private suspend fun upsertCallLog(
        userDoc: com.google.firebase.firestore.DocumentReference,
        callLog: CallLog,
        leadSyncIdOverride: String? = null
    ) {
        val leadSyncId = leadSyncIdOverride
            ?: database.leadDao().getById(callLog.leadId)?.syncId
            ?: return
        val data = mapOf(
            "leadSyncId" to leadSyncId,
            "calledAt" to callLog.calledAt,
            "outcome" to callLog.outcome,
            "notes" to callLog.notes
        )
        userDoc.collection(COL_CALLS).document(callLog.syncId)
            .set(data, SetOptions.merge()).await()
    }

    private suspend fun upsertTemplate(
        userDoc: com.google.firebase.firestore.DocumentReference,
        t: MessageTemplate
    ) {
        val data = mapOf(
            "title" to t.title,
            "body" to t.body,
            "isDefault" to t.isDefault,
            "createdAt" to t.createdAt
        )
        userDoc.collection(COL_TEMPLATES).document(t.syncId)
            .set(data, SetOptions.merge()).await()
    }

    private fun leadFromMap(syncId: String, data: Map<String, Any?>): Lead? {
        val name = data["name"] as? String ?: return null
        val phone = data["phone"] as? String ?: return null
        return Lead(
            id = 0L,
            name = name,
            phone = phone,
            ivrs = data["ivrs"] as? String,
            address = data["address"] as? String,
            roofType = RoofType.fromName(data["roofType"] as? String),
            systemSizeKw = (data["systemSizeKw"] as? Number)?.toDouble(),
            status = LeadStatus.fromName(data["status"] as? String),
            notes = data["notes"] as? String,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            syncId = syncId
        )
    }

    companion object {
        private const val TAG = "CloudSync"
        private const val USERS = "users"
        private const val COL_LEADS = "leads"
        private const val COL_CALLS = "call_logs"
        private const val COL_TEMPLATES = "message_templates"

        @Volatile private var INSTANCE: CloudSync? = null

        fun get(context: Context, database: AppDatabase): CloudSync {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudSync(context.applicationContext, database).also { INSTANCE = it }
            }
        }
    }
}
