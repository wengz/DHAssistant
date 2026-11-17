package com.example.dhassistant.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dhassistant.R;
import com.example.dhassistant.service.DHAccessibilityService;
import com.example.dhassistant.task.Account;
import com.example.dhassistant.task.AccountFileLoader;
import com.example.dhassistant.ui.overlay.OverlayController;
import com.example.dhassistant.ui.overlay.WindowPermissionHelper;
import com.example.dhassistant.util.AccessibilityHelper;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * 主界面：用于引导用户开启无障碍服务与悬浮窗权限，并管理 account.txt 的导入/刷新。
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvAccessibilityStatus;
    private TextView tvOverlayStatus;
    private Button btnAccessibility;
    private Button btnOverlay;
    private Button btnShowBall;
    private Button btnImportAccount;
    private Button btnRefreshAccount;
    private TextView tvAccountPath;
    private TextView tvAccountCount;

    private final ActivityResultLauncher<Intent> overlayLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                refreshStatus();
                refreshAccountInfo();
                if (WindowPermissionHelper.canDrawOverlays(this)
                        && DHAccessibilityService.getInstance() != null) {
                    OverlayController.get().showBall();
                }
            });

    private final ActivityResultLauncher<Intent> accessibilityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> refreshStatus());

    private final ActivityResultLauncher<Intent> importAccountLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleImportResult(result.getData()));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAccessibilityStatus = findViewById(R.id.tv_accessibility_status);
        tvOverlayStatus = findViewById(R.id.tv_overlay_status);
        btnAccessibility = findViewById(R.id.btn_open_accessibility);
        btnOverlay = findViewById(R.id.btn_open_overlay);
        btnShowBall = findViewById(R.id.btn_show_floating);
        btnImportAccount = findViewById(R.id.btn_import_account);
        btnRefreshAccount = findViewById(R.id.btn_refresh_account);
        tvAccountPath = findViewById(R.id.tv_account_path);
        tvAccountCount = findViewById(R.id.tv_account_count);

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
        btnImportAccount.setOnClickListener(v -> launchImport());
        btnRefreshAccount.setOnClickListener(v -> refreshAccountInfo());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        refreshAccountInfo();
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

    /**
     * 刷新账号文件路径与已加载的账号数量。
     *
     * <p>如果应用专属目录里没有 account.txt，但公共下载目录里有同名文件，
     * 会通过 {@link AccountFileLoader#loadAccounts(Context)} 触发一次 MediaStore
     * 兜底同步，把文件拷贝到应用专属目录后再展示。</p>
     */
    private void refreshAccountInfo() {
        File file = AccountFileLoader.locateAccountFile(this);
        if (file == null) {
            // 先调用一次 loadAccounts，触发公共下载目录 -> 应用专属目录的同步
            AccountFileLoader.loadAccounts(this);
            file = AccountFileLoader.locateAccountFile(this);
        }
        if (file == null) {
            tvAccountPath.setText(getString(R.string.account_path_none));
            tvAccountCount.setText(getString(R.string.account_count_zero));
            return;
        }
        tvAccountPath.setText(getString(R.string.account_path_public, file.getAbsolutePath()));
        List<Account> accounts = AccountFileLoader.loadAccounts(this);
        if (accounts.isEmpty()) {
            tvAccountCount.setText(getString(R.string.account_count_zero));
        } else {
            tvAccountCount.setText(getString(R.string.account_count, accounts.size()));
        }
    }

    /**
     * 拉起 SAF 让用户选择 account.txt，导入到应用专属目录。
     */
    private void launchImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"text/plain", "text/*", "application/octet-stream"});
        importAccountLauncher.launch(intent);
    }

    private void handleImportResult(@Nullable Intent data) {
        if (data == null) {
            return;
        }
        Uri uri = data.getData();
        if (uri == null) {
            return;
        }
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) {
                Toast.makeText(this, R.string.account_import_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            AccountFileLoader.copyToAppDir(this, is);
        } catch (Exception e) {
            Toast.makeText(this,
                    getString(R.string.account_import_failed, e.getMessage()),
                    Toast.LENGTH_LONG).show();
            return;
        }
        refreshAccountInfo();
        List<Account> accounts = AccountFileLoader.loadAccounts(this);
        Toast.makeText(this,
                getString(accounts.isEmpty()
                        ? R.string.account_import_none
                        : R.string.account_import_success, accounts.size()),
                Toast.LENGTH_SHORT).show();
    }
}
