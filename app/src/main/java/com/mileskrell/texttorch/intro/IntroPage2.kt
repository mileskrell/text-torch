package com.mileskrell.texttorch.intro

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import com.mileskrell.texttorch.R
import kotlinx.android.synthetic.main.fragment_intro_page_2.*

class IntroPage2 : Fragment(R.layout.fragment_intro_page_2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preText = getString(R.string.intro_verify_pre_text)
        val postText = getString(R.string.intro_verify_post_text)
        val linkText = getString(R.string.intro_verify_link_text)
        val url = getString(R.string.about_github_url)
        val linkColor = Color.parseColor(getString(R.string.intro_slide_2_link_color))

        intro_page_2_text_view_3.run {
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(linkColor)
            text = Html.fromHtml("$preText <a href=\"$url\">$linkText</a> $postText")
        }

        intro_permissions_button.setOnClickListener {
            (parentFragment as IntroFragment).onClickPermissionsButton()
        }
    }
}
