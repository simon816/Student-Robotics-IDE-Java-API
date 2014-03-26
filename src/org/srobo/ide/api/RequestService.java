package org.srobo.ide.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

class RequestService {
    private static final String TOKEN_NAME = "token";
    private final URL controlURL;
    private Map<String, List<String>> headers;
    private String token;
    private SRUser cUser;

    public RequestService(URL control) {
        controlURL = control;
        cUser = null;
    }

    private String postRequest(String path, String data) {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(controlURL, path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
            connection.setRequestProperty("User-Agent", "SRAPI/" + SRAPI.VERSION + "");
            if (token != null) {
                connection.setRequestProperty("Cookie", TOKEN_NAME + "=" + token);
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(data);
            output.flush();
            output.close();

            headers = connection.getHeaderFields();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();
            if (token == null || token.equals("")) {
                setToken();
            }
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private void setToken() {
        String cookie = getHeader("Set-Cookie");
        if (cookie != null) {
            List<HttpCookie> cookieData = HttpCookie.parse(cookie);
            for (HttpCookie c : cookieData) {
                if (c.getName().toLowerCase().equals(TOKEN_NAME)) {
                    token = c.getValue();
                }
            }
        }
    }

    protected void setToken(String tok) {
        token = tok;
    }

    private JSONObject putRequest(String path, String data) {
        String text = postRequest(path, data);
        if (text == null)
            return null;
        return new JSONObject(text);
    }

    public JSONObject request(String module, String command, Object jsondata) {
        String jtext = (jsondata != null) ? JSONObject.valueToString(jsondata) : "";
        return putRequest(module + "/" + command, jtext);
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getAllHeadersFor(String key) {
        return headers.get(key);
    }

    public String getHeader(String key) {
        if (headers.containsKey(key)) {
            return headers.get(key).get(0);
        }
        return null;
    }

    public Set<String> listHeaders() {
        return headers.keySet();
    }

    public String getToken() {
        return token;
    }

    public boolean deleteToken() {
        if (token == null)
            return false;
        token = null;
        return true;
    }

    public void _setUser(SRUser user) {
        cUser = user;
    }
    public SRUser getUser() {
        return cUser;
    }
}