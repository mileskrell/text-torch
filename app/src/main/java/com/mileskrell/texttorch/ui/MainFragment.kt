package com.mileskrell.texttorch.ui

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.model.SocialRecordsViewModel
import com.mileskrell.texttorch.model.SocialRecordsViewModel.SortType.*
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private val TAG = "MainFragment"

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel
    private val socialRecordAdapter = SocialRecordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        socialRecordsViewModel = ViewModelProviders.of(activity!!).get(SocialRecordsViewModel::class.java)

        if (socialRecordsViewModel.socialRecords.value == null) {
            // This should only happen after process death. In any case,
            // it means that we have to go back to the "analyze" page.
            findNavController().navigateUp()
        }

        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        socialRecordsViewModel.socialRecords.observe(this, Observer {
            socialRecordAdapter.loadSocialRecords(it)
        })

        setupUI()
    }

    private fun setupUI() {
        period_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Safety check because this method gets called on start for some reason >:(
                if (socialRecordsViewModel.socialRecords.value != null) {
                    socialRecordsViewModel.changePeriod(period_spinner.selectedItem.toString())
                }
            }
        }

        help_button.setOnClickListener {
            showTimeExplanation()
        }

        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = socialRecordAdapter
        recycler_view.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        MenuCompat.setGroupDividerEnabled(menu, true)
        inflater?.inflate(R.menu.main_menu, menu)

        // Restore menu state
        val checkedMenuItem = when (socialRecordsViewModel.sortType) {
            MOST_RECENT -> R.id.menu_item_sort_type_most_recent
            ALPHA -> R.id.menu_item_sort_type_alphabetical
            PEOPLE_YOU_TEXT_FIRST -> R.id.menu_item_sort_type_people_you_text_first
        }
        menu?.findItem(checkedMenuItem)?.isChecked = true
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

    private fun showTimeExplanation() {
        InfoDialogFragment().show(fragmentManager, null)
    }
}
