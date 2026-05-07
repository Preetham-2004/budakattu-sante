package com.budakattu.sante.data.local.converter

import androidx.room.TypeConverter
import com.budakattu.sante.domain.model.SyncStatus

class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>): String = value.joinToString(separator = "|")

    @TypeConverter
    fun toList(value: String): List<String> = value.split("|").filter { it.isNotBlank() }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
