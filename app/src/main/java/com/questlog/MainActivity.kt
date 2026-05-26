package com.questlog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import com.questlog.core.data.datastore.OnboardingPreferences
import com.questlog.core.ui.theme.QuestLogTheme
import com.questlog.feature.inbox.InboxScreen
import com.questlog.feature.inbox.InboxViewModel
import com.questlog.feature.inbox.widget.InboxWidgetProvider
import com.questlog.feature.journal.JournalScreen
import com.questlog.feature.onboarding.OnboardingScreen
import com.questlog.feature.questboard.ProjectDetailScreen
import com.questlog.feature.questboard.QuestBoardScreen
import com.questlog.feature.questboard.TaskDetailScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingPreferences: OnboardingPreferences

    private var pendingShareText by mutableStateOf<String?>(null)
    private var pendingOpenCapture by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        absorbIntent(intent)
        setContent {
            QuestLogTheme {
                val onboardingCompleted by onboardingPreferences.isCompleted.collectAsState(initial = null)
                when (onboardingCompleted) {
                    null -> Unit  // 로딩 중 — 빈 화면 (SplashScreen API 로 대체 예정)
                    false -> OnboardingScreen(onCompleted = { /* recompose via flow */ })
                    true -> QuestLogRoot(
                        pendingShareText = pendingShareText,
                        onShareConsumed = { pendingShareText = null },
                        pendingOpenCapture = pendingOpenCapture,
                        onOpenCaptureConsumed = { pendingOpenCapture = false },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        absorbIntent(intent)
    }

    private fun absorbIntent(intent: Intent?) {
        extractSharedText(intent)?.let { pendingShareText = it }
        if (intent?.getBooleanExtra(InboxWidgetProvider.EXTRA_OPEN_CAPTURE, false) == true) {
            pendingOpenCapture = true
        }
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type?.startsWith("text/") != true) return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
    }
}

private object Routes {
    const val INBOX = "inbox"
    const val QUEST_BOARD = "questboard"
    const val JOURNAL = "journal"
    const val PROJECT_DETAIL = "project/{projectId}"
    const val TASK_DETAIL = "task/{taskId}"
    fun project(id: String) = "project/$id"
    fun task(id: String) = "task/$id"
}

@Composable
private fun QuestLogRoot(
    pendingShareText: String?,
    onShareConsumed: () -> Unit,
    pendingOpenCapture: Boolean,
    onOpenCaptureConsumed: () -> Unit,
) {
    val inboxViewModel: InboxViewModel = hiltViewModel()

    val navController = rememberNavController()

    LaunchedEffect(pendingShareText) {
        pendingShareText?.let {
            inboxViewModel.captureFromShare(it)
            onShareConsumed()
        }
    }
    LaunchedEffect(pendingOpenCapture) {
        if (pendingOpenCapture) {
            // 위젯 탭은 어느 화면에 있든 Inbox 로 이동 후 시트를 연다.
            // QuickCaptureSheet 는 InboxScreen 내부에서만 렌더되므로, 다른 탭에서는 sheet 상태만
            // 토글되고 UI 가 보이지 않는 버그가 있었음 (Codex 정식 리뷰 P2).
            if (navController.currentBackStackEntry?.destination?.route != Routes.INBOX) {
                navController.navigate(Routes.INBOX) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            inboxViewModel.openSheet()
            onOpenCaptureConsumed()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomBar(navController) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.INBOX,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.INBOX) {
                InboxScreen(viewModel = inboxViewModel)
            }
            composable(Routes.QUEST_BOARD) {
                QuestBoardScreen(
                    onTaskClick = { id -> navController.navigate(Routes.task(id)) },
                    onProjectClick = { id -> navController.navigate(Routes.project(id)) },
                )
            }
            composable(Routes.JOURNAL) {
                JournalScreen()
            }
            composable(Routes.PROJECT_DETAIL) { backStack ->
                val projectId = backStack.arguments?.getString("projectId").orEmpty()
                ProjectDetailScreen(
                    projectId = projectId,
                    onBack = { navController.popBackStack() },
                    onTaskClick = { id -> navController.navigate(Routes.task(id)) },
                )
            }
            composable(Routes.TASK_DETAIL) { backStack ->
                val taskId = backStack.arguments?.getString("taskId").orEmpty()
                TaskDetailScreen(
                    taskId = taskId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.INBOX,
            onClick = {
                navController.navigate(Routes.INBOX) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Inbox, contentDescription = null) },
            label = { Text("Inbox") },
        )
        NavigationBarItem(
            selected = currentRoute == Routes.QUEST_BOARD,
            onClick = {
                navController.navigate(Routes.QUEST_BOARD) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Outlined.Map, contentDescription = null) },
            label = { Text("퀘스트") },
        )
        NavigationBarItem(
            selected = currentRoute == Routes.JOURNAL,
            onClick = {
                navController.navigate(Routes.JOURNAL) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Outlined.AutoStories, contentDescription = null) },
            label = { Text("저널") },
        )
    }
}
