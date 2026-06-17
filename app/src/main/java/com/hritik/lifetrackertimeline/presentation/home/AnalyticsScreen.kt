package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FE)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeeklyTrendCard(state.weeklyTrend)
            }

            item {
                TotalHoursCard(state.totalHoursThisWeek)
            }

            item {
                DailyAvgCard(state.dailyAvg)
            }

            item {
                PeakProductivityCard(state.peakProductivity)
            }

            item {
                FocusAllocationCard(state.focusAllocation)
            }

            item {
                MonthlyHighlightsSection(state.monthlyHighlights)
            }
        }
    }
}

@Composable
fun WeeklyTrendCard(trend: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Trend",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Activity intensity vs. baseline",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "Week",
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Month",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                if (trend.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxVal = (trend.maxOfOrNull { it.second } ?: 1).toFloat().coerceAtLeast(1f)
                        val stepX = size.width / (trend.size - 1)
                        val points = trend.mapIndexed { index, pair ->
                            Offset(index * stepX, size.height - (pair.second / maxVal * size.height))
                        }

                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            points.forEachIndexed { index, offset ->
                                if (index > 0) lineTo(offset.x, offset.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF3F51B5).copy(alpha = 0.3f), Color.Transparent)
                            )
                        )

                        drawPath(
                            path = path,
                            color = Color(0xFF3F51B5),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                trend.forEach { (day, _) ->
                    Text(text = day, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TotalHoursCard(totalHours: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D62ED)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "THIS WEEK", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White)
            }
            Text(
                text = totalHours,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            Text(text = "+12% vs last week", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DailyAvgCard(avg: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "DAILY AVG", color = Color.Gray, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = avg, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Goal: 8h", color = Color(0xFF3F51B5), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFF673AB7),
                trackColor = Color(0xFFF0F0F0)
            )
        }
    }
}

@Composable
fun PeakProductivityCard(data: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Peak Productivity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(color = Color(0xFFE8EAF6), shape = RoundedCornerShape(16.dp)) {
                    Text(text = "10 AM - 12 PM", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 10.sp, color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                if (data.isNotEmpty()) {
                    val maxVal = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
                    data.forEach { (label, value) ->
                        val heightFactor = value / maxVal
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight(heightFactor.coerceAtLeast(0.1f))
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                                    .background(if (value.toFloat() == maxVal && value > 0) Color(0xFF0047BB) else Color(0xFFE8EAF6))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = label, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusAllocationCard(items: List<FocusAllocationItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Focus Allocation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            items.forEach { item ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(item.color)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Text(text = "${item.hours}h", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { item.percentage },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = Color(item.color),
                        trackColor = Color(0xFFF0F0F0)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyHighlightsSection(items: List<MonthlyHighlightItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Monthly Highlights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Activity Name", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1.5f))
            Text(text = "Total Duration", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
            Text(text = "Monthly Trend", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }

        items.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(item.color).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = getIconByName(item.icon), contentDescription = null, tint = Color(item.color), modifier = Modifier.size(20.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${item.hours}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Hours", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    Box(modifier = Modifier.weight(1f).height(30.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (item.trend.size > 1) {
                                val maxVal = (item.trend.maxOrNull() ?: 1).toFloat().coerceAtLeast(1f)
                                val stepX = size.width / (item.trend.size - 1)
                                val points = item.trend.mapIndexed { index, value ->
                                    Offset(index * stepX, size.height - (value / maxVal * size.height))
                                }
                                val path = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    points.forEach { lineTo(it.x, it.y) }
                                }
                                drawPath(path, color = Color(item.color), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    }
                }
            }
        }
    }
}
