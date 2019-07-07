package com.mileskrell.texttorch.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)

        if (socialRecordsViewModel.socialRecords.value == null) {
            // Process death! Go back to MainFragment (which will in turn send us back to AnalyzeFragment).
            findNavController().navigateUp()
        }

        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}
