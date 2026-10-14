package com.v2ray.ang.ui.branding

import android.content.Context
import android.util.Base64
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.v2ray.ang.AppConfig
import com.v2ray.ang.util.LogUtil
import java.io.File

/**
 * Bundled Near Cloud typeface (the base64 payloads in the Font*.kt files of
 * this package): a variable-weight Quicksand TTF that is decoded once,
 * written to app-private storage, and then cached as a [FontFamily] for the
 * lifetime of the process. Returns null when decoding fails so callers can
 * fall back to the platform default typeface.
 */
object NearFont {

    private const val QUICKSAND_FILE = "quicksand.ttf"

    private var quicksandFamily: FontFamily? = null

    fun quicksand(context: Context): FontFamily? {
        quicksandFamily?.let { return it }
        val family = try {
            val bytes = Base64.decode(
                FONT_QUICKSAND_B64_PART1 + FONT_QUICKSAND_B64_PART2 + FONT_QUICKSAND_B64_PART3,
                Base64.DEFAULT
            )
            val file = File(context.filesDir, QUICKSAND_FILE)
            if (!file.exists() || file.length() != bytes.size.toLong()) {
                file.writeBytes(bytes)
            }
            FontFamily(
                Font(file, FontWeight.Normal),
                Font(file, FontWeight.Medium),
                Font(file, FontWeight.SemiBold),
                Font(file, FontWeight.Bold)
            )
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "NearFont: failed to load bundled Quicksand font", e)
            null
        }
        quicksandFamily = family
        return family
    }
}
