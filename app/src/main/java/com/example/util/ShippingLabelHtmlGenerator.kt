package com.example.util

import com.example.data.model.CompanyProfile
import com.example.data.model.ShippingLabel
import java.util.Locale

object ShippingLabelHtmlGenerator {

    /**
     * Generates a pristine, crisp HTML label ready for instant WebView rendering,
     * high-dpi thermal printing, and PDF export.
     */
    fun generateShippingLabelHtml(
        label: ShippingLabel,
        company: CompanyProfile
    ): String {
        val barcodeSvg = BarcodeUtil.generateCode128Svg(label.shippingNumber, heightPx = 55, barWidthPx = 1.6, includeText = true)
        val qrPayload = "VEDA-EXP|SHP:${label.shippingNumber}|INV:${label.invoiceNumber}|COD:${label.codAmount}|PIN:${label.pinCode}|MOB:${label.mobileNumber}"
        val qrSvg = BarcodeUtil.generateQrSvg(qrPayload, sizePx = 115)

        val isCod = label.isCod || label.codAmount > 0.0

        val (pageWidth, pageHeight, pageMargins) = when (label.labelSize) {
            "4x6_INCH" -> Triple("100mm", "150mm", "3mm")
            "100x150_MM" -> Triple("100mm", "150mm", "3mm")
            "A4_SHEET" -> Triple("210mm", "297mm", "8mm")
            else -> Triple("100mm", "150mm", "3mm")
        }

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Shipping Label - ${label.shippingNumber}</title>
    <style>
        @page {
            size: $pageWidth $pageHeight;
            margin: $pageMargins;
        }
        * {
            box-sizing: border-box;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
        }
        body {
            margin: 0;
            padding: 2px;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            font-size: 11px;
            color: #111;
            background: #fff;
            width: 100%;
        }
        .label-container {
            border: 2px solid #000;
            background: #fff;
            padding: 6px;
            max-width: 100%;
        }
        .header-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #000;
            padding-bottom: 6px;
            margin-bottom: 6px;
        }
        .brand-title {
            font-size: 16px;
            font-weight: 900;
            letter-spacing: 0.5px;
            color: #0D5C3A;
            line-height: 1.1;
        }
        .brand-sub {
            font-size: 8px;
            font-weight: bold;
            color: #444;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .priority-badge {
            background: #000;
            color: #fff;
            font-size: 10px;
            font-weight: 800;
            padding: 3px 8px;
            border-radius: 2px;
            letter-spacing: 1px;
            text-transform: uppercase;
        }
        .barcode-section {
            text-align: center;
            padding: 6px 0 4px 0;
            border-bottom: 2px solid #000;
            background: #fff;
        }
        .meta-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            border-bottom: 2px solid #000;
            font-size: 10px;
        }
        .meta-cell {
            padding: 4px 6px;
            border-right: 1px solid #000;
        }
        .meta-cell:last-child {
            border-right: none;
        }
        .meta-label {
            font-size: 8px;
            text-transform: uppercase;
            color: #555;
            font-weight: bold;
        }
        .meta-value {
            font-size: 11px;
            font-weight: bold;
            color: #000;
        }
        .payment-banner {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 6px 10px;
            border-bottom: 2px solid #000;
            background: ${if (isCod) "#fff3cd" else "#e8f5e9"};
        }
        .payment-status {
            font-size: 15px;
            font-weight: 900;
            letter-spacing: 1px;
            color: ${if (isCod) "#b71c1c" else "#1b5e20"};
        }
        .cod-amount {
            font-size: 18px;
            font-weight: 900;
            color: #b71c1c;
        }
        .shipping-split {
            display: flex;
            border-bottom: 2px solid #000;
        }
        .ship-to-box {
            flex: 1;
            padding: 6px 8px;
            border-right: 2px solid #000;
        }
        .qr-side-box {
            width: 125px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 6px;
            text-align: center;
            background: #fafafa;
        }
        .destination-title {
            font-size: 9px;
            font-weight: 900;
            text-transform: uppercase;
            color: #555;
            margin-bottom: 4px;
        }
        .customer-name {
            font-size: 14px;
            font-weight: 900;
            color: #000;
            line-height: 1.2;
            margin-bottom: 2px;
        }
        .clinic-name {
            font-size: 12px;
            font-weight: bold;
            color: #1a237e;
            margin-bottom: 4px;
        }
        .address-text {
            font-size: 11px;
            line-height: 1.35;
            color: #222;
            margin-bottom: 6px;
        }
        .pincode-highlight {
            display: inline-block;
            background: #000;
            color: #fff;
            font-size: 14px;
            font-weight: 900;
            padding: 2px 8px;
            border-radius: 2px;
            letter-spacing: 1px;
            margin: 4px 0;
        }
        .contact-line {
            font-size: 11px;
            font-weight: 900;
            margin-top: 4px;
        }
        .routing-details {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr;
            border-bottom: 2px solid #000;
            font-size: 9px;
            text-align: center;
            background: #fdfdfd;
        }
        .routing-cell {
            padding: 4px 2px;
            border-right: 1px solid #000;
        }
        .routing-cell:last-child {
            border-right: none;
        }
        .return-section {
            padding: 5px 6px;
            font-size: 8.5px;
            line-height: 1.25;
            background: #fff;
            border-bottom: 1px dashed #000;
        }
        .footer-note {
            padding: 4px 6px;
            font-size: 8px;
            font-weight: bold;
            text-align: center;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            background: #f1f1f1;
        }
    </style>
</head>
<body>
    <div class="label-container">
        <!-- 1. Header with Brand & Priority -->
        <div class="header-row">
            <div>
                <div class="brand-title">VEDA AYUR PHARMA</div>
                <div class="brand-sub">Ayurvedic Healthcare ERP Express Logistics</div>
            </div>
            <div style="text-align:right;">
                <span class="priority-badge">EXP-SURFACE</span>
                <div style="font-size:8px; font-weight:bold; margin-top:2px;">DL: ${company.dlNo.split(",").firstOrNull()?.trim() ?: "20B/21B"}</div>
            </div>
        </div>

        <!-- 2. High-Density Code 128 Tracking Barcode -->
        <div class="barcode-section">
            <div style="display:flex; justify-content:center;">
                $barcodeSvg
            </div>
        </div>

        <!-- 3. Key Order Metadata -->
        <div class="meta-grid">
            <div class="meta-cell">
                <div class="meta-label">Shipping No / AWB</div>
                <div class="meta-value">${label.shippingNumber}</div>
            </div>
            <div class="meta-cell">
                <div class="meta-label">Invoice / Order Ref</div>
                <div class="meta-value">${label.invoiceNumber.ifBlank { label.orderNumber.ifBlank { "N/A" } }}</div>
            </div>
            <div class="meta-cell">
                <div class="meta-label">Ship Date</div>
                <div class="meta-value">${label.shippingDate}</div>
            </div>
            <div class="meta-cell">
                <div class="meta-label">Courier Partner</div>
                <div class="meta-value">${label.courierPartner}</div>
            </div>
        </div>

        <!-- 4. COD / PREPAID High-Contrast Banner -->
        <div class="payment-banner">
            <div>
                <div style="font-size:8px; font-weight:bold; text-transform:uppercase;">Payment Terms</div>
                <div class="payment-status">${if (isCod) "CASH ON DELIVERY (COD)" else "PREPAID / BILLED"}</div>
            </div>
            <div style="text-align:right;">
                ${if (isCod) """<div style="font-size:8px; font-weight:bold; text-transform:uppercase; color:#b71c1c;">Collect From Consignee</div><div class="cod-amount">₹${String.format(Locale.US, "%.2f", label.codAmount)}</div>""" else """<div style="font-size:10px; font-weight:bold; color:#1b5e20;">NO CASH COLLECTION</div>"""}
            </div>
        </div>

        <!-- 5. Consignee Delivery Address & QR Code -->
        <div class="shipping-split">
            <div class="ship-to-box">
                <div class="destination-title">DELIVER TO / CONSIGNEE:</div>
                <div class="customer-name">${label.customerName}</div>
                ${if (label.clinicOrPharmacyName.isNotBlank() && label.clinicOrPharmacyName != label.customerName) """<div class="clinic-name">${label.clinicOrPharmacyName}</div>""" else ""}
                <div class="address-text">${label.address}</div>
                <div><b>City / District:</b> ${label.cityOrDistrict.ifBlank { "N/A" }}, <b>State:</b> ${label.state.ifBlank { "Andhra Pradesh" }}</div>
                <div>
                    <span class="pincode-highlight">PIN: ${label.pinCode.ifBlank { "500001" }}</span>
                </div>
                <div class="contact-line">TEL / MOB: ${label.mobileNumber}${if (label.alternatePhone.isNotBlank()) " / ${label.alternatePhone}" else ""}</div>
            </div>
            <div class="qr-side-box">
                <div style="margin-bottom:4px;">
                    $qrSvg
                </div>
                <div style="font-size:7.5px; font-weight:bold; text-transform:uppercase;">Scan for Express Dispatch</div>
                <div style="font-size:7px; color:#666; margin-top:2px;">GST: ${company.gstin}</div>
            </div>
        </div>

        <!-- 6. Logistics & Routing Specs -->
        <div class="routing-details">
            <div class="routing-cell">
                <div style="color:#666; font-size:7.5px; font-weight:bold;">WEIGHT</div>
                <div style="font-weight:bold; font-size:10px;">${String.format(Locale.US, "%.2f", label.packageWeightKg)} KG</div>
            </div>
            <div class="routing-cell">
                <div style="color:#666; font-size:7.5px; font-weight:bold;">PIECES / BOXES</div>
                <div style="font-weight:bold; font-size:10px;">${label.numberOfBoxes} BOX(ES)</div>
            </div>
            <div class="routing-cell">
                <div style="color:#666; font-size:7.5px; font-weight:bold;">DIMENSIONS</div>
                <div style="font-weight:bold; font-size:9.5px;">${label.dimensionsCm}</div>
            </div>
        </div>

        <!-- 7. Return to Shipper Address -->
        <div class="return-section">
            <b>IF UNDELIVERED, RETURN TO:</b><br/>
            ${label.returnAddress.ifBlank { "${company.companyName}, ${company.addressLine1}, ${company.addressLine2}, Phone: ${company.phone}" }}
        </div>

        <!-- 8. Package Handling Instructions -->
        <div class="footer-note">
            ⚠️ ${label.handlingInstructions}
        </div>
    </div>
</body>
</html>
        """.trimIndent()
    }
}
