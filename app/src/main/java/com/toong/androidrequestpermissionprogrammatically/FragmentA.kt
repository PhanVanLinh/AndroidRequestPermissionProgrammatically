package com.toong.androidrequestpermissionprogrammatically

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.toong.androidrequestpermissionprogrammatically.handler.RequestPermissionHandler
import kotlinx.android.synthetic.main.fragment_a.*

class FragmentA : Fragment() {

    private lateinit var smsAndStoragePermissionHandler: RequestPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smsAndStoragePermissionHandler = RequestPermissionHandler(fragment = this,
                permissions = setOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_EXTERNAL_STORAGE),
                listener = object : RequestPermissionHandler.Listener {
                    override fun onComplete(grantedPermissions: Set<String>, deniedPermissions: Set<String>) {
                        Toast.makeText(requireContext(), "complete", Toast.LENGTH_SHORT).show()
                        text_granted.text = "Granted: " + grantedPermissions.toString()
                        text_denied.text = "Denied: " + deniedPermissions.toString()
                    }

                    override fun onShowPermissionRationale(permissions: Set<String>): Boolean {
                        AlertDialog.Builder(requireActivity()).setMessage("To able to Send Photo, we need SMS and" + " Storage permission")
                                .setPositiveButton("OK") { _, _ ->
                                    smsAndStoragePermissionHandler.retryRequestDeniedPermission()
                                }
                                .setNegativeButton("Cancel") { dialog, _ ->
                                    smsAndStoragePermissionHandler.cancel()
                                    dialog.dismiss()
                                }
                                .show()
                        return true
                    }

                    override fun onShowSettingRationale(permissions: Set<String>): Boolean {
                        AlertDialog.Builder(requireActivity()).setMessage("Go Settings -> Permission. " + "Make SMS on and Storage on")
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
    }

    private fun handleRequestPermission() {
        smsAndStoragePermissionHandler.requestPermission()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_request.setOnClickListener { handleRequestPermission() }
        button_back.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }
}
