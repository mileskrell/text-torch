package com.mileskrell.texttorch.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mileskrell.texttorch.BuildConfig
import com.mileskrell.texttorch.R
import kotlinx.android.synthetic.main.fragment_about.*
import ly.count.android.sdk.Countly

class AboutFragment : Fragment(R.layout.fragment_about) {

    companion object {
        const val TAG = "AboutFragment"
    }

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        about_version.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        if (BuildConfig.DEBUG) {
            about_version.append("-debug")
        }

        about_feedback_button.setOnClickListener {
            Countly.sharedInstance().ratings().showFeedbackPopup(
                "5ecc6278562d9032a46c733d",
                getString(R.string.close),
                requireActivity()
            ) { error ->
                Countly.sharedInstance().crashes().recordHandledException(
                    RuntimeException("Error showing feedback popup: $error")
                )
                Snackbar.make(
                    about_constraint_layout,
                    getString(R.string.error_showing_feedback_dialog),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        about_github_button.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))))
        }

        about_licenses_button.setOnClickListener {
            findNavController().navigate(R.id.about_to_licenses_action)
        }

        about_donate_button.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_url))))
        }
    }
}
