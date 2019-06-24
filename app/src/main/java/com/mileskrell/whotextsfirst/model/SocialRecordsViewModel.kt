package com.mileskrell.whotextsfirst.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mileskrell.whotextsfirst.repo.Repository

/**
 * An [AndroidViewModel] holding a [MutableLiveData] containing a list of [SocialRecord]
 */
class SocialRecordsViewModel(val app: Application) : AndroidViewModel(app) {

    private val TAG = "SocialRecordViewModel"

    private val _socialRecords: MutableLiveData<List<SocialRecord>> = MutableLiveData()

    val socialRecords = _socialRecords as LiveData<List<SocialRecord>>

    private val repository = Repository(app.applicationContext)

    fun updateSocialRecords(periodStr: String) {
        val period = when (periodStr) {
            "6 hours"  ->  21_600_000
            "12 hours" ->  43_200_000
            "1 day"    ->  86_400_000
            "2 days"   -> 172_800_000
            else -> throw RuntimeException("$TAG: Unknown period")
        }

        _socialRecords.postValue(repository.getSocialRecords(period))
    }
}
