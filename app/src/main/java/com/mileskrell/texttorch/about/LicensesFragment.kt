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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commitNow
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.LifecycleLoggingFragment

class LicensesFragment : LifecycleLoggingFragment(R.layout.fragment_licenses) {

    companion object {
        const val TAG = "LicensesFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.commitNow {
            // TODO: Include copyright year manually where needed
            //  see https://github.com/mikepenz/AboutLibraries/issues/469
            replace(
                R.id.about_licenses_container,
                LibsBuilder().run {
                    aboutShowIcon = false
                    showLicense = true
                    supportFragment()
                }
            )
        }
    }
}
