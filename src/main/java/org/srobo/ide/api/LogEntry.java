package org.srobo.ide.api;

import java.util.Date;

import org.json.JSONObject;

public class LogEntry {

    public final String author;
    public final String hash;
    public final String message;
    public final Date time;

    LogEntry(JSONObject logEntry) {
        author = logEntry.getString("author");
        hash = logEntry.getString("hash");
        message = logEntry.getString("message");
        time = new Date(logEntry.getLong("time") * 1000);
    }

    @Override
    public String toString() {
        return "LogEntry(author=" + author + ", hash=" + hash + ", message=" + message + ", time=" + time + ")";
    }

}
