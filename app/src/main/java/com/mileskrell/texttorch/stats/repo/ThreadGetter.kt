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

package com.mileskrell.texttorch.stats.repo

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.SystemClock
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.lifecycle.MutableLiveData
import com.mileskrell.texttorch.stats.model.Message
import com.mileskrell.texttorch.stats.model.MessageThread
import com.mileskrell.texttorch.util.logEvent
import com.mileskrell.texttorch.util.logToBoth
import io.sentry.core.Sentry
import io.sentry.core.SentryLevel
import kotlin.math.roundToInt

/**
 * Retrieves threads of conversation, as a List<[MessageThread]>.
 *
 * Big thanks to QKSMS for helping me figure out how to get some of this data!
 * Specifically, the following file was a big help:
 * https://github.com/moezbhatti/qksms/blob/master/data/src/main/java/com/moez/QKSMS/mapper/CursorToMessageImpl.kt
 */
class ThreadGetter(val context: Context) {

    companion object {
        const val TAG = "ThreadGetter"

        val threadsUri: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
        val threadsProjection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.RECIPIENT_IDS
        )

        val singleThreadUri: Uri = Uri.parse("content://mms-sms/complete-conversations")
        val singleThreadProjection = arrayOf(
            Telephony.MmsSms._ID,
            Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
            Telephony.Mms.DATE,

            Telephony.Sms.BODY,
            Telephony.Sms.TYPE, // indicates whether message was sent or received

            Telephony.Mms.MESSAGE_BOX // indicates whether message was sent or received
        )
    }

    fun getThreads(
        threadsTotal: MutableLiveData<Int>,
        threadsCompleted: MutableLiveData<Int>,
        messagesTotal: MutableLiveData<Int>,
        messagesCompleted: MutableLiveData<Int>
    ): List<MessageThread> {
        val startTime = SystemClock.elapsedRealtime()
        /**
         * This might take a while - it seems that different devices require using different content URIs.
         * See https://seap.samsung.com/faq/why-does-sdk-return-nullpointerexception-when-i-access-smsmms-content-uri-0
         *
         * On my Motorola G6:
         * ☒ Telephony.MmsSms.CONTENT_URI | unrecognized URI
         * ☒ Telephony.MmsSms.CONTENT_FILTER_BYPHONE_URI | unrecognized URI
         * ☒ Telephony.MmsSms.CONTENT_CONVERSATIONS_URI | null object reference
         * ☑ content://mms-sms/conversations?simple=true
         * ☑ content://mms-sms/complete-conversations
         *
         * ☒☑
         *
         * TODO: Lots of testing with different devices
         */
        var numFailedNameLookups = 0
        var numSuccessfulNameLookups = 0
        var numMessages = 0

        fun addProgressBreadcrumbs() {
            Sentry.addBreadcrumb("[$TAG] Threads so far: ${threadsCompleted.value}")
            Sentry.addBreadcrumb("[$TAG] Messages so far: $numMessages")
            Sentry.addBreadcrumb("[$TAG] Addresses so far: ${numSuccessfulNameLookups + numFailedNameLookups}")
        }

        val oneRecipientMessageThreads = mutableListOf<MessageThread>()
        context.contentResolver.query(
            threadsUri,
            threadsProjection,
            // Filter out threads with spaces in the "recipients" string,
            // as that indicates multiple recipients
            "${Telephony.Threads.RECIPIENT_IDS} NOT LIKE '% %'",
            null,
            null
        )?.use { threadsCursor ->
            threadsTotal.postValue(threadsCursor.count)

            while (threadsCursor.moveToNext()) {
                val threadId = threadsCursor.getLong(Telephony.Threads._ID)
                if (threadId == null) {
                    addProgressBreadcrumbs()
                    Sentry.captureMessage(
                        "[$TAG] Couldn't get thread ID",
                        SentryLevel.ERROR
                    )
                    // TODO: Maybe display "x threads failed" in AnalyzeFragment? On the other hand,
                    //  that might just frustrate/confuse the user more. So instead, maybe I should
                    //  just add an "if threads are missing, report a bug" message somewhere.
                    threadsCompleted.run { postValue(value!! + 1) }
                    continue
                }

                val recipients = threadsCursor.getString(Telephony.Threads.RECIPIENT_IDS)
                if (recipients == null) {
                    addProgressBreadcrumbs()
                    Sentry.captureMessage(
                        "[$TAG] Couldn't get recipient IDs",
                        SentryLevel.ERROR
                    )
                    threadsCompleted.run { postValue(value!! + 1) }
                    continue
                }

                //////////////////////////////////////////////////////////////// Get name and address
                val addressCursor = context.contentResolver.query(
                    Uri.parse("content://mms-sms/canonical-addresses"),
                    null,
                    "${Telephony.CanonicalAddressesColumns._ID} = $recipients",
                    null,
                    null
                )
                if (addressCursor == null) {
                    addProgressBreadcrumbs()
                    Sentry.captureMessage(
                        "[$TAG] Address cursor is null",
                        SentryLevel.ERROR
                    )
                    threadsCompleted.run { postValue(value!! + 1) }
                    continue
                }
                val address = addressCursor.use {
                    it.moveToFirst()
                    it.getString(Telephony.CanonicalAddressesColumns.ADDRESS)
                }
                if (address == null) {
                    addProgressBreadcrumbs()
                    Sentry.captureMessage(
                        "[$TAG] Couldn't get other party's address",
                        SentryLevel.ERROR
                    )
                    threadsCompleted.run { postValue(value!! + 1) }
                    continue
                }

                if (address.isEmpty()) {
                    // I experienced this at one point; it makes the name lookup throw an
                    // IllegalArgumentException. Unfortunately, with no address or name to identify
                    // the person by, we can't really show this to the user.
                    addProgressBreadcrumbs()
                    logToBoth(TAG, "Other party's address is empty", SentryLevel.ERROR)
                    threadsCompleted.run { postValue(value!! + 1) }
                    continue
                }
                val name = getNameFromAddress(address)
                if (name != null) {
                    numSuccessfulNameLookups++
                } else {
                    numFailedNameLookups++
                }

                //////////////////////////////////////////////////////////////// Get messages
                val messages = mutableListOf<Message>()

                context.contentResolver.query(
                    singleThreadUri,
                    singleThreadProjection,
                    "${Telephony.Mms.THREAD_ID} == $threadId",
                    null,
                    Telephony.Sms.DATE
                )?.use { messagesCursor ->
                    messagesTotal.postValue(messagesCursor.count)
                    messagesCompleted.postValue(0)

                    messagesCursorLoop@ while (messagesCursor.moveToNext()) {
                        val messageId = messagesCursor.getLong(Telephony.MmsSms._ID)
                        if (messageId == null) {
                            addProgressBreadcrumbs()
                            Sentry.captureMessage(
                                "[$TAG] Couldn't get message ID",
                                SentryLevel.ERROR
                            )
                            messagesCompleted.run { postValue(messagesCursor.position + 1) }
                            continue
                        }

                        var date = messagesCursor.getLong(Telephony.Sms.DATE)
                        if (date == null) {
                            addProgressBreadcrumbs()
                            Sentry.captureMessage(
                                "[$TAG] Couldn't get message date",
                                SentryLevel.ERROR
                            )
                            messagesCompleted.run { postValue(messagesCursor.position + 1) }
                            continue
                        }
                        val sentByUser: Boolean
                        var body: String? = null

                        val messageType =
                            messagesCursor.getString(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN)
                        when (messageType) {
                            "sms" -> {
                                val type = messagesCursor.getInt(Telephony.Sms.TYPE)
                                sentByUser =
                                    type != Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX

                                body = messagesCursor.getString(Telephony.Sms.BODY)
                                if (body == null) {
                                    addProgressBreadcrumbs()
                                    Sentry.captureMessage(
                                        "[$TAG] Couldn't get SMS body",
                                        SentryLevel.ERROR
                                    )
                                }
                            }
                            "mms" -> {
                                date *= 1000L

                                val type = messagesCursor.getInt(Telephony.Mms.MESSAGE_BOX)
                                sentByUser = type != Telephony.Mms.MESSAGE_BOX_INBOX

                                context.contentResolver.query(
                                    Uri.parse("content://mms/part"), null,
                                    "${Telephony.Mms.Part.MSG_ID} = $messageId", null, null
                                )?.use { partsCursor ->
                                    while (partsCursor.moveToNext()) {
                                        val contentType =
                                            partsCursor.getString(Telephony.Mms.Part.CONTENT_TYPE)
                                        if (contentType == null) {
                                            addProgressBreadcrumbs()
                                            Sentry.captureMessage(
                                                "[$TAG] Couldn't get content type for MMS part",
                                                SentryLevel.ERROR
                                            )
                                        } else if (contentType == "text/plain") {
                                            body = partsCursor.getString(Telephony.Mms.Part.TEXT)
                                            if (body == null) {
                                                addProgressBreadcrumbs()
                                                Sentry.captureMessage(
                                                    "[$TAG] Couldn't get text for MMS part, even though its content type is \"text/plain\"",
                                                    SentryLevel.ERROR
                                                )
                                            }
                                            break
                                        }
                                    }
                                } ?: run {
                                    addProgressBreadcrumbs()
                                    Sentry.captureMessage(
                                        "[$TAG] MMS parts cursor is null",
                                        SentryLevel.ERROR
                                    )
                                }
                            }
                            else -> {
                                addProgressBreadcrumbs()
                                Sentry.captureMessage(
                                    "[$TAG] Unknown message type $messageType",
                                    SentryLevel.ERROR
                                )
                                messagesCompleted.run { postValue(messagesCursor.position + 1) }
                                continue@messagesCursorLoop
                            }
                        }

                        messages.add(Message(messageType, sentByUser, date, body))
                        numMessages++
                        messagesCompleted.run { postValue(messagesCursor.position + 1) }
                    }
                } ?: run {
                    addProgressBreadcrumbs()
                    Sentry.captureMessage(
                        "[$TAG] Messages cursor is null",
                        SentryLevel.ERROR
                    )
                }

                threadsCompleted.run { postValue(value!! + 1) }

                oneRecipientMessageThreads.add(MessageThread(address, name, messages))
            }
        } ?: Sentry.captureMessage(
            "[$TAG] Threads cursor is null; unable to view anything",
            SentryLevel.FATAL
        )

        val nonEmptyMessageThreads = oneRecipientMessageThreads.filter { messageThread ->
            messageThread.messages.isNotEmpty()
            // So there will probably be fewer threads returned than we had told the user
            // there would be, but whatever
        }

        // Lastly, we'll set a flag in each MessageThread indicating whether there are multiple
        // threads sharing the same name
        nonEmptyMessageThreads.filter { it.otherPartyName != null }
            .groupBy { it.otherPartyName }.map { it.value }.forEach { threadList ->
                // If there's more than one thread with this name...
                if (threadList.size > 1) {
                    // In each thread with this name, mark the name as non-unique
                    threadList.forEach {
                        it.nonUniqueName = true
                    }
                }
            }

        val numAddresses = numSuccessfulNameLookups + numFailedNameLookups
        val duration = SystemClock.elapsedRealtime() - startTime
        logEvent(
            TAG,
            "getThreads() finished",
            SentryLevel.INFO,
            true,
            mapOf(
                "duration (ms)" to duration,
                "number of messages" to numMessages,
                "number of addresses" to numAddresses,
                "number of successful name lookups" to numSuccessfulNameLookups,
                "number of failed name lookups" to numFailedNameLookups,
                "percentage of successful name lookups" to (100.0 * numSuccessfulNameLookups / numAddresses).roundToInt(),
                "percentage of failed name lookups" to (100.0 * numFailedNameLookups / numAddresses).roundToInt()
            )
        )
        return nonEmptyMessageThreads
    }

    private fun getNameFromAddress(address: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(address)
        )
        context.contentResolver.query(
            uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY), null, null, null
        )?.use { nameLookupCursor ->
            return if (nameLookupCursor.count > 0) {
                nameLookupCursor.moveToFirst()
                nameLookupCursor.getString(0)
            } else null
        } ?: Sentry.captureMessage("[$TAG] Name lookup cursor is null", SentryLevel.ERROR)
        return null
    }

    private fun Cursor.getString(name: String): String? {
        return try {
            getString(getColumnIndex(name))
        } catch (e: IllegalStateException) {
            null
        }
    }

    private fun Cursor.getInt(name: String): Int? {
        return try {
            getInt(getColumnIndex(name))
        } catch (e: IllegalStateException) {
            null
        }
    }

    private fun Cursor.getLong(name: String): Long? {
        return try {
            getLong(getColumnIndex(name))
        } catch (e: IllegalStateException) {
            null
        }
    }
}
