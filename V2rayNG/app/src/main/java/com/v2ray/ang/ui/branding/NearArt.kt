package com.v2ray.ang.ui.branding

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.v2ray.ang.AppConfig
import com.v2ray.ang.util.LogUtil
import java.io.File

/**
 * Bundled Near Cloud artwork (the base64 payloads in the Art*.kt files of this
 * package) plus the user-picked custom wallpaper file. Every image is decoded
 * once and then cached in memory for the lifetime of the process.
 */
object NearArt {

    private const val CUSTOM_WALLPAPER_FILE = "custom_wallpaper.jpg"
    private const val MAX_WALLPAPER_EDGE = 1920

    private var emiliaOpen: ImageBitmap? = null
    private var emiliaBlink: ImageBitmap? = null
    private var emiliaSmile: ImageBitmap? = null
    private var bgNight: ImageBitmap? = null

    private var customWallpaper: ImageBitmap? = null
    private var customWallpaperStamp: Long = Long.MIN_VALUE

    fun wallpaperFile(context: Context): File = File(context.filesDir, CUSTOM_WALLPAPER_FILE)

    fun emiliaOpen(): ImageBitmap? =
        emiliaOpen ?: decodeBase64(EMILIA_OPEN_B64_PART1 + EMILIA_OPEN_B64_PART2)?.also { emiliaOpen = it }

    fun emiliaBlink(): ImageBitmap? =
        emiliaBlink ?: decodeBase64(EMILIA_BLINK_B64_PART1 + EMILIA_BLINK_B64_PART2)?.also { emiliaBlink = it }

    fun emiliaSmile(): ImageBitmap? =
        emiliaSmile ?: decodeBase64(EMILIA_SMILE_B64_PART1 + EMILIA_SMILE_B64_PART2)?.also { emiliaSmile = it }

    fun bgNight(): ImageBitmap? =
        bgNight ?: decodeBase64(BG_NIGHT_B64_PART1 + BG_NIGHT_B64_PART2)?.also { bgNight = it }

    /**
     * The decoded custom wallpaper, downsampled so its long edge is at most
     * [MAX_WALLPAPER_EDGE] px. Returns null when no custom file exists or the
     * file cannot be decoded. The cache is keyed on the file stamp, so picking
     * a new photo replaces the cached image without a process restart.
     */
    fun customWallpaper(context: Context): ImageBitmap? {
        val file = wallpaperFile(context)
        if (!file.exists()) {
            customWallpaper = null
            customWallpaperStamp = Long.MIN_VALUE
            return null
        }
        val stamp = file.lastModified() xor (file.length() shl 1)
        customWallpaper?.let { if (stamp == customWallpaperStamp) return it }
        val decoded = decodeFileDownsampled(file)
        customWallpaper = decoded
        customWallpaperStamp = if (decoded != null) stamp else Long.MIN_VALUE
        return decoded
    }

    private fun decodeBase64(data: String): ImageBitmap? {
        return try {
            val bytes = Base64.decode(data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "NearArt: failed to decode bundled artwork", e)
            null
        }
    }

    private fun decodeFileDownsampled(file: File): ImageBitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            var sampleSize = 1
            val longestEdge = maxOf(bounds.outWidth, bounds.outHeight)
            while (longestEdge / (sampleSize * 2) >= MAX_WALLPAPER_EDGE) {
                sampleSize *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "NearArt: failed to decode custom wallpaper", e)
            null
        }
    }
}
