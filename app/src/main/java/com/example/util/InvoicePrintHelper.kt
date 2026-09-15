package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
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

object InvoicePrintHelper {

    fun printInvoice(activity: Activity, htmlContent: String, jobName: String = "Pharma_Bill") {
        activity.runOnUiThread {
            val webView = WebView(activity)
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            webView.webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                    return true
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager != null) {
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                            .setResolution(PrintAttributes.Resolution("pharma_pdf", "Pharma Print", 300, 300))
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

    fun shareInvoiceHtml(context: Context, htmlContent: String, invoiceNo: String) {
        try {
            val cachePath = File(context.cacheDir, "invoices")
            cachePath.mkdirs()
            val file = File(cachePath, "Invoice_${invoiceNo.replace("/", "_")}.html")
            val stream = FileOutputStream(file)
            stream.write(htmlContent.toByteArray())
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice $invoiceNo")
                putExtra(Intent.EXTRA_TEXT, "Here is Tax Invoice $invoiceNo (A4 Landscape Pharma Bill)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice $invoiceNo"))
        } catch (e: Exception) {
            // Fallback to text sharing
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Invoice $invoiceNo")
                putExtra(Intent.EXTRA_TEXT, htmlContent)
            }
            context.startActivity(Intent.createChooser(textIntent, "Share Invoice $invoiceNo"))
        }
    }
}
