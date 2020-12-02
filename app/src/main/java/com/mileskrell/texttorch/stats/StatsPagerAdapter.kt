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

package com.mileskrell.texttorch.stats

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.pages.AverageLengthFragment
import com.mileskrell.texttorch.stats.pages.TotalTextsFragment
import com.mileskrell.texttorch.stats.pages.WhoTextsFirstFragment
import com.mileskrell.texttorch.util.LifecycleLoggingFragment

class StatsPagerAdapter(val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        const val TAG = "StatsPagerAdapter"
    }

    private val pages = mutableListOf(
        WhoTextsFirstFragment(),
        TotalTextsFragment(),
        AverageLengthFragment()
    )

    override fun getCount() = pages.size

    override fun getItem(position: Int) = pages[position]

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.who_texts_first)
            1 -> context.getString(R.string.total_texts)
            2 -> context.getString(R.string.average_length)
            else -> null
        }
    }

    /**
     * Fix stats fragment references after configuration changes.
     *
     * See https://stackoverflow.com/a/17629575
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position).also {
            pages[position] = it as LifecycleLoggingFragment
        }
    }

    /**
     * Sync latest scroll position. Called by [StatsFragment].
     */
    fun onPageChanged(oldPosition: Int) {
        // TODO Can we set the old RecyclerView's velocity to zero right here?
        //  If the user makes the RecyclerView scroll and then taps another tab while it's still scrolling,
        //  they go to a page with a still RecyclerView.
        //  But if they quickly return to the first page, the old RecyclerView is still scrolling, and now that one
        //  is considered to have the latest state.
        //  (This is a very low-priority bug.)

        val latestState = pages[oldPosition].view?.findViewById<RecyclerView>(R.id.recycler_view)
            ?.layoutManager?.onSaveInstanceState()

        pages.forEach {
            it.view?.findViewById<RecyclerView>(R.id.recycler_view)
                ?.layoutManager?.onRestoreInstanceState(latestState)
        }
    }
}
