/*
 * Copyright (C) 2020 Miles Krell and the Text Torch contributors
 *
 * This file is part of Text Torch.
 *
 * Text Torch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Text Torch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Text Torch.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mileskrell.texttorch.stats

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mileskrell.texttorch.R

/**
 * The dialog that appears when the user attempts to change the sort order.
 *
 * TODO: Add descriptions to sort orders
 */
class SortTypeDialogFragment : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /**
         * It's okay that we don't provide the view root in this line, since we don't use any
         * important layout_* properties on the root of the inflated layout.
         *
         * The [SuppressLint] annotation above is for this line.
         */
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_sort_type, null)

        // Even though we're always setting the checked state of these to their *initial* values,
        // they're set to their *latest* values immediately after, so I guess it must be persisted
        // automatically by these widgets. This is nice because it lets this file be a lot shorter.
        dialogView.findViewById<RadioGroup>(R.id.radio_group_sort_type)!!
            .run {
                check(requireArguments().getInt(SORT_TYPE_ID))
            }
        dialogView.findViewById<MaterialCheckBox>(R.id.check_box_reversed)!!
            .run {
                isChecked = requireArguments().getBoolean(REVERSED)
            }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.order_by)
            .setView(dialogView)
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.update)) { _, _ ->
                setFragmentResult(
                    REQUEST_CODE,
                    bundleOf(
                        SORT_TYPE_ID to dialogView.findViewById<RadioGroup>(R.id.radio_group_sort_type)!!.checkedRadioButtonId,
                        REVERSED to dialogView.findViewById<MaterialCheckBox>(R.id.check_box_reversed)!!.isChecked
                    )
                )
            }
            .create().apply {
                window?.attributes?.windowAnimations = R.style.SlidingDialogStyle
            }
    }

    companion object {
        const val TAG = "SortTypeDialogFragment"

        // Used for both fragment arguments and bundle keys
        const val SORT_TYPE_ID = "sort type ID"
        const val REVERSED = "reversed"

        const val REQUEST_CODE = "sort type"

        @JvmStatic
        fun newInstance(checkedRadioButtonId: Int, reversed: Boolean) =
            SortTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(SORT_TYPE_ID, checkedRadioButtonId)
                    putBoolean(REVERSED, reversed)
                }
            }
    }
}
