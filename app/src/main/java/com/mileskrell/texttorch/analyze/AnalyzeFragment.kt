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
 */
class AnalyzeFragment : Fragment() {

    companion object {
        const val TAG = "AnalyzeFragment"
        const val CLICKED_ANALYZE = "clicked_analyze"
    }

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel

    private var clickedAnalyze = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_analyze, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        socialRecordsViewModel.socialRecords.observe(this, Observer {
            findNavController().navigate(R.id.main_action)
        })

        clickedAnalyze = savedInstanceState?.getBoolean(CLICKED_ANALYZE) ?: false
        if (clickedAnalyze) {
            analyze_button.visibility = View.INVISIBLE
            progress_bar.visibility = View.VISIBLE
        }

        analyze_button.setOnClickListener {
            clickedAnalyze = true
            analyze_button.visibility = View.INVISIBLE
            progress_bar.visibility = View.VISIBLE
            initializeSocialRecordList()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(CLICKED_ANALYZE, clickedAnalyze)
    }

    private fun initializeSocialRecordList() {
        socialRecordsViewModel.viewModelScope.launch(Dispatchers.IO) {
            socialRecordsViewModel.initializeSocialRecords()
        }
    }
}
