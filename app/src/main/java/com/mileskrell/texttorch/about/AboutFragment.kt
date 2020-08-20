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

package com.mileskrell.texttorch.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.BuildConfig
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.LifecycleLogggingFragment
import com.mileskrell.texttorch.util.logToBoth
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : LifecycleLogggingFragment(R.layout.fragment_about) {

    companion object {
        const val TAG = "AboutFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        about_version.text = getString(
            R.string.version_x,
            BuildConfig.VERSION_NAME + if (BuildConfig.DEBUG) "-debug" else ""
        )

        about_github_button.setOnClickListener {
            logToBoth(TAG, "Clicked \"view code\" button")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))))
        }

        about_licenses_button.setOnClickListener {
            logToBoth(TAG, "Clicked \"view licenses\" button")
            findNavController().navigate(R.id.about_to_licenses_action)
        }

        about_donate_button.setOnClickListener {
            logToBoth(TAG, "Clicked \"donate\" button")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_url))))
        }
    }
}
