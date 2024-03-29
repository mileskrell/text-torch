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
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.core.content.withStyledAttributes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.mileskrell.texttorch.MainActivity
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.analyze.AnalyzeFragment
import com.mileskrell.texttorch.databinding.FragmentIntroBinding
import com.mileskrell.texttorch.intro.pages.IntroPageEnterApp
import com.mileskrell.texttorch.regain.RegainPermissionsFragment
import com.mileskrell.texttorch.util.logToBoth
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import io.sentry.Sentry

// TODO: I'd like to have translucent status and navigation bars here

class IntroFragment : Fragment(R.layout.fragment_intro) {

    private var _binding: FragmentIntroBinding? = null
    private val b get() = _binding!!

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
                logToBoth(TAG, "Go to ${AnalyzeFragment.TAG} (user has already seen tutorial)")
                findNavController().navigate(R.id.intro_to_analyze_action)
            } else {
                // Permissions were granted (because tutorial was completed), but the user went in
                // and manually denied them later on. Prompt the user to grant them again.
                logToBoth(
                    TAG,
                    "Go to ${RegainPermissionsFragment.TAG} (user has already seen tutorial)"
                )
                findNavController().navigate(R.id.intro_to_regain_action)
            }
        } else {
            logToBoth(TAG, "Showing tutorial for first time")
        }
        requireActivity().withStyledAttributes(
            null, intArrayOf(
                R.attr.welcomePageBackgroundColor,
                R.attr.permissionsPageBackgroundColor,
                R.attr.analyticsPageBackgroundColor,
                R.attr.enterAppPageBackgroundColor,
            )
        ) { backgroundColors = List(length()) { getColor(it, 0) } }
        requireActivity().withStyledAttributes(
            null, intArrayOf(
                R.attr.welcomePageLogoColor,
                R.attr.permissionsPageLogoColor,
                R.attr.analyticsPageLogoColor,
                R.attr.enterAppPageLogoColor,
            )
        ) { logoColors = List(length()) { getColor(it, 0) } }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            backgroundColors = backgroundColors.filterIndexed { index, _ ->
                index != IntroViewModel.PAGE.PERMISSIONS.ordinal
            }
            logoColors = logoColors.filterIndexed { index, _ ->
                index != IntroViewModel.PAGE.PERMISSIONS.ordinal
            }
        }
    }

    private fun enterAlmostFullscreen() {
        (requireActivity() as MainActivity).supportActionBar?.hide()
    }

    private fun exitAlmostFullscreen() {
        (requireActivity() as MainActivity).supportActionBar?.show()
        requireActivity().withStyledAttributes(null, intArrayOf(android.R.attr.statusBarColor)) {
            requireActivity().window.statusBarColor = getColor(0, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIntroBinding.bind(view)
        // This check is needed because onViewCreated() is called even if we've already seen the
        // tutorial (since navigating to the next fragment takes a moment)
        if (hasSeenTutorial == false) {
            enterAlmostFullscreen()
        }

        b.introViewPager.offscreenPageLimit = IntroViewModel.PAGE.values().size
        b.introViewPager.adapter = IntroPagerAdapter(introViewModel, this)
        val colorBackground = (b.introViewPager.background as LayerDrawable)
            .getDrawable(0).mutate() as ColorDrawable
        val logoBackground = (b.introViewPager.background as LayerDrawable)
            .getDrawable(1).mutate() as BitmapDrawable

        b.introViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
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
                ) as Int).let { logoColor ->
                    logoBackground.setTint(logoColor)
                    requireActivity().window.statusBarColor = logoColor
                }

                // Since we don't add pages until the user is allowed to go to them, there's no
                // danger in showing this button while the last page is partly visible.
                refreshFabVisibility()
            }

            override fun onPageSelected(position: Int) {
                Sentry.addBreadcrumb(
                    "[$TAG] Switched to intro page at index ${b.introViewPager.currentItem}"
                )
            }
        })

        b.introArrowNext.setOnClickListener {
            b.introViewPager.currentItem = b.introViewPager.currentItem + 1
        }
    }

    fun refreshFabVisibility() {
        if (b.introViewPager.currentItem == b.introViewPager.adapter!!.itemCount - 1) {
            b.introArrowNext.hide()
        } else {
            b.introArrowNext.show()
        }
    }

    fun ensureAnalyticsPageAdded() {
        (b.introViewPager.adapter as IntroPagerAdapter).ensureAnalyticsPageAdded()
        refreshFabVisibility()
    }

    fun ensureEnterAppPageAdded() {
        (b.introViewPager.adapter as IntroPagerAdapter).ensureEnterAppPageAdded()
        refreshFabVisibility()
    }

    /**
     * Called when the "enter app" button in [IntroPageEnterApp] is clicked
     */
    fun onClickEnterAppButton() {
        exitAlmostFullscreen()

        // Save that tutorial has been seen
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
            putBoolean(getString(R.string.key_has_seen_tutorial), true)
        }
        logToBoth(TAG, "Clicked \"finish tutorial\" button")

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

    override fun onDestroyView() {
        super.onDestroyView()
        b.introViewPager.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "IntroFragment"
    }
}
