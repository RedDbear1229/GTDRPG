package com.questlog.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.feature.onboarding.steps.AbilityRollStep
import com.questlog.feature.onboarding.steps.CharacterNamingStep
import com.questlog.feature.onboarding.steps.ClassQuizStep
import com.questlog.feature.onboarding.steps.ClassSelectionStep
import com.questlog.feature.onboarding.steps.GtdTutorialStep
import com.questlog.feature.onboarding.steps.WelcomeStep

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = state.step != OnboardingStep.WELCOME) {
        viewModel.onBackPressed()
    }

    when (state.step) {
        OnboardingStep.WELCOME -> WelcomeStep(
            onContinue = viewModel::onWelcomeContinue,
        )
        OnboardingStep.CLASS_QUIZ -> ClassQuizStep(
            onAnswer = viewModel::onQuizAnswer,
            onComplete = viewModel::onQuizComplete,
            onSkip = viewModel::onSkipQuiz,
        )
        OnboardingStep.CLASS_SELECTION -> ClassSelectionStep(
            recommendations = state.quizRecommendations,
            selectedClass = state.selectedClass,
            onClassSelected = viewModel::onClassSelected,
            onConfirm = viewModel::onClassConfirmed,
        )
        OnboardingStep.ABILITY_ROLL -> AbilityRollStep(
            selectedClass = state.selectedClass,
            scores = state.abilityScores,
            onReroll = viewModel::rollAbilities,
            onConfirm = viewModel::onAbilitiesConfirmed,
        )
        OnboardingStep.CHARACTER_NAMING -> CharacterNamingStep(
            selectedClass = state.selectedClass,
            name = state.characterName,
            onNameChange = viewModel::onNameChange,
            onRandomize = viewModel::randomizeName,
            onConfirm = viewModel::onNamingConfirmed,
        )
        OnboardingStep.GTD_TUTORIAL -> GtdTutorialStep(
            characterName = state.characterName,
            onComplete = {
                viewModel.onOnboardingComplete()
                onCompleted()
            },
        )
    }
}
