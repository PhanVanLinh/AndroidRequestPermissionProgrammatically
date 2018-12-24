package com.toong.androidrequestpermissionprogrammatically.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import java.util.*

/**
 * Created by PhanVanLinh on 30/08/2017.
 * phanvanlinh.94vn@gmail.com
 */

class RequestPermissionHandler(private val activity: Activity? = null,
                               private val fragment: Fragment? = null,
                               private val permissions: Set<String> = hashSetOf(),
                               private val listener: Listener? = null
) {
    private var hadShowRationale: Boolean = false

    fun requestPermission() {
        hadShowRationale = showRationaleIfNeed()
        if (!hadShowRationale) {
            doRequestPermission(permissions)
        }
    }

    fun retryRequestDeniedPermission() {
        doRequestPermission(permissions)
    }

    private fun showRationaleIfNeed(): Boolean {
        val unGrantedPermissions = getPermission(permissions, Status.UN_GRANTED)
        val permanentDeniedPermissions = getPermission(unGrantedPermissions, Status.PERMANENT_DENIED)
        if (permanentDeniedPermissions.isNotEmpty()) {
            val consume = listener?.onShowSettingRationale(unGrantedPermissions)
            if (consume != null && consume) {
                return true
            }
        }

        val temporaryDeniedPermissions = getPermission(unGrantedPermissions, Status.TEMPORARY_DENIED)
        if (temporaryDeniedPermissions.isNotEmpty()) {
            val consume = listener?.onShowPermissionRationale(temporaryDeniedPermissions)
            if (consume != null && consume) {
                return true
            }
        }
        return false
    }

    fun requestPermissionInSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val packageName = activity?.packageName ?: run {
            fragment?.requireActivity()?.packageName
        }
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        activity?.apply {
            startActivityForResult(intent, REQUEST_CODE)
        } ?: run {
            fragment?.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                   grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    markNeverAskAgainPermission(permissions[i], false)
                } else if (!shouldShowRequestPermissionRationale(permissions[i])) {
                    markNeverAskAgainPermission(permissions[i], true)
                }
            }
            var hasShowRationale = false
            if (!hadShowRationale) {
                hasShowRationale = showRationaleIfNeed()
            }
            if (hadShowRationale || !hasShowRationale) {
                notifyComplete()
            }
        }
    }

    fun onActivityResult(requestCode: Int) {
        if (requestCode == REQUEST_CODE) {
            getPermission(permissions, Status.GRANTED).forEach {
                markNeverAskAgainPermission(it, false)
            }
            notifyComplete()
        }
    }

    fun cancel() {
        notifyComplete()
    }

    private fun doRequestPermission(permissions: Set<String>) {
        activity?.let {
            ActivityCompat.requestPermissions(it, permissions.toTypedArray(), REQUEST_CODE)
        } ?: run {
            fragment?.requestPermissions(permissions.toTypedArray(), REQUEST_CODE)
        }
    }

    private fun getPermission(permissions: Set<String>, status: Status): Set<String> {
        val targetPermissions = HashSet<String>()
        for (p in permissions) {
            when (status) {
                Status.GRANTED -> {
                    if (isPermissionGranted(p)) {
                        targetPermissions.add(p)
                    }
                }
                Status.TEMPORARY_DENIED -> {
                    if (shouldShowRequestPermissionRationale(p)) {
                        targetPermissions.add(p)
                    }
                }
                Status.PERMANENT_DENIED -> {
                    if (isNeverAskAgainPermission(p)) {
                        targetPermissions.add(p)
                    }
                }
                Status.UN_GRANTED -> {
                    if (!isPermissionGranted(p)) {
                        targetPermissions.add(p)
                    }
                }
            }
        }
        return targetPermissions
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return activity?.let {
            ActivityCompat.checkSelfPermission(it, permission) == PackageManager.PERMISSION_GRANTED
        } ?: run {
            ActivityCompat.checkSelfPermission(fragment!!.requireActivity(), permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: run {
            ActivityCompat.shouldShowRequestPermissionRationale(fragment!!.requireActivity(), permission)
        }
    }

    private fun notifyComplete() {
        listener?.onComplete(getPermission(permissions, Status.GRANTED), getPermission(permissions, Status.UN_GRANTED))
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("SHARED_PREFS_RUNTIME_PERMISSION", Context.MODE_PRIVATE)
    }

    private fun isNeverAskAgainPermission(permission: String): Boolean {
        return getPrefs(requireContext()).getBoolean(permission, false)
    }

    private fun markNeverAskAgainPermission(permission: String, value: Boolean) {
        getPrefs(requireContext()).edit().putBoolean(permission, value).apply()
    }

    private fun requireContext(): Context {
        return fragment?.requireContext() ?: run {
            activity!!
        }
    }

    enum class Status {
        GRANTED, UN_GRANTED, TEMPORARY_DENIED, PERMANENT_DENIED
    }

    interface Listener {
        fun onComplete(grantedPermissions: Set<String>, deniedPermissions: Set<String>)
        fun onShowPermissionRationale(permissions: Set<String>): Boolean
        fun onShowSettingRationale(permissions: Set<String>): Boolean
    }

    companion object {
        const val REQUEST_CODE = 200
    }
}
