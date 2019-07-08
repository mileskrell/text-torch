package com.mileskrell.texttorch.stats.pages

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.InfoDialogFragment
import com.mileskrell.texttorch.stats.SocialRecordAdapter
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel.Period.*
import kotlinx.android.synthetic.main.fragment_who_texts_first.*

class WhoTextsFirstFragment : Fragment() {

    companion object {
        const val TAG = "WhoTextsFirstFragment"
    }

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel
    private val socialRecordAdapter = SocialRecordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_who_texts_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        socialRecordsViewModel.socialRecords.observe(this, Observer {
            socialRecordAdapter.loadSocialRecords(it)
        })

        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = socialRecordAdapter
        recycler_view.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.who_texts_first_menu, menu)

        // Restore menu state
        val period = socialRecordsViewModel.period.menuId
        menu?.findItem(period)?.isChecked = true
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
        InfoDialogFragment().show(fragmentManager, null)
    }
}
