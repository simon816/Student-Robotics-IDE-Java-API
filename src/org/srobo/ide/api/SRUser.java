package org.srobo.ide.api;

import org.json.JSONArray;
import org.json.JSONObject;

public class SRUser extends Module {
    private String username;
    private String email;
    private SRTeam[] teams;
    private boolean isAdmin;
    private SRTeam currentTeam;
    private String displayName;
    private JSONObject settings;

    public SRUser(RequestService con, String username) throws SRException {
        super("user", con);
        con._setUser(this);
        this.username = username;
        JSONObject output = sendCommand("info");
        displayName = output.getString("display-name");
        email = output.getString("email");
        JSONArray teams = output.getJSONArray("teams");
        this.teams = new SRTeam[teams.length()];
        for (int i = 0; i < teams.length(); i++) {
            JSONObject team = teams.getJSONObject(i);
            this.teams[i] = new SRTeam(con, team.getString("id"), team.getString("name"), team.getBoolean("readOnly"));
        }
        isAdmin = output.getBoolean("is-admin");
        settings = output.getJSONObject("settings");
        switchTeam(0);
    }

    public SRTeam switchTeam(int i) {
        if (i >= 0 && i < teams.length) {
            currentTeam = teams[i];
        }
        return getTeam();
    }

    public SRTeam getTeam() {
        return currentTeam;
    }

    public SRTeam[] getTeams() {
        return teams;
    }

    public String getName() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String toString() {
        return "SRUser(" + username + " <" + email + "> activeTeam: " + currentTeam + ")";
    }

    public Object getSetting(String key) {
        synchronized (settings) {
            return settings.opt(key);
        }
    }

    public void putSetting(String key, Object value) throws SRException {
        synchronized (settings) {
            settings.put(key, value);
            JSONObject input = new JSONObject();
            input.put("settings", settings);
            sendCommand("settings-put", input);
        }
    }
}
