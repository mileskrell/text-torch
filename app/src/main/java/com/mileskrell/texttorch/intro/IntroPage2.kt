package com.mileskrell.texttorch.intro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.PERMISSIONS_REQUEST_CODE
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import com.mileskrell.texttorch.util.showAppSettingsDialog
import kotlinx.android.synthetic.main.fragment_intro_page_2.*

class IntroPage2 : Fragment(R.layout.fragment_intro_page_2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preText = getString(R.string.intro_verify_pre_text)
        val postText = getString(R.string.intro_verify_post_text)
        val linkText = getString(R.string.intro_verify_link_text)
        val url = getString(R.string.about_github_url)
        val linkColor = ContextCompat.getColor(requireContext(), R.color.intro_slide_2_link_color)

        intro_page_2_text_view_3.run {
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(linkColor)
            @Suppress("DEPRECATION")
            text = Html.fromHtml("$preText <a href=\"$url\">$linkText</a> $postText")
        }

        intro_permissions_button.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)
                || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                showRationale()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_CODE)
            }
        }

        // I think storing permission statuses in a ViewModel would just introduce more complexity,
        // so after configuration changes, we need to check them again.
        if (readSmsGranted() && readContactsGranted()) {
            // TODO: Move this check to onStart() or onResume(). Then we can detect if
            //  the user is returning after granting the permissions in the app's settings.
            // I haven't done this yet because it results in a "FragmentManager is already
            // executing transactions" error. Related: see BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
            // at the moment, fragments in the ViewPager are capped at STARTED.
            onPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onPermissionsGranted()
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

    private fun onPermissionsGranted() {
        intro_permissions_button.visibility = View.INVISIBLE
        intro_text_view_permissions_granted.visibility = View.VISIBLE
        (parentFragment as IntroFragment).introPagerAdapter.addAnalyticsPage()
    }

    private fun showRationale() {
        // TODO Figure out what to actually do here (maybe display a TextView with a nice animation?)

        // TODO: Do we actually need to show this additional rationale, only after the user has
        //  denied the permission? We should probably just make the initial rationale really clear.
        AlertDialog.Builder(requireContext()).apply {
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
}
