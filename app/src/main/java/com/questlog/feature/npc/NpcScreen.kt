package com.questlog.feature.npc

import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.CompatibilityLevel
import com.questlog.core.domain.model.NpcSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcScreen(
    onBack: () -> Unit,
    viewModel: NpcViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingClassPick by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            null, null, null,
        )
        val displayName = cursor?.use {
            if (it.moveToFirst()) it.getString(0) else null
        } ?: return@rememberLauncherForActivityResult
        pendingClassPick = displayName
    }

    if (showAddDialog) {
        AddNpcDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cls, notes ->
                viewModel.addManualNpc(name, cls, notes)
                showAddDialog = false
            },
        )
    }

    pendingClassPick?.let { displayName ->
        ClassPickDialog(
            title = "$displayName 의 클래스",
            onDismiss = { pendingClassPick = null },
            onConfirm = { cls ->
                viewModel.importContactNpc(displayName, null, cls)
                pendingClassPick = null
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NPC 명단") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_PICK).apply {
                            type = ContactsContract.Contacts.CONTENT_TYPE
                        }
                        contactPickerLauncher.launch(intent)
                    }) {
                        Icon(Icons.Filled.Contacts, contentDescription = "연락처에서 추가")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("NPC 추가") },
            )
        },
    ) { padding ->
        if (state.npcs.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "NPC가 없습니다\n+ 버튼으로 동료를 추가하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.npcs, key = { it.npc.id }) { item ->
                    NpcCard(
                        item = item,
                        onDelete = { viewModel.deleteNpc(item.npc) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NpcCard(
    item: NpcWithCompatibility,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompatibilityDot(item.compatibility)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.npc.name, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Text(item.npc.classType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.npc.source == NpcSource.PICKER) {
                        Spacer(Modifier.width(4.dp))
                        Text("📱", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(
                    item.compatibility.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(android.graphics.Color.parseColor(item.compatibility.colorHex)),
                )
                if (item.npc.notes.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(item.npc.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CompatibilityDot(level: CompatibilityLevel) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(
                Color(android.graphics.Color.parseColor(level.colorHex)),
                shape = CircleShape,
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNpcDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, cls: CharacterClass, notes: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(CharacterClass.FIGHTER) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NPC 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedClass.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("클래스") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        CharacterClass.entries.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls.label) },
                                onClick = { selectedClass = cls; expanded = false },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("메모 (선택)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedClass, notes) },
            ) { Text("추가") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassPickDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (CharacterClass) -> Unit,
) {
    var selectedClass by remember { mutableStateOf(CharacterClass.FIGHTER) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = selectedClass.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("클래스") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    CharacterClass.entries.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls.label) },
                            onClick = { selectedClass = cls; expanded = false },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedClass) }) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
