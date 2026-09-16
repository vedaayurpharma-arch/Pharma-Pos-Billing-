package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountsDao
import com.example.data.dao.CompanyDao
import com.example.data.dao.FieldSalesDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.InvoiceDesignerDao
import com.example.data.dao.PartyDao
import com.example.data.dao.ProductDao
import com.example.data.dao.PurchaseDao
import com.example.data.model.AccountsTransaction
import com.example.data.model.CompanyProfile
import com.example.data.model.CustomerVisit
import com.example.data.model.FieldCustomer
import com.example.data.model.FieldDocument
import com.example.data.model.FieldOrder
import com.example.data.model.FieldOrderItem
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDesignerConfig
import com.example.data.model.InvoiceItem
import com.example.data.model.Party
import com.example.data.model.Product
import com.example.data.model.ProductAllocation
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseRecord
import com.example.data.model.TourExpense
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
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
        InvoiceDesignerConfig::class,
        FieldCustomer::class,
        FieldOrder::class,
        FieldOrderItem::class,
        CustomerVisit::class,
        TourExpense::class,
        ProductAllocation::class,
        FieldDocument::class,
        UserAccount::class
    ],
    version = 3,
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
    abstract fun fieldSalesDao(): FieldSalesDao

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

            // Seed Field Sales User Accounts
            val dao = database.fieldSalesDao()
            dao.insertUsers(
                listOf(
                    UserAccount(
                        name = "Dr. Veda Murthy",
                        email = "vedaayurpharma@gmail.com",
                        phone = "+91 94401 23456",
                        role = UserRole.ADMIN,
                        stateAssigned = "All States (Executive)",
                        isGoogleAccount = true,
                        isCloudSyncEnabled = true,
                        lastSyncTime = "Synced with Cloud"
                    ),
                    UserAccount(
                        name = "Priya Sharma",
                        email = "priya.manager@vedaayur.in",
                        phone = "+91 98450 67890",
                        role = UserRole.MANAGER,
                        stateAssigned = "South Zone (KA, AP, TS)",
                        isGoogleAccount = false,
                        isCloudSyncEnabled = true,
                        lastSyncTime = "Synced with Cloud"
                    ),
                    UserAccount(
                        name = "Ramesh Kumar",
                        email = "ramesh.sales@vedaayur.in",
                        phone = "+91 91234 56789",
                        role = UserRole.FIELD_SALES,
                        stateAssigned = "Karnataka & Maharashtra",
                        isGoogleAccount = false,
                        isCloudSyncEnabled = true,
                        lastSyncTime = "Synced 10m ago"
                    ),
                    UserAccount(
                        name = "Kiran Reddy",
                        email = "kiran.sales@vedaayur.in",
                        phone = "+91 99887 65432",
                        role = UserRole.FIELD_SALES,
                        stateAssigned = "Andhra Pradesh & Telangana",
                        isGoogleAccount = false,
                        isCloudSyncEnabled = true,
                        lastSyncTime = "Synced 25m ago"
                    )
                )
            )

            // Seed Field Customers
            val p1Id = 1L
            val p2Id = 2L
            val p3Id = 3L
            val p4Id = 4L
            val p5Id = 5L
            val p6Id = 6L
            val p7Id = 7L
            val p8Id = 8L

            val fc1Id = dao.insertCustomer(
                FieldCustomer(
                    customerName = "Dr. S. K. Rao (BAMS)",
                    clinicOrPharmacyName = "Dhanvantari Ayurvedic Nilayam",
                    phone = "9845112233",
                    address = "#44, Temple Road, Malleshwaram 8th Cross",
                    state = "Karnataka",
                    city = "Bangalore",
                    pinCode = "560003",
                    latitude = 13.0031,
                    longitude = 77.5643,
                    salesRep = "Ramesh Kumar",
                    dateAdded = "12-09-2024",
                    totalPurchasesValue = 38500.0,
                    lastVisitDate = "14-09-2024"
                )
            )

            val fc2Id = dao.insertCustomer(
                FieldCustomer(
                    customerName = "K. Srinivasa Rao",
                    clinicOrPharmacyName = "Sri Balaji Ayurvedic Stores",
                    phone = "9440188990",
                    address = "D.No 12/88, Main Bazaar, Near Raj Vihar",
                    state = "Andhra Pradesh",
                    city = "Kurnool",
                    pinCode = "518001",
                    latitude = 15.8281,
                    longitude = 78.0373,
                    salesRep = "Kiran Reddy",
                    dateAdded = "10-09-2024",
                    totalPurchasesValue = 24900.0,
                    lastVisitDate = "15-09-2024"
                )
            )

            val fc3Id = dao.insertCustomer(
                FieldCustomer(
                    customerName = "Dr. Ananya Deshmukh",
                    clinicOrPharmacyName = "AyurVeda Clinic & Panchakarma",
                    phone = "9823044556",
                    address = "Plot 89, Shivaji Nagar, FC Road",
                    state = "Maharashtra",
                    city = "Pune",
                    pinCode = "411005",
                    latitude = 18.5204,
                    longitude = 73.8567,
                    salesRep = "Ramesh Kumar",
                    dateAdded = "08-09-2024",
                    totalPurchasesValue = 42000.0,
                    lastVisitDate = "13-09-2024"
                )
            )

            val fc4Id = dao.insertCustomer(
                FieldCustomer(
                    customerName = "Dr. G. Prabhakar (MD Ayur)",
                    clinicOrPharmacyName = "Charaka Ayurveda Hospital",
                    phone = "9490123888",
                    address = "Door 5-9-22, Tilak Road, Abids",
                    state = "Telangana",
                    city = "Hyderabad",
                    pinCode = "500001",
                    latitude = 17.3916,
                    longitude = 78.4747,
                    salesRep = "Kiran Reddy",
                    dateAdded = "09-09-2024",
                    totalPurchasesValue = 56000.0,
                    lastVisitDate = "15-09-2024"
                )
            )

            val fc5Id = dao.insertCustomer(
                FieldCustomer(
                    customerName = "Venkata Ramanan",
                    clinicOrPharmacyName = "Sanjeevani Herbal Aushadhalaya",
                    phone = "9840299112",
                    address = "14/2, Anna Salai, T. Nagar",
                    state = "Tamil Nadu",
                    city = "Chennai",
                    pinCode = "600017",
                    latitude = 13.0418,
                    longitude = 80.2341,
                    salesRep = "Kiran Reddy",
                    dateAdded = "11-09-2024",
                    totalPurchasesValue = 31200.0,
                    lastVisitDate = "14-09-2024"
                )
            )

            // Seed Field Orders
            val order1Id = dao.insertOrder(
                FieldOrder(
                    orderNumber = "FLD-VAP-2024-001",
                    customerId = fc1Id,
                    customerName = "Dr. S. K. Rao (BAMS)",
                    clinicOrPharmacyName = "Dhanvantari Ayurvedic Nilayam",
                    phone = "9845112233",
                    state = "Karnataka",
                    city = "Bangalore",
                    orderDate = "14-09-2024",
                    salesRep = "Ramesh Kumar",
                    totalMrp = 24600.0,
                    netWholesaleRate = 18200.0,
                    discountPercent = 5.0,
                    discountAmount = 910.0,
                    gstPercent = 5.0,
                    gstAmount = 864.5,
                    finalPurchaseValue = 18154.5,
                    digitalInvoiceUrl = "https://vedaayurpharma.com/inv/FLD-001",
                    physicalReceiptUri = "receipt_fld_001.jpg",
                    orderNotes = "Requires urgent dispatch for autumn arthritis camp. Prefer batch ASH2401.",
                    paymentStatus = "PAID"
                )
            )

            dao.insertOrderItems(
                listOf(
                    FieldOrderItem(
                        orderId = order1Id,
                        productId = p1Id,
                        productName = "VEDA ASHWAGANDHA CHURNA",
                        pack = "100g Jar",
                        batchNumber = "ASH2401",
                        quantity = 60,
                        mrp = 180.0,
                        wholesaleRate = 110.0,
                        subtotal = 6600.0
                    ),
                    FieldOrderItem(
                        orderId = order1Id,
                        productId = p2Id,
                        productName = "VEDA CHYAWANPRASH SPECIAL",
                        pack = "500g Pet Jar",
                        batchNumber = "CHY2403",
                        quantity = 30,
                        mrp = 395.0,
                        wholesaleRate = 240.0,
                        subtotal = 7200.0
                    ),
                    FieldOrderItem(
                        orderId = order1Id,
                        productId = p3Id,
                        productName = "VEDA TRIPHALA GUGGULU",
                        pack = "80 Tabs",
                        batchNumber = "TRP2402",
                        quantity = 40,
                        mrp = 160.0,
                        wholesaleRate = 110.0,
                        subtotal = 4400.0
                    )
                )
            )

            val order2Id = dao.insertOrder(
                FieldOrder(
                    orderNumber = "FLD-VAP-2024-002",
                    customerId = fc4Id,
                    customerName = "Dr. G. Prabhakar (MD Ayur)",
                    clinicOrPharmacyName = "Charaka Ayurveda Hospital",
                    phone = "9490123888",
                    state = "Telangana",
                    city = "Hyderabad",
                    orderDate = "15-09-2024",
                    salesRep = "Kiran Reddy",
                    totalMrp = 38500.0,
                    netWholesaleRate = 26500.0,
                    discountPercent = 8.0,
                    discountAmount = 2120.0,
                    gstPercent = 5.0,
                    gstAmount = 1219.0,
                    finalPurchaseValue = 25599.0,
                    digitalInvoiceUrl = "https://vedaayurpharma.com/inv/FLD-002",
                    physicalReceiptUri = "receipt_fld_002.jpg",
                    orderNotes = "Hospital inpatient supply, monthly standing order.",
                    paymentStatus = "PAID"
                )
            )

            dao.insertOrderItems(
                listOf(
                    FieldOrderItem(
                        orderId = order2Id,
                        productId = p2Id,
                        productName = "VEDA CHYAWANPRASH SPECIAL",
                        pack = "500g Pet Jar",
                        batchNumber = "CHY2403",
                        quantity = 60,
                        mrp = 395.0,
                        wholesaleRate = 240.0,
                        subtotal = 14400.0
                    ),
                    FieldOrderItem(
                        orderId = order2Id,
                        productId = p4Id,
                        productName = "VEDA BRAHMI TAILA",
                        pack = "200ml Bottle",
                        batchNumber = "BRH2401",
                        quantity = 50,
                        mrp = 220.0,
                        wholesaleRate = 140.0,
                        subtotal = 7000.0
                    ),
                    FieldOrderItem(
                        orderId = order2Id,
                        productId = p5Id,
                        productName = "VEDA KASAMRIT HERBAL SYRUP",
                        pack = "100ml",
                        batchNumber = "KAS2404",
                        quantity = 65,
                        mrp = 115.0,
                        wholesaleRate = 78.0,
                        subtotal = 5070.0
                    )
                )
            )

            // Seed GPS Customer Visits
            dao.insertVisit(
                CustomerVisit(
                    customerId = fc1Id,
                    customerName = "Dr. S. K. Rao (BAMS)",
                    clinicOrPharmacyName = "Dhanvantari Ayurvedic Nilayam",
                    salesRep = "Ramesh Kumar",
                    visitDateTime = "14-09-2024 11:30 AM",
                    latitude = 13.0031,
                    longitude = 77.5643,
                    address = "Malleshwaram 8th Cross, Bangalore",
                    visitNotes = "Doctor appreciated batch quality of Chyawanprash. Placed order FLD-001 with 5% seasonal discount.",
                    outcome = "ORDER_TAKEN"
                )
            )

            dao.insertVisit(
                CustomerVisit(
                    customerId = fc4Id,
                    customerName = "Dr. G. Prabhakar (MD Ayur)",
                    clinicOrPharmacyName = "Charaka Ayurveda Hospital",
                    salesRep = "Kiran Reddy",
                    visitDateTime = "15-09-2024 02:15 PM",
                    latitude = 17.3916,
                    longitude = 78.4747,
                    address = "Tilak Road, Abids, Hyderabad",
                    visitNotes = "Met Hospital superintendent. Collected Cheque for previous bill and booked fresh order FLD-002.",
                    outcome = "PAYMENT_COLLECTED"
                )
            )

            dao.insertVisit(
                CustomerVisit(
                    customerId = fc2Id,
                    customerName = "K. Srinivasa Rao",
                    clinicOrPharmacyName = "Sri Balaji Ayurvedic Stores",
                    salesRep = "Kiran Reddy",
                    visitDateTime = "15-09-2024 05:45 PM",
                    latitude = 15.8281,
                    longitude = 78.0373,
                    address = "Main Bazaar, Kurnool",
                    visitNotes = "Demonstrated new Kasamrit Herbal Syrup samples. Chemist agreed to stock 50 bottles next week.",
                    outcome = "SAMPLE_GIVEN"
                )
            )

            // Seed Tour Expenses
            dao.insertExpense(
                TourExpense(
                    expenseDate = "14-09-2024",
                    salesRep = "Ramesh Kumar",
                    travelAmount = 1450.0,
                    foodAmount = 450.0,
                    accommodationAmount = 1800.0,
                    otherAmount = 200.0,
                    totalAmount = 3900.0,
                    notes = "Bangalore Malleshwaram & Rajajinagar clinic tour (Cab + Food + Hotel)",
                    receiptUri = "exp_blr_1409.jpg",
                    status = "APPROVED"
                )
            )

            dao.insertExpense(
                TourExpense(
                    expenseDate = "15-09-2024",
                    salesRep = "Kiran Reddy",
                    travelAmount = 1200.0,
                    foodAmount = 380.0,
                    accommodationAmount = 0.0,
                    otherAmount = 150.0,
                    totalAmount = 1730.0,
                    notes = "Hyderabad to Kurnool highway toll and intercity field transit",
                    receiptUri = "exp_knl_1509.jpg",
                    status = "PENDING"
                )
            )

            dao.insertExpense(
                TourExpense(
                    expenseDate = "13-09-2024",
                    salesRep = "Ramesh Kumar",
                    travelAmount = 2800.0,
                    foodAmount = 650.0,
                    accommodationAmount = 2200.0,
                    otherAmount = 350.0,
                    totalAmount = 6000.0,
                    notes = "Pune Shivaji Nagar and FC Road clinic tie-up tour",
                    receiptUri = "exp_pune_1309.jpg",
                    status = "APPROVED"
                )
            )

            // Seed State Product Allocations
            val allStates = listOf("Karnataka", "Andhra Pradesh", "Telangana", "Maharashtra", "Tamil Nadu")
            val seededProducts: List<Pair<Long, String>> = listOf(
                Pair(p1Id, "VEDA ASHWAGANDHA CHURNA"),
                Pair(p2Id, "VEDA CHYAWANPRASH SPECIAL"),
                Pair(p3Id, "VEDA TRIPHALA GUGGULU"),
                Pair(p4Id, "VEDA BRAHMI TAILA"),
                Pair(p5Id, "VEDA KASAMRIT HERBAL SYRUP"),
                Pair(p6Id, "VEDA LIV-99 HEPATO TONIC"),
                Pair(p7Id, "VEDA MAHANARAYAN OIL"),
                Pair(p8Id, "VEDA SHILAJIT RESIN PURE")
            )

            val allocationList = mutableListOf<ProductAllocation>()
            for (state in allStates) {
                for ((pId, pName) in seededProducts) {
                    val isAvail = true
                    val stateDiscount = when (state) {
                        "Karnataka" -> 5.0
                        "Andhra Pradesh" -> 6.0
                        "Telangana" -> 5.0
                        "Maharashtra" -> 7.5
                        else -> 4.0
                    }
                    allocationList.add(
                        ProductAllocation(
                            state = state,
                            productId = pId,
                            productName = pName,
                            isAvailable = isAvail,
                            specialStateDiscount = stateDiscount,
                            allocatedQuota = 1000
                        )
                    )
                }
            }
            dao.insertAllocations(allocationList)

            // Seed Field Documents
            dao.insertDocument(
                FieldDocument(
                    title = "Bill Receipt - Dhanvantari Nilayam #001",
                    customerName = "Dr. S. K. Rao (BAMS)",
                    documentType = "PHYSICAL_BILL",
                    dateUploaded = "14-09-2024",
                    fileUrlOrUri = "docs/receipt_fld_001.pdf",
                    notes = "Signed and stamped physical copy of purchase order.",
                    orderId = order1Id
                )
            )

            dao.insertDocument(
                FieldDocument(
                    title = "Ayush Drug License 20B/21B - Sri Balaji",
                    customerName = "K. Srinivasa Rao",
                    documentType = "DRUG_LICENSE",
                    dateUploaded = "10-09-2024",
                    fileUrlOrUri = "docs/dl_balaji_knl.pdf",
                    notes = "Valid until 31-12-2028 under AP Ayush Authority.",
                    orderId = null
                )
            )

            dao.insertDocument(
                FieldDocument(
                    title = "GST Registration Certificate - Charaka Hospital",
                    customerName = "Dr. G. Prabhakar (MD Ayur)",
                    documentType = "GST_CERT",
                    dateUploaded = "09-09-2024",
                    fileUrlOrUri = "docs/gst_charaka_hyd.pdf",
                    notes = "GSTIN: 36AAACG1234H1Z5 verified.",
                    orderId = null
                )
            )
        }
    }
}
