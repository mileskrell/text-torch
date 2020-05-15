package com.mileskrell.texttorch.intro.pages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.intro.IntroFragment
import kotlinx.android.synthetic.main.fragment_intro_page_analytics.*

class IntroPageAnalytics : Fragment(R.layout.fragment_intro_page_analytics) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        intro_analytics_radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                // TODO: Enable/disable analytics
                R.id.intro_radio_button_enable -> {

                }
                R.id.intro_radio_button_disable -> {

                }
            }
            (parentFragment as IntroFragment).introPagerAdapter.addEnterAppPage()
        }
    }
}
