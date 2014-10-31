package org.srobo.ide.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.srobo.ide.api.SRFile.StagedType;
import org.srobo.ide.api.SRFile.State;

public class SRProject extends Module {

    private SRTeam team;
    private String name;
    private SRTree files;

    public SRProject(SRTeam hostTeam, String name) {
        super("proj");
        addRequiredData("team", hostTeam.id);
        addRequiredData("project", name);
        team = hostTeam;
        this.name = name;
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
        files = new SRTree(this, null, name);
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
                file = new SRTree(this, parent, element.getString("name"));
                walkTreeObject(element.getJSONArray("children"), (SRTree) file);
            } else if (element.get("kind").equals("FILE")) {
                file = new SRFile(this, parent, element.getString("name"), element.getLong("autosave"));
            } else {
                continue;
            }
            parent.addFile(file);
        }
    }

    public String commit(String message) throws SRException {
        JSONObject input = new JSONObject();
        input.put("message", message);
        ArrayList<SRFile> staged = files.getStagedChanges();
        if (staged.size() == 0)
            throw new SRException("Nothing to commit");
        JSONArray paths = new JSONArray();
        JSONArray delete = new JSONArray();
        JSONArray unindexedDeletions = new JSONArray();
        SRFile _file_res = null;
        for (SRFile file : staged) {
            if (file.getStagedType() == StagedType.DELETE) {
                delete.put(file.getPath());
                if (file.getState() == State.NEW) {
                    // File being deleted never existed
                    // Limitation (with IDE): file needs to be indexed before being deleted
                    unindexedDeletions.put(file.getPath());
                    file.putContents("");
                }
                if (_file_res == null)
                    _file_res = file;
            }
            paths.put(file.getPath());
        }
        if (unindexedDeletions.length() > 0) {
            // A commit is created to allow the files to be deleted
            JSONObject input2 = new JSONObject();
            input2.put("message", "Index file(s) to delete");
            input2.put("paths", unindexedDeletions);
            sendCommand("commit", input2);
        }
        if (delete.length() > 0) {
            _file_res.actuallyDeleteFiles(delete);
        }
        String commit = null;
        if (paths.length() > 0) {
            input.put("paths", paths);
            JSONObject output = sendCommand("commit", input);
            commit = output.getString("commit");
        }
        for (SRFile file : staged) {
            // TODO check this
            file.markCommited();
        }
        putStagedFiles(new JSONObject());
        return commit;
    }

    public JSONObject getStagedFiles() throws SRException {
        JSONObject allStaged = (JSONObject) SRAPI.getCurrentUser().getSetting("files.staged");
        JSONObject staged;
        if (allStaged == null) {
            allStaged = new JSONObject();
            allStaged.put(name, staged = new JSONObject());
            SRAPI.getCurrentUser().putSetting("files.staged", allStaged);
        } else {
            staged = allStaged.optJSONObject(name);
        }
        if (staged == null) {
            putStagedFiles(staged = new JSONObject());
        }
        return staged;
    }

    public void putStagedFiles(JSONObject stagedFiles) throws SRException {
        JSONObject allStaged = (JSONObject) SRAPI.getCurrentUser().getSetting("files.staged");
        allStaged.put(name, stagedFiles);
        SRAPI.getCurrentUser().putSetting("files.staged", allStaged);
    }

    public void deleteProject() throws SRException {
        sendCommand("del");
        JSONObject allStaged = (JSONObject) SRAPI.getCurrentUser().getSetting("files.staged");
        allStaged.remove(name);
        SRAPI.getCurrentUser().putSetting("files.staged", allStaged);
    }

    public void newProject() throws SRException {
        sendCommand("new");
    }

    public void getZip(String revision, final InputStreamHandler<ZipInputStream> callback) throws SRException {
        if (revision == null)
            revision = "HEAD";
        JSONObject input = new JSONObject();
        input.put("rev", revision);
        JSONObject output = sendCommand("co", input);
        getResourceAsStream(output.getString("url"), new InputStreamHandler<InputStream>() {

            @Override
            public void handleData(InputStream is) throws IOException {
                callback.handleData(new ZipInputStream(is));
            }
        });
    }

    public SRLog getLog() throws SRException {
        return new SRLog(sendCommand("log").getJSONArray("log"));
    }
}
