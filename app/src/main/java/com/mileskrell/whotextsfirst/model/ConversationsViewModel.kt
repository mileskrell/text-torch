package com.mileskrell.whotextsfirst.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mileskrell.whotextsfirst.repo.Repository
import kotlinx.coroutines.delay
import kotlin.random.Random

class ConversationsViewModel(val app: Application) : AndroidViewModel(app) {

    private val _conversations: MutableLiveData<List<Conversation>> = MutableLiveData()

    val conversations = _conversations as LiveData<List<Conversation>>

    val repository = Repository()

    suspend fun updateConversations(period: String) {
        delay(1000)
        _conversations.postValue(List(10) { index ->
            when (Random.nextBoolean()) {
                true -> Conversation("Person ${index + 1}", Random.nextInt(60, 100))
                else -> Conversation("Person ${index + 1}", Random.nextInt(0, 40))
            }
        })
    }
}
