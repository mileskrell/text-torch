package com.mileskrell.whotextsfirst.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mileskrell.whotextsfirst.R
import com.mileskrell.whotextsfirst.model.SocialRecord
import kotlinx.android.synthetic.main.social_record_view_holder.view.*

class SocialRecordAdapter : RecyclerView.Adapter<SocialRecordAdapter.SocialRecordViewHolder>() {

    private var socialRecords = listOf<SocialRecord>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialRecordViewHolder {
        return SocialRecordViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.social_record_view_holder, parent, false)
        )
    }

    override fun getItemCount() = socialRecords.size

    override fun onBindViewHolder(holder: SocialRecordViewHolder, position: Int) {
        holder.setupForSocialRecord(socialRecords[position])
    }

    fun loadSocialRecords(socialRecords: List<SocialRecord>) {
        this.socialRecords = socialRecords
        notifyDataSetChanged()
    }

    inner class SocialRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setupForSocialRecord(record: SocialRecord) {
            itemView.correspondent_name_text_view.text = record.correspondentName

            itemView.number_of_conversations_text_view.text =
                    itemView.context.getString(R.string.x_conversations, record.numConversations)
            itemView.correspondent_percent_text_view.text =
                    itemView.context.getString(R.string.x_percent, record.correspondentPercent)
            itemView.you_percent_text_view.text =
                    itemView.context.getString(R.string.x_percent, 100 - record.correspondentPercent)

            with(itemView.divider.layoutParams as ConstraintLayout.LayoutParams) {
                horizontalBias = (record.correspondentPercent / 100.0).toFloat()
            }
        }
    }
}
