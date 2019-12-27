package com.mileskrell.texttorch.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mileskrell.texttorch.R

/**
 * The settings page
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
