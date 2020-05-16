package com.mileskrell.texttorch

import android.app.Application
import ly.count.android.sdk.Countly
import ly.count.android.sdk.CountlyConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Countly.sharedInstance().init(
            CountlyConfig(
                applicationContext,
                "47ce1ac0ec04e8164b6c77eb439af0eb4532df1b",
                "https://countly.mileskrell.com"
            )
                .setLoggingEnabled(true)
                .enableCrashReporting()
                .setTrackOrientationChanges(true)
        )
    }
}
