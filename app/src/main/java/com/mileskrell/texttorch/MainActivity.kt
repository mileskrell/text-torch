package com.mileskrell.texttorch

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import ly.count.android.sdk.Countly

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController, AppBarConfiguration(setOf(
            // These are considered "top level destinations"
            R.id.intro_dest, R.id.regain_dest, R.id.analyze_dest, R.id.stats_dest
        )))
    }

    override fun onStart() {
        super.onStart()
        Countly.sharedInstance().onStart(this)
    }

    override fun onStop() {
        super.onStop()
        Countly.sharedInstance().onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Countly.sharedInstance().onConfigurationChanged(newConfig)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
