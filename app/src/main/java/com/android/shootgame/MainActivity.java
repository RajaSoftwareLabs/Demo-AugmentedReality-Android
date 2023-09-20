package com.android.shootgame;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.android.shootgame.databinding.ActivityMainBinding;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

public class MainActivity extends AppCompatActivity {

    private final static int CAMERA_PERMISSION_CODE = 101;
    private final static String PACKAGE_NAME = "package";
    private String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private boolean installRequested;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (!isCameraPermissionGranted()) {
            requestCameraPermission();
        }

        // Click listener to open basic demo activity (Default shapes)
        binding.basicDemoButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, BasicDemoActivity.class);
            startActivity(intent);
        });

        // Click listener to open custom object demo activity (Custom 3D models)
        binding.customObjectDemoButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, CustomObjectActivity.class);
            startActivity(intent);
        });

        // Click listener to open physics simulation demo activity
        binding.appliedPhysicsDemoButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, PhysicsSimulationActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            requestArCoreInstall();
        } catch (UnavailableException e) {
            handleSessionException(e);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (ActivityCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED) {
            return;
        }
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(getString(R.string.camera_permission_request_title))
               .setMessage(getString(R.string.camera_permission_request_message))
               .setPositiveButton(
                   android.R.string.ok,
                   (dialog, which) -> {
                       // If Ok was hit, bring up the Settings app.
                       Intent intent = new Intent();
                       intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                       intent.setData(Uri.fromParts(PACKAGE_NAME, getApplication().getPackageName(), null));
                       startActivity(intent);
                   })
               .setNegativeButton(android.R.string.cancel, null)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .setOnDismissListener(dialogInterface -> {
                       // If camera permission is not granted, finish the activity when this dialog is
                       // dismissed.
                       if (ActivityCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
                           finish();
                       }
                   })
               .show();
    }

    private void handleSessionException(UnavailableException sessionException) {

        String message;
        if (sessionException instanceof UnavailableArcoreNotInstalledException) {
            message = getString(R.string.install_arcore_message);
        } else if (sessionException instanceof UnavailableApkTooOldException) {
            message = getString(R.string.update_arcore_message);
        } else if (sessionException instanceof UnavailableSdkTooOldException) {
            message = getString(R.string.update_app_message);
        } else if (sessionException instanceof UnavailableDeviceNotCompatibleException) {
            message = getString(R.string.ar_unsupported_message);
        } else {
            message = getString(R.string.failed_ar_session_creation_message);
        }
        Log.e(TAG, getString(R.string.error_message, message), sessionException);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Method to check camera permission is granted or not.
     *
     * @return boolean value providing permission status.
     */
    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED;
    }

    /**
     * Method to request camera permission to user.
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, CAMERA_PERMISSION_CODE);
    }

    /**
     * Method to request user to install ARCore application.
     */
    private void requestArCoreInstall() throws UnavailableException {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
            case INSTALL_REQUESTED:
                installRequested = true;
            case INSTALLED:
                break;
        }
    }
}
