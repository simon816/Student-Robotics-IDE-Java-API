package org.srobo.ide.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SRFileLog extends SRLog {

    public final int pages;
    public final List<String> authors;

    SRFileLog(JSONObject log) {
        super(log.getJSONArray("log"));
        JSONArray authors = log.getJSONArray("authors");
        pages = log.getInt("pages");
        ArrayList<String> authorList = new ArrayList<String>();
        for (int i = 0, len = authors.length(); i < len; i++) {
            authorList.add(authors.getString(i));
        }
        this.authors = Collections.unmodifiableList(authorList);
    }

}
