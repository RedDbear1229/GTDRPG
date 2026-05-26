package com.questlog.feature.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.ClassQuiz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassQuizStep(
    onAnswer: (questionId: Int, optionIndex: Int) -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
) {
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var currentIndex by remember { mutableIntStateOf(0) }
    val questions = ClassQuiz.questions
    val question = questions[currentIndex]
    val total = questions.size
    val answered = answers[question.id]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("클래스 퀴즈 ${currentIndex + 1}/$total") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleLarge,
            )
            question.options.forEachIndexed { idx, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = answered == idx,
                            onClick = {
                                answers[question.id] = idx
                                onAnswer(question.id, idx)
                            },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = answered == idx, onClick = null)
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentIndex < total - 1) {
                    Button(
                        onClick = { if (answered != null) currentIndex++ },
                        enabled = answered != null,
                        modifier = Modifier.weight(1f),
                    ) { Text("다음") }
                } else {
                    Button(
                        onClick = onComplete,
                        enabled = answered != null,
                        modifier = Modifier.weight(1f),
                    ) { Text("결과 보기") }
                }
            }
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("퀴즈 건너뛰기")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
