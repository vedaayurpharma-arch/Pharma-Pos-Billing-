package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Party
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE type = 'CUSTOMER' ORDER BY name ASC")
    fun getCustomers(): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE type = 'SUPPLIER' ORDER BY name ASC")
    fun getSuppliers(): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyById(id: Long): Party?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(party: Party): Long

    @Update
    suspend fun update(party: Party)

    @Delete
    suspend fun delete(party: Party)

    @Query("UPDATE parties SET currentBalance = currentBalance + :amount WHERE id = :partyId")
    suspend fun adjustBalance(partyId: Long, amount: Double)
}
