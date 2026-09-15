package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseRecord
import kotlinx.coroutines.flow.Flow

data class PurchaseWithItems(
    @androidx.room.Embedded val purchase: PurchaseRecord,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "purchaseId"
    )
    val items: List<PurchaseItem>
)

@Dao
interface PurchaseDao {
    @Transaction
    @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
    fun getAllPurchases(): Flow<List<PurchaseWithItems>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchaseById(id: Long): PurchaseWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseItem>)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseRecord)

    @Transaction
    suspend fun insertPurchaseWithItems(purchase: PurchaseRecord, items: List<PurchaseItem>): Long {
        val purchaseId = insertPurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        insertItems(itemsWithId)
        return purchaseId
    }
}
