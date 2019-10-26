package com.mileskrell.texttorch.analyze

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyzeViewModel : ViewModel() {

    val threadsCompleted = MutableLiveData<Int>()

    val threadsTotal = MutableLiveData<Int>()

    var clickedAnalyze = false

    fun initializeSocialRecordList(socialRecordsViewModel: SocialRecordsViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            socialRecordsViewModel.initializeSocialRecords(threadsTotal, threadsCompleted)
        }
    }
}
