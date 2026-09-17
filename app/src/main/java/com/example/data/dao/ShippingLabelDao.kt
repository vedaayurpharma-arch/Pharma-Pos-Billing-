package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShippingLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface ShippingLabelDao {
    @Query("SELECT * FROM shipping_labels ORDER BY id DESC")
    fun getAllShippingLabels(): Flow<List<ShippingLabel>>

    @Query("SELECT * FROM shipping_labels WHERE id = :id LIMIT 1")
    fun getShippingLabelById(id: Long): Flow<ShippingLabel?>

    @Query("SELECT * FROM shipping_labels WHERE id = :id LIMIT 1")
    suspend fun getShippingLabelByIdDirect(id: Long): ShippingLabel?

    @Query("SELECT * FROM shipping_labels WHERE shippingNumber = :shippingNumber LIMIT 1")
    suspend fun getShippingLabelByNumber(shippingNumber: String): ShippingLabel?

    @Query("SELECT COUNT(*) FROM shipping_labels")
    suspend fun getShippingLabelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShippingLabel(label: ShippingLabel): Long

    @Update
    suspend fun updateShippingLabel(label: ShippingLabel)

    @Delete
    suspend fun deleteShippingLabel(label: ShippingLabel)

    @Query("DELETE FROM shipping_labels")
    suspend fun clearAllShippingLabels()
}
