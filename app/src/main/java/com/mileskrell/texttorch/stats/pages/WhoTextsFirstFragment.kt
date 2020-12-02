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
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.FragmentStatPageBinding
import com.mileskrell.texttorch.stats.PeriodDialogFragment
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel.Period.*
import com.mileskrell.texttorch.util.LifecycleLogggingFragment

class WhoTextsFirstFragment : LifecycleLogggingFragment(R.layout.fragment_stat_page) {

    companion object {
        const val TAG = "WhoTextsFirstFragment"
    }

    private var _binding: FragmentStatPageBinding? = null
    private val b get() = _binding!!

    private val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()
    private val socialRecordAdapter = SocialRecordAdapter(SocialRecordAdapter.SocialRecordAdapterType.WHO_TEXTS_FIRST)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatPageBinding.bind(view)
        setHasOptionsMenu(true)

        socialRecordsViewModel.socialRecords.observe({ lifecycle }) {
            b.recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_up)
            if (socialRecordsViewModel.showNonContacts) {
                socialRecordAdapter.loadSocialRecords(it)
            } else {
                socialRecordAdapter.loadSocialRecords(it.filter { socialRecord ->
                    socialRecord.correspondentName != null
                })
            }
            b.recyclerView.scheduleLayoutAnimation()
        }

        b.recyclerView.setHasFixedSize(true)
        b.recyclerView.adapter = socialRecordAdapter
        b.recyclerView.layoutManager = LinearLayoutManager(context)
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
                    socialRecordsViewModel.changePeriod(SIX_HOURS)
                }
                R.id.menu_item_period_12_hours -> {
                    socialRecordsViewModel.changePeriod(TWELVE_HOURS)
                }
                R.id.menu_item_period_1_day -> {
                    socialRecordsViewModel.changePeriod(ONE_DAY)
                }
                R.id.menu_item_period_2_days -> {
                    socialRecordsViewModel.changePeriod(TWO_DAYS)
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

    override fun onDestroyView() {
        super.onDestroyView()
        b.recyclerView.adapter = null
        _binding = null
    }
}
