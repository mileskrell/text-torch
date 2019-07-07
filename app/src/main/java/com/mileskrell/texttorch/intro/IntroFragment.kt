package com.mileskrell.texttorch.intro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mileskrell.texttorch.R
import kotlinx.android.synthetic.main.fragment_intro.*

class IntroFragment : Fragment() {

    companion object {
        const val MY_REQUEST_CODE = 1
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // TODO There's probably some earlier place to put this check
        if (readSmsGranted() && readContactsGranted()) {
            findNavController().navigate(R.id.analyze_action)
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
                requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS), MY_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED } ) {
                    findNavController().navigate(R.id.analyze_action)
                }
            }
        }
    }

    private fun showRationale() {
        // TODO Figure out what to actually do here (maybe display a TextView with a nice animation?)
        AlertDialog.Builder(context!!).apply {
            setTitle("Hey! You!")
            setMessage("Grant that permission!")
            setPositiveButton("Okay") { _, _ ->
                requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS), MY_REQUEST_CODE)
            }
            setNegativeButton("No!") { _, _ ->
                Toast.makeText(context, "Well that's just rude", Toast.LENGTH_LONG).show()
            }
            show()
        }
    }

    private fun readSmsGranted() =
        ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

    private fun readContactsGranted() =
        ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
}
