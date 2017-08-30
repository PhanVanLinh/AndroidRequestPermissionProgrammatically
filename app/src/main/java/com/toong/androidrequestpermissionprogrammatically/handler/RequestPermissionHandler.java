package com.toong.androidrequestpermissionprogrammatically.handler;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Created by PhanVanLinh on 30/08/2017.
 * phanvanlinh.94vn@gmail.com
 */

public class RequestPermissionHandler {
    private Activity mActivity;
    private RequestPermissionListener mRequestPermissionListener;
    private int mRequestCode;

    public void requestPermission(Activity activity, @NonNull String[] permissions,
            int requestCode, RequestPermissionListener listener) {
        mActivity = activity;
        mRequestCode = requestCode;
        mRequestPermissionListener = listener;

        if (notNeedRequestRuntimePermissions()) {
            mRequestPermissionListener.onSuccess();
            return;
        }
        if (isPermissionsGranted(permissions)) {
            mRequestPermissionListener.onSuccess();
            return;
        }
        ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
    }

    private boolean notNeedRequestRuntimePermissions() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    private boolean isPermissionsGranted(@NonNull String[] permissions) {
        int result = 0;
        for (String permission : permissions) {
            result += ActivityCompat.checkSelfPermission(mActivity, permission);
        }
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == mRequestCode) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mRequestPermissionListener.onFailed();
                        return;
                    }
                }
                mRequestPermissionListener.onSuccess();
            } else {
                mRequestPermissionListener.onFailed();
            }
        }
    }

    public interface RequestPermissionListener {
        void onSuccess();
        void onFailed();
    }
}
