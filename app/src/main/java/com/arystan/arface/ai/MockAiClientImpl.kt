package com.arystan.arface.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.arystan.arface.mask.MaskDescriptor
import com.arystan.arface.mask.MaskLayer
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MockAiClientImpl(private val context: Context) : MockAiClient {

    override suspend fun generateMask(prompt: String): MaskDescriptor {
        delay(1500)

        val uuid = UUID.randomUUID().toString()
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)

        val hue = (kotlin.math.abs(prompt.hashCode()) % 360).toFloat()
        val baseColor = Color.HSVToColor(floatArrayOf(hue, 0.6f, 0.95f))
        val compHue = (hue + 180f) % 360f
        val compColor = Color.HSVToColor(floatArrayOf(compHue, 0.5f, 0.9f))

        val canvas = Canvas(bitmap)

        // Fill with transparent
        canvas.drawColor(Color.TRANSPARENT)

        // Paint for ovals
        val ovalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // Bigger oval covering the face shape
        ovalPaint.color = Color.argb((0.45f * 255).toInt(), Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        canvas.drawOval(RectF(64f, 80f, 448f, 460f), ovalPaint)

        // Smaller oval for forehead
        ovalPaint.color = Color.argb((0.45f * 255).toInt(), Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        canvas.drawOval(RectF(128f, 60f, 384f, 200f), ovalPaint)

        // Complementary tinted layer as a soft gradient overlay
        val compPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb((0.25f * 255).toInt(), Color.red(compColor), Color.green(compColor), Color.blue(compColor))
        }
        canvas.drawOval(RectF(80f, 120f, 432f, 440f), compPaint)

        // Draw prompt text centered
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            textSize = 56f
            setShadowLayer(12f, 0f, 0f, Color.BLACK)
        }

        val lines = wrapText(prompt, maxCharsPerLine = 12, maxLines = 3)
        val lineHeight = textPaint.textSize * 1.2f
        val totalTextHeight = lines.size * lineHeight
        val startY = (512f / 2f) - (totalTextHeight / 2f) + textPaint.textSize

        lines.forEachIndexed { index, line ->
            canvas.drawText(line, 256f, startY + index * lineHeight, textPaint)
        }

        // Save bitmap to file
        val dir = File(context.filesDir, "generated")
        if (!dir.exists() && !dir.mkdirs()) {
            bitmap.recycle()
            throw RuntimeException("Failed to create directory: ${dir.absolutePath}")
        }

        val file = File(dir, "$uuid.png")
        try {
            FileOutputStream(file).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw RuntimeException("Failed to compress bitmap to PNG")
                }
            }
        } catch (e: Exception) {
            bitmap.recycle()
            throw RuntimeException("Failed to save generated mask: ${e.message}")
        } finally {
            bitmap.recycle()
        }

        val savedPath = file.absolutePath

        return MaskDescriptor(
            id = uuid,
            name = prompt.take(20).ifBlank { "AI" },
            thumbnail = "file:$savedPath",
            layers = listOf(
                MaskLayer(
                    texture = "file:$savedPath",
                    anchorLandmarks = listOf(10, 152, 234, 454),
                    widthScale = 1.0f,
                    rotationLandmarks = listOf(234, 454),
                )
            ),
        )
    }

    private fun wrapText(text: String, maxCharsPerLine: Int, maxLines: Int): List<String> {
        if (text.length <= maxCharsPerLine) return listOf(text)

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        val currentLine = StringBuilder()

        for (word in words) {
            if (lines.size >= maxLines - 1 && currentLine.isNotEmpty()) {
                // Last allowed line — append remaining with ellipsis if needed
                break
            }
            if (currentLine.isEmpty()) {
                currentLine.append(word)
            } else if (currentLine.length + 1 + word.length <= maxCharsPerLine) {
                currentLine.append(" ").append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine.clear()
                currentLine.append(word)
                if (lines.size >= maxLines - 1) break
            }
        }

        if (currentLine.isNotEmpty()) {
            val remaining = currentLine.toString()
            if (lines.size < maxLines) {
                // Check if there are more words that didn't fit
                val processed = lines.joinToString(" ") + " " + remaining
                val leftover = text.removePrefix(processed.trim())
                if (leftover.isNotBlank() && lines.size == maxLines - 1) {
                    val truncated = if (remaining.length > maxCharsPerLine - 1) {
                        remaining.take(maxCharsPerLine - 1) + "…"
                    } else {
                        remaining + "…"
                    }
                    lines.add(truncated)
                } else {
                    lines.add(remaining)
                }
            }
        }

        return lines.take(maxLines)
    }
}
