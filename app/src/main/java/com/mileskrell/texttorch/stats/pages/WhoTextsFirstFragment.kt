package com.mileskrell.texttorch.stats.pages

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.PeriodDialogFragment
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel.Period.*
import kotlinx.android.synthetic.main.fragment_stat_page.*

class WhoTextsFirstFragment : Fragment(R.layout.fragment_stat_page) {

    companion object {
        const val TAG = "WhoTextsFirstFragment"
    }

    private val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()
    private val socialRecordAdapter = SocialRecordAdapter(SocialRecordAdapter.SocialRecordAdapterType.WHO_TEXTS_FIRST)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        socialRecordsViewModel.socialRecords.observe({ lifecycle }) {
            if (socialRecordsViewModel.showNonContacts) {
                socialRecordAdapter.loadSocialRecords(it)
            } else {
                socialRecordAdapter.loadSocialRecords(it.filter { socialRecord ->
                    socialRecord.correspondentName != null
                })
            }
        }

        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = socialRecordAdapter
        recycler_view.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.who_texts_first_menu, menu)

        // Restore menu state
        val period = socialRecordsViewModel.period.menuId
        menu.findItem(period)?.isChecked = true
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
        PeriodDialogFragment().show(parentFragmentManager, null)
    }

    override fun onDestroyView() {
        recycler_view.adapter = null
        super.onDestroyView()
    }
}
