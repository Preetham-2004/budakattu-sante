package com.budakattu.sante.data.local.converter

import androidx.room.TypeConverter
import com.budakattu.sante.domain.model.ProductAvailability

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromProductAvailability(value: ProductAvailability): String {
        return value.name
    }

    @TypeConverter
    fun toProductAvailability(value: String): ProductAvailability {
        return ProductAvailability.valueOf(value)
    }
}
