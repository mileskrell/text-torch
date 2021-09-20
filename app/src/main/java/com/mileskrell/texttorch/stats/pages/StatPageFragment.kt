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

package com.mileskrell.texttorch.stats.pages

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.FragmentStatPageBinding
import com.mileskrell.texttorch.stats.PeriodDialogFragment
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.util.LifecycleLoggingFragment

/**
 * Used for the fragments shown in [com.mileskrell.texttorch.stats.StatsFragment]
 */
open class StatPageFragment(type: SocialRecordAdapter.SocialRecordAdapterType) :
    LifecycleLoggingFragment(R.layout.fragment_stat_page) {

    private var _binding: FragmentStatPageBinding? = null
    private val b get() = _binding!!

    protected val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()
    private val socialRecordAdapter = SocialRecordAdapter(type)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatPageBinding.bind(view)
        socialRecordsViewModel.socialRecords.observe({ lifecycle }) {
            if (socialRecordsViewModel.showNonContacts) {
                socialRecordAdapter.socialRecords = it
            } else {
                socialRecordAdapter.socialRecords = it.filter { socialRecord ->
                    socialRecord.correspondentName != null
                }
            }
            b.recyclerView.scheduleLayoutAnimation()
        }

        b.recyclerView.setHasFixedSize(true)
        b.recyclerView.adapter = socialRecordAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        b.recyclerView.adapter = null
        _binding = null
    }
}

// These are their own classes just so logging their lifecycle methods is easier

class WhoTextsFirstFragment :
    StatPageFragment(SocialRecordAdapter.SocialRecordAdapterType.WHO_TEXTS_FIRST) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.who_texts_first_menu, menu)

        // Restore menu state
        val period = socialRecordsViewModel.period.menuId
        menu.findItem(period)?.isChecked = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.groupId == R.id.menu_group_period) {
            when (item.itemId) {
                R.id.menu_item_period_6_hours -> {
                    socialRecordsViewModel.changePeriod(SocialRecordsViewModel.Period.SIX_HOURS)
                }
                R.id.menu_item_period_12_hours -> {
                    socialRecordsViewModel.changePeriod(SocialRecordsViewModel.Period.TWELVE_HOURS)
                }
                R.id.menu_item_period_1_day -> {
                    socialRecordsViewModel.changePeriod(SocialRecordsViewModel.Period.ONE_DAY)
                }
                R.id.menu_item_period_2_days -> {
                    socialRecordsViewModel.changePeriod(SocialRecordsViewModel.Period.TWO_DAYS)
                }
            }
            item.isChecked = true
            return true
        }

        return when (item.itemId) {
            R.id.menu_item_period_explanation -> {
                showTimeExplanation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTimeExplanation() {
        PeriodDialogFragment().show(parentFragmentManager, null)
    }

    companion object {
        const val TAG = "WhoTextsFirstFragment"
    }
}

class TotalTextsFragment :
    StatPageFragment(SocialRecordAdapter.SocialRecordAdapterType.TOTAL_TEXTS) {
    companion object {
        const val TAG = "TotalTextsFragment"
    }
}

// TODO: Add a disclaimer somewhere explaining how emoji (and other chars?) can mess with this
class AverageLengthFragment :
    StatPageFragment(SocialRecordAdapter.SocialRecordAdapterType.AVERAGE_LENGTH) {
    companion object {
        const val TAG = "AverageLengthFragment"
    }
}
