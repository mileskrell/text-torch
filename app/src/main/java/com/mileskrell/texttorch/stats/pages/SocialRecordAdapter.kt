package com.mileskrell.texttorch.stats.pages

import android.animation.ValueAnimator
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecord
import kotlinx.android.synthetic.main.social_record_view_holder.view.*

class SocialRecordAdapter(val type: SocialRecordAdapterType) : RecyclerView.Adapter<SocialRecordAdapter.SocialRecordViewHolder>() {

    private var lastLoadTime = System.nanoTime()
    private val timeElapsed
        get() = (System.nanoTime() - lastLoadTime) / 1_000_000

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
        this.lastLoadTime = System.nanoTime()
        notifyDataSetChanged()
    }

    enum class SocialRecordAdapterType {
        WHO_TEXTS_FIRST,
        TOTAL_TEXTS,
        AVERAGE_LENGTH
    }

    inner class SocialRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setupForSocialRecord(type: SocialRecordAdapterType, record: SocialRecord) {
            // Make the whole TextView bold in case part of it is still italicized
            itemView.correspondent_name_address_text_view.setTypeface(null, Typeface.BOLD)
            if (record.nonUniqueName) {
                // The name is guaranteed to be non-null here
                itemView.correspondent_name_address_text_view.text = SpannableStringBuilder("${record.correspondentName} (${record.correspondentAddress})")
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
                itemView.correspondent_name_address_text_view.text = record.correspondentName ?: record.correspondentAddress
            }

            val endPosition = when (type) {
                SocialRecordAdapterType.WHO_TEXTS_FIRST -> {
                    itemView.center_text_view.text =
                        itemView.resources.getQuantityString(
                            R.plurals.based_on_x_conversations,
                            record.numConversations,
                            record.numConversations
                        )
                    itemView.correspondent_data_text_view.text =
                        itemView.context.getString(R.string.texted_first_x_percent_of_the_time, record.correspondentInitPercent)
                    itemView.you_data_text_view.text =
                        itemView.context.getString(R.string.texted_first_x_percent_of_the_time, 100 - record.correspondentInitPercent)

                    record.correspondentInitPercent / 100f
                }
                SocialRecordAdapterType.TOTAL_TEXTS -> {
                    itemView.center_text_view.text =
                        itemView.resources.getQuantityString(
                            R.plurals.based_on_x_texts,
                            record.numTexts,
                            record.numTexts
                        )
                    itemView.correspondent_data_text_view.text =
                        itemView.context.getString(R.string.sent_x_percent_of_texts, record.correspondentTextPercent)
                    itemView.you_data_text_view.text =
                        itemView.context.getString(R.string.sent_x_percent_of_texts, 100 - record.correspondentTextPercent)

                    record.correspondentTextPercent / 100f
                }
                SocialRecordAdapterType.AVERAGE_LENGTH -> {
                    itemView.center_text_view.text =
                        itemView.resources.getQuantityString(
                            R.plurals.based_on_x_texts,
                            record.numTexts,
                            record.numTexts
                        )
                    itemView.correspondent_data_text_view.text =
                        itemView.context.getString(R.string.x_characters_on_average, record.correspondentAvgChars)
                    itemView.you_data_text_view.text =
                        itemView.context.getString(R.string.x_characters_on_average, record.ownAvgChars)

                    record.correspondentAvgCharsPercent / 100f
                }
            }

            val dividerLayoutParams = itemView.divider.layoutParams as ConstraintLayout.LayoutParams

            val animationLength = itemView.resources.getInteger(R.integer.divider_animation_length_ms)

            if (timeElapsed < animationLength) {
                // Determine proper start position (in between 0.5 and endPosition).
                // If all items started at 0.5, any items that weren't initially visible would animate too fast.
                val startPosition = if (endPosition >= 0.5) {
                    0.5f + (timeElapsed.toFloat() / animationLength * (endPosition - 0.5f))
                } else {
                    0.5f - (timeElapsed.toFloat() / animationLength * (0.5f - endPosition))
                }

                ValueAnimator.ofFloat(startPosition, endPosition).apply {
                    // Only animate until lastLoadTime + animationLength
                    duration = animationLength - timeElapsed
                    addUpdateListener {
                        dividerLayoutParams.horizontalBias = animatedValue as Float
                        itemView.divider.layoutParams = dividerLayoutParams
                    }
                    start()
                }
            } else {
                dividerLayoutParams.horizontalBias = endPosition
                itemView.divider.layoutParams = dividerLayoutParams
            }
        }
    }
}
