package com.aymane.quickchat.client;

public interface Authenticator {
    void startAuthentication(String username, String password, String authType);
}
