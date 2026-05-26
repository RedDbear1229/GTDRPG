package com.questlog.feature.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterNamingStep(
    selectedClass: CharacterClass?,
    name: String,
    onNameChange: (String) -> Unit,
    onRandomize: () -> Unit,
    onConfirm: () -> Unit,
) {
    val info = selectedClass?.let { ClassContent.forClass(it) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("캐릭터 이름") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            if (info != null) {
                Text(text = info.emoji, style = MaterialTheme.typography.displayMedium)
                Text(
                    text = "${info.classType.label} 모험가",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("이름") },
                    placeholder = { Text("모험가의 이름을 지어 주세요") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = onRandomize) {
                    Icon(Icons.Outlined.Shuffle, contentDescription = "랜덤 이름")
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (name.isBlank()) "이름 없이 계속" else "\"$name\" 확정")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
