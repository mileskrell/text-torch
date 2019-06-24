package com.mileskrell.whotextsfirst.repo

import android.content.Context
import com.mileskrell.whotextsfirst.model.SocialRecord
import kotlin.math.roundToInt

/**
 * The core functionality of the app, really.
 *
 * First, it uses [ThreadGetter] to fetch the user's texting history.
 *
 * Then, it computes a list of [SocialRecord] to be displayed to the user.
 *
 * TODO: Allow sorting the returned list 3 ways:
 *  - Most recently texted (like in an SMS app). This is what we're doing now, and is a good default.
 *  - Alphabetical
 *  - From initiates conversation least to most (or most to least)
 *
 */
class Repository(val context: Context) {

    private val TAG = "Repository"

    /**
     * Returns a list of [SocialRecord], based on the provided [period]
     *
     * @param period: Number of milliseconds of silence required until the next conversation has officially started
     */
    fun getSocialRecords(period: Int): List<SocialRecord> {
        val threads = ThreadGetter(context).getThreads()

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
            socialRecords.add(SocialRecord(theirName, theirPercent.roundToInt(), ownInits + theirInits))
        }

        return socialRecords
    }
}
