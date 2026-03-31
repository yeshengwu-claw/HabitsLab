package com.habitslab.ui.habit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitslab.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalHabits = uiState.habits.size
    val completedToday = uiState.habits.count { it.completedToday }
    val totalStreak = uiState.habits.maxOfOrNull { it.streak } ?: 0
    val overallRate = if (totalHabits > 0) uiState.habits.map { it.weeklyRate }.average().toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            LocalDate.now().format(
                                DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINESE)
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.habits_add_title))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                StatsHeader(
                    completedToday = completedToday,
                    totalHabits = totalHabits,
                    totalStreak = totalStreak,
                    overallRate = overallRate
                )
            }

            item {
                Text(
                    stringResource(R.string.habits_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.habits.isEmpty()) {
                item {
                    EmptyState(onAddClick = { viewModel.showAddDialog() })
                }
            } else {
                items(uiState.habits, key = { it.habit.id }) { habitWithRecord ->
                    HabitCard(
                        habitWithRecord = habitWithRecord,
                        onToggle = { viewModel.toggleHabitCompletion(habitWithRecord.habit.id) },
                        onDelete = { viewModel.deleteHabit(habitWithRecord.habit) },
                        onClick = { viewModel.selectHabit(habitWithRecord) }
                    )
                }
            }
        }

        if (uiState.showAddDialog) {
            AddHabitDialog(
                onDismiss = { viewModel.hideAddDialog() },
                onConfirm = { name, icon, color -> viewModel.addHabit(name, icon, color) }
            )
        }

        if (uiState.showDetailSheet && uiState.selectedHabit != null) {
            HabitDetailSheet(
                habitWithRecord = uiState.selectedHabit!!,
                selectedMonth = uiState.selectedMonth,
                onDismiss = { viewModel.hideDetailSheet() },
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )
        }
    }
}

@Composable
fun StatsHeader(
    completedToday: Int,
    totalHabits: Int,
    totalStreak: Int,
    overallRate: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.stats_today_complete),
                value = "$completedToday/$totalHabits",
                subtitle = stringResource(R.string.stats_habits),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.stats_streak),
                value = "$totalStreak",
                subtitle = stringResource(R.string.stats_days),
                icon = Icons.Default.LocalFireDepartment,
                color = Color(0xFFFF9800)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.stats_weekly_rate),
                value = "${(overallRate * 100).toInt()}%",
                subtitle = "",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = Color(0xFF2196F3)
            )
        }

        if (totalHabits > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.stats_progress), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            "$completedToday / $totalHabits",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
            Text(
                if (subtitle.isNotEmpty()) "$subtitle · $title" else title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HabitCard(
    habitWithRecord: HabitWithTodayRecord,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val habit = habitWithRecord.habit
    val isCompleted = habitWithRecord.completedToday

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                Color(habit.color).copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(habit.color))
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isCompleted,
                    transitionSpec = {
                        scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                    },
                    label = "icon"
                ) { _ ->
                    Text(habit.icon, fontSize = 26.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (isCompleted) Color(habit.color) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (habitWithRecord.streak > 0) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Text(
                            " ${habitWithRecord.streak}${stringResource(R.string.stats_days)}",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        " ${(habitWithRecord.weeklyRate * 100).toInt()}%/${stringResource(R.string.stats_weekly_rate)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isCompleted,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(habit.color))
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("🌱", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.habits_empty_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.habits_empty_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.habits_create_button))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailSheet(
    habitWithRecord: HabitWithTodayRecord,
    selectedMonth: YearMonth,
    onDismiss: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val habit = habitWithRecord.habit
    val records = habitWithRecord.records
    val firstDayOfMonth = selectedMonth.atDay(1)
    val lastDayOfMonth = selectedMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val completedDays = records.filter { it.completed }.map { it.date }.toSet()

    val weekdays = listOf(
        stringResource(R.string.weekday_sun),
        stringResource(R.string.weekday_mon),
        stringResource(R.string.weekday_tue),
        stringResource(R.string.weekday_wed),
        stringResource(R.string.weekday_thu),
        stringResource(R.string.weekday_fri),
        stringResource(R.string.weekday_sat)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(habit.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(habit.icon, fontSize = 24.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        stringResource(R.string.detail_total_completed, habitWithRecord.totalCompleted),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            " ${habitWithRecord.streak}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Text(
                        stringResource(R.string.detail_streak_label),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    "${selectedMonth.year}年${selectedMonth.monthValue}月",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = onNextMonth,
                    enabled = selectedMonth < YearMonth.now()
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = if (selectedMonth < YearMonth.now()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalCells = firstDayOfWeek + lastDayOfMonth.dayOfMonth
            val rows = (totalCells + 6) / 7

            Column {
                repeat(rows) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val cellIndex = row * 7 + col
                            val dayOfMonth = cellIndex - firstDayOfWeek + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayOfMonth in 1..lastDayOfMonth.dayOfMonth) {
                                    val date = selectedMonth.atDay(dayOfMonth)
                                    val isCompleted = completedDays.contains(date)
                                    val isToday = date == LocalDate.now()
                                    val isFuture = date.isAfter(LocalDate.now())

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCompleted -> Color(habit.color)
                                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .then(
                                                if (isToday && !isCompleted) {
                                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            dayOfMonth.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isCompleted -> Color.White
                                                isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(habit.color))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.detail_legend_done), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.detail_legend_today), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("✓") }
    var selectedColor by remember { mutableStateOf(0xFF4CAF50L) }

    val icons = listOf(
        "✓" to "icon_done",
        "💪" to "icon_fitness",
        "📚" to "icon_read",
        "🏃" to "icon_run",
        "💧" to "icon_water",
        "🧘" to "icon_meditate",
        "💤" to "icon_sleep",
        "🍎" to "icon_food",
        "✍️" to "icon_write",
        "🎯" to "icon_target",
        "🎨" to "icon_create",
        "🧹" to "icon_clean"
    )
    val colors = listOf(
        0xFF4CAF50L to "color_green",
        0xFF2196F3L to "color_blue",
        0xFFFF9800L to "color_orange",
        0xFFF44336L to "color_red",
        0xFF9C27B0L to "color_purple",
        0xFF00BCD4L to "color_cyan",
        0xFFE91E63L to "color_pink",
        0xFF607D8BL to "color_grayblue"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.habits_add_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.habits_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(stringResource(R.string.habits_icon_label), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(icons) { (icon, _) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIcon == icon) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(stringResource(R.string.habits_color_label), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(colors) { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedIcon, selectedColor) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
