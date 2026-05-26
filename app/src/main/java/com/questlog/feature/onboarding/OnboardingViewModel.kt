package com.questlog.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.data.datastore.OnboardingPreferences
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassContent
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.usecase.AbilityCalculator
import com.questlog.core.domain.usecase.AbilityScores
import com.questlog.core.domain.usecase.ClassRecommendationUseCase
import com.questlog.core.domain.usecase.HpCalculator
import com.questlog.core.domain.usecase.ProficiencyBonus
import com.questlog.core.domain.usecase.RollAbilityScoresUseCase
import com.questlog.core.domain.model.Character
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    CLASS_QUIZ,
    CLASS_SELECTION,
    ABILITY_ROLL,
    CHARACTER_NAMING,
    GTD_TUTORIAL,
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val quizAnswers: Map<Int, Int> = emptyMap(),        // questionId → optionIndex
    val quizRecommendations: List<CharacterClass> = emptyList(),
    val selectedClass: CharacterClass? = null,
    val abilityScores: AbilityScores? = null,
    val characterName: String = "",
    val isCompleting: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val characterRepository: CharacterRepository,
    private val rollAbilityScores: RollAbilityScoresUseCase,
    private val classRecommendation: ClassRecommendationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // WELCOME
    fun onWelcomeContinue() = goTo(OnboardingStep.CLASS_QUIZ)

    // CLASS_QUIZ
    fun onQuizAnswer(questionId: Int, optionIndex: Int) {
        _uiState.update { it.copy(quizAnswers = it.quizAnswers + (questionId to optionIndex)) }
    }

    fun onQuizComplete() {
        val recs = classRecommendation.recommend(_uiState.value.quizAnswers)
        _uiState.update { it.copy(quizRecommendations = recs) }
        goTo(OnboardingStep.CLASS_SELECTION)
    }

    fun onSkipQuiz() = goTo(OnboardingStep.CLASS_SELECTION)

    // CLASS_SELECTION
    fun onClassSelected(cls: CharacterClass) {
        _uiState.update { it.copy(selectedClass = cls) }
    }

    fun onClassConfirmed() {
        if (_uiState.value.selectedClass == null) return
        rollAbilities()
        goTo(OnboardingStep.ABILITY_ROLL)
    }

    // ABILITY_ROLL
    fun rollAbilities() {
        _uiState.update { it.copy(abilityScores = rollAbilityScores.rollAll()) }
    }

    fun onAbilitiesConfirmed() = goTo(OnboardingStep.CHARACTER_NAMING)

    // CHARACTER_NAMING
    fun onNameChange(name: String) = _uiState.update { it.copy(characterName = name) }

    fun randomizeName() {
        val cls = _uiState.value.selectedClass ?: return
        _uiState.update { it.copy(characterName = ClassContent.randomName(cls)) }
    }

    // GTD_TUTORIAL
    fun onNamingConfirmed() = goTo(OnboardingStep.GTD_TUTORIAL)

    fun onOnboardingComplete() {
        val state = _uiState.value
        val cls = state.selectedClass ?: return
        val scores = state.abilityScores ?: return
        val name = state.characterName.ifBlank { ClassContent.randomName(cls) }
        _uiState.update { it.copy(isCompleting = true) }
        viewModelScope.launch {
            val maxHp = HpCalculator.maxHp(cls, level = 1, constitutionScore = scores.constitution)
            val profBonus = ProficiencyBonus.forLevel(1)
            val character = Character(
                name = name,
                classType = cls,
                level = 1,
                maxHp = maxHp,
                currentHp = maxHp,
                strength = scores.strength,
                dexterity = scores.dexterity,
                constitution = scores.constitution,
                intelligence = scores.intelligence,
                wisdom = scores.wisdom,
                charisma = scores.charisma,
                proficiencyBonus = profBonus,
                armorClass = 10 + AbilityCalculator.modifier(scores.dexterity),
            )
            characterRepository.upsert(character)
            onboardingPreferences.setCompleted()
        }
    }

    fun onBackPressed() {
        val prev = when (_uiState.value.step) {
            OnboardingStep.WELCOME -> null
            OnboardingStep.CLASS_QUIZ -> OnboardingStep.WELCOME
            OnboardingStep.CLASS_SELECTION -> OnboardingStep.CLASS_QUIZ
            OnboardingStep.ABILITY_ROLL -> OnboardingStep.CLASS_SELECTION
            OnboardingStep.CHARACTER_NAMING -> OnboardingStep.ABILITY_ROLL
            OnboardingStep.GTD_TUTORIAL -> OnboardingStep.CHARACTER_NAMING
        }
        if (prev != null) goTo(prev)
    }

    private fun goTo(step: OnboardingStep) = _uiState.update { it.copy(step = step) }
}
