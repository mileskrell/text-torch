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

    private var period: Int = 21_600_000 // 6 hours by default
    private var sortType = SortType.MOST_RECENT
    private var reversed = false

    private val repository = Repository(app.applicationContext)

    fun initializeSocialRecords() {
        val initialSocialRecords = repository.initializeSocialRecords(period)
        sortAndSetSocialRecords(initialSocialRecords)
    }

    fun changePeriod(periodStr: String) {
        val period = when (periodStr) {
            "6 hours"  ->  21_600_000
            "12 hours" ->  43_200_000
            "1 day"    ->  86_400_000
            "2 days"   -> 172_800_000
            else -> throw RuntimeException("$TAG: Unknown period")
        }
        if (this.period != period) { // If nothing has changed, ignore it
            this.period = period
            _socialRecords.postValue(repository.getSocialRecordsFromPeriod(period))
        }
    }

    fun changeSortType(sortType: SortType) {
        if (this.sortType != sortType) { // If nothing has changed, ignore it
            this.sortType = sortType
            sortAndSetSocialRecords(socialRecords.value ?: return)
        }
    }

    fun changeReversed(reversed: Boolean) {
        if (this.reversed != reversed) { // If nothing has changed, ignore it
            this.reversed = reversed
            sortAndSetSocialRecords(socialRecords.value ?: return)
        }
    }

    private fun sortAndSetSocialRecords(records: List<SocialRecord>) {
        val sortedSocialRecords = repository.sortSocialRecords(
            records,
            sortType,
            reversed
        )
        _socialRecords.postValue(sortedSocialRecords)
    }

    enum class SortType {
        MOST_RECENT, ALPHA, WHO_TEXTS_FIRST
    }
}
