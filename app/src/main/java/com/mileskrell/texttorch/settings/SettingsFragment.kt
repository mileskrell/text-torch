package com.mileskrell.texttorch.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mileskrell.texttorch.R
import ly.count.android.sdk.Countly

/**
 * The settings page
 */
class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val TAG = "SettingsFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
    }
}
