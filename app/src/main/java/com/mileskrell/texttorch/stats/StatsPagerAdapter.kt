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

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.pages.AverageLengthFragment
import com.mileskrell.texttorch.stats.pages.TotalTextsFragment
import com.mileskrell.texttorch.stats.pages.WhoTextsFirstFragment

class StatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val pages = arrayOfNulls<Fragment>(3)

    override fun getItemCount() = 3

    override fun createFragment(position: Int) = when (position) {
        0 -> WhoTextsFirstFragment()
        1 -> TotalTextsFragment()
        2 -> AverageLengthFragment()
        else -> throw RuntimeException("invalid position $position")
    }.also { pages[position] = it }

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

        val latestState = pages[oldPosition]?.view?.findViewById<RecyclerView>(R.id.recycler_view)
            ?.layoutManager?.onSaveInstanceState()

        pages.forEach {
            it?.view?.findViewById<RecyclerView>(R.id.recycler_view)
                ?.layoutManager?.onRestoreInstanceState(latestState)
        }
    }

    companion object {
        const val TAG = "StatsPagerAdapter"
    }
}
