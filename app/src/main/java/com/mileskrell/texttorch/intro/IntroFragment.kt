package com.mileskrell.texttorch.intro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.PERMISSIONS_REQUEST_CODE
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import com.mileskrell.texttorch.util.showAppSettingsDialog
import kotlinx.android.synthetic.main.fragment_intro.*

class IntroFragment : Fragment() {

    companion object {
        const val KEY_HAS_SEEN_TUTORIAL = "has_seen_tutorial"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // TODO There's probably some earlier place to put this check
        val hasSeenTutorial = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            KEY_HAS_SEEN_TUTORIAL, false
        )

        if (hasSeenTutorial) {
            // If all permissions have been granted...
            if (readSmsGranted() && readContactsGranted()) {
                // Immediately go to AnalyzeFragment, without any animations
                findNavController().navigate(R.id.intro_to_analyze_action)
            } else {
                // Permissions were granted (because tutorial was completed), but the user went in
                // and manually denied them later on. Prompt the user to grant them again.
                findNavController().navigate(R.id.intro_to_regain_action)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button_intro.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)
                || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                showRationale()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {

                    // Save that tutorial has been seen
                    PreferenceManager.getDefaultSharedPreferences(context).edit {
                        putBoolean(KEY_HAS_SEEN_TUTORIAL, true)
                    }

                    // Animated navigation to AnalyzeFragment
                    findNavController().navigate(R.id.intro_to_analyze_action, null, navOptions {
                        anim {
                            enter = R.anim.slide_in_right
                            exit = R.anim.slide_out_left
                            popEnter = R.anim.slide_in_left
                            popExit = R.anim.slide_out_right
                        }
                        // We have to specify these 2 even though they're already in XML, because
                        // this NavOptions totally overrides what's in XML
                        popUpTo(R.id.intro_dest) {
                            inclusive = true
                        }
                    })
                } else {
                    // Not all permissions were granted
                    val canAskAgain = (readSmsGranted() || shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
                                && (readContactsGranted() || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                    if (!canAskAgain) {
                        // User checked "Never ask again", so open app settings page
                        showAppSettingsDialog()
                    }
                }
            }
        }
    }

    private fun showRationale() {
        // TODO Figure out what to actually do here (maybe display a TextView with a nice animation?)

        // TODO: Do we actually need to show this additional rationale, only after the user has
        //  denied the permission? We should probably just make the initial rationale really clear.
        AlertDialog.Builder(context!!).apply {
            setTitle("Hey! You!")
            setMessage("Grant that permission!")
            setPositiveButton("Okay") { _, _ ->
                requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_CODE)
            }
            setNegativeButton("No!") { _, _ ->
                Toast.makeText(context, "Well that's just rude", Toast.LENGTH_LONG).show()
            }
            show()
        }
    }

    // TODO: We'll want to do something if permissions are granted unexpectedly while in
    //  IntroFragment. But it'll look different from RegainPermissionsFragment.onResume().
    //  Remember, the tutorial is only "completed" when the user taps on whatever the final
    //  button is, *not* because we detect that they've granted all necessary permissions.
}
