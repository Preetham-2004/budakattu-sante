package com.budakattu.sante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.budakattu.sante.data.local.entity.MspEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MspDao {
    @Query("SELECT * FROM msp_records ORDER BY categoryName ASC")
    fun observeMspRecords(): Flow<List<MspEntity>>

    @Query("SELECT * FROM msp_records WHERE categoryId = :categoryId")
    fun observeMspRecord(categoryId: String): Flow<MspEntity?>

    @Upsert
    suspend fun upsertMspRecords(entities: List<MspEntity>)

    @Query("DELETE FROM msp_records")
    suspend fun deleteAll()
}
