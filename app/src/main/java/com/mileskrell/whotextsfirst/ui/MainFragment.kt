package com.mileskrell.whotextsfirst.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mileskrell.whotextsfirst.R
import com.mileskrell.whotextsfirst.model.SocialRecordsViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainFragment : Fragment(), CoroutineScope {

    private var job: Job? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private lateinit var socialRecordsViewModel: SocialRecordsViewModel
    private val socialRecordAdapter = SocialRecordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        socialRecordsViewModel = ViewModelProviders.of(this).get(SocialRecordsViewModel::class.java)
        socialRecordsViewModel.socialRecords.observe(this, Observer {
            socialRecordAdapter.loadSocialRecords(it)
            progress_bar.visibility = View.INVISIBLE
            recycler_view.visibility = View.VISIBLE
        })

        setupUI()
    }

    private fun setupUI() {

        help_button.setOnClickListener {
            showTimeExplanation()
        }

        go_button.setOnClickListener {
            job?.cancel()
            updateSocialRecordList()
        }

        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = socialRecordAdapter
        recycler_view.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    private fun updateSocialRecordList() {
        recycler_view.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        job = launch(Dispatchers.IO) {
            socialRecordsViewModel.updateSocialRecords(main_spinner.selectedItem.toString())
        }
    }
}
