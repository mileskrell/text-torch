package com.mileskrell.texttorch.repo

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import com.mileskrell.texttorch.model.Message

/**
 * Retrieves threads of conversation, as a List<List<[Message]>>
 *
 * TODO: Get MMS messages too. Ignore group MMS by checking if there's more than one ADDRESS within a single thread.
 */
class ThreadGetter(val context: Context) {

    companion object {
        const val TAG = "ThreadGetter"

        val MSG_COLUMNS = arrayOf(Telephony.Sms.THREAD_ID, Telephony.Sms.TYPE, Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.PERSON, Telephony.Sms.BODY)

        const val THREAD  = 0

        /**
         * If this equals Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX, the message was received
         */
        const val TYPE    = 1

        /**
         * The other party's address
         */
        const val ADDRESS = 2

        const val DATE    = 3

        /**
         * If not null, it means the sender is saved as a contact, and we can find their name
         */
        const val PERSON  = 4

        const val BODY    = 5
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getThreads(): List<List<Message>> {
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

        val threads = mutableMapOf<Int, MutableList<Message>>()

        val addressToNameCache = mutableMapOf<String, String>()
        val nameLookupFailures = mutableMapOf<Int, Int>()

        val messagesCursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, MSG_COLUMNS, null, null, "${Telephony.Sms.THREAD_ID}, ${Telephony.Sms.DATE}")
            ?: throw RuntimeException("ThreadGetter: messagesCursor is null")
//        PrintStream(FileOutputStream(File("/sdcard", "cursor_dump.txt"))).use {
//            DatabaseUtils.dumpCursor(messagesCursor, it)
//        }

        messagesCursor.moveToFirst()

        for (i in 0 until messagesCursor.count/*.coerceAtMost(14)*/) {
            // TODO Remove this lookup before actual release
            val contactId = try {
                messagesCursor.getInt(PERSON)
            } catch (e: IllegalStateException) {
                null
            }
//            if (contactId == 0) {
//                // These are messages I sent
//                Log.d(TAG, "From me:")
//                DatabaseUtils.dumpCurrentRow(messagesCursor)
//            }

//            if (contactId == -1) {
//                // These are messages I've received from non-contacts
//                Log.d(TAG, "From non-contact:")
//                DatabaseUtils.dumpCurrentRow(messagesCursor)
//            }

            val otherPartyName: String?
            val otherPartyAddress = messagesCursor.getString(ADDRESS) ?: throw RuntimeException("Other party's address is null")

            if (contactId == null) {
                // In the real version, I guess we'll just log this and move on...
                throw RuntimeException("Couldn't get person ID!")
            } else {
                // If name is cached, use it
                if (otherPartyAddress in addressToNameCache) {
                    otherPartyName = addressToNameCache[otherPartyAddress] ?: throw RuntimeException("addressToNameCache contains address $otherPartyAddress, but it's somehow null")
                } else {
                    // If that doesn't work, try to get their name from their number
                    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(otherPartyAddress))
                    val phoneLookupCursor = context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY), null, null, null)

                    if (phoneLookupCursor!= null && phoneLookupCursor.count > 0) {
                        phoneLookupCursor.moveToFirst()
                        otherPartyName = phoneLookupCursor.getString(0)
                        addressToNameCache[otherPartyAddress] = otherPartyName
                    } else {
                        // They must not be saved as a contact at all.
                        nameLookupFailures[contactId] = 1 + (nameLookupFailures[contactId] ?: 0)
                        otherPartyName = null
                    }
                    phoneLookupCursor?.close()
                }
            }

            val senderName: String?
            val senderAddress: String?
            val recipientName: String?
            val recipientAddress: String?

            if (messagesCursor.getInt(TYPE) == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX) {
                // We received this message, so the other party's name is the sender's name
                senderName = otherPartyName
                senderAddress = otherPartyAddress
                recipientName = null
                recipientAddress = null
            } else {
                // We sent this message, and there's no point trying to get our own name
                senderName = null
                senderAddress = null
                recipientName = otherPartyName
                recipientAddress = otherPartyAddress
            }

            val date = messagesCursor.getLong(DATE)
            val body = messagesCursor.getString(BODY)
            val threadId = messagesCursor.getInt(THREAD)

            if (threadId !in threads) {
                threads[threadId] = mutableListOf()
            }
            threads[threadId]?.add(Message(threadId, date, senderName, senderAddress, recipientName, recipientAddress, body))
            messagesCursor.moveToNext()
        }

        Log.d(TAG, "Got ${messagesCursor.count} messages")

        Log.d(TAG, "---\nName lookup failed for the following:")
        nameLookupFailures.forEach {
            Log.d(TAG, "ID was ${it.key} in ${it.value} messages")
        }

        messagesCursor.close()

        // TODO Remove this check before actual release
        for (thread in threads) {
            val names = mutableSetOf<String?>()
            thread.value.forEach { message ->
                names.add(message.senderName)
                names.add(message.recipientName)
            }
            if (names.size > 2) {
                throw RuntimeException("$TAG: More than 2 names in a thread: $names")
            }
        }

        // Return the threads, sorted like in a normal SMS app.

        // First make sure each thread is sorted by date,
        // then sort the whole list of threads by the date of the last message.

        return threads.values.run {
            forEach { thread ->
                // TODO Is this part needed? Can we change one of our queries above so they're already sorted this way?
                thread.sortBy { message ->
                    message.date
                }
            }
            sortedByDescending { thread ->
                thread.last().date
            }
        }
    }
}
