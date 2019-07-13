package com.mileskrell.texttorch.stats.pages

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import kotlinx.android.synthetic.main.fragment_stat_page.*

/**
 * TODO: Add a disclaimer somewhere explaining how emoji (and other chars?) can mess with this
 */
class AverageLengthFragment : Fragment() {

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel
    private val socialRecordAdapter = SocialRecordAdapter(SocialRecordAdapter.SocialRecordAdapterType.AVERAGE_LENGTH)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_stat_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        socialRecordsViewModel.socialRecords.observe(this, Observer {
            socialRecordAdapter.loadSocialRecords(it)
        })

        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = socialRecordAdapter
        recycler_view.layoutManager = LinearLayoutManager(context)
    }
}
