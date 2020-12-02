package com.mileskrell.texttorch.intro.pages

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.core.content.edit
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.FragmentIntroPageAnalyticsBinding
import com.mileskrell.texttorch.intro.IntroFragment
import com.mileskrell.texttorch.util.LifecycleLoggingFragment

class IntroPageAnalytics : LifecycleLoggingFragment(R.layout.fragment_intro_page_analytics) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = FragmentIntroPageAnalyticsBinding.bind(view)
        b.introAnalyticsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.intro_radio_button_enable -> {
                    PreferenceManager.getDefaultSharedPreferences(context).edit {
                        putBoolean(getString(R.string.key_enable_analytics), true)
                    }
                }
                R.id.intro_radio_button_disable -> {
                    PreferenceManager.getDefaultSharedPreferences(context).edit {
                        putBoolean(getString(R.string.key_enable_analytics), false)
                    }
                }
            }
            (parentFragment as IntroFragment).introPagerAdapter.addEnterAppPage()
        }
    }
}
