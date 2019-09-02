package com.mileskrell.texttorch.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R

/**
 * Permissions-related functions and resources used in multiple places
 */

const val PERMISSIONS_REQUEST_CODE = 1

fun Fragment.readSmsGranted() =
    ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

fun Fragment.readContactsGranted() =
    ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

fun Fragment.showAppSettingsDialog() {
    AlertDialog.Builder(context!!).apply {
        setMessage(R.string.app_settings_explanation)
        setPositiveButton(R.string.open_app_settings) { _, _ ->
            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            startActivity(appSettingsIntent)
        }
        show()
    }
}
