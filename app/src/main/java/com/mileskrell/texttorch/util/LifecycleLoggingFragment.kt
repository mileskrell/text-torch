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
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.sentry.core.Sentry

open class LifecycleLoggingFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Sentry.addBreadcrumb("${this::class.simpleName}#onAttach", "ui.lifecycle")
    }

    override fun onDetach() {
        super.onDetach()
        Sentry.addBreadcrumb("${this::class.simpleName}#onDetach", "ui.lifecycle")
    }

    override fun onStart() {
        super.onStart()
        Sentry.addBreadcrumb("${this::class.simpleName}#onStart", "ui.lifecycle")
    }

    override fun onStop() {
        super.onStop()
        Sentry.addBreadcrumb("${this::class.simpleName}#onStop", "ui.lifecycle")
    }

    override fun onResume() {
        super.onResume()
        Sentry.addBreadcrumb("${this::class.simpleName}#onResume", "ui.lifecycle")
    }

    override fun onPause() {
        super.onPause()
        Sentry.addBreadcrumb("${this::class.simpleName}#onPause", "ui.lifecycle")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Sentry.addBreadcrumb("${this::class.simpleName}#onDestroyView", "ui.lifecycle")
    }

    override fun onDestroy() {
        super.onDestroy()
        Sentry.addBreadcrumb("${this::class.simpleName}#onDestroy", "ui.lifecycle")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sentry.addBreadcrumb("${this::class.simpleName}#onCreate", "ui.lifecycle")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Sentry.addBreadcrumb("${this::class.simpleName}#onViewCreated", "ui.lifecycle")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Sentry.addBreadcrumb("${this::class.simpleName}#onSaveInstanceState", "ui.lifecycle")
    }
}
