package com.mileskrell.texttorch.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mileskrell.texttorch.R
import ly.count.android.sdk.Countly

class LicensesFragment : Fragment(R.layout.fragment_licenses) {

    companion object {
        const val TAG = "LicensesFragment"
    }

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.commitNow {
            // TODO: Include copyright year manually where needed
            //  see https://github.com/mikepenz/AboutLibraries/issues/469
            replace(
                R.id.about_licenses_container,
                LibsBuilder().run {
                    aboutShowIcon = false
                    showLicense = true
                    supportFragment()
                }
            )
        }
    }
}
