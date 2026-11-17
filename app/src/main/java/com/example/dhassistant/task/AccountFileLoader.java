package com.example.dhassistant.task;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.dhassistant.util.LogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 账号文件加载器。
 *
 * <p>读取策略（统一在应用专属目录里读，绕开 Scoped Storage）：</p>
 * <ol>
 *     <li>应用专属外部目录：{@code getExternalFilesDir(DIRECTORY_DOWNLOADS)/account.txt}</li>
 *     <li>应用内部目录：{@code filesDir/Download/account.txt}</li>
 * </ol>
 *
 * <p>如果应用专属目录里没有，但公共下载目录里有名为 {@code account.txt} 的文件，
 * 会通过 {@link MediaStore}（API 29+）或 {@link java.io.File}（API ≤ 28）把该文件
 * 拷贝一份到应用专属目录，然后再读取。</p>
 *
 * <p>文件格式：每行一个账号，邮箱和密码之间用空格分隔。例如：</p>
 * <pre>
 *     test@outlook.com mypassword123
 *     user2@gmail.com  pass456
 * </pre>
 */
public final class AccountFileLoader {

    public static final String ACCOUNT_FILE_NAME = "account.txt";

    private static final String TAG = "AccountFileLoader";

    private AccountFileLoader() {
    }

    // ------------------------------------------------------------------
    // 公开 API
    // ------------------------------------------------------------------

    /**
     * 加载 account.txt 中保存的全部账号。
     *
     * <p>找不到文件或解析失败时返回空列表，永不抛异常。</p>
     */
    @NonNull
    public static List<Account> loadAccounts(@NonNull Context context) {
        File file = locateOrSyncAccountFile(context);
        if (file == null || !file.isFile()) {
            LogUtil.w(TAG + ": 未找到 account.txt");
            return Collections.emptyList();
        }
        List<Account> accounts;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            accounts = parseAccounts(reader);
        } catch (IOException e) {
            LogUtil.e(TAG + ": 读取 account.txt 失败: " + file.getAbsolutePath());
            LogUtil.e(e.getMessage(), e);
            return Collections.emptyList();
        }
        LogUtil.i(TAG + ": 已从 " + file.getAbsolutePath() + " 加载 " + accounts.size() + " 个账号");
        return accounts;
    }

    /**
     * 与 {@link #loadAccounts(Context)} 等价，保留旧签名以兼容已有调用方。
     */
    @NonNull
    public static List<Account> loadAccountsCached(@NonNull Context context) {
        return loadAccounts(context);
    }

    /**
     * 把 {@code input} 写入应用专属目录下的 account.txt。会覆盖已有文件。
     *
     * @return 写入后的文件，绝对路径
     */
    @NonNull
    public static File copyToAppDir(@NonNull Context context, @NonNull InputStream input)
            throws IOException {
        File target = resolveAppDirFile(context);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("无法创建目录: " + parent.getAbsolutePath());
        }
        try (OutputStream out = new FileOutputStream(target)) {
            byte[] buf = new byte[8 * 1024];
            int n;
            while ((n = input.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.flush();
        }
        LogUtil.i(TAG + ": 已导入 account.txt -> " + target.getAbsolutePath());
        return target;
    }

    /**
     * 仅返回应用专属目录里 account.txt 的路径（不做 MediaStore 同步）。
     * 用于在 UI 上展示"当前读取位置"。
     */
    @Nullable
    public static File locateAccountFile(@NonNull Context context) {
        File primary = resolveAppDirFile(context);
        if (primary.isFile()) {
            return primary;
        }
        File secondary = resolveInternalDirFile(context);
        return secondary.isFile() ? secondary : null;
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /**
     * 查找 account.txt；若应用专属目录里没有，尝试从公共下载目录同步一次。
     */
    @Nullable
    private static File locateOrSyncAccountFile(@NonNull Context context) {
        File inApp = locateAccountFile(context);
        if (inApp != null) {
            return inApp;
        }
        // 应用专属目录没有 -> 兜底从公共下载目录同步一次
        try {
            if (syncFromPublicDownloads(context)) {
                return locateAccountFile(context);
            }
        } catch (IOException e) {
            LogUtil.w(TAG + ": 从公共下载目录同步 account.txt 失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 应用专属外部目录下的 account.txt 路径（不一定存在）。
     */
    @NonNull
    private static File resolveAppDirFile(@NonNull Context context) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            dir = new File(context.getFilesDir(), "Download");
        }
        return new File(dir, ACCOUNT_FILE_NAME);
    }

    /**
     * 应用内部目录下的 account.txt 路径（不一定存在）。
     */
    @NonNull
    private static File resolveInternalDirFile(@NonNull Context context) {
        return new File(new File(context.getFilesDir(), "Download"), ACCOUNT_FILE_NAME);
    }

    /**
     * 把公共下载目录里名为 {@link #ACCOUNT_FILE_NAME} 的文件拷贝到应用专属目录。
     *
     * @return 是否成功拷贝
     */
    private static boolean syncFromPublicDownloads(@NonNull Context context) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return syncFromPublicDownloadsQ(context);
        }
        return syncFromPublicDownloadsLegacy(context);
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private static boolean syncFromPublicDownloadsQ(@NonNull Context context) throws IOException {
        Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.IS_PENDING,
        };
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
        String[] args = {ACCOUNT_FILE_NAME};
        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(collection, projection, selection, args, null)) {
            if (cursor == null) {
                return false;
            }
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Downloads.DISPLAY_NAME));
                if (!ACCOUNT_FILE_NAME.equals(name)) {
                    continue;
                }
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID));
                Uri item = ContentUris.withAppendedId(collection, id);
                try (InputStream in = resolver.openInputStream(item)) {
                    if (in == null) {
                        continue;
                    }
                    copyToAppDir(context, in);
                    return true;
                } catch (FileNotFoundException e) {
                    LogUtil.w(TAG + ": MediaStore 文件不可读: " + item);
                }
            }
        }
        return false;
    }

    /**
     * 仅 API ≤ 28：通过传统文件 API 直接读公共下载目录。
     * API ≥ 29 不会走到这里。
     */
    @SuppressWarnings("deprecation")
    private static boolean syncFromPublicDownloadsLegacy(@NonNull Context context) throws IOException {
        File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File src = new File(publicDir, ACCOUNT_FILE_NAME);
        if (!src.isFile()) {
            return false;
        }
        try (InputStream in = new FileInputStream(src)) {
            copyToAppDir(context, in);
            return true;
        }
    }

    /**
     * 解析账号文件。空行 / {@code #} 开头跳过；每行用第一个空格切分邮箱和密码。
     */
    @NonNull
    private static List<Account> parseAccounts(@NonNull BufferedReader reader) throws IOException {
        List<Account> result = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int sep = trimmed.indexOf(' ');
            String email;
            String password;
            if (sep < 0) {
                email = trimmed;
                password = "";
            } else {
                email = trimmed.substring(0, sep);
                password = trimmed.substring(sep + 1).trim();
            }
            if (email.isEmpty()) {
                continue;
            }
            result.add(new Account(email, password));
        }
        return result;
    }
}
