package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    /**
     * Creates an 800x800 white placeholder image with initial character or default silhouette.
     */
    fun createDefaultPlaceholder(context: Context, name: String = "***"): File {
        val size = 800
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill with clean white background
        canvas.drawColor(Color.WHITE)

        // Draw soft grey border inside
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 12f
        }
        canvas.drawRect(6f, 6f, size - 6f, size - 6f, borderPaint)

        // Draw initials or icon text
        val textPaint = Paint().apply {
            color = Color.parseColor("#4A6572")
            textSize = 280f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val displayChar = if (name.isNotBlank() && name != "***") {
            name.trim().take(1).uppercase()
        } else {
            "👤"
        }

        val xPos = size / 2f
        val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(displayChar, xPos, yPos, textPaint)

        val avatarDir = File(context.filesDir, "avatars")
        if (!avatarDir.exists()) avatarDir.mkdirs()

        val placeholderFile = File(avatarDir, "placeholder_default.jpg")
        FileOutputStream(placeholderFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return placeholderFile
    }

    /**
     * Saves a cropped bitmap into 800x800 JPEG and compresses under 200KB into APP private storage.
     */
    fun saveCroppedAvatar(context: Context, croppedBitmap: Bitmap): String? {
        return try {
            val targetSize = 800
            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true)

            val avatarDir = File(context.filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()

            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val file = File(avatarDir, fileName)

            var quality = 90
            var stream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            // Compress until size is <= 200KB (200 * 1024 bytes) or quality is 40
            while (stream.toByteArray().size > 200 * 1024 && quality > 40) {
                stream.reset()
                quality -= 10
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            FileOutputStream(file).use { out ->
                out.write(stream.toByteArray())
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a local photo file if present.
     */
    fun deleteAvatarFile(filePath: String?) {
        if (!filePath.isNull_Blank()) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun String?.isNull_Blank(): Boolean = this == null || this.trim().isEmpty()

    /**
     * Loads a Bitmap safely from Uri.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
