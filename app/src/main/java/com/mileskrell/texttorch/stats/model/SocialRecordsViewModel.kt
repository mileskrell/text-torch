/*
 * Copyright (C) 2020 Miles Krell and the Text Torch contributors
 *
 * This file is part of Text Torch.
 *
 * Text Torch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Text Torch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Text Torch.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mileskrell.texttorch.stats.model

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.repo.Repository
import com.mileskrell.texttorch.util.logToBoth
import io.sentry.SentryLevel

/**
 * An [AndroidViewModel] holding a [MutableLiveData] containing a list of [SocialRecord]
 */
class SocialRecordsViewModel(val app: Application) : AndroidViewModel(app) {

    private val _socialRecords = MutableLiveData<List<SocialRecord>>()
    val socialRecords = _socialRecords as LiveData<List<SocialRecord>>

    var period = Period.SIX_HOURS
    var sortType = SortType.MOST_RECENT
    var reversed = false
    var showNonContacts = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
        .getBoolean(app.getString(R.string.key_show_non_contacts), false)

    private val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == app.getString(R.string.key_show_non_contacts)) {
                showNonContacts = sharedPreferences.getBoolean(key, false)
                logToBoth(TAG, "Set show/hide non-contacts setting to $showNonContacts")
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

    fun initializeSocialRecords(
        threadsTotal: MutableLiveData<Int>,
        threadsCompleted: MutableLiveData<Int>,
        messagesTotal: MutableLiveData<Int>,
        messagesCompleted: MutableLiveData<Int>
    ) {
        val initialSocialRecords = repository.initializeSocialRecords(
            period.ms, threadsTotal, threadsCompleted, messagesTotal, messagesCompleted
        )
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
            sortAndSetSocialRecords(socialRecords.value ?: run {
                logToBoth(TAG, "SocialRecords.value is null", SentryLevel.ERROR)
                return
            })
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
        ALPHABETICAL(R.id.radio_button_sort_type_alphabetical),
        NUMBER_OF_CONVERSATIONS(R.id.radio_button_sort_type_number_of_conversations),
        NUMBER_OF_TOTAL_TEXTS(R.id.radio_button_sort_type_number_of_total_texts),
        PEOPLE_YOU_TEXT_FIRST(R.id.radio_button_sort_type_percentage_of_the_time_youve_texted_first),
        PEOPLE_YOU_TEXT_MORE(R.id.radio_button_sort_type_percentage_of_the_texts_youve_sent),
        PEOPLE_YOU_SEND_LONGER_TEXTS(R.id.radio_button_sort_type_percentage_of_total_characters_youve_sent)
    }

    companion object {
        const val TAG = "SocialRecordViewModel"
    }
}
