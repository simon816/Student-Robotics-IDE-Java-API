package org.srobo.ide.api;

import org.json.JSONArray;

public class SRException extends Exception {

    private int code;
    private String message;
    private String stacktrace;

    public SRException(JSONArray error) {
        super();
        code = error.getInt(0);
        message = error.getString(1);
        stacktrace = error.getString(2);
    }

    public SRException(Object error) {
        this((JSONArray) error);
    }

    public SRException(String msg) {
        message = msg;
        code = -1;
        stacktrace = "";
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPHPStackTrace() {
        return stacktrace;
    }

}
