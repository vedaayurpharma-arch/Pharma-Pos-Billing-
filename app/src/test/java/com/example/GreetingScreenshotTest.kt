package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceWithItems
import com.example.ui.screens.InvoiceCardItem
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun invoice_card_screenshot() {
        val invoice = Invoice(
            id = 1,
            invoiceNumber = "VP-016398877",
            invoiceDate = "29/08/2024",
            partyName = "ASIA TICO PHARMA",
            grandTotal = 77.00,
            status = "PAID"
        )
        val items = listOf(
            InvoiceItem(
                serialNo = 1,
                productName = "CROCIN NEW 20MG",
                pack = "1*10",
                qty = 6,
                rate = 11.00,
                amount = 77.00
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                InvoiceCardItem(
                    invoiceWithItems = InvoiceWithItems(invoice, items),
                    onCardClick = {},
                    onPrintClick = {},
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/invoice_card.png")
    }
}
