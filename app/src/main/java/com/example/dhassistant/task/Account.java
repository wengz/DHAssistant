package com.example.dhassistant.task;

import androidx.annotation.NonNull;

/**
 * 账号信息（邮箱 + 密码）。
 */
public final class Account {

    private final String email;
    private final String password;

    public Account(@NonNull String email, @NonNull String password) {
        this.email = email;
        this.password = password;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @NonNull
    @Override
    public String toString() {
        return email;
    }
}
