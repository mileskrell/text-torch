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

package com.mileskrell.texttorch.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.mileskrell.texttorch.BuildConfig
import com.mileskrell.texttorch.R
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.Message

const val DSN = "https://b19179bf252d42149d7deda382a89af5@o403459.ingest.sentry.io/5405284"

fun initSentry(context: Context) {
    val enabled = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getBoolean(context.getString(R.string.key_enable_analytics), false)
    SentryAndroid.init(context) { options ->
        options.dsn = if (enabled) DSN else ""
        options.isDebug = BuildConfig.DEBUG
        options.environment = BuildConfig.BUILD_TYPE
        options.enableAllAutoBreadcrumbs(true)
    }
}

/**
 * For some things (e.g. button clicks), I want to log them as issues, but I also want to be able to
 * see that they happened when looking at events that happened later on.
 */
fun logToBoth(tag: String?, message: String, level: SentryLevel = SentryLevel.INFO) {
    Sentry.captureMessage(if (tag == null) message else "[$tag] $message", level)
    Sentry.addBreadcrumb(if (tag == null) message else "[$tag] $message")
}

fun logEvent(
    tag: String,
    message: String,
    level: SentryLevel,
    addBreadcrumb: Boolean,
    extras: Map<String, Any>? = null
) {
    if (extras == null) {
        Sentry.captureMessage("[$tag] $message", level)
        if (addBreadcrumb) {
            Sentry.addBreadcrumb("[$tag] $message")
        }
    } else {
        Sentry.captureEvent(SentryEvent().apply {
            this.message = Message().apply {
                this.message = "[$tag] $message"
            }
            this.level = level
            setExtras(extras)
        })
        if (addBreadcrumb) {
            Sentry.addBreadcrumb("[$tag] $message")
            extras.forEach { extra ->
                Sentry.addBreadcrumb("[$tag] ${extra.key}: ${extra.value}")
            }
        }
    }
}
