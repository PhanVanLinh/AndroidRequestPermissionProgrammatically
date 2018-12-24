package com.toong.androidrequestpermissionprogrammatically

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.toong.androidrequestpermissionprogrammatically.handler.RequestPermissionHandler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var smsAndStoragePermissionHandler: RequestPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        smsAndStoragePermissionHandler = RequestPermissionHandler(this@MainActivity,
                permissions = setOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_EXTERNAL_STORAGE),
                listener = object : RequestPermissionHandler.Listener {
                    override fun onComplete(grantedPermissions: Set<String>, deniedPermissions: Set<String>) {
                        Toast.makeText(this@MainActivity, "complete", Toast.LENGTH_SHORT).show()
                        text_granted.text = "Granted: " + grantedPermissions.toString()
                        text_denied.text = "Denied: " + deniedPermissions.toString()
                    }

                    override fun onShowPermissionRationale(permissions: Set<String>): Boolean {
                        AlertDialog.Builder(this@MainActivity).setMessage("To able to Send Photo, we need SMS and" + " Storage permission")
                                .setPositiveButton("OK") { _, _ ->
                                    smsAndStoragePermissionHandler.retryRequestDeniedPermission()
                                }
                                .setNegativeButton("Cancel") { dialog, _ ->
                                    smsAndStoragePermissionHandler.cancel()
                                    dialog.dismiss()
                                }
                                .show()
                        return true // don't want to show any rationale, just return false here
                    }

                    override fun onShowSettingRationale(permissions: Set<String>): Boolean {
                        AlertDialog.Builder(this@MainActivity).setMessage("Go Settings -> Permission. " + "Make SMS on and Storage on")
                                .setPositiveButton("Settings") { _, _ ->
                                    smsAndStoragePermissionHandler.requestPermissionInSetting()
                                }
                                .setNegativeButton("Cancel") { dialog, _ ->
                                    smsAndStoragePermissionHandler.cancel()
                                    dialog.cancel()
                                }
                                .show()
                        return true
                    }
                })

        button_request.setOnClickListener { handleRequestPermission() }
        button_open_fragment.setOnClickListener { openFragmentA() }
    }

    private fun handleRequestPermission() {
        smsAndStoragePermissionHandler.requestPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        smsAndStoragePermissionHandler.onRequestPermissionsResult(requestCode, permissions,
                grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        smsAndStoragePermissionHandler.onActivityResult(requestCode)
    }

    private fun openFragmentA() {
        supportFragmentManager.beginTransaction().add(R.id.frame_container, FragmentA()).addToBackStack("").commit()
    }
}
