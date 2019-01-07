package com.example.miles.whotextsfirst

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ArrayAdapter.createFromResource(context!!, R.array.times, android.R.layout.simple_spinner_item)
            .let { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                main_spinner.adapter = adapter
            }

        help_button.setOnClickListener {
            showTimeExplanation()
        }
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
}
