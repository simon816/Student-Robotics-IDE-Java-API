package org.srobo.ide.api;

import org.json.JSONObject;

public class Auth extends Module {

    private boolean loggedin;

    public Auth() {
        super("auth");
        loggedin = false;
    }

    public String login(String username, String password) throws SRException {
        JSONObject input = new JSONObject();
        input.put("username", username);
        input.put("password", password);
        JSONObject output;
        output = sendCommand("authenticate", input);
        loggedin = true;
        return output.getString("display-name");
    }

    public boolean logout() {
        if (loggedin) {
            try {
                sendCommand("deauthenticate");
                return true;
            } catch (SRException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    void _setLogin() {
        loggedin = true;
    }
}
