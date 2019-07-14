package com.mileskrell.texttorch.stats

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel.SortType.*
import kotlinx.android.synthetic.main.fragment_stats.*

class StatsFragment : Fragment() {

    companion object {
        const val TAG = "StatsFragment"
    }

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)
        if (socialRecordsViewModel.socialRecords.value == null) {
            // This should only happen after process death. In any case,
            // it means that we have to go back to the "analyze" page.
            findNavController().navigateUp()
        }
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stats_view_pager.offscreenPageLimit = 2
        stats_tab_layout.setupWithViewPager(stats_view_pager)
        stats_view_pager.adapter = StatsPagerAdapter(context!!, childFragmentManager)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        MenuCompat.setGroupDividerEnabled(menu, true)
        inflater?.inflate(R.menu.stats_menu, menu)

        // Restore menu state
        val sortType = socialRecordsViewModel.sortType.menuId
        menu?.findItem(sortType)?.isChecked = true
        menu?.findItem(R.id.menu_item_sort_reversed)?.isChecked = socialRecordsViewModel.reversed
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.groupId == R.id.menu_group_sort_type) {
            when (item.itemId) {
                R.id.menu_item_sort_type_most_recent -> {
                    socialRecordsViewModel.changeSortType(MOST_RECENT)
                }
                R.id.menu_item_sort_type_alphabetical -> {
                    socialRecordsViewModel.changeSortType(ALPHA)
                }
                R.id.menu_item_sort_type_people_you_text_first -> {
                    socialRecordsViewModel.changeSortType(PEOPLE_YOU_TEXT_FIRST)
                }
            }
            item.isChecked = true
            return true
        }

        return when (item.itemId) {
            R.id.menu_item_sort_reversed -> {
                item.isChecked = !item.isChecked
                socialRecordsViewModel.changeReversed(item.isChecked)
                true
            }
            R.id.menu_item_about -> {
                findNavController().navigate(R.id.about_action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
