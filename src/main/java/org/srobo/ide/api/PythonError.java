package org.srobo.ide.api;

import org.json.JSONObject;

public class PythonError {

    public final String level;
    public final int line;
    public final String message;

    PythonError(JSONObject error) {
        level = error.getString("level");
        line = error.getInt("lineNumber");
        message = error.getString("message");
    }

    @Override
    public String toString() {
        return "PythonError([" + level + "] " + message + " line " + line + ")";
    }
}
