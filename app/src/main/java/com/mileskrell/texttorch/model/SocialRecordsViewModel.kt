package com.mileskrell.texttorch.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mileskrell.texttorch.repo.Repository

/**
 * An [AndroidViewModel] holding a [MutableLiveData] containing a list of [SocialRecord]
 */
class SocialRecordsViewModel(val app: Application) : AndroidViewModel(app) {

    companion object {
        const val TAG = "SocialRecordViewModel"
    }

    private val _socialRecords: MutableLiveData<List<SocialRecord>> = MutableLiveData()
    val socialRecords = _socialRecords as LiveData<List<SocialRecord>>

    private var period = Period.SIX_HOURS
    var sortType = SortType.MOST_RECENT
    var reversed = false

    private val repository = Repository(app.applicationContext)

    fun initializeSocialRecords() {
        val initialSocialRecords = repository.initializeSocialRecords(period.ms)
        sortAndSetSocialRecords(initialSocialRecords)
    }

    fun changePeriod(period: Period) {
        if (this.period != period) { // If nothing has changed, ignore it
            this.period = period
            sortAndSetSocialRecords(repository.getSocialRecordsFromPeriod(period.ms))
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

    enum class Period(val ms: Int) {
        SIX_HOURS(21_600_000),
        TWELVE_HOURS(43_200_000),
        ONE_DAY(86_400_000),
        TWO_DAYS(172_800_000)
    }

    enum class SortType {
        MOST_RECENT, ALPHA, PEOPLE_YOU_TEXT_FIRST
    }
}
