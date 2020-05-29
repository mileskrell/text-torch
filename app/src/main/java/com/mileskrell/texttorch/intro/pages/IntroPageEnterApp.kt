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
