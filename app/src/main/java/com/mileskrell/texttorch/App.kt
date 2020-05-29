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
