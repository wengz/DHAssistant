package com.example.dhassistant.main;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dhassistant.R;
import com.example.dhassistant.service.DHAccessibilityService;
import com.example.dhassistant.ui.overlay.OverlayController;
import com.example.dhassistant.ui.overlay.WindowPermissionHelper;
import com.example.dhassistant.util.AccessibilityHelper;

/**
 * 主界面：用于引导用户开启无障碍服务与悬浮窗权限。
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvAccessibilityStatus;
    private TextView tvOverlayStatus;
    private Button btnAccessibility;
    private Button btnOverlay;
    private Button btnShowBall;

    private final ActivityResultLauncher<Intent> overlayLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                refreshStatus();
                if (WindowPermissionHelper.canDrawOverlays(this)
                        && DHAccessibilityService.getInstance() != null) {
                    OverlayController.get().showBall();
                }
            });

    private final ActivityResultLauncher<Intent> accessibilityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> refreshStatus());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAccessibilityStatus = findViewById(R.id.tv_accessibility_status);
        tvOverlayStatus = findViewById(R.id.tv_overlay_status);
        btnAccessibility = findViewById(R.id.btn_open_accessibility);
        btnOverlay = findViewById(R.id.btn_open_overlay);
        btnShowBall = findViewById(R.id.btn_show_floating);

        btnAccessibility.setOnClickListener(v ->
                accessibilityLauncher.launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        btnOverlay.setOnClickListener(v ->
                overlayLauncher.launch(WindowPermissionHelper.buildOverlayPermissionIntent(this)));
        btnShowBall.setOnClickListener(v -> {
            if (DHAccessibilityService.getInstance() == null) {
                accessibilityLauncher.launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return;
            }
            if (!WindowPermissionHelper.canDrawOverlays(this)) {
                overlayLauncher.launch(WindowPermissionHelper.buildOverlayPermissionIntent(this));
                return;
            }
            OverlayController.get().showBall();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        boolean a11y = AccessibilityHelper.isServiceEnabled(this);
        tvAccessibilityStatus.setText(getString(a11y ? R.string.status_accessibility_on
                : R.string.status_accessibility_off));
        tvAccessibilityStatus.setTextColor(getColor(a11y ? R.color.overlay_status_ok
                : R.color.overlay_status_error));

        boolean overlay = WindowPermissionHelper.canDrawOverlays(this);
        tvOverlayStatus.setText(getString(overlay ? R.string.status_overlay_on
                : R.string.status_overlay_off));
        tvOverlayStatus.setTextColor(getColor(overlay ? R.color.overlay_status_ok
                : R.color.overlay_status_error));
    }
}
