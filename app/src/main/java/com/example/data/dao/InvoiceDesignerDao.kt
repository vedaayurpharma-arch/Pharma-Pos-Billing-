package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InvoiceDesignerConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDesignerDao {
    @Query("SELECT * FROM invoice_designer_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<InvoiceDesignerConfig?>

    @Query("SELECT * FROM invoice_designer_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigSync(): InvoiceDesignerConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: InvoiceDesignerConfig)
}
