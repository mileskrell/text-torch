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
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.FragmentRegainPermissionsBinding
import com.mileskrell.texttorch.intro.pages.IntroPagePermissions
import com.mileskrell.texttorch.util.*
import io.sentry.SentryLevel

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
class RegainPermissionsFragment : LifecycleLoggingFragment(R.layout.fragment_regain_permissions) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = FragmentRegainPermissionsBinding.bind(view)
        b.regrantExplanationTextView.run {
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(
                ContextCompat.getColor(requireContext(), R.color.light_blue_link_color)
            )
            @Suppress("DEPRECATION")
            text = Html.fromHtml(
                getString(R.string.regrant_explanation, getString(R.string.github_url))
            )
        }

        b.regainButton.setOnClickListener {
            requestPermissions(
                arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    logToBoth(IntroPagePermissions.TAG, "User regranted all permissions")
                    findNavController().navigate(R.id.regain_to_analyze_action)
                } else {
                    // Not all permissions were granted
                    logEvent(
                        TAG,
                        "User only regranted some permissions",
                        SentryLevel.INFO,
                        true,
                        mapOf(
                            "READ_SMS" to readSmsGranted(),
                            "READ_CONTACTS" to readContactsGranted()
                        )
                    )
                    val canAskAgain = (readSmsGranted() || shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
                            && (readContactsGranted() || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                    if (!canAskAgain) {
                        // User checked "Never ask again", so open app settings page
                        logToBoth(TAG, "Showed app settings dialog")
                        showAppSettingsDialog(TAG)
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
        if (readSmsGranted() && readContactsGranted()) {
            // The user finally granted the permissions! Continue to AnalyzeFragment.
            findNavController().navigate(R.id.regain_to_analyze_action)
        }
    }

    companion object {
        const val TAG = "RegainPermissionsFragment"
    }
}
