package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.notification.NotificationChannels
import com.questlog.core.notification.NotificationHelper
import com.questlog.core.notification.NotificationIds
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

// 매일 12:00 실행. HP < 30% 면 위기 알림. 자정 Long Rest 후 회복되므로 점심에 한번만 체크.
@HiltWorker
class HpCrisisWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val characterRepository: CharacterRepository,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "hp_crisis"
        private const val CRISIS_THRESHOLD = 0.30f
    }

    override suspend fun doWork(): Result {
        val character = runCatching {
            characterRepository.getActive()
        }.getOrElse { e ->
            Timber.e(e, "HpCrisisWorker: 캐릭터 조회 실패")
            return Result.failure()
        } ?: return Result.success()

        if (character.maxHp == 0) return Result.success()
        val hpRatio = character.currentHp.toFloat() / character.maxHp.toFloat()
        if (hpRatio >= CRISIS_THRESHOLD) return Result.success()

        NotificationHelper.send(
            context = context,
            notificationId = NotificationIds.HP_CRISIS,
            channelId = NotificationChannels.HP_CRISIS,
            title = "💀 HP 위기! ${character.name}이(가) 쓰러지기 직전입니다",
            body = "현재 HP ${character.currentHp}/${character.maxHp} (${(hpRatio * 100).toInt()}%). 오늘 퀘스트를 완료해 전세를 역전시키세요!",
        )
        return Result.success()
    }
}
