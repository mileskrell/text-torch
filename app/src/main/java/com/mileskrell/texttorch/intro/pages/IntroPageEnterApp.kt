package com.mileskrell.texttorch.intro.pages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.intro.IntroFragment
import kotlinx.android.synthetic.main.fragment_intro_page_enter_app.*

class IntroPageEnterApp : Fragment(R.layout.fragment_intro_page_enter_app) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        intro_button_enter_app.setOnClickListener {
            (parentFragment as IntroFragment).onClickEnterAppButton()
        }
    }
}
