package com.mileskrell.texttorch.stats.model

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.repo.Repository

/**
 * An [AndroidViewModel] holding a [MutableLiveData] containing a list of [SocialRecord]
 */
class SocialRecordsViewModel(val app: Application) : AndroidViewModel(app) {

    companion object {
        const val TAG = "SocialRecordViewModel"
    }

    private val _socialRecords = MutableLiveData<List<SocialRecord>>()
    val socialRecords = _socialRecords as LiveData<List<SocialRecord>>

    var period = Period.SIX_HOURS
    var sortType = SortType.MOST_RECENT
    var reversed = false
    var showNonContacts = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
        .getBoolean(app.getString(R.string.key_show_non_contacts), false)

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == app.getString(R.string.key_show_non_contacts)) {
            showNonContacts = sharedPreferences.getBoolean(key, false)
            // Let the stats fragments know about the change. Their parent fragments will check the
            // value of showNonContacts when they reload the data.
            _socialRecords.value = _socialRecords.value
        }
    }

    private val repository = Repository(app.applicationContext)

    init {
        PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
            .registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    override fun onCleared() {
        PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
            .unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    fun initializeSocialRecords(threadsTotal: MutableLiveData<Int>, threadsCompleted: MutableLiveData<Int>) {
        val initialSocialRecords = repository.initializeSocialRecords(period.ms, threadsTotal, threadsCompleted)
        sortAndSetSocialRecords(initialSocialRecords)
    }

    fun changePeriod(period: Period) {
        if (this.period != period) { // If nothing has changed, ignore it
            this.period = period
            sortAndSetSocialRecords(repository.getSocialRecordsFromPeriod(period.ms))
        }
    }

    /**
     * These are both changed within one method because both are set in
     * [com.mileskrell.texttorch.stats.SortTypeDialogFragment], and the changes are seen simultaneously
     * when the dialog is dismissed.
     */
    fun changeSortTypeAndReversed(sortType: SortType, reversed: Boolean) {
        if (this.sortType != sortType || this.reversed != reversed) { // If nothing has changed, ignore it
            this.sortType = sortType
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

    enum class Period(val ms: Int, val menuId: Int) {
        SIX_HOURS(21_600_000, R.id.menu_item_period_6_hours),
        TWELVE_HOURS(43_200_000, R.id.menu_item_period_12_hours),
        ONE_DAY(86_400_000, R.id.menu_item_period_1_day),
        TWO_DAYS(172_800_000, R.id.menu_item_period_2_days)
    }

    enum class SortType(val radioButtonId: Int) {
        MOST_RECENT(R.id.radio_button_sort_type_most_recent),
        ALPHA(R.id.radio_button_sort_type_alphabetical),
        NUMBER_OF_CONVERSATIONS(R.id.radio_button_sort_type_number_of_conversations),
        NUMBER_OF_TOTAL_TEXTS(R.id.radio_button_sort_type_number_of_total_texts),
        PEOPLE_YOU_TEXT_FIRST(R.id.radio_button_sort_type_percentage_of_the_time_youve_texted_first),
        PEOPLE_YOU_TEXT_MORE(R.id.radio_button_sort_type_percentage_of_the_texts_youve_sent),
        PEOPLE_YOU_SEND_LONGER_TEXTS(R.id.radio_button_sort_type_percentage_of_total_characters_youve_sent)
    }
}
