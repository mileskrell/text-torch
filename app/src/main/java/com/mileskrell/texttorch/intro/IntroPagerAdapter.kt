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
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mileskrell.texttorch.intro.IntroViewModel.PAGE.ENTER_APP
import com.mileskrell.texttorch.intro.pages.IntroPageEnterApp
import com.mileskrell.texttorch.intro.pages.IntroPagePermissions
import com.mileskrell.texttorch.intro.pages.IntroPageWelcome
import com.mileskrell.texttorch.util.LifecycleLogggingFragment

class IntroPagerAdapter(val introViewModel: IntroViewModel, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val pages = mutableListOf<LifecycleLogggingFragment>(
        IntroPageWelcome(),
    ).apply {
        // On Lollipop, we only show the first page (with an "enter app" button)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            add(IntroPagePermissions())
        }
        if (introViewModel.lastPageVisible.ordinal >= ENTER_APP.ordinal) {
            add(IntroPageEnterApp())
        }
    }

    fun addEnterAppPage() {
        if (introViewModel.lastPageVisible.ordinal < ENTER_APP.ordinal) {
            introViewModel.lastPageVisible = ENTER_APP
            pages.add(IntroPageEnterApp())
            notifyDataSetChanged()
        }
    }

    override fun getItem(position: Int) = pages[position]

    override fun getCount() = pages.size

    /**
     * Fix intro fragment references after configuration changes.
     *
     * See https://stackoverflow.com/a/17629575
     */
    // TODO: Hold on, I'm actually not sure if I need this. Figure out what's going on here.
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position).also {
            pages[position] = it as LifecycleLogggingFragment
        }
    }
}
