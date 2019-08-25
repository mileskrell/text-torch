package com.mileskrell.texttorch.stats.repo

import android.content.Context
import com.mileskrell.texttorch.analyze.AnalyzeViewModel
import com.mileskrell.texttorch.stats.model.MessageThread
import com.mileskrell.texttorch.stats.model.SocialRecord
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
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

    lateinit var threads: List<MessageThread>

    /**
     * Returns a list of [SocialRecord], based on the provided [period]
     *
     * @param period: Number of milliseconds of silence required until the next conversation has officially started
     */
    fun initializeSocialRecords(period: Int, analyzeViewModel: AnalyzeViewModel): List<SocialRecord> {
        if (!::threads.isInitialized) {
            threads = ThreadGetter(context).getThreads(analyzeViewModel)
        }

        return getSocialRecordsFromPeriod(period)
    }

    fun getSocialRecordsFromPeriod(period: Int): List<SocialRecord> {
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

    fun sortSocialRecords(socialRecords: List<SocialRecord>, sortType: SocialRecordsViewModel.SortType, reversed: Boolean = false): List<SocialRecord> {
        return when (sortType) {
            SocialRecordsViewModel.SortType.MOST_RECENT -> {
                if (reversed) {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.mostRecentMessageDate
                    }
                } else {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.mostRecentMessageDate
                    }
                }
            }
            SocialRecordsViewModel.SortType.ALPHA -> {
                if (reversed) {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.correspondentName.toLowerCase()
                    }
                } else {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.correspondentName.toLowerCase()
                    }
                }
            }
            SocialRecordsViewModel.SortType.NUMBER_OF_CONVERSATIONS -> {
                if (reversed) {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.numConversations
                    }
                } else {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.numConversations
                    }
                }
            }
            SocialRecordsViewModel.SortType.NUMBER_OF_TOTAL_TEXTS -> {
                if (reversed) {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.numTexts
                    }
                } else {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.numTexts
                    }
                }
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_TEXT_FIRST -> {
                if (reversed) {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.correspondentInitPercent
                    }
                } else {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.correspondentInitPercent
                    }
                }
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_TEXT_MORE -> {
                if (reversed) {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.correspondentTextPercent
                    }
                } else {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.correspondentTextPercent
                    }
                }
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_SEND_LONGER_TEXTS -> {
                if (reversed) {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.correspondentAvgCharsPercent
                    }
                } else {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.correspondentAvgCharsPercent
                    }
                }
            }
        }
    }
}
