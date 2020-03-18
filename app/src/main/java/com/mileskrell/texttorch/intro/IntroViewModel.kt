package com.mileskrell.texttorch.intro

import androidx.lifecycle.ViewModel

class IntroViewModel : ViewModel() {
    enum class PAGE {
        WELCOME, PERMISSIONS, ANALYTICS, ENTER_APP
    }

    var lastPageVisible = PAGE.PERMISSIONS
}
