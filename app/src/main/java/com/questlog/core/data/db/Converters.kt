package com.questlog.core.data.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.CaptureSource
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClarifyResultType
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.ItemRarity
import com.questlog.core.domain.model.ItemType
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.model.ProjectStatus
import com.questlog.core.domain.model.TaskStatus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Enum: name 문자열 저장. 새 enum 추가 시 여기에 변환쌍 추가.
// JSON collections: kotlinx.serialization 단독 (Gson 금지).
@ProvidedTypeConverter
class Converters @Inject constructor(
    private val json: Json,
) {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.let { json.decodeFromString<List<String>>(it) }

    @TypeConverter fun fromTaskStatus(v: TaskStatus?): String? = v?.name
    @TypeConverter fun toTaskStatus(v: String?): TaskStatus? = v?.let(TaskStatus::valueOf)

    @TypeConverter fun fromProjectStatus(v: ProjectStatus?): String? = v?.name
    @TypeConverter fun toProjectStatus(v: String?): ProjectStatus? = v?.let(ProjectStatus::valueOf)

    @TypeConverter fun fromLifeArea(v: LifeArea?): String? = v?.name
    @TypeConverter fun toLifeArea(v: String?): LifeArea? = v?.let(LifeArea::valueOf)

    @TypeConverter fun fromAbilityType(v: AbilityType?): String? = v?.name
    @TypeConverter fun toAbilityType(v: String?): AbilityType? = v?.let(AbilityType::valueOf)

    @TypeConverter fun fromMonsterType(v: MonsterType?): String? = v?.name
    @TypeConverter fun toMonsterType(v: String?): MonsterType? = v?.let(MonsterType::valueOf)

    @TypeConverter fun fromCaptureSource(v: CaptureSource?): String? = v?.name
    @TypeConverter fun toCaptureSource(v: String?): CaptureSource? = v?.let(CaptureSource::valueOf)

    @TypeConverter fun fromClarifyResultType(v: ClarifyResultType?): String? = v?.name
    @TypeConverter fun toClarifyResultType(v: String?): ClarifyResultType? = v?.let(ClarifyResultType::valueOf)

    @TypeConverter fun fromCharacterClass(v: CharacterClass?): String? = v?.name
    @TypeConverter fun toCharacterClass(v: String?): CharacterClass? = v?.let(CharacterClass::valueOf)

    @TypeConverter fun fromItemType(v: ItemType?): String? = v?.name
    @TypeConverter fun toItemType(v: String?): ItemType? = v?.let(ItemType::valueOf)

    @TypeConverter fun fromItemRarity(v: ItemRarity?): String? = v?.name
    @TypeConverter fun toItemRarity(v: String?): ItemRarity? = v?.let(ItemRarity::valueOf)

    @TypeConverter fun fromEquipmentSlot(v: EquipmentSlot?): String? = v?.name
    @TypeConverter fun toEquipmentSlot(v: String?): EquipmentSlot? = v?.let(EquipmentSlot::valueOf)
}
