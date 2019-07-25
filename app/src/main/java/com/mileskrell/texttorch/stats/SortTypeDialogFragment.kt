package com.mileskrell.texttorch.stats

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mileskrell.texttorch.R

/**
 * The dialog that appears when the user attempts to change the sort order.
 *
 * The reason this is a bit complicated is that it needs some way to save and restore its state.
 *
 * If we provided a callback as an argument, we would need somewhere to store it across configuration changes, but it
 * wouldn't be able to be saved in [onSaveInstanceState]. So it would require creating a ViewModel just for this
 * dialog, which feels over-the-top.
 *
 * TODO: Add descriptions to sort orders
 *
 * TODO: Add "by number of conversations" and "by combined total texts" sort orders
 */
class SortTypeDialogFragment() : DialogFragment() {

    private var currentlyCheckedRadioButtonId: Int? = null
    private var currentlyReversed: Boolean? = null

    constructor(radioButtonId: Int, reversed: Boolean) : this() {
        this.currentlyCheckedRadioButtonId = radioButtonId
        this.currentlyReversed = reversed
    }

    companion object {
        const val TAG = "SortTypeDialogFragment"
        const val REVERSED_KEY = "reversed"
        const val SORT_TYPE_ID_KEY = "sort_type_id"
        const val REQUEST_CODE = 0
        const val RESULT_CODE = 0
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.PeriodDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (savedInstanceState != null) {
            currentlyCheckedRadioButtonId = savedInstanceState.getInt(SORT_TYPE_ID_KEY)
            currentlyReversed = savedInstanceState.getBoolean(REVERSED_KEY)
        }

        /**
         * It's okay that we don't provide the view root in this line, since we don't use any important layout_*
         * properties on the root of the inflated layout.
         */
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_sort_type, null)

        dialogView.findViewById<RadioGroup>(R.id.radio_group_sort_type)!!
            .apply {
                check(currentlyCheckedRadioButtonId!!)
                setOnCheckedChangeListener { _, checkedId ->
                    currentlyCheckedRadioButtonId = checkedId
                }
            }
        dialogView.findViewById<CheckBox>(R.id.check_box_reversed)!!
            .apply {
                isChecked = currentlyReversed!!
                setOnCheckedChangeListener { _, isChecked ->
                    currentlyReversed = isChecked
                }
            }

        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(R.string.order_by)
            .setView(dialogView)
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.update)) { _, _ ->
                targetFragment?.onActivityResult(
                    REQUEST_CODE,
                    RESULT_CODE,
                    Intent()
                        .putExtra(SORT_TYPE_ID_KEY, currentlyCheckedRadioButtonId)
                        .putExtra(REVERSED_KEY, currentlyReversed)
                )
            }
            .create()

        return alertDialog ?: throw IllegalStateException("$TAG: Dialog is null")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SORT_TYPE_ID_KEY, currentlyCheckedRadioButtonId!!)
        outState.putBoolean(REVERSED_KEY, currentlyReversed!!)
    }
}
