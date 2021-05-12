package com.anik.digitalsignature

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.os.Environment
import androidx.core.content.ContextCompat
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class FileUtil {



    companion object {

        @JvmStatic
        fun dateToString(date: Date, format: String): String {
            var dateStr = ""
            val formatter = SimpleDateFormat(format, Locale.US)
            try {
                dateStr = formatter.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dateStr
        }

        fun copyAsset(context: Context, assetManager: AssetManager) {
            val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                inputStream = assetManager.open("sample.pdf")
                val outFile = File(path, Constants.ORIGINAL_PDF_NAME)
                outputStream = FileOutputStream(outFile)
                copyFile(inputStream, outputStream)
            } catch (e: java.lang.Exception) {
                val message = e.message
            }
        }

        private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
        }

        private fun generateDate(width: Float, context: Context): String {

            val bitmap = Bitmap.createBitmap(width.toInt(), 25, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            val paint = Paint()
            paint.color = Color.BLACK
            paint.isAntiAlias = true
            paint.textSize = 10f

            val r = Rect()
            canvas.getClipBounds(r)
            val cHeight: Int = r.height()
            val cWidth: Int = r.width()
            paint.textAlign = Paint.Align.LEFT

            var formattedDate = dateToString(Date(), Constants.SIGNATURE_DATE_FORMAT)

            paint.getTextBounds(formattedDate, 0, formattedDate.length, r)
            val x: Float = cWidth / 2f - r.width() / 2f - r.left
            val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
            canvas.drawText(formattedDate, x, y, paint)
            canvas.save()
            canvas.restore()

            val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + Constants.GENERATED_SIGNATURE_IMAGE_NAME
            val fos = FileOutputStream(File(path))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            return path
        }

        fun saveSignature(signature: Bitmap, context: Context): String? {
            try {
                val path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
                val dir = File(path)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                var output = File(path, Constants.SAVED_SIGNATURE_IMAGE_NAME)
                saveBitmapToPNG(signature, output, context)
                return output.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        @Throws(IOException::class)
        private fun saveBitmapToPNG(bitmap: Bitmap, photo: File, context: Context) {
            val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newBitmap)
            canvas.drawColor(ContextCompat.getColor(context, R.color.black))
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            val stream: OutputStream = FileOutputStream(photo)
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        }

        fun generateSignedPdf(signatureImagePath: String, context: Context): String? {
            var srcPdf = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + Constants.ORIGINAL_PDF_NAME
            var destPdf = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + Constants.GENERATED_PDF_NAME
            try{
                val reader = PdfReader(srcPdf)
                val stamper = PdfStamper(reader, FileOutputStream(destPdf))
                val content = stamper.getOverContent(1)
                val image = Image.getInstance(signatureImagePath)
                image.scalePercent(15f, 2f)
                image.setAbsolutePosition(262f, 115f)
                image.alignment = Image.RIGHT
                content.addImage(image)
                val path = generateDate(image.scaledWidth, context)
                val dateImage = Image.getInstance(path)
                dateImage.setAbsolutePosition(262f, image.absoluteY - dateImage.height)
                content.addImage(dateImage)
                stamper.close()
            } catch (e: Exception){
                var message = e.message
            }
            return destPdf
        }

    }

}