package com.mileskrell.texttorch.stats.repo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.mileskrell.texttorch.stats.model.MessageThread
import com.mileskrell.texttorch.stats.model.SocialRecord
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The core functionality of the app, really.
 *
 * First, it uses [ThreadGetter] to fetch the user's texting history.
 *
 * Then, it computes a list of [SocialRecord] to be displayed to the user.
 */
class Repository(val context: Context) {

    companion object {
        const val TAG = "Repository"
    }

    private val getNameLowerCase = { socialRecord: SocialRecord ->
        socialRecord.correspondentName.toLowerCase(Locale.getDefault())
    }

    lateinit var threads: List<MessageThread>

    /**
     * Returns a list of [SocialRecord], based on the provided [period]
     *
     * @param period: Number of milliseconds of silence required until the next conversation has officially started
     */
    fun initializeSocialRecords(period: Int, threadsTotal: MutableLiveData<Int>, threadsCompleted: MutableLiveData<Int>): List<SocialRecord> {
        if (!::threads.isInitialized) {
            threads = ThreadGetter(context).getThreads(threadsTotal, threadsCompleted)
        }

        return getSocialRecordsFromPeriod(period)
    }

    fun getSocialRecordsFromPeriod(period: Int): List<SocialRecord> {
        // TODO: I think this is the method where I should identify duplicate names and set some boolean flag on them that will make them display their addresses
        val socialRecords = mutableListOf<SocialRecord>()

        threads.forEach { thread ->
            val theirName = thread.otherPartyName ?: thread.otherPartyAddress
            var ownInits = 0
            var theirInits = 0
            var ownTexts = 0
            var theirTexts = 0
            var ownTextsWithBody = 0
            var theirTextsWithBody = 0
            var ownTotalChars = 0
            var theirTotalChars = 0

            val firstMessageBody = thread.messages[0].body

            if (thread.messages[0].sentByUser) {
                ownInits++
                ownTexts++
                if (firstMessageBody != null) {
                    ownTextsWithBody++
                    ownTotalChars += firstMessageBody.length
                }
            } else {
                theirInits++
                theirTexts++
                if (firstMessageBody != null) {
                    theirTextsWithBody++
                    theirTotalChars += firstMessageBody.length
                }
            }
            var latestTime = thread.messages[0].date

            thread.messages.drop(1).forEach { message ->
                if (message.sentByUser) {
                    ownTexts++
                    if (message.body != null) {
                        ownTextsWithBody++
                        ownTotalChars += message.body.length
                    }
                    if (message.date - latestTime > period) {
                        ownInits++
                    }
                } else {
                    theirTexts++
                    if (message.body != null) {
                        theirTextsWithBody++
                        theirTotalChars += message.body.length
                    }
                    if (message.date - latestTime > period) {
                        theirInits++
                    }
                }
                latestTime = message.date
            }

            val ownAvgChars = if (ownTextsWithBody == 0) {
                0
            } else {
                (1.0 * ownTotalChars / ownTextsWithBody).roundToInt()
            }

            val theirAvgChars = if (theirTextsWithBody == 0) {
                0
            } else {
                (1.0 * theirTotalChars / theirTextsWithBody).roundToInt()
            }

            socialRecords.add(SocialRecord(
                theirName,
                thread.messages.last().date,
                ownInits,
                theirInits,
                ownTexts,
                theirTexts,
                ownAvgChars,
                theirAvgChars
            ))
        }

        return socialRecords
    }

    /*
    Notes on sorting:
    1. "Reversed" only reverses the primary level of sorting; further levels remain unchanged.
        For example, when using alphabetical as a secondary sort, we always sort A-Z, never Z-A.
    2. In theory, after sorting by name (case-insensitive), we could sort by name (case-sensitive).
       But that would only matter if there were multiple people with the same name, which is rather
       unlikely, so I don't think we need to bother.
     */
    fun sortSocialRecords(socialRecords: List<SocialRecord>, sortType: SocialRecordsViewModel.SortType, reversed: Boolean = false): List<SocialRecord> {
        return when (sortType) {
            SocialRecordsViewModel.SortType.MOST_RECENT -> {
                // These dates are ms since epoch, so it's almost impossible that any two will be
                // equal. Because of that, we don't bother sorting further.
                if (reversed) {
                    socialRecords.sortedBy { it.mostRecentMessageDate }
                } else {
                    socialRecords.sortedByDescending { it.mostRecentMessageDate }
                }
            }
            SocialRecordsViewModel.SortType.ALPHA -> {
                if (reversed) {
                    socialRecords.sortedByDescending(getNameLowerCase)
                } else {
                    socialRecords.sortedBy(getNameLowerCase)
                } // TODO: Then by address
            }
            SocialRecordsViewModel.SortType.NUMBER_OF_CONVERSATIONS -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareBy<SocialRecord> { it.numConversations }
                    } else {
                        compareByDescending { it.numConversations }
                    }.thenBy(getNameLowerCase) // TODO: Then by address
                )
            }
            SocialRecordsViewModel.SortType.NUMBER_OF_TOTAL_TEXTS -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareBy<SocialRecord> { it.numTexts }
                    } else {
                        compareByDescending { it.numTexts }
                    }.thenBy(getNameLowerCase) // TODO: Then by address
                )
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_TEXT_FIRST -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareByDescending<SocialRecord> { it.correspondentInitPercent }
                    } else {
                        compareBy { it.correspondentInitPercent }
                    }.thenBy(getNameLowerCase) // TODO: Then by address
                )
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_TEXT_MORE -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareByDescending<SocialRecord> { it.correspondentTextPercent }
                    } else {
                        compareBy { it.correspondentTextPercent }
                    }.thenBy(getNameLowerCase) // TODO: Then by address
                )
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_SEND_LONGER_TEXTS -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareByDescending<SocialRecord> { it.correspondentAvgCharsPercent }
                    } else {
                        compareBy { it.correspondentAvgCharsPercent }
                    }.thenBy(getNameLowerCase) // TODO: Then by address
                )
            }
        }
    }
}
