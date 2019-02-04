package com.rubenwardy.minetestmodmanager.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.content.Intent
import android.widget.Toast
import com.rubenwardy.minetestmodmanager.R


class SplashActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: Int = 122


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
    }

    private fun permissionsOK() {
        val intent = Intent(this, ModListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun permissionsDenied() {
        Toast.makeText(this, R.string.perm_storage_needed, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionsDenied()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            }
        } else {
            permissionsOK()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    permissionsOK()
                } else {
                    permissionsDenied()
                }
                return
            }
        }
    }
}
