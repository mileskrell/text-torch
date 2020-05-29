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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import kotlinx.android.synthetic.main.fragment_stats.*
import ly.count.android.sdk.Countly

class StatsFragment : Fragment(R.layout.fragment_stats) {

    companion object {
        const val TAG = "StatsFragment"
    }

    private val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()

    /**
     * Position of the most-recently-viewed page. When the user navigates to another page,
     * the state of this page's RecyclerView will be propagated to the other pages.
     */
    var lastPage = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (socialRecordsViewModel.socialRecords.value == null) {
            // This should only happen after process death. In any case,
            // it means that we have to go back to the "analyze" page.
            findNavController().navigateUp()
        }
        setHasOptionsMenu(true)

        socialRecordsViewModel.run {
            // If list is totally empty
            if (socialRecords.value!!.isEmpty()
                // OR if only showing contacts, but the list contains no contacts
                || !showNonContacts && socialRecords.value!!.none { it.correspondentName != null }
            ) {
                stats_view_pager.visibility = View.GONE
                stats_no_records_text_view.visibility = View.VISIBLE
                return
            }
        }
        stats_view_pager.offscreenPageLimit = 2
        stats_tab_layout.setupWithViewPager(stats_view_pager)
        stats_view_pager.adapter = StatsPagerAdapter(requireContext(), childFragmentManager)
        stats_view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Sync RecyclerView scroll position immediately when user drags another page into view.
                // Otherwise, the scroll position wouldn't be updated until the page had settled.
                (stats_view_pager.adapter as StatsPagerAdapter).onPageChanged(lastPage)
            }

            override fun onPageSelected(position: Int) {
                // The previous callback is only called on drags, so this one is needed for tapping on tabs.
                (stats_view_pager.adapter as StatsPagerAdapter).onPageChanged(lastPage)

                // Save the new position, so that when the user moves somewhere else,
                // we know which page's state is most recent.
                lastPage = position
                Countly.sharedInstance().events().recordEvent(
                    "switched stats page", mapOf(
                        "page" to when (position) {
                            0 -> "who texts first"
                            1 -> "total texts"
                            2 -> "average length"
                            else -> "invalid position $position"
                        }
                    )
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_menu, menu)
        if (socialRecordsViewModel.socialRecords.value?.isEmpty() == true) {
            menu.findItem(R.id.menu_item_sorting)?.isVisible = false
            // The alternative would be splitting the stats menu into 2 files and only inflating
            // the one with the sorting action if the list isn't empty.
            // But I don't think there's any real advantage to doing that.
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Countly.sharedInstance().events()
            .recordEvent("menu item selected", mapOf("item" to item.title))
        return when (item.itemId) {
            R.id.menu_item_sorting -> {
                val sortTypeDialogFragment = SortTypeDialogFragment.newInstance(socialRecordsViewModel.sortType.radioButtonId, socialRecordsViewModel.reversed)
                    .apply { setTargetFragment(this@StatsFragment, SortTypeDialogFragment.REQUEST_CODE) }
                sortTypeDialogFragment.show(parentFragmentManager, null)
                true
            }
            R.id.menu_item_settings -> {
                findNavController().navigate(R.id.stats_to_settings_action)
                true
            }
            R.id.menu_item_about -> {
                findNavController().navigate(R.id.stats_to_about_action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called by [SortTypeDialogFragment] when user presses the "update" button
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SortTypeDialogFragment.REQUEST_CODE && resultCode == SortTypeDialogFragment.RESULT_CODE) {
            val checkedRadioButtonId = data?.getIntExtra(SortTypeDialogFragment.SORT_TYPE_ID, -1)
            val reversed = data?.getBooleanExtra(SortTypeDialogFragment.REVERSED, false)!!

            val newSortType = SocialRecordsViewModel.SortType.values()
                .first { it.radioButtonId == checkedRadioButtonId }

            Countly.sharedInstance().events().recordEvent("changed sort type",
                mapOf("type" to newSortType.name, "reversed" to reversed)
            )
            socialRecordsViewModel.changeSortTypeAndReversed(newSortType, reversed)
        }
    }
}
