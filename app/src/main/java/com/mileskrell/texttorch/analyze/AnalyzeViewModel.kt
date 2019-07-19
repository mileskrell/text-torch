package com.mileskrell.texttorch.analyze

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalyzeViewModel : ViewModel() {

    val threadsCompleted = MutableLiveData<Int>()

    val threadsTotal = MutableLiveData<Int>()
}
