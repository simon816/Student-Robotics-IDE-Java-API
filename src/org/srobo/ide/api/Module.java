package org.srobo.ide.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

public abstract class Module {
    private final String module;
    private RequestService con;
    private HashMap<String, Object> required;

    public Module(String module, RequestService con) {
        this.module = module;
        this.con = con;
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
            Iterator<?> i = required.entrySet().iterator();
            while (i.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, Object> e = (Map.Entry<String, Object>) i.next();
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
}