package com.mileskrell.texttorch.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R
import kotlinx.android.synthetic.main.fragment_intro_page_4.*

class IntroPage4 : Fragment(R.layout.fragment_intro_page_4) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        intro_button_enter_app.setOnClickListener {
            (parentFragment as IntroFragment).onClickEnterAppButton()
        }
    }
}
