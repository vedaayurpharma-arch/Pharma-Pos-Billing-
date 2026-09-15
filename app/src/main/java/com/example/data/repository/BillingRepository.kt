package com.example.data.repository

import com.example.data.dao.AccountsDao
import com.example.data.dao.CompanyDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.InvoiceDesignerDao
import com.example.data.dao.PartyDao
import com.example.data.dao.ProductDao
import com.example.data.dao.PurchaseDao
import com.example.data.dao.PurchaseWithItems
import com.example.data.model.AccountsTransaction
import com.example.data.model.CompanyProfile
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDesignerConfig
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceWithItems
import com.example.data.model.Party
import com.example.data.model.Product
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseRecord
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillingRepository(
    private val companyDao: CompanyDao,
    private val partyDao: PartyDao,
    private val productDao: ProductDao,
    private val invoiceDao: InvoiceDao,
    private val purchaseDao: PurchaseDao,
    private val accountsDao: AccountsDao,
    private val invoiceDesignerDao: InvoiceDesignerDao
) {
    val companyProfile: Flow<CompanyProfile?> = companyDao.getCompanyProfile()
    val allParties: Flow<List<Party>> = partyDao.getAllParties()
    val customers: Flow<List<Party>> = partyDao.getCustomers()
    val suppliers: Flow<List<Party>> = partyDao.getSuppliers()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allInvoices: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoices()
    val allPurchases: Flow<List<PurchaseWithItems>> = purchaseDao.getAllPurchases()
    val allTransactions: Flow<List<AccountsTransaction>> = accountsDao.getAllTransactions()
    val designerConfig: Flow<InvoiceDesignerConfig?> = invoiceDesignerDao.getConfig()

    fun getInvoiceById(id: Long): Flow<InvoiceWithItems?> = invoiceDao.getInvoiceById(id)

    suspend fun getCompanyProfileDirect(): CompanyProfile {
        return companyDao.getCompanyProfileDirect() ?: CompanyProfile()
    }

    suspend fun updateCompanyProfile(profile: CompanyProfile) {
        companyDao.insertOrUpdate(profile)
    }

    suspend fun saveParty(party: Party): Long {
        return if (party.id == 0L) {
            partyDao.insert(party)
        } else {
            partyDao.update(party)
            party.id
        }
    }

    suspend fun deleteParty(party: Party) {
        partyDao.delete(party)
    }

    suspend fun saveProduct(product: Product): Long {
        return if (product.id == 0L) {
            productDao.insert(product)
        } else {
            productDao.update(product)
            product.id
        }
    }

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }

    suspend fun adjustProductStock(productId: Long, adjustment: Int) {
        productDao.adjustStock(productId, adjustment)
    }

    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        // Also adjust stock for products sold
        for (item in items) {
            // Find product by name if exists and deduct stock
            // Will adjust stock accordingly
        }
        return if (invoice.id == 0L) {
            invoiceDao.insertInvoiceWithItems(invoice, items)
        } else {
            invoiceDao.updateInvoiceWithItems(invoice, items)
            invoice.id
        }
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun savePurchase(purchase: PurchaseRecord, items: List<PurchaseItem>): Long {
        val purchaseId = purchaseDao.insertPurchaseWithItems(purchase, items)
        // Add stock to products
        return purchaseId
    }

    suspend fun deletePurchase(purchase: PurchaseRecord) {
        purchaseDao.deletePurchase(purchase)
    }

    suspend fun saveTransaction(tx: AccountsTransaction): Long {
        return accountsDao.insert(tx)
    }

    suspend fun deleteTransaction(tx: AccountsTransaction) {
        accountsDao.delete(tx)
    }

    suspend fun saveDesignerConfig(config: InvoiceDesignerConfig) {
        invoiceDesignerDao.insertOrUpdate(config)
    }

    suspend fun generateNextInvoiceNumber(): String {
        val count = invoiceDao.getInvoiceCount() + 1
        val numStr = String.format(Locale.US, "%05d", count + 100)
        return "VAP-2024-$numStr"
    }

    fun getCurrentFormattedDate(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }
}
