package com.mileskrell.texttorch.stats

import android.content.Context
import androidx.fragment.app.Fragment
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

    private var whoTextsFirstFragment: WhoTextsFirstFragment = WhoTextsFirstFragment()
    private var totalTextsFragment: TotalTextsFragment = TotalTextsFragment()
    private var averageLengthFragment: AverageLengthFragment = AverageLengthFragment()

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> whoTextsFirstFragment
            1 -> totalTextsFragment
            2 -> averageLengthFragment
            else -> throw RuntimeException("$TAG: Requested fragment out of range")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.who_texts_first)
            1 -> context.getString(R.string.total_texts)
            2 -> context.getString(R.string.average_length)
            else -> null
        }
    }

    override fun getCount() = 3
}
