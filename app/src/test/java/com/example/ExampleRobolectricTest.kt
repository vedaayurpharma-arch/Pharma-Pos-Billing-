package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CompanyProfile
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceWithItems
import com.example.util.InvoiceHtmlGenerator
import com.example.util.NumberToWordsConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Veda Ayur Pharma ERP", appName)
    }

    @Test
    fun `number to words converter formats correctly`() {
        val words = NumberToWordsConverter.convert(77.00)
        assertEquals("Rs. Seventy Seven only", words)
    }

    @Test
    fun `html generator contains landscape pharma bill elements`() {
        val invoice = Invoice(
            invoiceNumber = "VP-016398877",
            invoiceDate = "29/08/2024",
            partyName = "ASIA TICO PHARMA",
            grandTotal = 77.00,
            amountInWords = "Rs. Seventy Seven only"
        )
        val items = listOf(
            InvoiceItem(
                serialNo = 1,
                productName = "CROCIN NEW 20MG",
                pack = "1*10",
                batch = "6546TRYTR",
                exp = "2/28",
                hsn = "90189023",
                mrp = 25.00,
                rate = 11.00,
                qty = 6,
                discountPercent = 6.00,
                sgstPercent = 6.00,
                cgstPercent = 6.00,
                gstVal = 7.44,
                amount = 77.00
            )
        )
        val html = InvoiceHtmlGenerator.generateHtml(
            invoiceWithItems = InvoiceWithItems(invoice, items),
            company = CompanyProfile()
        )

        assertTrue(html.contains("TAX INVOICE"))
        assertTrue(html.contains("A4 landscape"))
        assertTrue(html.contains("VP-016398877"))
        assertTrue(html.contains("ASIA TICO PHARMA"))
        assertTrue(html.contains("CROCIN NEW 20MG"))
    }
}
