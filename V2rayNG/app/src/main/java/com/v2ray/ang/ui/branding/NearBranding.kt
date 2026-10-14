package com.v2ray.ang.ui.branding

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.handler.MmkvManager

/**
 * Near Cloud branding helpers: the user can type a custom app name in
 * Settings (Appearance). It replaces the app name shown INSIDE the app
 * (top bar title, drawer header); the launcher icon label stays the
 * manifest app_name because Android cannot change it at runtime.
 */
object NearBranding {

    fun displayName(context: Context): String {
        val custom = MmkvManager.decodeSettingsString(
            AppConfig.PREF_CUSTOM_APP_NAME, ""
        )?.trim().orEmpty()
        return custom.ifEmpty { context.getString(R.string.app_name) }
    }
}

/** Display name that refreshes whenever the screen resumes. */
@Composable
fun rememberDisplayAppName(): String {
    val context = LocalContext.current
    var name by remember { mutableStateOf(NearBranding.displayName(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                name = NearBranding.displayName(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return name
}
