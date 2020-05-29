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

package com.mileskrell.texttorch.analyze

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyzeViewModel : ViewModel() {

    val threadsTotal = MutableLiveData<Int>()
    val threadsCompleted = MutableLiveData(0)

    // These are for the current message thread
    val messagesTotal = MutableLiveData<Int>()
    val messagesCompleted = MutableLiveData(0)

    var clickedAnalyze = false
    var clickedShowDetails = false

    fun initializeSocialRecordList(socialRecordsViewModel: SocialRecordsViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            socialRecordsViewModel.initializeSocialRecords(
                threadsTotal, threadsCompleted, messagesTotal, messagesCompleted
            )
        }
    }
}
