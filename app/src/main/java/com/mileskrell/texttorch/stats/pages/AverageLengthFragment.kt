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
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.util.LifecycleLogggingFragment
import kotlinx.android.synthetic.main.fragment_stat_page.*

/**
 * TODO: Add a disclaimer somewhere explaining how emoji (and other chars?) can mess with this
 */
class AverageLengthFragment : LifecycleLogggingFragment(R.layout.fragment_stat_page) {

    companion object {
        const val TAG = "AverageLengthFragment"
    }

    private val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()
    private val socialRecordAdapter = SocialRecordAdapter(SocialRecordAdapter.SocialRecordAdapterType.AVERAGE_LENGTH)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socialRecordsViewModel.socialRecords.observe({ lifecycle }) {
            recycler_view.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_up)
            if (socialRecordsViewModel.showNonContacts) {
                socialRecordAdapter.loadSocialRecords(it)
            } else {
                socialRecordAdapter.loadSocialRecords(it.filter { socialRecord ->
                    socialRecord.correspondentName != null
                })
            }
            recycler_view.scheduleLayoutAnimation()
        }

        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = socialRecordAdapter
        recycler_view.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recycler_view.adapter = null
    }
}
