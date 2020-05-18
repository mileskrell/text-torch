package com.mileskrell.texttorch.stats.repo

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mileskrell.texttorch.stats.model.Message
import com.mileskrell.texttorch.stats.model.MessageThread
import ly.count.android.sdk.Countly

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

    var numEmptyAddresses = 0
    var numFailedNameLookups = 0
    var numSuccessfulNameLookups = 0
    var numMessages = 0

    fun getThreads(
        threadsTotal: MutableLiveData<Int>,
        threadsCompleted: MutableLiveData<Int>
    ): List<MessageThread> {
        Countly.sharedInstance().events().startEvent("getThreads()")
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

        val oneRecipientMessageThreads = mutableListOf<MessageThread>()
        val threadsCursor = context.contentResolver.query(
            threadsUri,
            threadsProjection,
            // Filter out threads with spaces in the "recipients" string,
            // as that indicates multiple recipients
            "${Telephony.Threads.RECIPIENT_IDS} NOT LIKE '% %'",
            null,
            null
        )
            ?: throw RuntimeException("ThreadGetter: threadsCursor is null")

        threadsTotal.postValue(threadsCursor.count)

        while (threadsCursor.moveToNext()) {
            val threadId = threadsCursor.getLong(Telephony.Threads._ID)
                ?: throw RuntimeException("Couldn't get thread ID")
            val recipients = threadsCursor.getString(Telephony.Threads.RECIPIENT_IDS)
                ?: throw RuntimeException("Couldn't get recipients for thread $threadId")

            //////////////////////////////////////////////////////////////// Get name and address
            val addressCursor = context.contentResolver.query(
                Uri.parse("content://mms-sms/canonical-addresses"),
                null,
                "${Telephony.CanonicalAddressesColumns._ID} = $recipients",
                null,
                null
            )
                ?: throw RuntimeException("Address cursor was null when trying to get other party address for thread $threadId")
            addressCursor.moveToFirst()
            val address = addressCursor.getString(Telephony.CanonicalAddressesColumns.ADDRESS)
                ?: throw RuntimeException("Could not get address for recipient $recipients in thread $threadId")
            addressCursor.close()

            if (address.isEmpty()) {
                // I experienced this at one point; it makes the name lookup throw an
                // IllegalArgumentException. Unfortunately, with no address or name to identify the
                // person by, we can't really show this to the user.
                numEmptyAddresses++
                Log.d(TAG, "Skipping empty address")
                threadsCompleted.run {
                    postValue(1 + value!!)
                }
                continue
            }
            val name = getNameFromAddress(address)

            //////////////////////////////////////////////////////////////// Get messages
            val messages = mutableListOf<Message>()

            val messagesCursor = context.contentResolver.query(
                singleThreadUri,
                singleThreadProjection,
                "${Telephony.Mms.THREAD_ID} == $threadId",
                null,
                Telephony.Sms.DATE
            ) ?: throw RuntimeException("ThreadGetter: messagesCursor is null")

            while (messagesCursor.moveToNext()) {
                val messageId = messagesCursor.getLong(Telephony.MmsSms._ID)
                    ?: throw RuntimeException("Message ID is null")

                val messageType =
                    messagesCursor.getString(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN)
                val sentByUser: Boolean
                var date = messagesCursor.getLong(Telephony.Mms.DATE)
                    ?: throw RuntimeException("Date is null for message $messageId in thread $threadId")
                var body: String? = null

                when (messageType) {
                    "sms" -> {
                        val type = messagesCursor.getInt(Telephony.Sms.TYPE)
                        sentByUser = type != Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX

                        body = messagesCursor.getString(Telephony.Sms.BODY)
                            ?: throw RuntimeException("Null body for message $messageId in thread $threadId")
                    }
                    "mms" -> {
                        date *= 1000L

                        val type = messagesCursor.getInt(Telephony.Mms.MESSAGE_BOX)
                        sentByUser = type != Telephony.Mms.MESSAGE_BOX_INBOX

                        val partsCursor = context.contentResolver.query(
                            Uri.parse("content://mms/part"), null,
                            "${Telephony.Mms.Part.MSG_ID} = $messageId", null, null
                        )
                            ?: throw RuntimeException("MMS parts cursor null for thread $threadId")

                        while (partsCursor.moveToNext()) {
                            val contentType = partsCursor.getString(Telephony.Mms.Part.CONTENT_TYPE)
                                ?: throw RuntimeException("Could not get content type for message $messageId in thread $threadId")
                            if (contentType == "text/plain") {
                                body = partsCursor.getString(Telephony.Mms.Part.TEXT)
                                if (body == null) {
                                    Countly.sharedInstance().crashes().recordHandledException(
                                        RuntimeException("MMS part text is null, even though content type is \"text/plain\"")
                                    )
                                }
                                break
                            }
                        }
                        partsCursor.close()
                    }
                    else -> {
                        throw RuntimeException("Unknown message type $messageType")
                    }
                }

                messages.add(Message(messageType, sentByUser, date, body))
                numMessages++
            }

            messagesCursor.close()
            threadsCompleted.run {
                postValue(1 + value!!)
            }

            oneRecipientMessageThreads.add(MessageThread(address, name, messages))
        }
        threadsCursor.close()

        val nonEmptyMessageThreads = oneRecipientMessageThreads.filter { messageThread ->
            messageThread.messages.isNotEmpty()
            // So there will probably be fewer threads returned than we had told the user
            // there would be, but whatever
        }

        // Lastly, we'll set a flag in each MessageThread indicating whether there are multiple
        // threads sharing the same name
        nonEmptyMessageThreads.filter { it.otherPartyName != null }
            .groupBy { it.otherPartyName }.forEach {
                // If there's more than one thread with this name...
                if (it.value.size > 1) {
                    // In each thread with this name, mark the name as non-unique
                    it.value.forEach {
                        it.nonUniqueName = true
                    }
                }
            }

        val numAddresses = numSuccessfulNameLookups + numFailedNameLookups + numEmptyAddresses
        Countly.sharedInstance().events().endEvent(
            "getThreads()",
            mapOf(
                "total number of messages" to numMessages,
                "total number of addresses" to numAddresses,
                "number of successful name lookups" to numSuccessfulNameLookups,
                "number of failed name lookups" to numFailedNameLookups,
                "number of empty addresses" to numEmptyAddresses,
                "percentage successful name lookups" to 100.0 * numSuccessfulNameLookups / numAddresses,
                "percentage failed name lookups" to 100.0 * numFailedNameLookups / numAddresses,
                "percentage empty addresses" to 100.0 * numEmptyAddresses / numAddresses
            ),
            1,
            0.0
        )
        return nonEmptyMessageThreads
    }

    private fun getNameFromAddress(address: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(address)
        )
        val phoneLookupCursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY),
            null,
            null,
            null
        )
        val name = if (phoneLookupCursor != null && phoneLookupCursor.count > 0) {
            numSuccessfulNameLookups++
            phoneLookupCursor.moveToFirst()
            phoneLookupCursor.getString(0)
        } else {
            numFailedNameLookups++
            Log.d(TAG, "Name lookup failed for address $address")
            null
        }
        phoneLookupCursor?.close()
        return name
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
