# 10. 디자인 시스템

## 10.1 색상 팔레트

### 다크 테마 (기본)

```kotlin
// core/ui/theme/Color.kt
object QuestLogColors {
    
    // ─── 배경 ───────────────────────────────
    val Background = Color(0xFF0D1117)       // 가장 어두운 배경 (던전의 밤)
    val Surface = Color(0xFF161B22)          // 카드, 시트 배경
    val SurfaceVariant = Color(0xFF21262D)   // 구분선, 입력 필드 배경
    val SurfaceElevated = Color(0xFF2D333B)  // 떠있는 요소 (BottomSheet 상단)
    
    // ─── 주색 (골드, 판타지 황금) ───────────
    val Gold = Color(0xFFC19A6B)             // 주 강조색 (XP, 레벨, 버튼)
    val GoldLight = Color(0xFFD4B483)        // 밝은 골드 (Hover)
    val GoldDark = Color(0xFF8B6914)         // 어두운 골드 (눌림 상태)
    val GoldContainer = Color(0xFF2A2010)    // 골드 배경 (칩, 배지 배경)
    
    // ─── 보라 (마법, 신비) ──────────────────
    val Magic = Color(0xFF7C3AED)            // 마법 능력, 특수 효과
    val MagicLight = Color(0xFF9F67FF)       // 밝은 보라
    val MagicContainer = Color(0xFF1A0D2E)   // 보라 배경
    
    // ─── CR 색상 시스템 ─────────────────────
    val CrEasy = Color(0xFF10B981)           // CR 0-1: 초록 (슬라임/고블린)
    val CrNormal = Color(0xFFEAB308)         // CR 2-4: 노랑 (오크/리자드맨)
    val CrHard = Color(0xFFF97316)           // CR 5-9: 주황 (트롤)
    val CrVeryHard = Color(0xFFEF4444)       // CR 10-14: 빨강 (드래곤)
    val CrEpic = Color(0xFFA855F7)           // CR 15-19: 보라 (고대 드래곤)
    val CrLegendary = Color(0xFF18181B)      // CR 20+: 검정+금테 (리치/타라스크)
    
    // ─── 상태 색상 ──────────────────────────
    val Success = Color(0xFF10B981)          // 성공, 명중
    val Warning = Color(0xFFF59E0B)          // 경고, 마감 임박
    val Error = Color(0xFFEF4444)            // 실패, 크리티컬 미스, HP 위험
    val Info = Color(0xFF3B82F6)             // 정보
    
    // ─── HP 상태 색상 ───────────────────────
    val HpHealthy = Color(0xFF10B981)        // HP > 75%
    val HpTired = Color(0xFFEAB308)          // HP 50-75%
    val HpWounded = Color(0xFFF97316)        // HP 25-50%
    val HpCritical = Color(0xFFEF4444)       // HP < 25%
    
    // ─── 텍스트 ─────────────────────────────
    val TextPrimary = Color(0xFFE6EDF3)      // 주 텍스트
    val TextSecondary = Color(0xFF8B949E)    // 보조 텍스트
    val TextDisabled = Color(0xFF484F58)     // 비활성화
    val TextGold = Color(0xFFC19A6B)         // 강조 텍스트 (XP 수치 등)
    
    // ─── 생활 영역 색상 ─────────────────────
    val AreaWork = Color(0xFF2196F3)         // 업무: 파랑
    val AreaHealth = Color(0xFF4CAF50)       // 건강: 초록
    val AreaLearning = Color(0xFF9C27B0)     // 학습: 보라
    val AreaRelationship = Color(0xFFE91E63) // 관계: 분홍
    val AreaFinance = Color(0xFFFF9800)      // 재정: 주황
    val AreaPersonal = Color(0xFF00BCD4)     // 개인: 청록
    val AreaCreative = Color(0xFFFF5722)     // 창작: 딥 오렌지
}
```

---

## 10.2 타이포그래피

```kotlin
// core/ui/theme/Typography.kt
val QuestLogTypography = Typography(
    // 화면 제목 (WelcomeScreen 앱 로고 등)
    displayLarge = TextStyle(
        fontFamily = MedievalSharpFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    
    // 섹션 헤더 (캐릭터 이름, 레벨업 제목)
    headlineLarge = TextStyle(
        fontFamily = MedievalSharpFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    
    // 화면 제목 (TopAppBar 제목)
    titleLarge = TextStyle(
        fontFamily = MedievalSharpFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    
    // 카드 제목 (QuestCard 제목)
    titleMedium = TextStyle(
        fontFamily = NotoSansKRFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    
    // 본문 (설명 텍스트)
    bodyLarge = TextStyle(
        fontFamily = NotoSansKRFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    
    // 보조 본문 (메타 정보)
    bodyMedium = TextStyle(
        fontFamily = NotoSansKRFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    
    // 레이블 (배지, 태그)
    labelLarge = TextStyle(
        fontFamily = NotoSansKRFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    
    // 작은 레이블 (시간, 날짜 등 메타)
    labelSmall = TextStyle(
        fontFamily = NotoSansKRFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

// 폰트 패밀리
private val MedievalSharpFamily = FontFamily(
    Font(R.font.medievalsharp_regular, FontWeight.Normal),
    Font(R.font.medievalsharp_bold, FontWeight.Bold)
)

private val NotoSansKRFamily = FontFamily(
    Font(R.font.notosanskr_regular, FontWeight.Normal),
    Font(R.font.notosanskr_medium, FontWeight.Medium),
    Font(R.font.notosanskr_semibold, FontWeight.SemiBold),
    Font(R.font.notosanskr_bold, FontWeight.Bold)
)
```

---

## 10.3 공통 컴포넌트

### QuestCard

```kotlin
@Composable
fun QuestCard(
    task: Task,
    onComplete: (String) -> Unit,
    onTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val crColor = getCRColor(task.challengeRating)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap(task.id) },
        colors = CardDefaults.cardColors(
            containerColor = QuestLogColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row {
            // 좌측 CR 색상 바
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(crColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = QuestLogColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    CrBadge(cr = task.challengeRating)
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    task.context?.let {
                        ContextChip(context = it)
                    }
                    task.estimatedMinutes?.let {
                        TimeChip(minutes = it)
                    }
                    if (task.isRecurring) RecurringIcon()
                }
                
                task.dueDate?.let { due ->
                    DeadlineTimer(dueDate = due)
                }
            }
            
            // 완료 체크박스
            CompleteButton(
                onComplete = { onComplete(task.id) },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
```

### D20DiceView

```kotlin
@Composable
fun D20DiceView(
    result: Int?,
    isRolling: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRolling) {
            // Lottie 주사위 회전 애니메이션
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.dice_rolling)
            )
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            result?.let { value ->
                val color = when {
                    value == 20 -> QuestLogColors.Gold
                    value == 1  -> QuestLogColors.Error
                    value >= 15 -> QuestLogColors.Success
                    else        -> QuestLogColors.TextPrimary
                }
                
                // D20 모양의 다각형 배경
                D20Shape(color = color.copy(alpha = 0.2f))
                
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

### HPBar

```kotlin
@Composable
fun HPBar(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    val percentage = (currentHp.toFloat() / maxHp).coerceIn(0f, 1f)
    val color by animateColorAsState(
        targetValue = when {
            percentage > 0.75f -> QuestLogColors.HpHealthy
            percentage > 0.50f -> QuestLogColors.HpTired
            percentage > 0.25f -> QuestLogColors.HpWounded
            else               -> QuestLogColors.HpCritical
        },
        animationSpec = tween(300)
    )
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "❤️ HP",
                style = MaterialTheme.typography.labelSmall,
                color = QuestLogColors.TextSecondary
            )
            Text(
                text = "$currentHp / $maxHp",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = QuestLogColors.SurfaceVariant
        )
    }
}
```

### CrBadge

```kotlin
@Composable
fun CrBadge(
    cr: Float,
    modifier: Modifier = Modifier
) {
    val (color, monsterEmoji) = getCRVisuals(cr)
    val crText = when {
        cr < 1f -> "CR ${cr}"
        else -> "CR ${cr.toInt()}"
    }
    
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = monsterEmoji, fontSize = 10.sp)
            Text(
                text = crText,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getCRVisuals(cr: Float): Pair<Color, String> = when {
    cr < 1f  -> QuestLogColors.CrEasy to "🟢"
    cr < 2f  -> QuestLogColors.CrEasy to "👾"
    cr < 5f  -> QuestLogColors.CrNormal to "⚔️"
    cr < 10f -> QuestLogColors.CrHard to "🔥"
    cr < 15f -> QuestLogColors.CrVeryHard to "🐉"
    cr < 20f -> QuestLogColors.CrEpic to "👑"
    else     -> QuestLogColors.Gold to "💀"
}
```

---

## 10.4 애니메이션 가이드

### 전환 애니메이션

```kotlin
// NavGraph.kt
composable(
    route = Screen.QuestBoard.route,
    enterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, easing = EaseOutCubic)
        )
    },
    exitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = tween(300, easing = EaseInCubic)
        )
    }
)
```

### XP 획득 애니메이션

```kotlin
@Composable
fun XpBar(
    currentXp: Long,
    maxXp: Long,
    gainedXp: Long = 0,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(currentXp) {
        // 이전 값 → 새 값으로 애니메이션
        animatedProgress.animateTo(
            targetValue = currentXp.toFloat() / maxXp,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutBounce
            )
        )
    }
    
    // gainedXp > 0이면 +XP 텍스트 팝업 표시
    if (gainedXp > 0) {
        FloatingXPLabel(xp = gainedXp)
    }
    
    LinearProgressIndicator(
        progress = { animatedProgress.value },
        color = QuestLogColors.Gold,
        trackColor = QuestLogColors.GoldContainer
    )
}
```

### 크리티컬 히트 파티클

```kotlin
@Composable
fun CriticalHitEffect(
    isVisible: Boolean,
    onComplete: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.critical_hit_particles)
    )
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(500))
    ) {
        LottieAnimation(
            composition = composition,
            iterations = 1,
            modifier = Modifier.fillMaxSize(),
            speed = 1.5f
        )
    }
}
```

---

## 10.5 아이콘 시스템

### 클래스 아이콘 (이모지 기반 v1.0)

```kotlin
object ClassIcons {
    fun getEmoji(classType: CharacterClass): String = when (classType) {
        BARBARIAN -> "🪓"
        BARD      -> "🎸"
        CLERIC    -> "✝️"
        DRUID     -> "🌿"
        FIGHTER   -> "⚔️"
        MONK      -> "🥋"
        PALADIN   -> "⚜️"
        RANGER    -> "🏹"
        ROGUE     -> "🗡️"
        SORCERER  -> "⚡"
        WARLOCK   -> "🕯️"
        WIZARD    -> "📚"
    }
}
```

### 생활 영역 아이콘

```kotlin
object LifeAreaIcons {
    fun getEmoji(area: LifeArea): String = when (area) {
        WORK         -> "🏢"
        HEALTH       -> "💪"
        LEARNING     -> "📚"
        RELATIONSHIP -> "👥"
        FINANCE      -> "💰"
        PERSONAL     -> "✨"
        CREATIVE     -> "🎨"
    }
}
```

---

## 10.6 사운드 디자인 (선택적)

```kotlin
object SoundEffects {
    // 퀘스트 완료 체크 시: 검 휘두르는 소리 (짧게 0.3초)
    const val QUEST_CHECK = R.raw.sword_swipe
    
    // D20 굴림: 주사위 구르는 소리 (0.5초)
    const val DICE_ROLL = R.raw.dice_rolling_sound
    
    // 명중: 충격음 (0.4초)
    const val COMBAT_HIT = R.raw.hit_impact
    
    // 크리티컬 히트: 웅장한 타격음 (1초)
    const val CRITICAL_HIT = R.raw.critical_impact
    
    // 미스: 실패음 (0.4초)
    const val COMBAT_MISS = R.raw.miss_sound
    
    // 레벨업: 환호 + 팡파르 (2초)
    const val LEVEL_UP = R.raw.level_up_fanfare
    
    // XP 획득: 코인 소리 (0.3초)
    const val XP_GAIN = R.raw.coin_collect
    
    // 아이템 드롭: 마법 소리 (0.5초)
    const val ITEM_DROP = R.raw.magic_item
    
    // Inbox Zero: 성취음 (1초)
    const val INBOX_ZERO = R.raw.achievement_sound
}
```

**사운드 재생 (SoundPool 사용)**:
```kotlin
class SoundManager @Inject constructor(
    private val context: Context,
    private val appSettings: AppSettings
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .build()
    
    fun play(soundRes: Int) {
        if (appSettings.soundEffects) {
            val soundId = soundPool.load(context, soundRes, 1)
            soundPool.setOnLoadCompleteListener { pool, id, _ ->
                if (id == soundId) pool.play(id, 1f, 1f, 1, 0, 1f)
            }
        }
    }
}
```

---

## 10.7 Lottie 애니메이션 목록

| 파일명 | 용도 | 재생 방식 | 예상 길이 |
|--------|------|----------|---------|
| welcome_logo.json | WelcomeScreen 로고 | 1회 | 3초 |
| dice_rolling.json | D20 굴림 중 | 반복 | 1초 루프 |
| critical_hit_particles.json | 크리티컬 히트 파티클 | 1회 | 2초 |
| level_up_burst.json | 레벨업 폭발 | 1회 | 2.5초 |
| character_victory.json | 전투 승리 포즈 | 1회 | 2초 |
| inbox_zero_sparkle.json | Inbox Zero 반짝임 | 1회 | 1.5초 |
| item_drop_glow.json | 아이템 드롭 광채 | 1회 | 1초 |
| weekly_review_complete.json | 주간 리뷰 완료 | 1회 | 2초 |

**소스**: LottieFiles.com (무료 라이선스 확인 필수) 또는 자체 제작
