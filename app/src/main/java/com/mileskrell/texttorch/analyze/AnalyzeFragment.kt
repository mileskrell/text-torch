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
import com.mileskrell.texttorch.databinding.FragmentAnalyzeBinding
import com.mileskrell.texttorch.stats.StatsFragment
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.util.LifecycleLoggingFragment
import com.mileskrell.texttorch.util.logToBoth
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import io.sentry.Sentry
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
class AnalyzeFragment : LifecycleLoggingFragment(R.layout.fragment_analyze) {

    private var _binding: FragmentAnalyzeBinding? = null
    private val b get() = _binding!!

    private val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()
    private val analyzeViewModel: AnalyzeViewModel by activityViewModels()
    private var valueAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnalyzeBinding.bind(view)
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

        b.analyzeButton.setOnClickListener {
            logToBoth(TAG, "Clicked \"analyze\" button")
            analyzeViewModel.clickedAnalyze = true
            enterProgressDisplayingMode()
            analyzeViewModel.initializeSocialRecordList(socialRecordsViewModel)
        }
    }

    private fun enterProgressDisplayingMode() {
        b.analyzeButton.visibility = View.INVISIBLE
        b.analyzingMessageThreadsTextView.visibility = View.VISIBLE
        b.threadsProgressBar.visibility = View.VISIBLE
        b.threadsProgressFractionTextView.visibility = View.VISIBLE
        b.threadsProgressPercentageTextView.visibility = View.VISIBLE
        b.threadsProgressFractionTextView.text =
            getString(R.string.x_out_of_y_message_threads, 0, "?")
        b.threadsProgressPercentageTextView.text = getString(R.string.x_percent, 0)

        b.showDetailsButton.visibility = View.VISIBLE
        b.showDetailsButton.setOnClickListener {
            logToBoth(TAG, "Clicked \"show details\" button")
            analyzeViewModel.clickedShowDetails = true
            enterProgressDetailsMode()
        }

        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                b.analyzingMessageThreadsTextView.alpha = it.animatedValue as Float
            }
            start()
        }

        var threadsTotal = -1
        analyzeViewModel.threadsTotal.observe({ lifecycle }) { newThreadsTotal ->
            threadsTotal = newThreadsTotal
            b.threadsProgressBar.max = threadsTotal
        }
        analyzeViewModel.threadsCompleted.observe({ lifecycle }) { newThreadsCompleted ->
            if (threadsTotal != -1) {
                b.threadsProgressBar.progress = newThreadsCompleted
                b.threadsProgressFractionTextView.text = getString(
                    R.string.x_out_of_y_message_threads,
                    newThreadsCompleted,
                    threadsTotal.toString()
                )
                val percentDone = (100.0 * newThreadsCompleted / threadsTotal).roundToInt()
                b.threadsProgressPercentageTextView.text =
                    getString(R.string.x_percent, percentDone)
            }
        }
        var messagesTotal = -1
        analyzeViewModel.messagesTotal.observe({ lifecycle }) { newMessagesTotal ->
            messagesTotal = newMessagesTotal
            b.messagesProgressBar.max = newMessagesTotal
        }
        analyzeViewModel.messagesCompleted.observe({ lifecycle }) { newMessagesCompleted ->
            if (messagesTotal != -1) {
                b.messagesProgressBar.progress = newMessagesCompleted
                b.messagesProgressFractionTextView.text = getString(
                    R.string.x_out_of_y_messages,
                    newMessagesCompleted,
                    messagesTotal.toString()
                )
                val percentDone =
                    if (messagesTotal == 0) 100
                    else (100.0 * newMessagesCompleted / messagesTotal).roundToInt()
                b.messagesProgressPercentageTextView.text =
                    getString(R.string.x_percent, percentDone)
            }
        }
    }

    private fun enterProgressDetailsMode() {
        b.showDetailsButton.visibility = View.INVISIBLE
        b.messagesProgressFractionTextView.text =
            getString(R.string.x_out_of_y_messages, 0, "?")
        b.messagesProgressPercentageTextView.text = getString(R.string.x_percent, 0)
        b.forCurrentMessageThreadTextView.visibility = View.VISIBLE
        b.messagesProgressBar.visibility = View.VISIBLE
        b.messagesProgressPercentageTextView.visibility = View.VISIBLE
        b.messagesProgressFractionTextView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        valueAnimator?.cancel()
    }

    companion object {
        const val TAG = "AnalyzeFragment"
    }
}
