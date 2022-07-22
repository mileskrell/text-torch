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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import io.sentry.android.navigation.SentryNavigationListener

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController
    private val sentryNavListener = SentryNavigationListener(
        enableNavigationBreadcrumbs = true,
        enableNavigationTracing = true
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(
            navController, AppBarConfiguration(
                // These are considered "top level destinations"
                setOf(R.id.intro_dest, R.id.regain_dest, R.id.analyze_dest, R.id.stats_dest)
            )
        )
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(sentryNavListener)
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(sentryNavListener)
    }

    override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()
}
