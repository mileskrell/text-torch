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

package com.mileskrell.texttorch.intro

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.mileskrell.texttorch.MainActivity
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import kotlinx.android.synthetic.main.fragment_intro.*
import ly.count.android.sdk.Countly

// TODO: I'd like to have translucent status and navigation bars here

class IntroFragment : Fragment(R.layout.fragment_intro) {

    companion object {
        const val TAG = "IntroFragment"
    }

    lateinit var introPagerAdapter: IntroPagerAdapter

    private val introViewModel: IntroViewModel by activityViewModels()
    private var hasSeenTutorial: Boolean? = null // Because primitives can't be lateinit

    // Colors for ViewPager background
    private lateinit var backgroundColors: List<Int>
    private lateinit var logoColors: List<Int>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // TODO There's probably some earlier place to put this check
        hasSeenTutorial = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            getString(R.string.key_has_seen_tutorial), false
        )

        if (hasSeenTutorial == true) {
            // If all permissions have been granted...
            if (readSmsGranted() && readContactsGranted()) {
                // Immediately go to AnalyzeFragment, without any animations
                Countly.sharedInstance().events().recordEvent("intro to analyze")
                findNavController().navigate(R.id.intro_to_analyze_action)
            } else {
                // Permissions were granted (because tutorial was completed), but the user went in
                // and manually denied them later on. Prompt the user to grant them again.
                Countly.sharedInstance().events().recordEvent("intro to regain")
                findNavController().navigate(R.id.intro_to_regain_action)
            }
        }

        backgroundColors = resources.getStringArray(R.array.intro_background_colors)
            .map { Color.parseColor(it) }
        logoColors = resources.getStringArray(R.array.intro_logo_colors)
            .map { Color.parseColor(it) }
    }

    private fun enterAlmostFullscreen() {
        (activity as? MainActivity)?.supportActionBar?.hide()
    }

    private fun exitAlmostFullscreen() {
        (activity as? MainActivity)?.supportActionBar?.show()
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // This check is needed because onViewCreated() is called even if we've already seen the
        // tutorial (since navigating to the next fragment takes a moment)
        if (hasSeenTutorial == false) {
            enterAlmostFullscreen()
        }

        intro_view_pager.offscreenPageLimit = IntroViewModel.PAGE.values().size
        intro_view_pager.adapter = IntroPagerAdapter(introViewModel, childFragmentManager).also {
            introPagerAdapter = it
        }
        val colorBackground = (intro_view_pager.background as LayerDrawable)
            .getDrawable(0) as ColorDrawable
        val logoBackground = (intro_view_pager.background as LayerDrawable)
            .getDrawable(1) as BitmapDrawable

        intro_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Thanks to https://kubaspatny.github.io/2014/09/18/viewpager-background-transition/
                colorBackground.color = ArgbEvaluator().evaluate(
                    positionOffset,
                    backgroundColors[position],
                    backgroundColors.getOrNull(position + 1) ?: backgroundColors[position]
                ) as Int
                (ArgbEvaluator().evaluate(
                    positionOffset,
                    logoColors[position],
                    logoColors.getOrNull(position + 1) ?: logoColors[position]
                ) as Int).let { darkColor ->
                    logoBackground.setTint(darkColor)
                    activity?.window?.statusBarColor = darkColor
                }

                // Since we don't add pages until the user is allowed to go to them, there's no
                // danger in showing this button while the last page is partly visible.
                if (position == (intro_view_pager.adapter as IntroPagerAdapter).count - 1) {
                    intro_arrow_next.hide()
                } else {
                    intro_arrow_next.show()
                }
            }
            override fun onPageSelected(position: Int) {}
        })

        // TODO: Can we make this button scroll more slowly?
        intro_arrow_next.setOnClickListener {
            intro_view_pager.arrowScroll(View.FOCUS_RIGHT)
        }
    }

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
    }

    /**
     * Called when the "enter app" button in [IntroPageEnterApp] is clicked
     */
    fun onClickEnterAppButton() {
        exitAlmostFullscreen()

        // Save that tutorial has been seen
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(getString(R.string.key_has_seen_tutorial), true)
        }
        Countly.sharedInstance().events().recordEvent("finished tutorial")

        // Animated navigation to AnalyzeFragment
        findNavController().navigate(R.id.intro_to_analyze_action, null, navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
            // We have to specify these 2 even though they're already in XML, because
            // this NavOptions totally overrides what's in XML
            popUpTo(R.id.intro_dest) {
                inclusive = true
            }
        })
    }
}
