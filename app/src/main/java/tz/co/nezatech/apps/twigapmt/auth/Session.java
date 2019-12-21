package tz.co.nezatech.apps.twigapmt.auth;

public interface Session {
    boolean isLoggedIn();

    void saveToken(String token);

    String getToken();

    void saveUsername(String username);

    String getUsername();

    void savePassword(String password);

    String getPassword();

    void invalidate();
}
