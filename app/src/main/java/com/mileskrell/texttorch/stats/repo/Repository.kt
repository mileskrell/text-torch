package com.mileskrell.texttorch.stats.repo

import android.content.Context
import com.mileskrell.texttorch.stats.model.Message
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

    lateinit var threads: List<List<Message>>

    /**
     * Returns a list of [SocialRecord], based on the provided [period]
     *
     * @param period: Number of milliseconds of silence required until the next conversation has officially started
     */
    fun initializeSocialRecords(period: Int): List<SocialRecord> {
        if (!::threads.isInitialized) {
            threads = ThreadGetter(context).getThreads()
        }

        return getSocialRecordsFromPeriod(period)
    }

    fun getSocialRecordsFromPeriod(period: Int): List<SocialRecord> {
        val socialRecords = mutableListOf<SocialRecord>()

        threads.forEach { thread ->
            val theirName = thread[0].senderName
                ?: thread[0].recipientName
                ?: thread[0].senderAddress
                ?: thread[0].recipientAddress
                ?: throw RuntimeException("$TAG: Couldn't determine other person's name OR address")
            var ownInits = 0
            var theirInits = 0

            if (thread[0].senderAddress == null) {
                ownInits++
            } else {
                theirInits++
            }
            var latestTime = thread[0].date

            thread.drop(1).forEach { message ->
                if (message.date - latestTime > period) {
                    if (message.senderAddress == null) {
                        ownInits++
                    } else {
                        theirInits++
                    }
                }
                latestTime = message.date
            }

            val theirPercent = 100.0 * theirInits / (theirInits + ownInits)
            socialRecords.add(SocialRecord(theirName,
                theirPercent.roundToInt(),
                ownInits + theirInits,
                thread.last().date)
            )
        }

        return socialRecords
    }

    fun sortSocialRecords(socialRecords: List<SocialRecord>, sortType: SocialRecordsViewModel.SortType, reversed: Boolean = false): List<SocialRecord> {
        return when (sortType) {
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
            SocialRecordsViewModel.SortType.PEOPLE_YOU_TEXT_FIRST -> {
                if (reversed) {
                    socialRecords.sortedByDescending { socialRecord ->
                        socialRecord.correspondentPercent
                    }
                } else {
                    socialRecords.sortedBy { socialRecord ->
                        socialRecord.correspondentPercent
                    }
                }
            }
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
        }
    }
}
