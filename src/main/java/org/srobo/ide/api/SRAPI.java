package org.srobo.ide.api;

import java.net.MalformedURLException;

public class SRAPI {
    static final RequestService con;
    private Auth auth;
    private static SRUser currentUser;
    public final static String VERSION = "0.4";

    static {
        try {
            // con = new RequestService("http://localhost/", "control.php/");
            con = new RequestService("https://www.studentrobotics.org/ide/", "control.php/");
        } catch (MalformedURLException e) {
            // never happens
            throw new RuntimeException(e);
        }
    }

    public SRAPI() {
        auth = new Auth();
    }

    public SRUser login(String username, String password) throws SRException {
        auth.login(username, password);
        currentUser = new SRUser(username);
        return currentUser;
    }

    public SRUser Tlogin(String username, String token) throws SRException {
        con.setToken(token);
        try {
            currentUser = new SRUser(username);
            auth._setLogin();
        } catch (SRException e) {
            if (e.getCode() == ErrorConstants.E_BAD_AUTH_TOKEN.code)
                con.deleteToken();
            throw e;
        }
        return currentUser;
    }

    public boolean logout() {
        boolean l = auth.logout();
        if (l) {
            con.deleteToken();
            if (currentUser != null) {
                for (SRTeam team : currentUser.getTeams()) {
                    team.poll.stop();
                }
            }
        }
        return l;
    }

    public String getToken() {
        return con.getToken();
    }

    public static SRUser getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(SRUser user) {
        currentUser = user;
    }
}
