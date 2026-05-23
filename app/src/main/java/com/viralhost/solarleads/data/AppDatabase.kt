package com.viralhost.solarleads.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.viralhost.solarleads.data.dao.CallLogDao
import com.viralhost.solarleads.data.dao.LeadDao
import com.viralhost.solarleads.data.dao.ReminderDao
import com.viralhost.solarleads.data.model.CallLog
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.Reminder

@Database(
    entities = [Lead::class, CallLog::class, Reminder::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun leadDao(): LeadDao
    abstract fun callLogDao(): CallLogDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solar_leads.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
