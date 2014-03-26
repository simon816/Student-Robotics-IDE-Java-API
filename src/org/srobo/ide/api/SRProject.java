package org.srobo.ide.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.srobo.ide.api.SRFile.State;

public class SRProject extends Module {

    private SRTeam team;
    private String name;
    private RequestService con;
    private SRTree files;

    public SRProject(RequestService con, SRTeam hostTeam, String name) {
        super("proj", con);
        addRequiredData("team", hostTeam.id);
        addRequiredData("project", name);
        team = hostTeam;
        this.name = name;
        this.con = con;
        files = null;
    }

    public SRTeam getTeam() {
        return team;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "SRProject(" + name + " <" + team + ">)";
    }

    public SRTree listAllFiles(boolean refresh) throws Exception {
        if (!refresh && files != null) {
            return files;
        }
        JSONObject input;
        files = new SRTree(con, this, null, name);
        input = files.listAllFiles();
        JSONArray tree = input.getJSONArray("tree");
        walkTreeObject(tree, files);
        return files;
    }

    public SRTree listAllFiles() throws Exception {
        return listAllFiles(false);
    }

    private void walkTreeObject(JSONArray tree, SRTree parent) {
        for (int i = 0; i < tree.length(); i++) {
            JSONObject element = tree.getJSONObject(i);
            AbstractFile file = null;
            if (element.get("kind").equals("FOLDER")) {
                file = new SRTree(con, this, parent, element.getString("name"));
                walkTreeObject(element.getJSONArray("children"), (SRTree) file);
            } else if (element.get("kind").equals("FILE")) {
                file = new SRFile(con, this, parent, element.getString("name"), element.getLong("autosave"));
            } else {
                continue;
            }
            parent.addFile(file);
        }
    }

    public JSONObject commit(String message) throws SRException {
        JSONObject input = new JSONObject();
        input.put("message", message);
        ArrayList<SRFile> staged = files.getStagedChanges();
        JSONArray paths = new JSONArray();
        JSONArray delete = new JSONArray();
        SRFile _file_res = null;
        for (SRFile file : staged) {
            paths.put(file.getPath());
            if (file.getState() == State.Staged_del) {
                delete.put(file.getPath());
                if (_file_res == null)
                    _file_res = file;
            }
        }
        if (delete.length() > 0) {
            _file_res.delete(delete);
        }
        input.put("paths", paths);
        JSONObject output = sendCommand("commit", input);
        for (SRFile file : staged) {
            file.fileCommited();
        }
        putStagedFiles(new JSONObject());
        return output;
    }

    public JSONObject getStagedFiles() throws SRException {
        JSONObject allStaged = (JSONObject) con.getUser().getSetting("files.staged");
        JSONObject staged;
        if (allStaged == null) {
            allStaged = new JSONObject();
            allStaged.put(name, staged = new JSONObject());
            con.getUser().putSetting("files.staged", allStaged);
        } else {
            staged = allStaged.optJSONObject(name);
        }
        if (staged == null) {
            putStagedFiles(staged = new JSONObject());
        }
        return staged;
    }

    public void putStagedFiles(JSONObject stagedFiles) throws SRException {
        JSONObject allStaged = (JSONObject) con.getUser().getSetting("files.staged");
        allStaged.put(name, stagedFiles);
        con.getUser().putSetting("files.staged", allStaged);
    }

    public void deleteProject() throws SRException {
        sendCommand("del");
        JSONObject allStaged = (JSONObject) con.getUser().getSetting("files.staged");
        allStaged.remove(name);
        con.getUser().putSetting("files.staged", allStaged);
    }

    public void newProject() throws SRException {
        sendCommand("new");
    }
}
