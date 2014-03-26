package org.srobo.ide.api;

import java.net.MalformedURLException;
import java.net.URL;

public class SRAPI {
    private RequestService con;
    private Auth auth;
    private SRUser currentUser;
    public final static String VERSION = "0.1";

    public SRAPI() {
        try {
            // con = new RequestService(new URL("http://localhost/ide/control.php/"));
            con = new RequestService(new URL("https://www.studentrobotics.org/ide/control.php/"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        auth = new Auth(con);
    }

    public SRUser login(String username, String password) throws SRException {
        auth.login(username, password);
        currentUser = new SRUser(con, username);
        return currentUser;
    }

    public SRUser Tlogin(String username, String token) throws SRException {
        con.setToken(token);
        try {
            currentUser = new SRUser(con, username);
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
        if (l)
            con.deleteToken();
        return l;
    }

    public String getToken() {
        return con.getToken();
    }

    public SRUser getCurrentUser() {
        return currentUser;
    }
}
