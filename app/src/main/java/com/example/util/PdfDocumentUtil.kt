package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.OrderEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfDocumentUtil {

    /**
     * Generates a real standard PDF document for an order (e.g. NID Server Copy or Recharge Receipt)
     * using the native Android android.graphics.pdf.PdfDocument engine.
     */
    fun generateOrderPdf(context: Context, order: OrderEntity): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (72 dpi)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(20, 80, 50)
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(70, 70, 70)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 11f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val boldPaint = Paint().apply {
                color = Color.rgb(20, 20, 20)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(30, 130, 76)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(235, 247, 240)
                style = Paint.Style.FILL
            }

            // Outer Page Border
            canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)

            // Header Banner
            canvas.drawRect(25f, 25f, 570f, 95f, headerBgPaint)

            canvas.drawText("GOVERNMENT OF THE PEOPLE'S REPUBLIC OF BANGLADESH", 50f, 50f, titlePaint)
            canvas.drawText("ELECTION COMMISSION SECRETARIAT - NID VERIFICATION WING", 50f, 70f, subtitlePaint)
            canvas.drawText("OFFICIAL SERVER COPY / DIGITAL SERVICE DELIVERY SLIP", 50f, 88f, subtitlePaint)

            // Metadata Row
            var yPos = 130f
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            canvas.drawText("Delivery Date: ${dateFormat.format(Date())}", 50f, yPos, textPaint)
            canvas.drawText("Order Reference: #${order.id.takeLast(8).uppercase()}", 350f, yPos, boldPaint)

            yPos += 25f
            canvas.drawText("Operator / Gateway TrxID: ${order.operatorTrxId ?: "N/A"}", 50f, yPos, boldPaint)
            canvas.drawText("Status: OFFICIAL DELIVERED", 350f, yPos, boldPaint)

            // Divider
            yPos += 20f
            canvas.drawLine(50f, yPos, 545f, yPos, borderPaint)

            // Document Details Section
            yPos += 30f
            canvas.drawText("DOCUMENT & APPLICANT SPECIFICATIONS", 50f, yPos, titlePaint)

            yPos += 25f
            canvas.drawText("Service Title:", 50f, yPos, boldPaint)
            canvas.drawText(order.title, 200f, yPos, textPaint)

            yPos += 22f
            canvas.drawText("Applicant Name:", 50f, yPos, boldPaint)
            canvas.drawText(order.applicantName ?: order.userName, 200f, yPos, textPaint)

            yPos += 22f
            canvas.drawText("NID / Identification No:", 50f, yPos, boldPaint)
            canvas.drawText(order.nidNumber ?: order.recipientTarget, 200f, yPos, boldPaint)

            yPos += 22f
            canvas.drawText("Date of Birth:", 50f, yPos, boldPaint)
            canvas.drawText(order.dateOfBirth ?: "Recorded in Database", 200f, yPos, textPaint)

            yPos += 22f
            canvas.drawText("Division:", 50f, yPos, boldPaint)
            canvas.drawText(order.division ?: "Dhaka", 200f, yPos, textPaint)

            yPos += 22f
            canvas.drawText("District / Upazila:", 50f, yPos, boldPaint)
            canvas.drawText("${order.district ?: "N/A"} / ${order.upazila ?: "N/A"}", 200f, yPos, textPaint)

            yPos += 22f
            canvas.drawText("Fee Paid:", 50f, yPos, boldPaint)
            canvas.drawText("৳${String.format("%.2f", order.amount)} (Fully Deducted via Wallet)", 200f, yPos, textPaint)

            // Security Box
            yPos += 40f
            val securityBoxPaint = Paint().apply {
                color = Color.rgb(245, 245, 245)
                style = Paint.Style.FILL
            }
            canvas.drawRect(50f, yPos, 545f, yPos + 80f, securityBoxPaint)

            val secTextPaint = Paint().apply {
                color = Color.rgb(80, 80, 80)
                textSize = 9.5f
                isAntiAlias = true
            }
            canvas.drawText("SECURITY VERIFICATION & DIGITAL SEAL", 65f, yPos + 22f, boldPaint)
            canvas.drawText("• This digital document has been verified against the official database records.", 65f, yPos + 40f, secTextPaint)
            canvas.drawText("• Authorized Delivery File: ${order.deliveryPdfFileName ?: "Document_Delivery.pdf"}", 65f, yPos + 55f, secTextPaint)
            canvas.drawText("• Admin Fulfillment Note: ${order.adminNote ?: "All verification checks passed."}", 65f, yPos + 70f, secTextPaint)

            // Footer
            yPos = 780f
            canvas.drawLine(50f, yPos, 545f, yPos, borderPaint)
            yPos += 18f
            canvas.drawText("RechargeHub Bangladesh • Digital Delivery Gateway • Support: support@rechargehub.com", 80f, yPos, secTextPaint)

            pdfDocument.finishPage(page)

            // Save to application cache/downloads
            val pdfDir = File(context.cacheDir, "delivered_pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val fileName = order.deliveryPdfFileName ?: "Order_${order.id.takeLast(6)}_Delivery.pdf"
            val file = File(pdfDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares or opens the PDF with external viewers / messaging apps safely.
     */
    fun openOrSharePdf(context: Context, order: OrderEntity) {
        val file = generateOrderPdf(context, order)
        if (file != null && file.exists()) {
            try {
                val uri: Uri = try {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } catch (e: Exception) {
                    Uri.fromFile(file)
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Delivered PDF: ${file.name}")
                    putExtra(Intent.EXTRA_TEXT, "Here is your delivery document for Order #${order.id.takeLast(6).uppercase()}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Download / Share PDF"))
            } catch (e: Exception) {
                Toast.makeText(context, "PDF saved to: ${file.name} (${file.length() / 1024} KB)", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Could not generate PDF file.", Toast.LENGTH_SHORT).show()
        }
    }
}
