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

package com.mileskrell.texttorch.stats.pages

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.databinding.SocialRecordViewHolderBinding
import com.mileskrell.texttorch.stats.model.SocialRecord

class SocialRecordAdapter(val type: SocialRecordAdapterType) : RecyclerView.Adapter<SocialRecordAdapter.SocialRecordViewHolder>() {

    private var socialRecords = listOf<SocialRecord>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialRecordViewHolder {
        return SocialRecordViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.social_record_view_holder, parent, false)
        )
    }

    override fun getItemCount() = socialRecords.size

    override fun onBindViewHolder(holder: SocialRecordViewHolder, position: Int) {
        holder.setupForSocialRecord(type, socialRecords[position])
    }

    fun loadSocialRecords(socialRecords: List<SocialRecord>) {
        this.socialRecords = socialRecords
        notifyDataSetChanged()
    }

    enum class SocialRecordAdapterType {
        WHO_TEXTS_FIRST,
        TOTAL_TEXTS,
        AVERAGE_LENGTH
    }

    inner class SocialRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val b = SocialRecordViewHolderBinding.bind(itemView)
        fun setupForSocialRecord(type: SocialRecordAdapterType, record: SocialRecord) {
            // Make the whole TextView bold in case part of it is still italicized
            b.correspondentNameAddressTextView.setTypeface(null, Typeface.BOLD)
            if (record.nonUniqueName) {
                // The name is guaranteed to be non-null here
                b.correspondentNameAddressTextView.text = SpannableStringBuilder("${record.correspondentName} (${record.correspondentAddress})")
                    .apply {
                        // Make the number and surrounding parentheses also italic
                        setSpan(
                            StyleSpan(Typeface.BOLD_ITALIC),
                            record.correspondentName!!.length,
                            length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
            } else {
                b.correspondentNameAddressTextView.text = record.correspondentName ?: record.correspondentAddress
            }

            when (type) {
                SocialRecordAdapterType.WHO_TEXTS_FIRST -> {
                    b.centerTextView.text =
                        itemView.resources.getQuantityString(
                            R.plurals.based_on_x_conversations,
                            record.numConversations,
                            record.numConversations
                        )
                    b.correspondentDataTextView.text =
                        itemView.context.getString(R.string.texted_first_x_percent_of_the_time, record.correspondentInitPercent)
                    b.youDataTextView.text =
                        itemView.context.getString(R.string.texted_first_x_percent_of_the_time, 100 - record.correspondentInitPercent)

                    b.divider.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        horizontalBias = record.correspondentInitPercent / 100f
                    }
                }
                SocialRecordAdapterType.TOTAL_TEXTS -> {
                    b.centerTextView.text =
                        itemView.resources.getQuantityString(
                            R.plurals.based_on_x_texts,
                            record.numTexts,
                            record.numTexts
                        )
                    b.correspondentDataTextView.text =
                        itemView.context.getString(R.string.sent_x_percent_of_texts, record.correspondentTextPercent)
                    b.youDataTextView.text =
                        itemView.context.getString(R.string.sent_x_percent_of_texts, 100 - record.correspondentTextPercent)

                    b.divider.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        horizontalBias = record.correspondentTextPercent / 100f
                    }
                }
                SocialRecordAdapterType.AVERAGE_LENGTH -> {
                    b.centerTextView.text =
                        itemView.resources.getQuantityString(
                            R.plurals.based_on_x_texts,
                            record.numTexts,
                            record.numTexts
                        )
                    b.correspondentDataTextView.text =
                        itemView.context.getString(R.string.x_characters_on_average, record.correspondentAvgChars)
                    b.youDataTextView.text =
                        itemView.context.getString(R.string.x_characters_on_average, record.ownAvgChars)

                    b.divider.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        horizontalBias = record.correspondentAvgCharsPercent / 100f
                    }
                }
            }
        }
    }
}
