package com.mileskrell.texttorch.stats

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import kotlinx.android.synthetic.main.fragment_stats.*

class StatsFragment : Fragment() {

    companion object {
        const val TAG = "StatsFragment"
    }

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel

    /**
     * Position of the most-recently-viewed page. When the user navigates to another page,
     * the state of this page's RecyclerView will be propagated to the other pages.
     */
    var lastPage = 0

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
        if (socialRecordsViewModel.socialRecords.value?.isEmpty() == true) {
            stats_view_pager.visibility = View.GONE
            stats_no_records_text_view.visibility = View.VISIBLE
            return
        }
        stats_view_pager.offscreenPageLimit = 2
        stats_tab_layout.setupWithViewPager(stats_view_pager)
        stats_view_pager.adapter = StatsPagerAdapter(context!!, childFragmentManager)
        stats_view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Sync RecyclerView scroll position immediately when user drags another page into view.
                // Otherwise, the scroll position wouldn't be updated until the page had settled.
                (stats_view_pager.adapter as StatsPagerAdapter).onPageChanged(lastPage)
            }

            override fun onPageSelected(position: Int) {
                // The previous callback is only called on drags, so this one is needed for tapping on tabs.
                (stats_view_pager.adapter as StatsPagerAdapter).onPageChanged(lastPage)

                // Save the new position, so that when the user moves somewhere else,
                // we know which page's state is most recent.
                lastPage = position
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.stats_menu, menu)
        if (socialRecordsViewModel.socialRecords.value?.isEmpty() == true) {
            menu?.findItem(R.id.menu_item_sorting)?.isVisible = false
            // The alternative would be splitting the stats menu into 2 files and only inflating
            // the one with the sorting action if the list isn't empty.
            // But I don't think there's any real advantage to doing that.
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_sorting -> {
                SortTypeDialogFragment(socialRecordsViewModel.sortType.radioButtonId, socialRecordsViewModel.reversed)
                    .apply { setTargetFragment(this@StatsFragment, SortTypeDialogFragment.REQUEST_CODE) }
                    .show(fragmentManager, null)
                true
            }
            R.id.menu_item_about -> {
                findNavController().navigate(R.id.about_action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called by [SortTypeDialogFragment] when user presses the "update" button
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SortTypeDialogFragment.REQUEST_CODE && resultCode == SortTypeDialogFragment.RESULT_CODE) {
            val checkedRadioButtonId = data?.getIntExtra(SortTypeDialogFragment.SORT_TYPE_ID_KEY, -1)
            val reversed = data?.getBooleanExtra(SortTypeDialogFragment.REVERSED_KEY, false)!!

            val newSortType = SocialRecordsViewModel.SortType.values()
                .first { it.radioButtonId == checkedRadioButtonId }

            socialRecordsViewModel.changeSortTypeAndReversed(newSortType, reversed)
        }
    }
}
