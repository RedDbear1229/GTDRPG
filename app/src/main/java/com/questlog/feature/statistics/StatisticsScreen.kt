package com.questlog.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.questlog.core.data.db.dao.D20Count
import com.questlog.core.data.db.dao.DayCount
import com.questlog.core.data.db.dao.DayXp
import com.questlog.core.data.db.dao.LifeAreaCount
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.LifeArea
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("통계") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { SummaryCard(state) }
                item {
                    ChartCard(title = "주간 퀘스트 완료") {
                        WeeklyTaskBarChart(state.weeklyDayCounts)
                    }
                }
                item {
                    ChartCard(title = "일별 XP 획득") {
                        DailyXpLineChart(state.dailyXpData)
                    }
                }
                item {
                    ChartCard(title = "D20 주사위 분포") {
                        D20DistributionChart(state.d20Distribution)
                    }
                }
                item {
                    ChartCard(title = "생활 영역별 퀘스트") {
                        LifeAreaPieChart(state.lifeAreaCounts)
                    }
                }
                if (state.character != null) {
                    item {
                        ChartCard(title = "능력치 레이더") {
                            AbilityRadarChart(state.character!!)
                        }
                    }
                }
                item {
                    ChartCard(title = "최근 28일 스트릭") {
                        StreakGrid(state.completedDates)
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ── 요약 카드 ──────────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(state: StatisticsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            state.character?.let { char ->
                Text(
                    "${char.name} · Lv.${char.level} ${char.classType.label}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SummaryItem("${state.totalCompleted}", "총 완료")
                SummaryItem("${state.totalXpEarned}", "총 XP")
                SummaryItem("${state.currentStreak}일", "스트릭")
                SummaryItem("${state.totalCritHits}", "크리티컬")
            }
        }
    }
}

@Composable
private fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── 차트 래퍼 카드 ────────────────────────────────────────────────────────────

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── Vico: 주간 바 차트 ────────────────────────────────────────────────────────

@Composable
private fun WeeklyTaskBarChart(data: List<DayCount>) {
    if (data.isEmpty()) {
        EmptyChart("완료한 퀘스트가 없습니다")
        return
    }
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        producer.runTransaction {
            columnSeries { series(y = data.map { it.count.toFloat() }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ ->
                    data.getOrNull(x.toInt())?.day?.takeLast(5) ?: ""
                }
            ),
        ),
        modelProducer = producer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    )
}

// ── Vico: 일별 XP 라인 차트 ──────────────────────────────────────────────────

@Composable
private fun DailyXpLineChart(data: List<DayXp>) {
    if (data.isEmpty()) {
        EmptyChart("XP 기록이 없습니다")
        return
    }
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        producer.runTransaction {
            lineSeries { series(y = data.map { it.xp.toFloat() }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ ->
                    data.getOrNull(x.toInt())?.day?.takeLast(5) ?: ""
                }
            ),
        ),
        modelProducer = producer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    )
}

// ── Vico: D20 분포 히스토그램 ─────────────────────────────────────────────────

@Composable
private fun D20DistributionChart(data: List<D20Count>) {
    if (data.isEmpty()) {
        EmptyChart("전투 기록이 없습니다")
        return
    }
    // 1~20 전체 슬롯을 채움 (롤되지 않은 숫자는 0)
    val fullData = (1..20).map { roll ->
        data.find { it.d20Result == roll }?.count?.toFloat() ?: 0f
    }
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        producer.runTransaction {
            columnSeries { series(y = fullData) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ -> (x.toInt() + 1).toString() }
            ),
        ),
        modelProducer = producer,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    )
}

// ── Canvas: 생활 영역 파이 차트 ───────────────────────────────────────────────

private val LIFE_AREA_COLORS = listOf(
    Color(0xFF6366F1), Color(0xFF22C55E), Color(0xFFF59E0B),
    Color(0xFFEC4899), Color(0xFF14B8A6), Color(0xFF8B5CF6), Color(0xFFF97316),
)

@Composable
private fun LifeAreaPieChart(data: List<LifeAreaCount>) {
    if (data.isEmpty()) {
        EmptyChart("완료한 퀘스트가 없습니다")
        return
    }
    val total = data.sumOf { it.count }.toFloat()
    val areas = LifeArea.entries
    val colorMap = areas.zip(LIFE_AREA_COLORS).toMap()

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            val diameter = min(size.width, size.height) * 0.9f
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            var startAngle = -90f
            data.forEach { entry ->
                val area = LifeArea.entries.firstOrNull { it.name == entry.lifeArea } ?: return@forEach
                val sweep = (entry.count / total) * 360f
                drawArc(
                    color = colorMap[area] ?: Color.Gray,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                )
                startAngle += sweep
            }
        }
        Spacer(Modifier.height(8.dp))
        // 범례
        data.chunked(2).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                chunk.forEach { entry ->
                    val area = LifeArea.entries.firstOrNull { it.name == entry.lifeArea }
                    val color = area?.let { colorMap[it] } ?: Color.Gray
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(color)
                        }
                        Text(
                            "${area?.label ?: entry.lifeArea} ${entry.count}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                if (chunk.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ── Canvas: 능력치 레이더 차트 ────────────────────────────────────────────────

@Composable
private fun AbilityRadarChart(character: Character) {
    val labels = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA")
    val values = listOf(
        character.strength, character.dexterity, character.constitution,
        character.intelligence, character.wisdom, character.charisma,
    ).map { it / 20f } // 20이 최대 (D&D 기준)

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.4f),
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = min(cx, cy) * 0.7f
        val n = 6
        val angleStep = (2 * PI / n).toFloat()
        val startAngle = (-PI / 2).toFloat()

        // 격자 3개
        listOf(0.33f, 0.66f, 1f).forEach { scale ->
            val path = Path()
            for (i in 0 until n) {
                val angle = startAngle + i * angleStep
                val x = cx + radius * scale * cos(angle)
                val y = cy + radius * scale * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, surfaceVariant, style = Stroke(width = 1.dp.toPx()))
        }

        // 축 선
        for (i in 0 until n) {
            val angle = startAngle + i * angleStep
            drawLine(
                color = surfaceVariant,
                start = Offset(cx, cy),
                end = Offset(cx + radius * cos(angle), cy + radius * sin(angle)),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // 데이터 폴리곤
        val dataPath = Path()
        for (i in 0 until n) {
            val angle = startAngle + i * angleStep
            val r = radius * values[i].coerceIn(0f, 1f)
            val x = cx + r * cos(angle)
            val y = cy + r * sin(angle)
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()
        drawPath(dataPath, primary.copy(alpha = 0.3f))
        drawPath(dataPath, primary, style = Stroke(width = 2.dp.toPx()))

        // 꼭짓점 점
        for (i in 0 until n) {
            val angle = startAngle + i * angleStep
            val r = radius * values[i].coerceIn(0f, 1f)
            drawCircle(primary, radius = 4.dp.toPx(), center = Offset(cx + r * cos(angle), cy + r * sin(angle)))
        }
    }

    // 레이블 (Canvas 밖 Row로 표시)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        labels.zip(values).forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("${(value * 20).toInt()}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Canvas: 스트릭 그리드 (28일) ──────────────────────────────────────────────

@Composable
private fun StreakGrid(completedDates: Set<String>) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val days = (27 downTo 0).map { today.minusDays(it.toLong()) }

    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
    ) {
        val cols = 7
        val rows = 4
        val cellSize = size.width / cols
        val cellPad = 3.dp.toPx()

        days.forEachIndexed { idx, date ->
            val col = idx % cols
            val row = idx / cols
            val x = col * cellSize + cellPad
            val y = row * (size.height / rows) + cellPad
            val w = cellSize - cellPad * 2
            val h = size.height / rows - cellPad * 2
            val isActive = completedDates.contains(date.format(formatter))
            drawRoundRect(
                color = if (isActive) primary else surfaceVariant,
                topLeft = Offset(x, y),
                size = Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            )
        }
    }
    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(days.first().format(DateTimeFormatter.ofPattern("M/d")),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("오늘", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── 빈 상태 ───────────────────────────────────────────────────────────────────

@Composable
private fun EmptyChart(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Suppress("unused")
private fun DrawScope.unused() = Unit
