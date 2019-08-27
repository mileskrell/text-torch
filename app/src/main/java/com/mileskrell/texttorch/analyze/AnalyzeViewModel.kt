package com.mileskrell.texttorch.analyze

import android.animation.ValueAnimator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalyzeViewModel : ViewModel() {

    val threadsCompleted = MutableLiveData<Int>()

    val threadsTotal = MutableLiveData<Int>()

    /**
     * Used to animate "Analyzing message threads…" TextView
     */
    var valueAnimator: ValueAnimator? = null
}
