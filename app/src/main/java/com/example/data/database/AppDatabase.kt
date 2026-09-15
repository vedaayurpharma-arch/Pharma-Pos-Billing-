package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountsDao
import com.example.data.dao.CompanyDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.InvoiceDesignerDao
import com.example.data.dao.PartyDao
import com.example.data.dao.ProductDao
import com.example.data.dao.PurchaseDao
import com.example.data.model.AccountsTransaction
import com.example.data.model.CompanyProfile
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDesignerConfig
import com.example.data.model.InvoiceItem
import com.example.data.model.Party
import com.example.data.model.Product
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CompanyProfile::class,
        Party::class,
        Product::class,
        Invoice::class,
        InvoiceItem::class,
        PurchaseRecord::class,
        PurchaseItem::class,
        AccountsTransaction::class,
        InvoiceDesignerConfig::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun partyDao(): PartyDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun accountsDao(): AccountsDao
    abstract fun invoiceDesignerDao(): InvoiceDesignerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pharma_erp_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    seedInitialData(database)
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            // Seed Company Profile with VEDA AYUR PHARMA
            database.companyDao().insertOrUpdate(
                CompanyProfile(
                    companyName = "VEDA AYUR PHARMA",
                    tagline = "Ayurvedic & Pharmaceutical Healthcare ERP",
                    addressLine1 = "H.No. 4-22/A, Ayurveda Bhavan, Herbal Complex",
                    addressLine2 = "Kurnool - 518002 (A.P.) / Hyderabad / Bangalore",
                    phone = "+91 94401 23456 / 98450 12345",
                    email = "vedaayurpharma@gmail.com",
                    dlNo = "20B-AP-KNL-102930, 21B-AP-KNL-102931",
                    gstin = "37AAKFV1234F1Z8",
                    pan = "AAKFV1234F",
                    state = "Andhra Pradesh (37)",
                    bankName = "State Bank of India",
                    accountNo = "39882201948",
                    ifscCode = "SBIN0001234",
                    branch = "Main Branch, Kurnool",
                    upiId = "vedapay@upi",
                    invoicePrefix = "VAP-"
                )
            )

            // Seed Default Invoice Designer Config
            database.invoiceDesignerDao().insertOrUpdate(
                InvoiceDesignerConfig(
                    selectedTemplate = "Veda Pharma A4 Landscape",
                    primaryColorHex = "#0D5C3A",
                    accentColorHex = "#1E824C"
                )
            )

            // Seed Parties: Customers & Suppliers
            val pCustomer1 = database.partyDao().insert(
                Party(
                    name = "ASIA TICO PHARMA",
                    type = "CUSTOMER",
                    address = "#65-54-23, GROUND FLOOR, 33 MAIN ROAD, BTM LAYOUT, 2ND STAGE, BANGALORE-68",
                    phone = "9845012345",
                    email = "asiaticopharma@gmail.com",
                    dlNo = "20B-KA-B62-202160, 21B-KA-B62-202161",
                    gstin = "29AHHPT3055G1Z2",
                    creditLimit = 150000.0,
                    creditDays = 30,
                    currentBalance = 14500.0,
                    area = "South Bangalore",
                    route = "BTM Route"
                )
            )

            val pCustomer2 = database.partyDao().insert(
                Party(
                    name = "SRI BALAJI AYURVEDIC MEDICALS",
                    type = "CUSTOMER",
                    address = "D.No 12/88, Main Bazaar, Near Raj Vihar, Kurnool-518001",
                    phone = "9440188990",
                    email = "balajiayurvedic@gmail.com",
                    dlNo = "20B-AP-KNL-554421, 21B-AP-KNL-554422",
                    gstin = "37AAGCS9912E1Z4",
                    creditLimit = 80000.0,
                    creditDays = 21,
                    currentBalance = 8250.0,
                    area = "Old Town",
                    route = "Town Route"
                )
            )

            database.partyDao().insert(
                Party(
                    name = "MEDPLUS HEALTHCARE",
                    type = "CUSTOMER",
                    address = "Plot 12, Indiranagar 100ft Road, Bangalore-560038",
                    phone = "9880199882",
                    dlNo = "20B-KA-B11-102938, 21B-KA-B11-102939",
                    gstin = "29AAECM1234A1Z9",
                    creditLimit = 200000.0,
                    creditDays = 30,
                    currentBalance = 0.0
                )
            )

            val pSupplier1 = database.partyDao().insert(
                Party(
                    name = "HERBAL ROOTS EXTRACTS LTD",
                    type = "SUPPLIER",
                    address = "Plot 45, Haridwar Industrial Area, Uttarakhand-249403",
                    phone = "9837012999",
                    email = "sales@herbalroots.in",
                    gstin = "05AABCH4912J1Z3",
                    creditLimit = 500000.0,
                    creditDays = 45,
                    currentBalance = -45000.0 // We owe supplier 45,000
                )
            )

            // Seed Ayurvedic & Allopathic Products
            val p1 = Product(
                name = "VEDA ASHWAGANDHA CHURNA",
                code = "VAP-ASH-100",
                category = "Ayurvedic Classical",
                composition = "Withania somnifera Pure Root Extract 100%",
                pack = "100g Jar",
                unit = "Jar",
                batchNumber = "ASH2401",
                mfgDate = "02/24",
                expiryDate = "01/27",
                hsnCode = "30049011",
                mrp = 180.00,
                purchaseRate = 85.00,
                saleRate = 120.00,
                wholesaleRate = 110.00,
                dealerRate = 105.00,
                defaultDiscount = 10.00,
                sgstPercent = 6.00,
                cgstPercent = 6.00,
                stockQty = 150,
                reorderLevel = 25,
                dosage = "3g to 5g with warm milk at bedtime"
            )

            val p2 = Product(
                name = "VEDA TRIPHALA GUGGULU",
                code = "VAP-TRP-80",
                category = "Ayurvedic Classical",
                composition = "Haritaki, Bibhitaki, Amalaki, Shuddha Guggulu",
                pack = "80 Tabs",
                unit = "Bottle",
                batchNumber = "TRP2408",
                mfgDate = "03/24",
                expiryDate = "02/27",
                hsnCode = "30049011",
                mrp = 160.00,
                purchaseRate = 72.00,
                saleRate = 105.00,
                wholesaleRate = 95.00,
                dealerRate = 90.00,
                defaultDiscount = 8.00,
                sgstPercent = 6.00,
                cgstPercent = 6.00,
                stockQty = 180,
                reorderLevel = 30
            )

            val p3 = Product(
                name = "VEDA CHYAWANPRASH SPECIAL",
                code = "VAP-CHY-500",
                category = "Ayurvedic Patent",
                composition = "Amalaki, Dashmool, Kesar, Suvarna Bhasma, Honey",
                pack = "500g",
                unit = "Jar",
                batchNumber = "CHY2411",
                mfgDate = "04/24",
                expiryDate = "10/26",
                hsnCode = "30049011",
                mrp = 395.00,
                purchaseRate = 195.00,
                saleRate = 280.00,
                wholesaleRate = 260.00,
                dealerRate = 245.00,
                defaultDiscount = 12.00,
                sgstPercent = 6.00,
                cgstPercent = 6.00,
                stockQty = 85,
                reorderLevel = 20
            )

            val p4 = Product(
                name = "VEDA GILOY GHANVATI",
                code = "VAP-GLY-60",
                category = "Ayurvedic Classical",
                composition = "Tinospora cordifolia (Amrita) water extract",
                pack = "60 Tabs",
                unit = "Bottle",
                batchNumber = "GLY2403",
                mfgDate = "01/24",
                expiryDate = "12/26",
                hsnCode = "30049011",
                mrp = 130.00,
                purchaseRate = 58.00,
                saleRate = 85.00,
                wholesaleRate = 78.00,
                dealerRate = 74.00,
                defaultDiscount = 5.00,
                sgstPercent = 6.00,
                cgstPercent = 6.00,
                stockQty = 12, // LOW STOCK < 25
                reorderLevel = 25
            )

            val p5 = Product(
                name = "CROCIN NEW 20MG",
                code = "ALLO-CRC-20",
                category = "Allopathic",
                composition = "Paracetamol IP 20mg Dispersible",
                pack = "1*10",
                unit = "Strip",
                batchNumber = "6546TRYTR",
                mfgDate = "02/23",
                expiryDate = "2/28",
                hsnCode = "90189023",
                mrp = 25.00,
                purchaseRate = 7.50,
                saleRate = 11.00,
                wholesaleRate = 10.00,
                dealerRate = 9.50,
                defaultDiscount = 6.00,
                sgstPercent = 6.00,
                cgstPercent = 6.00,
                stockQty = 120,
                reorderLevel = 20
            )

            val p6 = Product(
                name = "DISPOVAN 10ML",
                code = "ALLO-DSP-10",
                category = "Allopathic",
                composition = "Single use sterile syringe with needle",
                pack = "50'S",
                unit = "Box",
                batchNumber = "407104JD1",
                mfgDate = "05/24",
                expiryDate = "10/29",
                hsnCode = "90189099",
                mrp = 9.50,
                purchaseRate = 2.40,
                saleRate = 3.85,
                wholesaleRate = 3.50,
                dealerRate = 3.20,
                defaultDiscount = 12.00,
                sgstPercent = 6.00,
                cgstPercent = 6.00,
                stockQty = 350,
                reorderLevel = 50
            )

            database.productDao().insertAll(listOf(p1, p2, p3, p4, p5, p6))

            // Seed Reference Invoice
            val invoice1 = Invoice(
                invoiceNumber = "VAP-016398877",
                invoiceDate = "14-06-2024",
                dueDate = "05-07-2024",
                orderNo = "ORD-65644",
                orderDate = "25-05-2024",
                invoiceType = "TAX_INVOICE",
                partyId = pCustomer1,
                partyName = "ASIA TICO PHARMA",
                partyAddress = "#65-54-23, GROUND FLOOR, 33 MAIN ROAD, BTM LAYOUT, 2ND STAGE\nBANGALORE-68",
                partyPhone = "9845012345",
                partyDlNo = "20B-KA-B62-202160, 21B-KA-B62-202161",
                partyGstin = "29AHHPT3055G1Z2",
                paymentMode = "CREDIT",
                subTotalTaxable = 73.70,
                totalDiscount = 4.88,
                sgstPayable = 4.13,
                cgstPayable = 4.13,
                crDrNote = 0.00,
                grandTotal = 77.00,
                paidAmount = 77.00,
                balanceAmount = 0.00,
                amountInWords = "Rs. Seventy Seven only",
                status = "PAID",
                eInvoiceIrn = "7f8b9a1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a",
                eInvoiceAckNo = "112348912903",
                eInvoiceAckDate = "14-06-2024 11:42",
                eWayBillNo = "221089230192",
                vehicleNo = "KA-01-MJ-8821",
                templateName = "Veda Pharma A4 Landscape"
            )

            val invoice1Items = listOf(
                InvoiceItem(
                    serialNo = 1,
                    qty = 6,
                    freeQty = 0,
                    pack = "1*10",
                    productName = "CROCIN NEW 20MG",
                    batch = "6546TRYTR",
                    exp = "2/28",
                    hsn = "90189023",
                    mrp = 25.00,
                    rate = 11.00,
                    discountPercent = 6.00,
                    sgstPercent = 6.00,
                    cgstPercent = 6.00,
                    gstVal = 7.44,
                    amount = 69.48
                ),
                InvoiceItem(
                    serialNo = 2,
                    qty = 2,
                    freeQty = 0,
                    pack = "50'S",
                    productName = "DISPOVAN 10ML",
                    batch = "407104JD1",
                    exp = "10/29",
                    hsn = "90189099",
                    mrp = 9.50,
                    rate = 3.85,
                    discountPercent = 12.00,
                    sgstPercent = 6.00,
                    cgstPercent = 6.00,
                    gstVal = 0.82,
                    amount = 7.60
                )
            )
            database.invoiceDao().insertInvoiceWithItems(invoice1, invoice1Items)

            // Seed Ayurvedic Bill
            val invoice2 = Invoice(
                invoiceNumber = "VAP-2024-00102",
                invoiceDate = "10-09-2024",
                dueDate = "01-10-2024",
                orderNo = "PO-891",
                orderDate = "08-09-2024",
                invoiceType = "WHOLESALE",
                partyId = pCustomer2,
                partyName = "SRI BALAJI AYURVEDIC MEDICALS",
                partyAddress = "D.No 12/88, Main Bazaar, Near Raj Vihar, Kurnool-518001",
                partyPhone = "9440188990",
                partyDlNo = "20B-AP-KNL-554421, 21B-AP-KNL-554422",
                partyGstin = "37AAGCS9912E1Z4",
                paymentMode = "UPI",
                subTotalTaxable = 4650.00,
                totalDiscount = 372.00,
                sgstPayable = 256.68,
                cgstPayable = 256.68,
                crDrNote = 0.00,
                grandTotal = 4791.00,
                paidAmount = 4791.00,
                balanceAmount = 0.00,
                amountInWords = "Rs. Four Thousand Seven Hundred Ninety One only",
                status = "PAID",
                eInvoiceIrn = "9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b",
                templateName = "Veda Pharma A4 Landscape"
            )

            val invoice2Items = listOf(
                InvoiceItem(
                    serialNo = 1,
                    qty = 20,
                    freeQty = 2,
                    pack = "100g Jar",
                    productName = "VEDA ASHWAGANDHA CHURNA",
                    batch = "ASH2401",
                    exp = "01/27",
                    hsn = "30049011",
                    mrp = 180.00,
                    rate = 110.00,
                    discountPercent = 8.00,
                    sgstPercent = 6.00,
                    cgstPercent = 6.00,
                    gstVal = 242.88,
                    amount = 2266.88
                ),
                InvoiceItem(
                    serialNo = 2,
                    qty = 10,
                    freeQty = 1,
                    pack = "500g",
                    productName = "VEDA CHYAWANPRASH SPECIAL",
                    batch = "CHY2411",
                    exp = "10/26",
                    hsn = "30049011",
                    mrp = 395.00,
                    rate = 260.00,
                    discountPercent = 8.00,
                    sgstPercent = 6.00,
                    cgstPercent = 6.00,
                    gstVal = 287.04,
                    amount = 2524.12
                )
            )
            database.invoiceDao().insertInvoiceWithItems(invoice2, invoice2Items)

            // Seed Initial Purchase Record from Supplier
            val purchase1 = PurchaseRecord(
                purchaseInvoiceNo = "PUR-HR-8821",
                purchaseDate = "01-09-2024",
                supplierId = pSupplier1,
                supplierName = "HERBAL ROOTS EXTRACTS LTD",
                supplierGstin = "05AABCH4912J1Z3",
                totalTaxable = 28500.00,
                totalGst = 3420.00,
                grandTotal = 31920.00,
                paymentMode = "CREDIT",
                status = "RECEIVED"
            )
            val purchase1Items = listOf(
                PurchaseItem(
                    productName = "VEDA ASHWAGANDHA CHURNA RAW",
                    batchNumber = "ASH2401",
                    expiryDate = "01/27",
                    pack = "50Kg Drum",
                    qty = 2,
                    purchaseRate = 12000.00,
                    mrp = 18000.00,
                    gstPercent = 12.0,
                    amount = 26880.00
                )
            )
            database.purchaseDao().insertPurchaseWithItems(purchase1, purchase1Items)

            // Seed Accounting Transactions
            database.accountsDao().insert(
                AccountsTransaction(
                    date = "14-06-2024",
                    type = "RECEIPT",
                    category = "Customer Collection",
                    amount = 77.00,
                    partyName = "ASIA TICO PHARMA",
                    paymentMode = "CASH",
                    referenceNo = "REC-1001",
                    notes = "Against bill VP-016398877"
                )
            )
            database.accountsDao().insert(
                AccountsTransaction(
                    date = "10-09-2024",
                    type = "RECEIPT",
                    category = "Customer Collection",
                    amount = 4791.00,
                    partyName = "SRI BALAJI AYURVEDIC MEDICALS",
                    paymentMode = "UPI",
                    referenceNo = "UPI-7749120934",
                    notes = "Against bill VAP-2024-00102"
                )
            )
            database.accountsDao().insert(
                AccountsTransaction(
                    date = "11-09-2024",
                    type = "EXPENSE",
                    category = "Shop Rent",
                    amount = 12000.00,
                    partyName = "Commercial Landlord",
                    paymentMode = "BANK",
                    referenceNo = "NEFT-991203",
                    notes = "Monthly shop rent"
                )
            )
            database.accountsDao().insert(
                AccountsTransaction(
                    date = "12-09-2024",
                    type = "EXPENSE",
                    category = "Electricity",
                    amount = 2450.00,
                    partyName = "APSPDCL",
                    paymentMode = "UPI",
                    referenceNo = "UPI-ELEC-441",
                    notes = "Commercial electricity power bill"
                )
            )
        }
    }
}
