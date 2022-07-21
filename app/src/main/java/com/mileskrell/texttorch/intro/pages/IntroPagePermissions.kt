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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.FragmentIntroPagePermissionsBinding
import com.mileskrell.texttorch.intro.IntroFragment
import com.mileskrell.texttorch.util.*
import io.sentry.SentryLevel

class IntroPagePermissions : Fragment(R.layout.fragment_intro_page_permissions) {

    private var _binding: FragmentIntroPagePermissionsBinding? = null
    private val b get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIntroPagePermissionsBinding.bind(view)
        b.introPage2TextView3.run {
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(
                ContextCompat.getColor(requireContext(), R.color.light_blue_link_color)
            )
            @Suppress("DEPRECATION")
            text = Html.fromHtml(
                getString(R.string.intro_you_can_see_the_code, getString(R.string.github_url))
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

        b.introPermissionsButton.setOnClickListener {
            logToBoth(TAG, "Clicked \"grant needed permissions\" button")
            requestPermissionsLaucher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS
                )
            )
        }

        // I think storing permission statuses in a ViewModel would just introduce more complexity,
        // so after configuration changes, we need to check them again.
        if (readSmsGranted() && readContactsGranted()) {
            // TODO: Move this check to onStart() or onResume(). Then we can detect if
            //  the user is returning after granting the permissions in the app's settings.
            // I haven't done this yet because it results in a "FragmentManager is already
            // executing transactions" error. Related: see BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
            // at the moment, fragments in the ViewPager are capped at STARTED.
            onPermissionsGranted()
        }
    }

    private fun onPermissionsGranted() {
        b.introPermissionsButton.visibility = View.INVISIBLE
        b.introTextViewPermissionsGranted.visibility = View.VISIBLE
        (parentFragment as IntroFragment).ensureAnalyticsPageAdded()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "IntroPagePermissions"
    }
}
