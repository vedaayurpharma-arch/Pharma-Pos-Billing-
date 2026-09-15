package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AccountsTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountsDao {
    @Query("SELECT * FROM accounts_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<AccountsTransaction>>

    @Query("SELECT * FROM accounts_transactions WHERE type = :type ORDER BY createdAt DESC")
    fun getTransactionsByType(type: String): Flow<List<AccountsTransaction>>

    @Query("SELECT * FROM accounts_transactions WHERE paymentMode = 'CASH' ORDER BY createdAt DESC")
    fun getCashBook(): Flow<List<AccountsTransaction>>

    @Query("SELECT * FROM accounts_transactions WHERE paymentMode = 'BANK' OR paymentMode = 'UPI' ORDER BY createdAt DESC")
    fun getBankBook(): Flow<List<AccountsTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: AccountsTransaction): Long

    @Delete
    suspend fun delete(transaction: AccountsTransaction)
}
