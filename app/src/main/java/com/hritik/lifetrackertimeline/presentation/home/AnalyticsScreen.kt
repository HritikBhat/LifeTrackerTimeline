package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hritik.lifetrackertimeline.R

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
                TrendCard(
                    trend = state.trendData,
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) }
                )
            }

            item {
                TotalHoursCard(
                    totalHours = state.totalHours,
                    delta = state.totalHoursDelta,
                    isWeekly = state.selectedPeriod == AnalyticsPeriod.WEEK
                )
            }

            item {
                DailyAvgCard(
                    avg = state.dailyAvg,
                    progress = state.dailyAvgProgress
                )
            }

            item {
                PeakProductivityCard(
                    data = state.peakProductivity,
                    peakTimeRange = state.peakTimeRange
                )
            }

            item {
                FocusAllocationCard(state.focusAllocation)
            }

            if (state.unproductiveActivities.isNotEmpty()) {
                item {
                    UnproductiveActivitiesCard(state.unproductiveActivities)
                }
            }

            item {
                MonthlyHighlightsSection(state.monthlyHighlights)
            }
        }
    }
}

@Composable
fun TrendCard(
    trend: List<Pair<String, Int>>,
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedPeriod == AnalyticsPeriod.WEEK) 
                            stringResource(R.string.weekly_trend) 
                        else 
                            "Monthly Trend",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = stringResource(R.string.activity_intensity_baseline),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PeriodTab(
                            text = stringResource(R.string.week),
                            isSelected = selectedPeriod == AnalyticsPeriod.WEEK,
                            onClick = { onPeriodSelected(AnalyticsPeriod.WEEK) }
                        )
                        PeriodTab(
                            text = stringResource(R.string.month),
                            isSelected = selectedPeriod == AnalyticsPeriod.MONTH,
                            onClick = { onPeriodSelected(AnalyticsPeriod.MONTH) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)) {
                if (trend.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxVal = (trend.maxOfOrNull { it.second } ?: 1).toFloat().coerceAtLeast(1f)
                        val stepX = size.width / (trend.size - 1).coerceAtLeast(1)
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
                        
                        if (selectedPeriod == AnalyticsPeriod.WEEK) {
                            points.forEach { offset ->
                                drawCircle(
                                    color = Color(0xFF3F51B5),
                                    radius = 3.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 1.5.dp.toPx(),
                                    center = offset
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trend.forEachIndexed { index, (label, _) ->
                    val isVisible = if (selectedPeriod == AnalyticsPeriod.MONTH) {
                        index % 5 == 0 || index == trend.size - 1
                    } else true
                    
                    if (isVisible) {
                        Text(
                            text = label, 
                            fontSize = 10.sp, 
                            color = Color.Gray,
                            modifier = Modifier.width(if (selectedPeriod == AnalyticsPeriod.MONTH) 25.dp else 40.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else Color.Gray,
            maxLines = 1
        )
    }
}

@Composable
fun TotalHoursCard(totalHours: String, delta: String, isWeekly: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D62ED)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (isWeekly) stringResource(R.string.this_week) else "THIS MONTH", 
                    color = Color.White.copy(alpha = 0.8f), 
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White)
            }
            Text(
                text = totalHours,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            Text(
                text = "$delta vs last ${if (isWeekly) "week" else "month"}", 
                color = Color.White.copy(alpha = 0.8f), 
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DailyAvgCard(avg: String, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000), label = "DailyAvgProgress"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = stringResource(R.string.daily_avg), color = Color.Gray, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = avg, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.goal_hours, "8h"), color = Color(0xFF3F51B5), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFF673AB7),
                trackColor = Color(0xFFF0F0F0)
            )
        }
    }
}

@Composable
fun PeakProductivityCard(data: List<Pair<String, Int>>, peakTimeRange: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.peak_productivity), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
//                Surface(color = Color(0xFFE8EAF6), shape = RoundedCornerShape(16.dp)) {
//                    Text(text = peakTimeRange, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 10.sp, color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
//                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                if (data.isNotEmpty() && data.any { it.second > 0 }) {
                    val maxVal = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
                    data.forEach { (label, value) ->
                        val heightFactor = value / maxVal
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight(heightFactor.coerceAtLeast(0.1f))
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                                    .background(if (value.toFloat() == maxVal && value > 0) Color(0xFF0047BB) else Color(0xFFE8EAF6))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = label, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    EmptyStateView(
                        description = stringResource(R.string.focus_allocation_empty_desc)
                    )
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
            Text(
                text = stringResource(R.string.focus_allocation),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            
            if (items.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                EmptyStateView(
                    description = stringResource(R.string.focus_allocation_empty_desc)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                items.forEach { item ->
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(item.color))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = stringResource(R.string.hours_value, String.format("%.1f", item.hours)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(item.percentage)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(Color(item.color))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnproductiveActivitiesCard(items: List<UnproductiveItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.unproductive_activities),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(item.color))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = stringResource(R.string.hours_value, String.format("%.1f", item.hours)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                if (items.last() != item) {
                    HorizontalDivider(modifier = Modifier.padding(start = 22.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun MonthlyHighlightsSection(items: List<MonthlyHighlightItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.monthly_highlights), 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold, 
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            color = Color(0xFF1A1C1E)
        )
        
        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    EmptyStateView(
                        description = stringResource(R.string.monthly_highlights_empty_desc)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.activity_name), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1.5f))
                Text(text = stringResource(R.string.total_duration), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(text = "Trend", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }

            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(item.color).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(item.icon), 
                                contentDescription = null, 
                                tint = Color(item.color), 
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", item.hours),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1C1E)
                            )
                            Text(
                                text = stringResource(R.string.hours_label), 
                                style = MaterialTheme.typography.labelSmall, 
                                color = Color.Gray
                            )
                        }

                        Box(modifier = Modifier.weight(1f).height(32.dp)) {
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
                                    drawPath(
                                        path = path, 
                                        color = Color(item.color), 
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            tint = Color(0xFFE0E0E0),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_data_available),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
