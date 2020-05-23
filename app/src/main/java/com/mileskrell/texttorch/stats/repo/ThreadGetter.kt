package com.mileskrell.texttorch.stats.repo

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
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
        var numFailedNameLookups = 0
        var numSuccessfulNameLookups = 0
        var numMessages = 0

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
                    Countly.sharedInstance().crashes().recordHandledException(
                        RuntimeException("Couldn't get thread ID")
                    )
                    continue
                }

                val recipients = threadsCursor.getString(Telephony.Threads.RECIPIENT_IDS)
                if (recipients == null) {
                    Countly.sharedInstance().crashes().recordHandledException(
                        RuntimeException("Couldn't get recipients for thread $threadId")
                    )
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
                    Countly.sharedInstance().crashes().recordHandledException(
                        RuntimeException("Address cursor is null for thread $threadId")
                    )
                    continue
                }
                val address = addressCursor.use {
                    it.moveToFirst()
                    it.getString(Telephony.CanonicalAddressesColumns.ADDRESS)
                }
                if (address == null) {
                    Countly.sharedInstance().crashes().recordHandledException(
                        RuntimeException("Other party's address is null for thread $threadId")
                    )
                    continue
                }

                if (address.isEmpty()) {
                    // I experienced this at one point; it makes the name lookup throw an
                    // IllegalArgumentException. Unfortunately, with no address or name to identify
                    // the person by, we can't really show this to the user.
                    Countly.sharedInstance().crashes().recordHandledException(
                        RuntimeException("Other party's address is empty for thread $threadId")
                    )
                    threadsCompleted.run {
                        postValue(1 + value!!)
                    }
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
                    messagesCursorLoop@ while (messagesCursor.moveToNext()) {
                        val messageId = messagesCursor.getLong(Telephony.MmsSms._ID)
                        if (messageId == null) {
                            Countly.sharedInstance().crashes().recordHandledException(
                                RuntimeException("Message ID is null for thread $threadId")
                            )
                            continue
                        }

                        var date = messagesCursor.getLong(Telephony.Sms.DATE)
                        if (date == null) {
                            Countly.sharedInstance().crashes().recordHandledException(
                                RuntimeException("Date is null for message $messageId in thread $threadId")
                            )
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
                                    Countly.sharedInstance().crashes().recordHandledException(
                                        RuntimeException("Null body for SMS with message ID $messageId in thread $threadId")
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
                                            Countly.sharedInstance().crashes()
                                                .recordHandledException(
                                                    RuntimeException("Could not get content type for message $messageId in thread $threadId")
                                                )
                                        } else if (contentType == "text/plain") {
                                            body = partsCursor.getString(Telephony.Mms.Part.TEXT)
                                            if (body == null) {
                                                Countly.sharedInstance().crashes()
                                                    .recordHandledException(
                                                        RuntimeException("MMS part text is null, even though content type is \"text/plain\"")
                                                    )
                                            }
                                            break
                                        }
                                    }
                                } ?: Countly.sharedInstance().crashes().recordHandledException(
                                    RuntimeException("MMS parts cursor for thread $threadId is null")
                                )
                            }
                            else -> {
                                Countly.sharedInstance().crashes().recordHandledException(
                                    RuntimeException("Unknown message type $messageType")
                                )
                                continue@messagesCursorLoop
                            }
                        }

                        messages.add(Message(messageType, sentByUser, date, body))
                        numMessages++
                    }
                } ?: Countly.sharedInstance().crashes().recordHandledException(
                    RuntimeException("Messages cursor is null for thread $threadId")
                )

                threadsCompleted.run {
                    postValue(1 + value!!)
                }

                oneRecipientMessageThreads.add(MessageThread(address, name, messages))
            }
        } ?: Countly.sharedInstance().crashes().recordUnhandledException(
            RuntimeException("Threads cursor is null; unable to view anything")
        )

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

        val numAddresses = numSuccessfulNameLookups + numFailedNameLookups
        Countly.sharedInstance().events().endEvent(
            "getThreads()",
            mapOf(
                "total number of messages" to numMessages,
                "total number of addresses" to numAddresses,
                "number of successful name lookups" to numSuccessfulNameLookups,
                "number of failed name lookups" to numFailedNameLookups,
                "percentage successful name lookups" to 100.0 * numSuccessfulNameLookups / numAddresses,
                "percentage failed name lookups" to 100.0 * numFailedNameLookups / numAddresses
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
        context.contentResolver.query(
            uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY), null, null, null
        )?.use { nameLookupCursor ->
            return if (nameLookupCursor.count > 0) {
                nameLookupCursor.moveToFirst()
                nameLookupCursor.getString(0)
            } else null
        } ?: Countly.sharedInstance().crashes().recordHandledException(
            RuntimeException("Name lookup cursor is null")
        )
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
