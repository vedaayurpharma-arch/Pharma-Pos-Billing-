package com.example.util

import com.example.data.model.CompanyProfile
import com.example.data.model.InvoiceDesignerConfig
import com.example.data.model.InvoiceWithItems
import java.net.URLEncoder
import java.util.Locale

object InvoiceHtmlGenerator {

    data class GstSlabSummary(
        val label: String,
        val taxable: Double = 0.0,
        val scheme: Double = 0.0,
        val discount: Double = 0.0,
        val sgst: Double = 0.0,
        val cgst: Double = 0.0,
        val totalGst: Double = 0.0
    )

    fun generateHtml(
        invoiceWithItems: InvoiceWithItems,
        company: CompanyProfile,
        config: InvoiceDesignerConfig = InvoiceDesignerConfig()
    ): String {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val totalQty = items.sumOf { it.qty }
        val totalItemsCount = items.size

        // Calculate GST slabs: 5%, 12%, 18%, 28%
        val slabMap = mutableMapOf<Int, GstSlabSummary>()
        val knownRates = listOf(5, 12, 18, 28)
        for (rate in knownRates) {
            slabMap[rate] = GstSlabSummary("GST ${String.format(Locale.US, "%.2f", rate.toDouble())}%")
        }

        var totalTaxableAll = 0.0
        var totalDiscountAll = 0.0
        var totalSgstAll = 0.0
        var totalCgstAll = 0.0
        var totalGstAll = 0.0

        for (item in items) {
            val totalTaxRate = (item.sgstPercent + item.cgstPercent).toInt()
            val baseRate = item.rate * item.qty
            val discountAmt = baseRate * (item.discountPercent / 100.0)
            val taxable = baseRate - discountAmt
            val sgstAmt = taxable * (item.sgstPercent / 100.0)
            val cgstAmt = taxable * (item.cgstPercent / 100.0)
            val gstVal = sgstAmt + cgstAmt

            totalTaxableAll += taxable
            totalDiscountAll += discountAmt
            totalSgstAll += sgstAmt
            totalCgstAll += cgstAmt
            totalGstAll += gstVal

            val current = slabMap[totalTaxRate] ?: GstSlabSummary("GST ${String.format(Locale.US, "%.2f", totalTaxRate.toDouble())}%")
            slabMap[totalTaxRate] = current.copy(
                taxable = current.taxable + taxable,
                discount = current.discount + discountAmt,
                sgst = current.sgst + sgstAmt,
                cgst = current.cgst + cgstAmt,
                totalGst = current.totalGst + gstVal
            )
        }

        val upiString = "upi://pay?pa=${company.upiId}&pn=${URLEncoder.encode(company.companyName, "UTF-8")}&am=${String.format(Locale.US, "%.2f", invoice.grandTotal)}&cu=INR"
        val upiQrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=70x70&data=${URLEncoder.encode(upiString, "UTF-8")}"

        val irnData = if (invoice.eInvoiceIrn.isNotEmpty()) invoice.eInvoiceIrn else "IRN:37AAKFV1234F1Z8:${invoice.invoiceNumber}:${invoice.grandTotal}"
        val einvoiceQrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=70x70&data=${URLEncoder.encode(irnData, "UTF-8")}"

        // Handle Thermal Receipt Templates
        if (config.selectedTemplate.contains("Thermal", ignoreCase = true)) {
            return generateThermalHtml(invoiceWithItems, company, config, upiQrCodeUrl)
        }

        // Handle Portrait Template
        if (config.paperOrientation.equals("PORTRAIT", ignoreCase = true) || config.selectedTemplate.contains("Portrait", ignoreCase = true)) {
            return generatePortraitHtml(invoiceWithItems, company, config, upiQrCodeUrl, einvoiceQrCodeUrl, slabMap, totalTaxableAll, totalDiscountAll, totalSgstAll, totalCgstAll, totalGstAll)
        }

        // Default & Flagship: VEDA AYUR PHARMA A4 Landscape Format
        val itemsHtml = buildString {
            items.forEachIndexed { index, item ->
                append("<tr>")
                append("<td class='text-center'>${index + 1}.</td>")
                append("<td class='text-center'>${item.qty}</td>")
                append("<td class='text-center'>${item.freeQty}</td>")
                append("<td class='text-center'>${item.pack}</td>")
                append("<td class='font-bold'>${item.productName}</td>")
                if (config.showBatch) append("<td class='text-center'>${item.batch}</td>")
                if (config.showExpiry) append("<td class='text-center'>${item.exp}</td>")
                if (config.showHsn) append("<td class='text-center'>${item.hsn}</td>")
                if (config.showMrp) append("<td class='text-right'>${String.format(Locale.US, "%.2f", item.mrp)}</td>")
                append("<td class='text-right'>${String.format(Locale.US, "%.2f", item.rate)}</td>")
                if (config.showDiscount) append("<td class='text-center'>${String.format(Locale.US, "%.2f", item.discountPercent)}</td>")
                if (config.showCgstSgst) {
                    append("<td class='text-center'>${String.format(Locale.US, "%.2f", item.sgstPercent)}</td>")
                    append("<td class='text-center'>${String.format(Locale.US, "%.2f", item.cgstPercent)}</td>")
                    append("<td class='text-right'>${String.format(Locale.US, "%.2f", item.gstVal)}</td>")
                }
                append("<td class='text-right font-bold'>${String.format(Locale.US, "%.2f", item.amount)}</td>")
                append("</tr>\n")
            }

            // Add spacer row if few items to match landscape layout
            val spacerHeight = if (items.size < 4) "60px" else "15px"
            val colSpan = 5 + (if (config.showBatch) 1 else 0) + (if (config.showExpiry) 1 else 0) +
                    (if (config.showHsn) 1 else 0) + (if (config.showMrp) 1 else 0) + 1 +
                    (if (config.showDiscount) 1 else 0) + (if (config.showCgstSgst) 3 else 0) + 1
            append("""<tr style="height: $spacerHeight;"><td colspan="$colSpan"></td></tr>""")
        }

        val gstRowsHtml = buildString {
            for (rate in knownRates) {
                val slab = slabMap[rate] ?: GstSlabSummary("GST ${String.format(Locale.US, "%.2f", rate.toDouble())}%")
                append("""
                <tr>
                    <td>${slab.label}</td>
                    <td>${String.format(Locale.US, "%.2f", slab.taxable)}</td>
                    <td>${String.format(Locale.US, "%.2f", slab.scheme)}</td>
                    <td>${String.format(Locale.US, "%.2f", slab.discount)}</td>
                    <td>${String.format(Locale.US, "%.2f", slab.sgst)}</td>
                    <td>${String.format(Locale.US, "%.2f", slab.cgst)}</td>
                    <td>${String.format(Locale.US, "%.2f", slab.totalGst)}</td>
                </tr>
                """.trimIndent())
            }
            append("""
            <tr class="font-bold">
                <td>TOTAL</td>
                <td>${String.format(Locale.US, "%.2f", totalTaxableAll)}</td>
                <td>0.00</td>
                <td>${String.format(Locale.US, "%.2f", totalDiscountAll)}</td>
                <td>${String.format(Locale.US, "%.2f", totalSgstAll)}</td>
                <td>${String.format(Locale.US, "%.2f", totalCgstAll)}</td>
                <td>${String.format(Locale.US, "%.2f", totalGstAll)}</td>
            </tr>
            """.trimIndent())
        }

        val termsLines = company.termsConditions.lines().joinToString("") { "<div>$it</div>" }
        val headerColor = config.primaryColorHex

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${company.companyName} - ${invoice.invoiceNumber}</title>
    <style>
        @page {
            size: A4 landscape;
            margin: 6mm;
        }
        * {
            box-sizing: border-box;
            font-family: Arial, Helvetica, sans-serif;
            font-size: ${config.fontSizePt}px;
            margin: 0;
            padding: 0;
        }
        body {
            background-color: #f5f5f5;
            padding: 8px;
        }
        .page {
            width: 283mm;
            min-height: 194mm;
            background: #fff;
            margin: auto;
            border: 1px solid #111;
            padding: 5px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }
        .text-center { text-align: center; }
        .text-right { text-align: right; }
        .text-left { text-align: left; }
        .font-bold { font-weight: bold; }
        .theme-header {
            background-color: $headerColor;
            color: #ffffff;
            padding: 4px 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .theme-header h1 {
            font-size: 16px;
            letter-spacing: 0.5px;
        }
        .top-meta-table {
            width: 100%;
            border-collapse: collapse;
            border-bottom: 1px solid #000;
            margin-bottom: 3px;
        }
        .top-meta-table td {
            padding: 3px;
            vertical-align: top;
            font-size: 9.5px;
        }
        .header-box {
            border: 1px solid #000;
            margin-bottom: 3px;
        }
        .header-box-table {
            width: 100%;
            border-collapse: collapse;
        }
        .header-box-table td {
            padding: 3px 5px;
            vertical-align: top;
        }
        .border-right { border-right: 1px solid #000; }
        .border-bottom { border-bottom: 1px solid #000; }
        .item-table-container {
            flex-grow: 1;
            margin-bottom: 3px;
        }
        .item-table {
            width: 100%;
            border-collapse: collapse;
        }
        .item-table th {
            border: 1px solid #000;
            padding: 4px 2px;
            background: #f0f4f0;
            font-size: 9px;
            font-weight: bold;
            text-align: center;
        }
        .item-table td {
            border-left: 1px solid #000;
            border-right: 1px solid #000;
            padding: 3px 2px;
            font-size: 9px;
        }
        .footer-section {
            display: flex;
            border: 1px solid #000;
            margin-bottom: 3px;
        }
        .gst-grid {
            width: 62%;
            border-right: 1px solid #000;
        }
        .gst-grid table {
            width: 100%;
            border-collapse: collapse;
        }
        .gst-grid th, .gst-grid td {
            border: 1px solid #000;
            padding: 2px 4px;
            font-size: 8.5px;
            text-align: right;
        }
        .gst-grid th {
            text-align: center;
            background: #f9f9f9;
        }
        .totals-grid {
            width: 38%;
            display: flex;
        }
        .totals-left {
            width: 45%;
            border-right: 1px solid #000;
            padding: 4px;
            font-size: 9.5px;
        }
        .totals-right {
            width: 55%;
        }
        .totals-row {
            display: flex;
            justify-content: space-between;
            padding: 2px 4px;
            font-size: 9px;
            border-bottom: 1px dotted #ccc;
        }
        .grand-total-box {
            font-size: 12px;
            font-weight: bold;
            background: #e8f5e9;
            color: #0d5302;
            border-top: 1px solid #000;
            padding: 5px;
        }
        .bottom-section {
            display: flex;
            border: 1px solid #000;
            padding: 4px;
            background: #fafafa;
        }
        .bank-terms {
            width: 65%;
            font-size: 8.5px;
            line-height: 1.25;
        }
        .qr-section {
            width: 15%;
            text-align: center;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            border-left: 1px dashed #aaa;
            border-right: 1px dashed #aaa;
        }
        .signature-section {
            width: 20%;
            text-align: right;
            padding: 4px;
            font-size: 9.5px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }
    </style>
</head>
<body>

<div class="page">
    <!-- Header Banner -->
    <div class="theme-header">
        <div>
            <h1>${company.companyName}</h1>
            <div style="font-size: 9px; font-weight: normal;">${company.tagline}</div>
        </div>
        <div style="text-align: right;">
            <div style="font-size: 11px; font-weight: bold;">TAX INVOICE (${invoice.invoiceType})</div>
            <div style="font-size: 8.5px;">ORIGINAL FOR RECIPIENT</div>
        </div>
    </div>

    <!-- Metadata Table -->
    <table class="top-meta-table">
        <tr>
            <td style="width: 50%;">
                <div><b>D.L.No. :</b> ${company.dlNo}</div>
                <div><b>GSTIN :</b> ${company.gstin} | <b>PAN :</b> ${company.pan}</div>
                <div><b>ADDRESS :</b> ${company.addressLine1}, ${company.addressLine2}</div>
                <div><b>PHONE :</b> ${company.phone} | <b>EMAIL :</b> ${company.email}</div>
            </td>
            <td style="width: 50%;" class="text-right">
                <div><b>Invoice No. :</b> <span style="font-size:11px; font-weight:bold; color:$headerColor;">${invoice.invoiceNumber}</span></div>
                <div><b>Invoice Date :</b> ${invoice.invoiceDate} | <b>Due Date :</b> ${invoice.dueDate}</div>
                <div><b>Order No. :</b> ${invoice.orderNo.ifEmpty { "-" }} | <b>Order Date :</b> ${invoice.orderDate.ifEmpty { "-" }}</div>
                <div><b>Payment Mode :</b> <b>${invoice.paymentMode}</b> | Status: <b>${invoice.status}</b></div>
                ${if (invoice.eInvoiceAckNo.isNotEmpty()) "<div><b>e-Inv Ack:</b> ${invoice.eInvoiceAckNo} (${invoice.eInvoiceAckDate})</div>" else ""}
                ${if (invoice.eWayBillNo.isNotEmpty()) "<div><b>E-Way Bill:</b> ${invoice.eWayBillNo} | Veh: ${invoice.vehicleNo}</div>" else ""}
            </td>
        </tr>
    </table>

    <!-- Buyer Details Box -->
    <div class="header-box">
        <table class="header-box-table">
            <tr>
                <td style="width: 60%;" class="border-right">
                    <div style="font-size:8.5px; color:#555;">DETAILS OF RECEIVER / BILLED TO :</div>
                    <div class="font-bold" style="font-size: 11px;">${invoice.partyName}</div>
                    <div>${invoice.partyAddress.replace("\n", "<br/>")}</div>
                    <div><b>Phone :</b> ${invoice.partyPhone.ifEmpty { "-" }}</div>
                    ${if (config.showCustomerDl) "<div><b>D.L.No. :</b> ${invoice.partyDlNo.ifEmpty { "-" }}</div>" else ""}
                    ${if (config.showCustomerGstin) "<div><b>GSTIN :</b> ${invoice.partyGstin.ifEmpty { "URP (Unregistered)" }}</div>" else ""}
                </td>
                <td style="width: 40%;">
                    <div style="font-size:8.5px; color:#555;">SHIPPED TO / DISPATCH DETAILS :</div>
                    <div class="font-bold">${invoice.partyName}</div>
                    <div>${invoice.partyAddress.lines().firstOrNull() ?: ""}</div>
                    <div><b>Place of Supply :</b> ${company.state}</div>
                    <div><b>Reverse Charge :</b> No</div>
                </td>
            </tr>
        </table>
    </div>

    <!-- Line Items Table -->
    <div class="item-table-container">
        <table class="item-table">
            <thead>
                <tr>
                    <th style="width: 3%;">S.N</th>
                    <th style="width: 4%;">Qty.</th>
                    <th style="width: 4%;">Free</th>
                    <th style="width: 6%;">Pack</th>
                    <th style="width: 25%;">Product Name / Ayurvedic Composition</th>
                    ${if (config.showBatch) "<th style='width: 9%;'>Batch</th>" else ""}
                    ${if (config.showExpiry) "<th style='width: 5%;'>Exp</th>" else ""}
                    ${if (config.showHsn) "<th style='width: 8%;'>HSN</th>" else ""}
                    ${if (config.showMrp) "<th style='width: 6%;'>M.R.P</th>" else ""}
                    <th style="width: 6%;">Rate</th>
                    ${if (config.showDiscount) "<th style='width: 5%;'>DIS %</th>" else ""}
                    ${if (config.showCgstSgst) "<th style='width: 5%;'>SGST%</th><th style='width: 5%;'>CGST%</th><th style='width: 5%;'>GST Val</th>" else ""}
                    <th style="width: 7%;">Amount (₹)</th>
                </tr>
            </thead>
            <tbody>
                $itemsHtml
            </tbody>
        </table>
    </div>

    <!-- GST Breakdown & Totals -->
    <div class="footer-section">
        <div class="gst-grid">
            <table>
                <thead>
                    <tr>
                        <th>CLASS</th>
                        <th>TAXABLE</th>
                        <th>SCHEME</th>
                        <th>DISCOUNT</th>
                        <th>SGST</th>
                        <th>CGST</th>
                        <th>TOTAL GST</th>
                    </tr>
                </thead>
                <tbody>
                    $gstRowsHtml
                </tbody>
            </table>
            <div style="padding: 4px; font-weight: bold; background: #fff;">Amount in words: ${invoice.amountInWords}</div>
        </div>
        <div class="totals-grid">
            <div class="totals-left">
                <div class="totals-row"><span>Total Items:</span> <b>$totalItemsCount</b></div>
                <div class="totals-row"><span>Total Qty:</span> <b>$totalQty</b></div>
                <div class="totals-row"><span>Scheme Disc:</span> <b>0.00</b></div>
                <div class="totals-row"><span>Taxable Value:</span> <b>₹${String.format(Locale.US, "%.2f", totalTaxableAll)}</b></div>
                <div class="totals-row"><span>Total Tax:</span> <b>₹${String.format(Locale.US, "%.2f", totalGstAll)}</b></div>
            </div>
            <div class="totals-right">
                <div class="totals-row"><span>SUB TOTAL</span> <span>₹${String.format(Locale.US, "%.2f", invoice.subTotalTaxable)}</span></div>
                <div class="totals-row"><span>DISCOUNT</span> <span>- ₹${String.format(Locale.US, "%.2f", invoice.totalDiscount)}</span></div>
                <div class="totals-row"><span>SGST</span> <span>+ ₹${String.format(Locale.US, "%.2f", invoice.sgstPayable)}</span></div>
                <div class="totals-row"><span>CGST</span> <span>+ ₹${String.format(Locale.US, "%.2f", invoice.cgstPayable)}</span></div>
                <div class="totals-row"><span>CR/DR NOTE</span> <span>₹${String.format(Locale.US, "%.2f", invoice.crDrNote)}</span></div>
                <div class="totals-row grand-total-box"><span>NET PAYABLE</span> <span>₹${String.format(Locale.US, "%.2f", invoice.grandTotal)}</span></div>
            </div>
        </div>
    </div>

    <!-- Terms, Bank & QR Payments -->
    <div class="bottom-section">
        <div class="bank-terms">
            ${if (config.showTerms) """
                <div class="font-bold" style="text-decoration:underline;">Terms & Conditions</div>
                $termsLines
            """ else ""}
            ${if (config.showBankDetails) """
                <div class="font-bold" style="margin-top: 3px; text-decoration:underline;">Bank Details for Payment</div>
                <div>Bank: <b>${company.bankName}</b> | A/c No: <b>${company.accountNo}</b></div>
                <div>IFSC: <b>${company.ifscCode}</b> | Branch: <b>${company.branch}</b> | UPI ID: <b>${company.upiId}</b></div>
            """ else ""}
        </div>
        ${if (config.showUpiQr) """
        <div class="qr-section">
            <img src="$upiQrCodeUrl" width="55" height="55" alt="UPI Scan & Pay" />
            <div style="font-size:7.5px; font-weight:bold; margin-top:2px;">Scan to Pay UPI</div>
        </div>
        """ else ""}
        ${if (config.showEInvoiceQr && invoice.eInvoiceIrn.isNotEmpty()) """
        <div class="qr-section">
            <img src="$einvoiceQrCodeUrl" width="55" height="55" alt="e-Invoice QR" />
            <div style="font-size:7.5px; font-weight:bold; margin-top:2px;">GST e-Invoice QR</div>
        </div>
        """ else ""}
        ${if (config.showSignature) """
        <div class="signature-section">
            <div>For <b>${company.companyName}</b></div>
            <div style="margin-top: 25px;">Authorised Signatory</div>
        </div>
        """ else ""}
    </div>
</div>

</body>
</html>
        """.trimIndent()
    }

    private fun generateThermalHtml(
        invoiceWithItems: InvoiceWithItems,
        company: CompanyProfile,
        config: InvoiceDesignerConfig,
        upiQrCodeUrl: String
    ): String {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items
        val widthMm = config.thermalPaperWidthMm

        val itemsRows = items.joinToString("") { item ->
            """
            <tr>
                <td colspan="3" style="font-weight:bold;">${item.productName} (${item.pack})</td>
            </tr>
            <tr>
                <td>${item.qty} x ₹${String.format(Locale.US, "%.2f", item.rate)}</td>
                <td style="text-align:center;">${if (item.discountPercent > 0) "${item.discountPercent}% off" else ""}</td>
                <td style="text-align:right; font-weight:bold;">₹${String.format(Locale.US, "%.2f", item.amount)}</td>
            </tr>
            <tr><td colspan="3" style="font-size:8px; color:#555;">Batch: ${item.batch} | Exp: ${item.exp} | HSN: ${item.hsn}</td></tr>
            <tr><td colspan="3" style="border-bottom: 1px dotted #ccc;"></td></tr>
            """.trimIndent()
        }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Receipt ${invoice.invoiceNumber}</title>
    <style>
        @page { size: ${widthMm}mm auto; margin: 2mm; }
        body { font-family: monospace; font-size: 11px; width: ${widthMm - 4}mm; margin: 0 auto; padding: 4px; }
        .center { text-align: center; }
        .right { text-align: right; }
        .bold { font-weight: bold; }
        .divider { border-top: 1px dashed #000; margin: 4px 0; }
        table { width: 100%; border-collapse: collapse; }
    </style>
</head>
<body>
    <div class="center bold" style="font-size: 14px;">${company.companyName}</div>
    <div class="center">${company.addressLine1}</div>
    <div class="center">Ph: ${company.phone}</div>
    <div class="center">GSTIN: ${company.gstin}</div>
    <div class="center">DL: ${company.dlNo}</div>
    <div class="divider"></div>
    <div><b>Bill No:</b> ${invoice.invoiceNumber}</div>
    <div><b>Date:</b> ${invoice.invoiceDate}</div>
    <div><b>Customer:</b> ${invoice.partyName}</div>
    <div><b>DL:</b> ${invoice.partyDlNo}</div>
    <div class="divider"></div>
    <table>
        $itemsRows
    </table>
    <div class="divider"></div>
    <table>
        <tr><td>Sub Total:</td><td class="right">₹${String.format(Locale.US, "%.2f", invoice.subTotalTaxable)}</td></tr>
        <tr><td>Discount:</td><td class="right">-₹${String.format(Locale.US, "%.2f", invoice.totalDiscount)}</td></tr>
        <tr><td>CGST:</td><td class="right">+₹${String.format(Locale.US, "%.2f", invoice.cgstPayable)}</td></tr>
        <tr><td>SGST:</td><td class="right">+₹${String.format(Locale.US, "%.2f", invoice.sgstPayable)}</td></tr>
        <tr style="font-size: 14px;" class="bold"><td>TOTAL:</td><td class="right">₹${String.format(Locale.US, "%.2f", invoice.grandTotal)}</td></tr>
    </table>
    <div class="divider"></div>
    <div class="center">
        <img src="$upiQrCodeUrl" width="80" height="80" alt="UPI QR" />
        <div style="font-size:9px;">Scan UPI to Pay ₹${String.format(Locale.US, "%.2f", invoice.grandTotal)}</div>
    </div>
    <div class="divider"></div>
    <div class="center" style="font-size: 9px;">${company.termsConditions.lines().firstOrNull() ?: "Thank You! Visit Again."}</div>
</body>
</html>
        """.trimIndent()
    }

    private fun generatePortraitHtml(
        invoiceWithItems: InvoiceWithItems,
        company: CompanyProfile,
        config: InvoiceDesignerConfig,
        upiQrCodeUrl: String,
        einvoiceQrCodeUrl: String,
        slabMap: Map<Int, GstSlabSummary>,
        totalTaxableAll: Double,
        totalDiscountAll: Double,
        totalSgstAll: Double,
        totalCgstAll: Double,
        totalGstAll: Double
    ): String {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val itemsHtml = buildString {
            items.forEachIndexed { index, item ->
                append("""
                <tr>
                    <td style="text-align:center;">${index + 1}</td>
                    <td style="font-weight:bold;">${item.productName}</td>
                    <td style="text-align:center;">${item.pack}</td>
                    <td style="text-align:center;">${item.batch}</td>
                    <td style="text-align:center;">${item.exp}</td>
                    <td style="text-align:center;">${item.qty}</td>
                    <td style="text-align:right;">${String.format(Locale.US, "%.2f", item.mrp)}</td>
                    <td style="text-align:right;">${String.format(Locale.US, "%.2f", item.rate)}</td>
                    <td style="text-align:center;">${item.discountPercent}%</td>
                    <td style="text-align:right; font-weight:bold;">${String.format(Locale.US, "%.2f", item.amount)}</td>
                </tr>
                """.trimIndent())
            }
        }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Invoice ${invoice.invoiceNumber}</title>
    <style>
        @page { size: A4 portrait; margin: 10mm; }
        body { font-family: Arial, sans-serif; font-size: 11px; line-height: 1.3; }
        .header { border-bottom: 2px solid ${config.primaryColorHex}; padding-bottom: 8px; margin-bottom: 12px; }
        .title { font-size: 20px; font-weight: bold; color: ${config.primaryColorHex}; }
        table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        th { background: #f0f0f0; border: 1px solid #ccc; padding: 6px; font-size: 10px; }
        td { border: 1px solid #ddd; padding: 5px; font-size: 10px; }
    </style>
</head>
<body>
    <div class="header">
        <div style="display:flex; justify-content:space-between;">
            <div>
                <div class="title">${company.companyName}</div>
                <div>${company.addressLine1}, ${company.addressLine2}</div>
                <div>Phone: ${company.phone} | GSTIN: ${company.gstin} | DL: ${company.dlNo}</div>
            </div>
            <div style="text-align:right;">
                <h2 style="color:${config.primaryColorHex};">TAX INVOICE</h2>
                <div><b>Inv No:</b> ${invoice.invoiceNumber}</div>
                <div><b>Date:</b> ${invoice.invoiceDate}</div>
            </div>
        </div>
    </div>
    <div style="margin-bottom:10px; padding:6px; background:#fafafa; border:1px solid #eee;">
        <b>Billed To:</b> ${invoice.partyName}<br/>
        Address: ${invoice.partyAddress}<br/>
        Phone: ${invoice.partyPhone} | GSTIN: ${invoice.partyGstin} | DL: ${invoice.partyDlNo}
    </div>
    <table>
        <thead>
            <tr>
                <th>#</th><th>Item Name</th><th>Pack</th><th>Batch</th><th>Exp</th><th>Qty</th><th>MRP</th><th>Rate</th><th>Dis%</th><th>Amount</th>
            </tr>
        </thead>
        <tbody>
            $itemsHtml
        </tbody>
    </table>
    <div style="display:flex; justify-content:space-between; margin-top:15px;">
        <div style="width:60%;">
            <b>Amount in Words:</b> ${invoice.amountInWords}<br/><br/>
            <b>Bank Details:</b> ${company.bankName} | A/C: ${company.accountNo} | IFSC: ${company.ifscCode}<br/>
            <div style="margin-top:10px;">
                <img src="$upiQrCodeUrl" width="65" height="65" alt="UPI QR" />
                <span style="font-size:9px; vertical-align:top; margin-left:8px;">Scan to Pay UPI (₹${String.format(Locale.US, "%.2f", invoice.grandTotal)})</span>
            </div>
        </div>
        <div style="width:35%; text-align:right;">
            <div>Taxable: ₹${String.format(Locale.US, "%.2f", totalTaxableAll)}</div>
            <div>Discount: ₹${String.format(Locale.US, "%.2f", totalDiscountAll)}</div>
            <div>GST (CGST+SGST): ₹${String.format(Locale.US, "%.2f", totalGstAll)}</div>
            <hr/>
            <div style="font-size:14px; font-weight:bold; color:${config.primaryColorHex};">Grand Total: ₹${String.format(Locale.US, "%.2f", invoice.grandTotal)}</div>
            <div style="margin-top:40px;">For ${company.companyName}<br/><br/>Authorized Signatory</div>
        </div>
    </div>
</body>
</html>
        """.trimIndent()
    }
}
