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
import com.mileskrell.texttorch.stats.StatsFragment
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.util.LifecycleLogggingFragment
import com.mileskrell.texttorch.util.logToBoth
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import io.sentry.core.Sentry
import kotlinx.android.synthetic.main.fragment_analyze.*
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
class AnalyzeFragment : LifecycleLogggingFragment(R.layout.fragment_analyze) {

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
            Sentry.captureMessage("[$TAG] Finished getting records; going to ${StatsFragment.TAG}")
            findNavController().navigate(R.id.analyze_to_stats_action)
        }

        if (analyzeViewModel.clickedAnalyze) {
            enterProgressDisplayingMode()
        }

        if (analyzeViewModel.clickedShowDetails) {
            enterProgressDetailsMode()
        }

        analyze_button.setOnClickListener {
            logToBoth(TAG, "Clicked \"analyze\" button")
            analyzeViewModel.clickedAnalyze = true
            enterProgressDisplayingMode()
            analyzeViewModel.initializeSocialRecordList(socialRecordsViewModel)
        }
    }

    private fun enterProgressDisplayingMode() {
        analyze_button.visibility = View.INVISIBLE
        analyzing_message_threads_text_view.visibility = View.VISIBLE
        threads_progress_bar.visibility = View.VISIBLE
        threads_progress_fraction_text_view.visibility = View.VISIBLE
        threads_progress_percentage_text_view.visibility = View.VISIBLE
        threads_progress_fraction_text_view.text =
            getString(R.string.x_out_of_y_message_threads, 0, "?")
        threads_progress_percentage_text_view.text = getString(R.string.x_percent, 0)

        show_details_button.visibility = View.VISIBLE
        show_details_button.setOnClickListener {
            logToBoth(TAG, "Clicked \"show details\" button")
            analyzeViewModel.clickedShowDetails = true
            enterProgressDetailsMode()
        }

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
            threads_progress_bar.max = threadsTotal
        }
        analyzeViewModel.threadsCompleted.observe({ lifecycle }) { newThreadsCompleted ->
            if (threadsTotal != -1) {
                threads_progress_bar.progress = newThreadsCompleted
                threads_progress_fraction_text_view.text = getString(
                    R.string.x_out_of_y_message_threads,
                    newThreadsCompleted,
                    threadsTotal.toString()
                )
                val percentDone = (100.0 * newThreadsCompleted / threadsTotal).roundToInt()
                threads_progress_percentage_text_view.text =
                    getString(R.string.x_percent, percentDone)
            }
        }
        var messagesTotal = -1
        analyzeViewModel.messagesTotal.observe({ lifecycle }) { newMessagesTotal ->
            messagesTotal = newMessagesTotal
            messages_progress_bar.max = newMessagesTotal
        }
        analyzeViewModel.messagesCompleted.observe({ lifecycle }) { newMessagesCompleted ->
            if (messagesTotal != -1) {
                messages_progress_bar.progress = newMessagesCompleted
                messages_progress_fraction_text_view.text = getString(
                    R.string.x_out_of_y_messages,
                    newMessagesCompleted,
                    messagesTotal.toString()
                )
                val percentDone =
                    if (messagesTotal == 0) 100
                    else (100.0 * newMessagesCompleted / messagesTotal).roundToInt()
                messages_progress_percentage_text_view.text =
                    getString(R.string.x_percent, percentDone)
            }
        }
    }

    private fun enterProgressDetailsMode() {
        show_details_button.visibility = View.INVISIBLE
        messages_progress_fraction_text_view.text =
            getString(R.string.x_out_of_y_messages, 0, "?")
        messages_progress_percentage_text_view.text = getString(R.string.x_percent, 0)
        for_current_message_thread_text_view.visibility = View.VISIBLE
        messages_progress_bar.visibility = View.VISIBLE
        messages_progress_percentage_text_view.visibility = View.VISIBLE
        messages_progress_fraction_text_view.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        valueAnimator?.cancel()
    }
}
