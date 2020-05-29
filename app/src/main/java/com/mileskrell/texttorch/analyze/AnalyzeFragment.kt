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

package com.mileskrell.texttorch.analyze

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import kotlinx.android.synthetic.main.fragment_analyze.*
import ly.count.android.sdk.Countly
import kotlin.math.roundToInt

/**
 * A [Fragment] containing an "analyze" button, which will prepare the data needed for the main fragment.
 *
 * This fragment also has its own ViewModel, [AnalyzeViewModel]. This is used to relay info from
 * [com.mileskrell.texttorch.stats.repo.ThreadGetter] back here, so we can display the progress to the user.
 *
 * This initially just used a simple callback, but I found that this crashed on configuration changes.
 * With ViewModel and LiveData, we can handle these events.
 */
class AnalyzeFragment : Fragment(R.layout.fragment_analyze) {

    companion object {
        const val TAG = "AnalyzeFragment"
    }

    private val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()
    private val analyzeViewModel: AnalyzeViewModel by activityViewModels()
    var valueAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // AnalyzeFragment is the only page where these permissions are used, so this is the only
        // page where we only need to check if permissions were lost while running.

        if (!readSmsGranted() || !readContactsGranted()) {
            // Since we've been clearing the stack as we navigate (to prevent the user from going
            // back), this will result in IntroFragment opening, which will in turn immediately open
            // RegainPermissionsFragment.
            // TODO: If the user denies a permission while on e.g. AboutFragment, we still return to
            //  RegainPermissionsFragment. This is fine, but I should understand why it's happening.
            findNavController().navigateUp()
        }

        socialRecordsViewModel.socialRecords.observe({ lifecycle }) {
            findNavController().navigate(R.id.analyze_to_stats_action)
        }

        if (analyzeViewModel.clickedAnalyze) {
            enterProgressDisplayingMode()
        }

        analyze_button.setOnClickListener {
            analyzeViewModel.clickedAnalyze = true
            enterProgressDisplayingMode()
            analyzeViewModel.initializeSocialRecordList(socialRecordsViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
    }

    private fun enterProgressDisplayingMode() {
        analyze_button.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        progress_fraction_text_view.visibility = View.VISIBLE
        progress_percentage_text_view.visibility = View.VISIBLE
        progress_fraction_text_view.text = getString(R.string.x_out_of_y_threads, 0, "?")
        progress_percentage_text_view.text = getString(R.string.x_percent, 0)
        analyzing_message_threads_text_view.visibility = View.VISIBLE

        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                analyzing_message_threads_text_view.alpha = it.animatedValue as Float
            }
            start()
        }

        var threadsTotal = -1
        analyzeViewModel.threadsTotal.observe({ lifecycle }) { newThreadsTotal ->
            threadsTotal = newThreadsTotal
            progress_bar.max = threadsTotal
        }
        analyzeViewModel.threadsCompleted.observe({ lifecycle }) { newThreadsCompleted ->
            if (threadsTotal != -1) {
                progress_bar.progress = newThreadsCompleted
                progress_fraction_text_view.text = getString(
                    R.string.x_out_of_y_threads,
                    newThreadsCompleted,
                    threadsTotal.toString()
                )
                val percentDone = (100.0 * newThreadsCompleted / threadsTotal).roundToInt()
                progress_percentage_text_view.text = getString(R.string.x_percent, percentDone)
            }
        }
    }

    override fun onDestroyView() {
        valueAnimator?.cancel()
        super.onDestroyView()
    }
}
