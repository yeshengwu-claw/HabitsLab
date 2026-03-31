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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitslab.R
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(viewModel: HabitViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showCelebration by remember { mutableStateOf(false) }
    var celebrationColor by remember { mutableStateOf(Color(0xFF4CAF50)) }

    val totalHabits = uiState.habits.size
    val completedToday = uiState.habits.count { it.completedToday }
    val totalStreak = uiState.habits.maxOfOrNull { it.streak } ?: 0
    val overallRate = if (totalHabits > 0) uiState.habits.map { it.weeklyRate }.average().toFloat() else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.showAddDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.habits_add_title))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { DateHeader() }
                item { StatsHeader(completedToday, totalHabits, totalStreak, overallRate) }
                item {
                    Text(stringResource(R.string.habits_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                }
                if (uiState.isLoading) {
                    item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                } else if (uiState.habits.isEmpty()) {
                    item { EmptyState(onAddClick = { viewModel.showAddDialog() }) }
                } else {
                    items(uiState.habits, key = { it.habit.id }) { habitWithRecord ->
                        HabitCard(
                            habitWithRecord = habitWithRecord,
                            onToggle = {
                                viewModel.toggleHabitCompletion(habitWithRecord.habit.id)
                                if (!habitWithRecord.completedToday) {
                                    celebrationColor = Color(habitWithRecord.habit.color)
                                    showCelebration = true
                                }
                            },
                            onDelete = { viewModel.deleteHabit(habitWithRecord.habit) },
                            onClick = { viewModel.selectHabit(habitWithRecord) }
                        )
                    }
                }
            }
        }

        if (showCelebration) {
            ConfettiCelebration(color = celebrationColor, onFinished = { showCelebration = false })
        }

        if (uiState.showAddDialog) {
            AddHabitDialog(onDismiss = { viewModel.hideAddDialog() }, onConfirm = { n, i, c -> viewModel.addHabit(n, i, c) })
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
fun DateHeader() {
    val today = LocalDate.now()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(today.format(DateTimeFormatter.ofPattern("M月d日")), fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE), fontWeight = FontWeight.Medium, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ConfettiCelebration(color: Color, onFinished: () -> Unit) {
    val messages = listOf("太棒了！🎉", "继续保持！💪", "加油！🌟", "你真厉害！👏", "太酷了！🔥", "完美！✨", "坚持就是胜利！🏆", "做得好！👍", "太牛了！🚀", "太赞了！💫")
    var currentMessage by remember { mutableStateOf(messages.random()) }

    // Firework bursts
    val bursts = remember {
        listOf(
            FireworkBurst(x = 0.3f, y = 0.35f, color = color),
            FireworkBurst(x = 0.7f, y = 0.4f, color = Color(0xFFFFD700)),
            FireworkBurst(x = 0.5f, y = 0.25f, color = Color(0xFFFF6B6B)),
            FireworkBurst(x = 0.2f, y = 0.5f, color = Color(0xFF4ECDC4)),
            FireworkBurst(x = 0.8f, y = 0.3f, color = Color(0xFFFFE66D))
        )
    }

    val particles = remember {
        bursts.flatMap { burst ->
            List(40) {
                val angle = (it * 9f) % 360f
                val speed = Random.nextFloat() * 0.3f + 0.1f
                FireworkParticle(
                    burstX = burst.x,
                    burstY = burst.y,
                    angle = angle,
                    speed = speed,
                    color = burst.color,
                    pSize = Random.nextFloat() * 6f + 3f,
                    delay = Random.nextFloat() * 0.3f
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "c")
    val time by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "t")

    LaunchedEffect(Unit) { delay(2500); onFinished() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val t = (time - p.delay).coerceIn(0f, 1f)
                val eased = 1f - (1f - t) * (1f - t)
                val cx = p.burstX + (cos(Math.toRadians(p.angle.toDouble())) * p.speed * eased).toFloat()
                val cy = p.burstY + (sin(Math.toRadians(p.angle.toDouble())) * p.speed * eased).toFloat()
                val alpha = (1f - t).coerceIn(0f, 1f)
                val currentSize = p.pSize * (1f - t * 0.5f)
                drawCircle(p.color.copy(alpha = alpha), currentSize, Offset(cx * size.width, cy * size.height))
            }
            bursts.forEach { burst ->
                val sparkAlpha = (1f - time * 2f).coerceIn(0f, 1f)
                if (sparkAlpha > 0f) {
                    drawCircle(burst.color.copy(alpha = sparkAlpha), 8f + time * 20f, Offset(burst.x * size.width, burst.y * size.height))
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = currentMessage, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
            color = Color.White, textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                val scale = 1f + sin(time * 10f) * 0.05f
                scaleX = scale
                scaleY = scale
            }
        )
    }
}

private data class FireworkBurst(val x: Float, val y: Float, val color: Color)
private data class FireworkParticle(val burstX: Float, val burstY: Float, val angle: Float, val speed: Float, val color: Color, val pSize: Float, val delay: Float)



@Composable
fun StatsHeader(completedToday: Int, totalHabits: Int, totalStreak: Int, overallRate: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.stats_today_complete), value = "$completedToday/$totalHabits", icon = Icons.Default.CheckCircle, color = Color(0xFF4CAF50))
            StatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.stats_streak), value = "$totalStreak", icon = Icons.Default.LocalFireDepartment, color = Color(0xFFFF9800))
            StatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.stats_weekly_rate), value = "${(overallRate * 100).toInt()}%", icon = Icons.AutoMirrored.Filled.TrendingUp, color = Color(0xFF2196F3))
        }
        if (totalHabits > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.stats_progress), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("$completedToday / $totalHabits", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun HabitCard(habitWithRecord: HabitWithTodayRecord, onToggle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    val habit = habitWithRecord.habit
    val isCompleted = habitWithRecord.completedToday

    // Bounce animation when completing
    val scaleAnim by animateFloatAsState(
        targetValue = if (isCompleted) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "s"
    )
    // Icon bounce
    val iconScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "icon"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scaleAnim)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 8.dp else 1.dp),
        border = if (isCompleted) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isCompleted) Color(habit.color) else Color.Transparent,
            shape = RoundedCornerShape(20.dp)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with star overlay
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isCompleted) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(habit.color),
                                    Color(habit.color).copy(alpha = 0.8f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(habit.color).copy(alpha = 0.5f),
                                    Color(habit.color).copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Text("⭐", fontSize = 28.sp, modifier = Modifier.scale(iconScale))
                } else {
                    Text(habit.icon, fontSize = 28.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (habitWithRecord.streak > 0) {
                        HabitChip("🔥", "${habitWithRecord.streak}天", if (isCompleted) Color.White.copy(alpha = 0.9f) else Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    HabitChip("📊", "${(habitWithRecord.weeklyRate * 100).toInt()}%", if (isCompleted) Color.White.copy(alpha = 0.9f) else Color(habit.color))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { habitWithRecord.weeklyRate },
                        modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = if (isCompleted) Color.White.copy(alpha = 0.9f) else Color(habit.color),
                        trackColor = if (isCompleted) Color.White.copy(alpha = 0.3f) else Color(habit.color).copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${habitWithRecord.totalCompleted}次",
                        fontSize = 11.sp,
                        color = if (isCompleted) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }
    }
}

@Composable
fun HabitChip(icon: String, text: String, color: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
fun EmptyState(onAddClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("🌱", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(stringResource(R.string.habits_empty_title), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(10.dp))
        Text(stringResource(R.string.habits_empty_subtitle), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(28.dp))
        Button(onClick = onAddClick, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.habits_create_button), fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailSheet(habitWithRecord: HabitWithTodayRecord, selectedMonth: YearMonth, onDismiss: () -> Unit, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    val habit = habitWithRecord.habit
    val records = habitWithRecord.records
    val firstDayOfMonth = selectedMonth.atDay(1)
    val lastDayOfMonth = selectedMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val completedDays = records.filter { it.completed }.map { it.date }.toSet()
    val weekdays = listOf(stringResource(R.string.weekday_sun), stringResource(R.string.weekday_mon), stringResource(R.string.weekday_tue), stringResource(R.string.weekday_wed), stringResource(R.string.weekday_thu), stringResource(R.string.weekday_fri), stringResource(R.string.weekday_sat))

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(28.dp), containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(habit.color)), contentAlignment = Alignment.Center) { Text(habit.icon, fontSize = 28.sp, color = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(stringResource(R.string.detail_total_completed, habitWithRecord.totalCompleted), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp)); Text(" ${habitWithRecord.streak}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFFF9800)) }
                    Text(stringResource(R.string.detail_streak_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousMonth) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", modifier = Modifier.size(28.dp)) }
                Text("${selectedMonth.year}年${selectedMonth.monthValue}月", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onNextMonth, enabled = selectedMonth < YearMonth.now()) { Icon(Icons.Default.ChevronRight, contentDescription = "Next", modifier = Modifier.size(28.dp), tint = if (selectedMonth < YearMonth.now()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) { weekdays.forEach { Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium) } }
            Spacer(modifier = Modifier.height(10.dp))
            val totalCells = firstDayOfWeek + lastDayOfMonth.dayOfMonth
            val rows = (totalCells + 6) / 7
            Column {
                repeat(rows) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val cellIndex = row * 7 + col
                            val dayOfMonth = cellIndex - firstDayOfWeek + 1
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
                                if (dayOfMonth in 1..lastDayOfMonth.dayOfMonth) {
                                    val date = selectedMonth.atDay(dayOfMonth)
                                    val isCompleted = completedDays.contains(date)
                                    val isToday = date == LocalDate.now()
                                    val isFuture = date.isAfter(LocalDate.now())
                                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(when { isCompleted -> Color(habit.color); isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f); else -> Color.Transparent }).then(if (isToday && !isCompleted) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)) else Modifier), contentAlignment = Alignment.Center) {
                                        Text(dayOfMonth.toString(), fontSize = 14.sp, fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal, color = when { isCompleted -> Color.White; isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f); else -> MaterialTheme.colorScheme.onSurface })
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(Color(habit.color)))
                Spacer(modifier = Modifier.width(8.dp)); Text(stringResource(R.string.detail_legend_done), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(24.dp))
                Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.width(8.dp)); Text(stringResource(R.string.detail_legend_today), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String, String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("✓") }
    var selectedColor by remember { mutableStateOf(0xFF4CAF50L) }
    val icons = listOf("✓", "💪", "📚", "🏃", "💧", "🧘", "💤", "🍎", "✍️", "🎯", "🎨", "🧹")
    val colors = listOf(0xFF4CAF50L, 0xFF2196F3L, 0xFFFF9800L, 0xFFF44336L, 0xFF9C27B0L, 0xFF00BCD4L, 0xFFE91E63L, 0xFF607D8BL)

    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(R.string.habits_add_title), fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.habits_name_label)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true, textStyle = TextStyle(fontSize = 16.sp))
                Spacer(modifier = Modifier.height(22.dp))
                Text(stringResource(R.string.habits_icon_label), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(icons) { icon -> Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(if (selectedIcon == icon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant).clickable { selectedIcon = icon }, contentAlignment = Alignment.Center) { Text(icon, fontSize = 24.sp) } } }
                Spacer(modifier = Modifier.height(22.dp))
                Text(stringResource(R.string.habits_color_label), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(colors) { color -> Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(color)).clickable { selectedColor = color }, contentAlignment = Alignment.Center) { if (selectedColor == color) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) } } }
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onConfirm(name, selectedIcon, selectedColor) }, enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.create)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
