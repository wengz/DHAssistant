package com.example.dhassistant.ui.overlay;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dhassistant.R;
import com.example.dhassistant.task.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号选择弹窗。
 *
 * <p>点击 {@link com.example.dhassistant.task.Register} 等需要选择账号的任务时弹出，
 * 列出 {@code account.txt} 中保存的全部账号。</p>
 */
final class AccountPickerDialog {

    interface OnAccountSelectedListener {
        void onAccountSelected(@NonNull Account account);
    }

    private AlertDialog dialog = null;

    AccountPickerDialog(@NonNull Context context,
                        @NonNull List<Account> accounts,
                        @NonNull OnAccountSelectedListener listener) {
        ListView listView = new ListView(context);
        AccountAdapter adapter = new AccountAdapter(context, accounts);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Account selected = adapter.getItem(position);
            if (selected == null) {
                return;
            }
            dialog.dismiss();
            listener.onAccountSelected(selected);
        });

        dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.account_picker_title)
                .setView(listView)
                .setNegativeButton(R.string.account_picker_cancel, (d, w) -> d.dismiss())
                .create();

        // 由于该对话框是从无障碍服务（没有 Activity/窗口 token）发起的，
        // 必须将其窗口类型设置为系统悬浮窗类型，否则 AlertDialog.show()
        // 会因为拿不到有效的 window token 而抛出 BadTokenException。
        Window window = dialog.getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }
    }

    void show() {
        if (!WindowPermissionHelper.canDrawOverlays(dialog.getContext())) {
            return;
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 简单账号列表 Adapter，邮箱 + 部分掩码的密码用于展示。
     */
    private static final class AccountAdapter extends BaseAdapter implements Filterable {

        private final Context context;
        private final List<Account> accounts;

        AccountAdapter(@NonNull Context context, @NonNull List<Account> accounts) {
            this.context = context;
            this.accounts = new ArrayList<>(accounts);
        }

        @Override
        public int getCount() {
            return accounts.size();
        }

        @Override
        public Account getItem(int position) {
            return accounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.layout_account_item, parent, false);
            }
            Account account = accounts.get(position);
            TextView emailView = convertView.findViewById(R.id.account_item_email);
            TextView passwordView = convertView.findViewById(R.id.account_item_password);
            emailView.setText(account.getEmail());
            passwordView.setText(maskPassword(account.getPassword()));
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return null;
        }

        private static String maskPassword(@NonNull String password) {
            int len = password.length();
            if (len <= 2) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < len; i++) sb.append('•');
                return sb.toString();
            }
            if (len <= 4) {
                return password.charAt(0)
                        + "•".repeat(len - 2)
                        + password.charAt(len - 1);
            }
            return password.substring(0, 2) + "••••" + password.substring(len - 2);
        }
    }
}
