package com.questlog.feature.character

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.core.domain.model.ClassContent
import com.questlog.core.domain.usecase.ProficiencyBonus
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LevelUpScreen(
    onContinue: () -> Unit,
    viewModel: CharacterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val character = state.character

    val scale = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val sparkleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = InfiniteRepeatableSpec(tween(3000), RepeatMode.Restart),
        label = "sparkle_angle",
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension * 0.38f
            repeat(12) { i ->
                val angle = Math.toRadians((sparkleAngle + i * 30.0))
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                drawCircle(primaryColor.copy(alpha = 0.25f), radius = 6f, center = Offset(x, y))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("✨", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "LEVEL UP!",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
            if (character != null) {
                val info = ClassContent.forClass(character.classType)
                Text(
                    "${info.emoji} ${character.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${info.classType.label} → Lv.${character.level}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("📊 새로운 스탯", style = MaterialTheme.typography.titleSmall)
                    Text("❤️ 최대 HP: ${character.maxHp}")
                    Text("⚡ 숙련 보너스: +${ProficiencyBonus.forLevel(character.level)}")
                }
            }
            Spacer(Modifier.height(48.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("모험을 계속하자!")
            }
        }
    }
}
