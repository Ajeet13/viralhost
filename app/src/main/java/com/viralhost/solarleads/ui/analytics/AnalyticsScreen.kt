package com.viralhost.solarleads.ui.analytics

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.RoofType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    vm: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.factory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Analytics") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Total leads", state.totalLeads.toString(), Modifier.weight(1f))
                StatCard("Calls (7d)", state.totalCalls.toString(), Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Pending callbacks", state.pendingCallbacks.toString(), Modifier.weight(1f))
                StatCard("Conversion", "${state.conversionRatePct}%", Modifier.weight(1f))
            }

            // Pipeline funnel
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pipeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    val maxCount = (state.statusCounts.values.maxOrNull() ?: 1).coerceAtLeast(1)
                    LeadStatus.values().forEach { s ->
                        val count = state.statusCounts[s] ?: 0
                        StatusBar(label = s.display, count = count, total = maxCount)
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            // Roof type breakdown (horizontal bars too, simple + readable)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Roof Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    val total = state.roofCounts.values.sum().coerceAtLeast(1)
                    RoofType.values().forEach { r ->
                        val count = state.roofCounts[r] ?: 0
                        val pct = (count * 100 / total)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.display, modifier = Modifier.width(64.dp))
                            ProgressBar(
                                fraction = if (total == 0) 0f else count / total.toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                            )
                            Text(
                                "  $count ($pct%)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Calls last 7 days bar chart
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Calls — last 7 days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    BarChart(
                        data = state.callsLast7Days,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        barColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatusBar(label: String, count: Int, total: Int) {
    val fraction = if (total == 0) 0f else count / total.toFloat()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyMedium)
        ProgressBar(
            fraction = fraction,
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
        )
        Text("  $count", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val fill = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        drawRect(track, size = Size(size.width, size.height))
        drawRect(fill, size = Size(size.width * fraction.coerceIn(0f, 1f), size.height))
    }
}

@Composable
private fun BarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barColor: Color
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodySmall)
        }
        return
    }
    val maxValue = (data.maxOf { it.second }).coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurface
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val n = data.size
            val gap = 8.dp.toPx()
            val barWidth = (size.width - gap * (n - 1)) / n
            val maxHeight = size.height
            data.forEachIndexed { i, (_, v) ->
                val h = (v.toFloat() / maxValue) * maxHeight
                val left = i * (barWidth + gap)
                val top = maxHeight - h
                drawRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, h)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { (label, count) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(count.toString(), style = MaterialTheme.typography.labelSmall)
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
