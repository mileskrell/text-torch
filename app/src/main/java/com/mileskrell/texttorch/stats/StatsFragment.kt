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

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.FragmentStatsBinding
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.stats.pages.AverageLengthFragment
import com.mileskrell.texttorch.stats.pages.TotalTextsFragment
import com.mileskrell.texttorch.stats.pages.WhoTextsFirstFragment
import com.mileskrell.texttorch.util.logEvent
import com.mileskrell.texttorch.util.logToBoth
import io.sentry.Sentry
import io.sentry.SentryLevel

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val b get() = _binding!!

    private val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()
    private var tabLayoutMediator: TabLayoutMediator? = null

    /**
     * Position of the most-recently-viewed page. When the user navigates to another page,
     * the state of this page's RecyclerView will be propagated to the other pages.
     */
    var lastPage = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)
        if (socialRecordsViewModel.socialRecords.value == null) {
            // This should only happen after process death. In any case,
            // it means that we have to go back to the "analyze" page.
            findNavController().navigateUp()
        }

        socialRecordsViewModel.run {
            // If list is totally empty
            if (socialRecords.value!!.isEmpty()
                // OR if only showing contacts, but the list contains no contacts
                || !showNonContacts && socialRecords.value!!.none { it.correspondentName != null }
            ) {
                b.statsViewPager.visibility = View.GONE
                b.statsNoRecordsTextView.visibility = View.VISIBLE
                return
            }
        }
        b.statsViewPager.offscreenPageLimit = 2
        b.statsViewPager.adapter = StatsPagerAdapter(this)
        tabLayoutMediator = TabLayoutMediator(b.statsTabLayout, b.statsViewPager) { tab, position ->
            tab.text = requireContext().getString(
                when (position) {
                    0 -> R.string.who_texts_first
                    1 -> R.string.total_texts
                    2 -> R.string.average_length
                    else -> throw RuntimeException("invalid position $position")
                }
            )
        }
        tabLayoutMediator!!.attach()
        b.statsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // Sync RecyclerView scroll position immediately when user drags another page into view.
                // Otherwise, the scroll position wouldn't be updated until the page had settled.
                (b.statsViewPager.adapter as StatsPagerAdapter).onPageChanged(lastPage)
            }

            override fun onPageSelected(position: Int) {
                // The previous callback is only called on drags, so this one is needed for tapping on tabs.
                (b.statsViewPager.adapter as StatsPagerAdapter).onPageChanged(lastPage)

                // Save the new position, so that when the user moves somewhere else,
                // we know which page's state is most recent.
                lastPage = position
                Sentry.addBreadcrumb(
                    "[$TAG] Switched stats page to ${
                        when (position) {
                            0 -> WhoTextsFirstFragment.TAG
                            1 -> TotalTextsFragment.TAG
                            2 -> AverageLengthFragment.TAG
                            else -> "invalid position $position"
                        }
                    }"
                )
            }
        })
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.stats_menu, menu)
                if (socialRecordsViewModel.socialRecords.value?.isEmpty() == true) {
                    menu.findItem(R.id.menu_item_sorting)?.isVisible = false
                    // The alternative would be splitting the stats menu into 2 files and only inflating
                    // the one with the sorting action if the list isn't empty.
                    // But I don't think there's any real advantage to doing that.
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                logToBoth(TAG, "Selected menu item with title \"${menuItem.title}\"")
                return when (menuItem.itemId) {
                    R.id.menu_item_sorting -> {
                        val sortTypeDialogFragment = SortTypeDialogFragment.newInstance(
                            socialRecordsViewModel.sortType.radioButtonId,
                            socialRecordsViewModel.reversed
                        )
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
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        /**
         * Called by [SortTypeDialogFragment] when user presses the "update" button
         */
        setFragmentResultListener(SortTypeDialogFragment.REQUEST_CODE) { _, bundle ->
            val checkedRadioButtonId = bundle.getInt(SortTypeDialogFragment.SORT_TYPE_ID)
            val reversed = bundle.getBoolean(SortTypeDialogFragment.REVERSED)

            val newSortType = SocialRecordsViewModel.SortType.values()
                .first { it.radioButtonId == checkedRadioButtonId }

            logEvent(
                TAG,
                "Changed sort type",
                SentryLevel.INFO,
                true,
                mapOf("type" to newSortType.name, "reversed" to reversed)
            )
            socialRecordsViewModel.changeSortTypeAndReversed(newSortType, reversed)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        b.statsViewPager.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "StatsFragment"
    }
}
