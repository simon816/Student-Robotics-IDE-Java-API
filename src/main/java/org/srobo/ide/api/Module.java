package org.srobo.ide.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

public abstract class Module {
    private final String module;
    private final RequestService con = SRAPI.con;
    private Map<String, Object> required;

    public Module(String moduleName) {
        this.module = moduleName;
        this.required = null;
    }

    protected void addRequiredData(String key, Object value) {
        if (required == null)
            required = new HashMap<String, Object>();
        required.put(key, value);
    }

    protected JSONObject sendCommand(String command, JSONObject input) throws SRException {
        if (required != null) {
            if (input == null)
                input = new JSONObject();
            Iterator<Map.Entry<String, Object>> i = required.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, Object> e = i.next();
                Object value = e.getValue();
                input.put(e.getKey(), value);
            }
        }
        JSONObject output = con.request(module, command, input);
        if (output.has("error")) {
            throw new SRException(output.get("error"));
        }
        return output;
    }

    protected JSONObject sendCommand(String command) throws SRException {
        return sendCommand(command, null);
    }

    public void getResourceAsStream(String path, InputStreamHandler<InputStream> callback) {
        con.getResourceAsStream(path, callback);
    }
}