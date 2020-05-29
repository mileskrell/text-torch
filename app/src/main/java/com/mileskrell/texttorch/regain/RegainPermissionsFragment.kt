/*
 * Copyright (C) 2020 Miles Krell and the Text Torch contributors
 *
 * This file is part of Text Torch.
 *
 * Text Torch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Text Torch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Text Torch.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mileskrell.texttorch.regain

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.PERMISSIONS_REQUEST_CODE
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import com.mileskrell.texttorch.util.showAppSettingsDialog
import kotlinx.android.synthetic.main.fragment_regain_permissions.*
import ly.count.android.sdk.Countly

/**
 * This page is opened if the user has completed the tutorial, but we don't have all the permissions
 * we need. Since the user has to grant permissions to complete the tutorial, this indicates that at
 * some point, the user went and manually denied these permissions.
 *
 * This won't happen very often, but it's still important that we handle it properly.
 *
 * This page can be opened either on app start (by IntroFragment) or in AnalyzeFragment's
 * onCreateView.
 */
class RegainPermissionsFragment : Fragment(R.layout.fragment_regain_permissions) {

    companion object {
        const val TAG = "RegainPermissionsFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        regain_button.setOnClickListener {
            requestPermissions(
                arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED } ) {
                    findNavController().navigate(R.id.regain_to_analyze_action)
                } else {
                    // Not all permissions were granted
                    val canAskAgain = (readSmsGranted() || shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
                            && (readContactsGranted() || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                    if (!canAskAgain) {
                        // User checked "Never ask again", so open app settings page
                        showAppSettingsDialog()
                    }
                }
            }
        }
    }

    /**
     * Called when the user returns from the app settings page. This makes it so the user doesn't
     * need to tap the "regrant permissions" button when they return.
     */
    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
        if (readSmsGranted() && readContactsGranted()) {
            // The user finally granted the permissions! Continue to AnalyzeFragment.
            findNavController().navigate(R.id.regain_to_analyze_action)
        }
    }
}
