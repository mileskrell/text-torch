package com.mileskrell.texttorch.intro

import android.Manifest
import android.animation.ArgbEvaluator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.viewpager.widget.ViewPager
import com.mileskrell.texttorch.MainActivity
import com.mileskrell.texttorch.R
import com.mileskrell.texttorch.util.PERMISSIONS_REQUEST_CODE
import com.mileskrell.texttorch.util.readContactsGranted
import com.mileskrell.texttorch.util.readSmsGranted
import com.mileskrell.texttorch.util.showAppSettingsDialog
import kotlinx.android.synthetic.main.fragment_intro.*

// TODO: I'd like to have translucent status and navigation bars here

class IntroFragment : Fragment(R.layout.fragment_intro) {

    companion object {
        const val KEY_HAS_SEEN_TUTORIAL = "has_seen_tutorial"
    }

    // Colors for ViewPager background
    lateinit var backgroundColors: List<Int>
    lateinit var logoColors: List<Int>
    var hasSeenTutorial: Boolean? = null // Because primitives can't be lateinit

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // TODO There's probably some earlier place to put this check
        hasSeenTutorial = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            KEY_HAS_SEEN_TUTORIAL, false
        )

        if (hasSeenTutorial == true) {
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

        backgroundColors = resources.getStringArray(R.array.intro_background_colors)
            .map { Color.parseColor(it) }
        logoColors = resources.getStringArray(R.array.intro_logo_colors)
            .map { Color.parseColor(it) }
    }

    fun enterAlmostFullscreen() {
        /*
        Option 1: translucent bars with dark scrim.
        activity.window.add/clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        ---
        Option 2: transparent bar backgrounds, but views can go behind bars.
        activity.window.add/clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        ---
        Option 3: like 2, but views don't go behind the nav bar (and it takes a couple more lines).
        Note that FLAG_TRANSLUCENT_NAVIGATION and FLAG_TRANSLUCENT_STATUS will override manually-set
        colors, so don't set them if you want fully transparent backgrounds.
        On the other hand, FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS must be set.
         */
        (activity as? MainActivity)?.supportActionBar?.hide()
//        intro_fragment_root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        activity?.window?.apply {
////            setBackgroundDrawable(null) // this makes wild graphical glitches happen
//            statusBarColor = Color.TRANSPARENT
//            navigationBarColor = Color.TRANSPARENT
//        }
    }

    fun exitAlmostFullscreen() {
        (activity as? MainActivity)?.supportActionBar?.show()
        activity?.window?.statusBarColor = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
//        intro_fragment_root.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
//        activity?.window?.apply {
//            statusBarColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
//            navigationBarColor = Color.BLACK
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // This check is needed because onViewCreated() is called even if we've already seen the
        // tutorial (since navigating to the next fragment takes a moment)
        if (hasSeenTutorial == false) {
            enterAlmostFullscreen()
        }

//        // Adjust bottom margins so views aren't obscured by navigation bar
//        intro_fragment_root.setOnApplyWindowInsetsListener { v, insets ->
//            // The FAB is constrained relative to the page indicator, so we don't need to touch it
//            intro_page_indicator.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                val pageIndicatorInitialBottomMargin = TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    30f,
//                    resources.displayMetrics
//                ).toInt()
//                bottomMargin = pageIndicatorInitialBottomMargin + insets.systemWindowInsetBottom
//            }
//            insets.consumeSystemWindowInsets()
//        }

        intro_view_pager.offscreenPageLimit = 2
        intro_view_pager.adapter = IntroPagerAdapter(childFragmentManager)
        val colorBackground = (intro_view_pager.background as LayerDrawable)
            .getDrawable(0) as ColorDrawable
        val logoBackground = (intro_view_pager.background as LayerDrawable)
            .getDrawable(1) as BitmapDrawable

        intro_view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Thanks to https://kubaspatny.github.io/2014/09/18/viewpager-background-transition/
                colorBackground.color = ArgbEvaluator().evaluate(
                    positionOffset,
                    backgroundColors[position],
                    backgroundColors.getOrNull(position + 1) ?: backgroundColors[position]
                ) as Int
                (ArgbEvaluator().evaluate(
                    positionOffset,
                    logoColors[position],
                    logoColors.getOrNull(position + 1) ?: logoColors[position]
                ) as Int).let { darkColor ->
                    logoBackground.setTint(darkColor)
                    activity?.window?.statusBarColor = darkColor
                }

                // If on last page OR if (on 2nd-to-last and scrolling toward last)
                if (position == (intro_view_pager.adapter as IntroPagerAdapter).count - 1
                    || (position == (intro_view_pager.adapter as IntroPagerAdapter).count - 2 && positionOffset > 0)) {
                    intro_arrow_next.hide()
                } else {
                    intro_arrow_next.show()
                }
            }
            override fun onPageSelected(position: Int) {}
        })

        // TODO: Can we make this button scroll more slowly?
        intro_arrow_next.setOnClickListener {
            intro_view_pager.arrowScroll(View.FOCUS_RIGHT)
        }
    }

    /**
     * Called when the "grant needed permissions" button in [IntroPage2] is clicked
     */
    fun onClickPermissionsButton() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)
            || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            showRationale()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    exitAlmostFullscreen()

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
