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

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.LifecycleLoggingDialogFragment
import com.mileskrell.texttorch.util.logToBoth

class PeriodDialogFragment : LifecycleLoggingDialogFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.PeriodDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.whats_this)
            .setMessage(getString(R.string.time_explanation))
            .setNeutralButton(getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logToBoth(TAG, "Viewed period explanation")
    }

    companion object {
        const val TAG = "PeriodDialogFragment"
    }
}
