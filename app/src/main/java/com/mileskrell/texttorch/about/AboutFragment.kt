package com.mileskrell.texttorch.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.BuildConfig
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import ly.count.android.sdk.Countly
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutFragment : Fragment() {

    companion object {
        const val TAG = "AboutFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val socialRecordsViewModel: SocialRecordsViewModel by activityViewModels()

        if (socialRecordsViewModel.socialRecords.value == null) {
            // Process death! Go back to StatsFragment (which will in turn send us back to AnalyzeFragment).
            findNavController().navigateUp()
        }

        val githubItem = Element().apply {
            title = getString(R.string.about_github_title)
            iconDrawable = R.drawable.ic_github_mark
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_github_url))))
            }
        }

        val emailItem = Element().apply {
            title = getString(R.string.about_email_title)
            iconDrawable = R.drawable.ic_email_black_24dp
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + getString(R.string.about_email_email))))
            }
        }

        val donateItem = Element().apply {
            title = getString(R.string.about_donate_title)
            iconDrawable = R.drawable.ic_attach_money_black_24dp
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_donate_url))))
            }
        }

        val versionString =
            getString(R.string.version) + " " + BuildConfig.VERSION_NAME + if (BuildConfig.DEBUG) "-debug" else ""

        return AboutPage(context)
            .setImage(R.drawable.text_torch)
            .setDescription(getString(R.string.about_description))
            .addItem(Element().setTitle(versionString))
            .addItem(githubItem)
            .addItem(emailItem)
            .addItem(donateItem)
            .create()
    }

    override fun onResume() {
        super.onResume()
        Countly.sharedInstance().views().recordView(TAG)
    }
}
