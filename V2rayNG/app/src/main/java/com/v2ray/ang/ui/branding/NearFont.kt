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
 * this package): static Baloo 2 files (Medium 500 and Bold 700) that are
 * decoded once, written to app-private storage, and then cached as a
 * [FontFamily] for the lifetime of the process. Returns null when decoding
 * fails so callers can fall back to the platform default typeface.
 */
object NearFont {

    private const val BALOO_500_FILE = "baloo-500.ttf"
    private const val BALOO_700_FILE = "baloo-700.ttf"

    private var balooFamily: FontFamily? = null

    fun baloo(context: Context): FontFamily? {
        balooFamily?.let { return it }
        val family = try {
            val mediumFile = decodeToFile(
                context,
                BALOO_500_B64_PART1 + BALOO_500_B64_PART2,
                BALOO_500_FILE
            )
            val boldFile = decodeToFile(
                context,
                BALOO_700_B64_PART1 + BALOO_700_B64_PART2,
                BALOO_700_FILE
            )
            FontFamily(
                Font(mediumFile, FontWeight.Normal),
                Font(mediumFile, FontWeight.Medium),
                Font(boldFile, FontWeight.SemiBold),
                Font(boldFile, FontWeight.Bold)
            )
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "NearFont: failed to load bundled Baloo 2 font", e)
            null
        }
        balooFamily = family
        return family
    }

    private fun decodeToFile(context: Context, base64: String, fileName: String): File {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val file = File(context.filesDir, fileName)
        if (!file.exists() || file.length() != bytes.size.toLong()) {
            file.writeBytes(bytes)
        }
        return file
    }
}
