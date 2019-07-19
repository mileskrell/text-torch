package com.mileskrell.texttorch.analyze

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import kotlinx.android.synthetic.main.fragment_analyze.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A [Fragment] containing an "analyze" button, which will prepare the data needed for the main fragment.
 *
 * This fragment also has its own ViewModel, [AnalyzeViewModel]. This is used to relay info from
 * [com.mileskrell.texttorch.stats.repo.ThreadGetter] back here, so we can display the progress to the user.
 *
 * This initially just used a simple callback, but I found that this crashed on configuration changes.
 * With ViewModel and LiveData, we can handle these events.
 */
class AnalyzeFragment : Fragment() {

    companion object {
        const val TAG = "AnalyzeFragment"
        const val CLICKED_ANALYZE = "clicked_analyze"
    }

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel
    private lateinit var analyzeViewModel: AnalyzeViewModel

    private var clickedAnalyze = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)
        analyzeViewModel = ViewModelProviders.of(this).get(AnalyzeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_analyze, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        socialRecordsViewModel.socialRecords.observe(this, Observer {
            findNavController().navigate(R.id.main_action)
        })

        clickedAnalyze = savedInstanceState?.getBoolean(CLICKED_ANALYZE) ?: false
        if (clickedAnalyze) {
            enterProgressDisplayingMode()
        }

        analyze_button.setOnClickListener {
            clickedAnalyze = true
            enterProgressDisplayingMode()
            initializeSocialRecordList(analyzeViewModel)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(CLICKED_ANALYZE, clickedAnalyze)
    }

    private fun enterProgressDisplayingMode() {
        analyze_button.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        progress_text_view.visibility = View.VISIBLE

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
                progress_text_view.text = "$newThreadsCompleted/$threadsTotal"
            }
        })
    }

    private fun initializeSocialRecordList(analyzeViewModel: AnalyzeViewModel) {
        socialRecordsViewModel.viewModelScope.launch(Dispatchers.IO) {
            socialRecordsViewModel.initializeSocialRecords(analyzeViewModel)
        }
    }
}
