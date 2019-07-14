package com.mileskrell.texttorch.stats

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.pages.AverageLengthFragment
import com.mileskrell.texttorch.stats.pages.TotalTextsFragment
import com.mileskrell.texttorch.stats.pages.WhoTextsFirstFragment

class StatsPagerAdapter(val context: Context, fm: FragmentManager): FragmentPagerAdapter(fm) {

    companion object {
        const val TAG = "StatsPagerAdapter"
    }

    private val pages = listOf(
        WhoTextsFirstFragment(),
        TotalTextsFragment(),
        AverageLengthFragment()
    )

    override fun getCount() = pages.size

    override fun getItem(position: Int) = pages[position]

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.who_texts_first)
            1 -> context.getString(R.string.total_texts)
            2 -> context.getString(R.string.average_length)
            else -> null
        }
    }
}
