package com.viralhost.solarleads.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.viralhost.solarleads.ui.nav.SolarLeadsNavHost
import com.viralhost.solarleads.ui.theme.SolarLeadsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialLeadId = intent?.getLongExtra("leadId", -1L)?.takeIf { it > 0L }
        setContent {
            SolarLeadsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SolarLeadsNavHost(initialLeadId = initialLeadId)
                }
            }
        }
    }
}
