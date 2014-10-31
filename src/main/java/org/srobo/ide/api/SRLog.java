package org.srobo.ide.api;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;

public class SRLog {

    private final ArrayList<LogEntry> log;

    SRLog(JSONArray logArray) {
        log = new ArrayList<LogEntry>();
        for (int i = 0, len = logArray.length(); i < len; i++) {
            log.add(new LogEntry(logArray.getJSONObject(i)));
        }
    }

    public Iterator<LogEntry> iterator() {
        return log.iterator();
    }

    public LogEntry get(int index) {
        return log.get(index);
    }

    public int count() {
        return log.size();
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + log.toString() + ")";
    }
}
