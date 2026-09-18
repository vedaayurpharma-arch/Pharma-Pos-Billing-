package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShippingLabelPrintHelper {

    /**
     * Sends the shipping label HTML to Android PrintManager for direct printing
     * on thermal sticker printers (4x6 / 100x150mm) or A4 sheet printers.
     */
    fun printShippingLabel(
        activity: Activity,
        htmlContent: String,
        labelNumber: String,
        labelSize: String = "4x6_INCH"
    ) {
        activity.runOnUiThread {
            val webView = WebView(activity)
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            webView.webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean = true
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                override fun onPageFinished(view: WebView, url: String) {
                    val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager != null) {
                        val jobName = "Shipping_Label_${labelNumber.replace("/", "_")}"
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)

                        val mediaSize = when (labelSize) {
                            "A4_SHEET" -> PrintAttributes.MediaSize.ISO_A4.asPortrait()
                            else -> PrintAttributes.MediaSize("LABEL_4X6", "4x6 Thermal Label", 4000, 6000)
                        }

                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(mediaSize)
                            .setResolution(PrintAttributes.Resolution("pharma_label_res", "Thermal Label", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()

                        printManager.print(jobName, printAdapter, printAttributes)
                    } else {
                        Toast.makeText(activity, "Print service not available on this device", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
        }
    }

    /**
     * Converts the HTML shipping label into a physical file and triggers a callback with the generated File.
     */
    fun exportShippingLabelToPdfFile(
        activity: Activity,
        htmlContent: String,
        labelNumber: String,
        labelSize: String = "4x6_INCH",
        onPdfReady: (File) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            val cachePath = File(activity.cacheDir, "shipping_labels")
            cachePath.mkdirs()
            val file = File(cachePath, "Shipping_Label_${labelNumber.replace("/", "_")}.html")
            val fos = FileOutputStream(file)
            fos.write(htmlContent.toByteArray(Charsets.UTF_8))
            fos.close()
            onPdfReady(file)
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Shares the shipping label as an HTML or PDF attachment to any app or WhatsApp
     */
    fun shareShippingLabel(
        context: Context,
        htmlContent: String,
        labelNumber: String,
        customerName: String
    ) {
        try {
            val cachePath = File(context.cacheDir, "shipping_labels")
            cachePath.mkdirs()
            val file = File(cachePath, "Label_${labelNumber.replace("/", "_")}.html")
            val stream = FileOutputStream(file)
            stream.write(htmlContent.toByteArray(Charsets.UTF_8))
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Shipping Label $labelNumber - $customerName")
                putExtra(Intent.EXTRA_TEXT, "Shipping Label $labelNumber for consignee $customerName (Veda Ayur Pharma Express Logistics)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Shipping Label $labelNumber"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share label file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Direct WhatsApp Sharing with optional customer phone number
     */
    fun shareToWhatsApp(
        activity: Activity,
        htmlContent: String,
        labelNumber: String,
        customerName: String,
        phoneNumber: String = ""
    ) {
        try {
            val cachePath = File(activity.cacheDir, "shipping_labels")
            cachePath.mkdirs()
            val file = File(cachePath, "Shipping_Label_${labelNumber.replace("/", "_")}.html")
            val stream = FileOutputStream(file)
            stream.write(htmlContent.toByteArray(Charsets.UTF_8))
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                file
            )

            val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "").let {
                if (it.length == 10) "91$it" else it
            }

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "Hello $customerName,\nHere is your official dispatch Shipping Label from VEDA AYUR PHARMA.\nShipping No: $labelNumber")
                if (cleanPhone.isNotEmpty()) {
                    putExtra("jid", "$cleanPhone@s.whatsapp.net")
                }
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                activity.startActivity(whatsappIntent)
            } catch (e: Exception) {
                // Try WhatsApp Business package
                whatsappIntent.setPackage("com.whatsapp.w4b")
                try {
                    activity.startActivity(whatsappIntent)
                } catch (e2: Exception) {
                    // Fallback to standard share chooser
                    shareShippingLabel(activity, htmlContent, labelNumber, customerName)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Error sharing via WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
