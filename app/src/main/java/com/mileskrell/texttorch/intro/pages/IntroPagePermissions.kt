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

package com.mileskrell.texttorch.intro.pages

import android.Manifest
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.FragmentIntroPagePermissionsBinding
import com.mileskrell.texttorch.intro.IntroFragment
import com.mileskrell.texttorch.intro.IntroViewModel
import com.mileskrell.texttorch.util.*
import io.sentry.SentryLevel

class IntroPagePermissions : Fragment(R.layout.fragment_intro_page_permissions) {

    private var _binding: FragmentIntroPagePermissionsBinding? = null
    private val b get() = _binding!!

    private val introViewModel: IntroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIntroPagePermissionsBinding.bind(view)
        b.introPage2TextView3.run {
            movementMethod = LinkMovementMethod.getInstance()
            text = Html.fromHtml(
                getString(R.string.intro_you_can_see_the_code, getString(R.string.github_url)),
                Html.FROM_HTML_MODE_LEGACY
            )
        }

        val requestPermissionsLaucher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.values.all { it }) {
                logToBoth(TAG, "User granted all permissions")
                onPermissionsGranted()
            } else {
                // Not all permissions were granted
                logEvent(
                    TAG,
                    "User only granted some permissions",
                    SentryLevel.INFO,
                    true,
                    mapOf(
                        "READ_SMS" to readSmsGranted(),
                        "READ_CONTACTS" to readContactsGranted()
                    )
                )
                val canAskAgain = (readSmsGranted()
                        || shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
                        && (readContactsGranted()
                        || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                if (!canAskAgain) {
                    // User checked "Never ask again", so open app settings page
                    logToBoth(TAG, "Showed app settings dialog")
                    showAppSettingsDialog(TAG)
                }
            }
        }

        // TODO: Also adding the analytics page here on app startup would be nice, but
        //  it results in a "FragmentManager is already executing transactions" error.
        if (readSmsGranted() && readContactsGranted() && introViewModel.analyticsPageAdded) {
            indicatePermissionsGranted()
        }

        b.introPermissionsButton.setOnClickListener {
            logToBoth(TAG, "Clicked \"grant needed permissions\" button")
            requestPermissionsLaucher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS
                )
            )
        }
    }

    private fun onPermissionsGranted() {
        indicatePermissionsGranted()
        (parentFragment as IntroFragment).ensureAnalyticsPageAdded()
    }

    private fun indicatePermissionsGranted() {
        b.introPermissionsButton.visibility = View.INVISIBLE
        b.introTextViewPermissionsGranted.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "IntroPagePermissions"
    }
}
