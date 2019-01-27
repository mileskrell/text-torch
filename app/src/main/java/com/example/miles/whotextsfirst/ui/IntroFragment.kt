package com.example.miles.whotextsfirst.ui

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
import com.example.miles.whotextsfirst.R
import kotlinx.android.synthetic.main.fragment_intro.*

const val REQUEST_CODE_READ_SMS = 1

class IntroFragment : Fragment() {

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // TODO There's probably some earlier place to put this check
        if (readSmsGranted()) {
            findNavController().navigate(R.id.main_action)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button_intro.setOnClickListener {
            if (!readSmsGranted()) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                    showRationale()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.READ_SMS), REQUEST_CODE_READ_SMS)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_READ_SMS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findNavController().navigate(R.id.main_action)
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
                requestPermissions(arrayOf(Manifest.permission.READ_SMS), REQUEST_CODE_READ_SMS)
            }
            setNegativeButton("No!") { _, _ ->
                Toast.makeText(context, "Well that's just rude", Toast.LENGTH_LONG).show()
            }
            show()
        }
    }

    private fun readSmsGranted(): Boolean = (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_SMS)
            == PackageManager.PERMISSION_GRANTED)
}
