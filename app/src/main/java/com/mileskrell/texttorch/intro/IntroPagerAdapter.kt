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

package com.mileskrell.texttorch.intro

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mileskrell.texttorch.intro.pages.IntroPageAnalytics
import com.mileskrell.texttorch.intro.pages.IntroPageEnterApp
import com.mileskrell.texttorch.intro.pages.IntroPagePermissions
import com.mileskrell.texttorch.intro.pages.IntroPageWelcome

class IntroPagerAdapter(private val introViewModel: IntroViewModel, fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    fun ensureAnalyticsPageAdded() {
        if (!introViewModel.analyticsPageAdded) {
            introViewModel.analyticsPageAdded = true
            notifyItemInserted(itemCount)
        }
    }

    fun ensureEnterAppPageAdded() {
        if (!introViewModel.enterAppPageAdded) {
            introViewModel.enterAppPageAdded = true
            notifyItemInserted(itemCount)
        }
    }

    override fun getItemCount(): Int {
        return 1 +
                (if (introViewModel.permissionsPageAdded) 1 else 0) +
                (if (introViewModel.analyticsPageAdded) 1 else 0) +
                (if (introViewModel.enterAppPageAdded) 1 else 0)
    }

    override fun createFragment(position: Int) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) when (position) {
            0 -> IntroPageWelcome()
            1 -> IntroPageAnalytics()
            2 -> IntroPageEnterApp()
            else -> throw RuntimeException("invalid position $position")
        } else when (position) {
            0 -> IntroPageWelcome()
            1 -> IntroPagePermissions()
            2 -> IntroPageAnalytics()
            3 -> IntroPageEnterApp()
            else -> throw RuntimeException("invalid position $position")
        }
}
