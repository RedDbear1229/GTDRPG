package com.questlog.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.questlog.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * F7.1 E2E: Inbox 캡처 → 명료화(2분 룰) → Inbox에서 사라짐
 *
 * 'XP 획득' 단계는 캐릭터 없이도 무결성 유지됨을 단위 테스트(CompleteTaskUseCaseTest)가 보장.
 * TestAppModule 이 DataStore에 onboarding_completed = true 를 사전 설정해 OnboardingScreen 을 건너뜀.
 */
@HiltAndroidTest
class InboxToClarifyFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun inboxCapture_clarify2MinRule_inboxBecomesEmpty() {
        // 1. Inbox starts empty
        composeRule.onNodeWithText("Inbox가 비어 있어요").assertIsDisplayed()

        // 2. Open QuickCaptureSheet
        composeRule.onNodeWithContentDescription("캡처").performClick()

        // 3. Type text in the sheet's text field, then submit
        composeRule.onNode(hasSetTextAction()).performTextInput("E2E 테스트 퀘스트")
        composeRule.onNodeWithText("저장").performClick()

        // 4. Item appears in Inbox list
        composeRule.onNodeWithText("E2E 테스트 퀘스트").assertIsDisplayed()

        // 5. Tap item to open ClarifySheet (full-screen dialog)
        composeRule.onNodeWithText("E2E 테스트 퀘스트").performClick()

        // 6. Q1 — select actionable
        composeRule.onNodeWithText("예, 행동 가능").performClick()

        // 7. Q2 — confirm next action (leave empty, tap 다음)
        composeRule.onNodeWithText("다음").performClick()

        // 8. Q3 — 2-minute rule: immediate completion + item removed from Inbox
        composeRule.onNodeWithText("예 — 지금 끝낸다").performClick()

        // 9. ClarifySheet dismisses → Inbox is now empty
        composeRule.onNodeWithText("Inbox가 비어 있어요").assertIsDisplayed()
    }
}
