package com.mileskrell.texttorch.intro

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class IntroPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val pages = mutableListOf(
        IntroPage1(),
        IntroPage2(),
        IntroPage3()
    )

    override fun getItem(position: Int) = pages[position]

    override fun getCount() = pages.size

    /**
     * Fix intro fragment references after configuration changes.
     *
     * See https://stackoverflow.com/a/17629575
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position).also {
            pages[position] = it as Fragment
        }
    }
}
