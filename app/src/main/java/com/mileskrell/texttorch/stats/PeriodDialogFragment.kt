package com.mileskrell.texttorch.stats

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mileskrell.texttorch.R
import ly.count.android.sdk.Countly

class PeriodDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "PeriodDialogFragment"
    }

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

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().events().recordEvent("viewed period explanation")
    }
}
