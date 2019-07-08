package com.mileskrell.texttorch.stats.pages

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R

class AverageLengthFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_average_length, container, false)
    }
}
