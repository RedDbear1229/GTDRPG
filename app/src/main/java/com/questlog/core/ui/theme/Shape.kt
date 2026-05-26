package com.questlog.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val QuestLogShapes = Shapes(
    // 아이콘 버튼, 배지
    extraSmall = RoundedCornerShape(4.dp),
    // 카드 모서리
    small = RoundedCornerShape(8.dp),
    // 바텀시트, 다이얼로그
    medium = RoundedCornerShape(12.dp),
    // 모달 시트
    large = RoundedCornerShape(16.dp),
    // 풀스크린 시트
    extraLarge = RoundedCornerShape(24.dp)
)
