package com.mileskrell.texttorch.analyze

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
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
class AnalyzeFragment : Fragment(R.layout.fragment_analyze) {

    companion object {
        const val TAG = "AnalyzeFragment"
    }

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel
    private lateinit var analyzeViewModel: AnalyzeViewModel
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

        socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)
        analyzeViewModel = ViewModelProviders.of(activity!!).get(AnalyzeViewModel::class.java)

        socialRecordsViewModel.socialRecords.observe(this, Observer {
            findNavController().navigate(R.id.main_action)
        })

        if (analyzeViewModel.clickedAnalyze) {
            enterProgressDisplayingMode()
        }

        analyze_button.setOnClickListener {
            analyzeViewModel.clickedAnalyze = true
            enterProgressDisplayingMode()
            analyzeViewModel.initializeSocialRecordList(socialRecordsViewModel)
        }
    }

    private fun enterProgressDisplayingMode() {
        analyze_button.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        progress_fraction_text_view.visibility = View.VISIBLE
        progress_percentage_text_view.visibility = View.VISIBLE
        progress_fraction_text_view.text = getString(R.string.x_out_of_y, 0, "?")
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

        var threadsTotal = 10_000
        analyzeViewModel.threadsTotal.observe(this, Observer { newThreadsTotal ->
            activity?.runOnUiThread {
                threadsTotal = newThreadsTotal
                progress_bar.max = threadsTotal
            }
        })
        analyzeViewModel.threadsCompleted.observe(this, Observer { newThreadsCompleted ->
            activity?.runOnUiThread {
                progress_bar.progress = newThreadsCompleted
                progress_fraction_text_view.text =
                    getString(R.string.x_out_of_y, newThreadsCompleted, threadsTotal.toString())
                val percentDone = (100.0 * newThreadsCompleted / threadsTotal).roundToInt()
                progress_percentage_text_view.text = getString(R.string.x_percent, percentDone)
            }
        })
    }

    override fun onDestroyView() {
        valueAnimator?.cancel()
        super.onDestroyView()
    }
}
