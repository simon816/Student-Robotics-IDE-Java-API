package org.srobo.ide.api;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class SRFile extends AbstractFile {
    public static enum State {
        Unmodified, Unstaged, Staged_del, Staged_mod, Staged_new;
    }

    private String content;
    private Date mtime;
    private State state;

    public SRFile(RequestService con, SRProject project, SRTree tree, String name, Long autosave) {
        super(con, project, tree, name);
        content = null;

        JSONObject staged;
        try {
            staged = project.getStagedFiles();
        } catch (SRException e) {
            e.printStackTrace();
            staged = new JSONObject();
        }

        if (autosave != null) {
            mtime = new Date(autosave * 1000);
            if (staged.has(getPath())) {
                state = getStateForId(staged.getString(getPath()));
            } else if (autosave == 0)
                state = State.Unmodified;
            else
                state = State.Unstaged;
            // timestamp in seconds, Date wants it in milliseconds
        } else {
            state = State.Unstaged;
            mtime = new Date(0);
        }
    }

    public static State getStateForId(String id) {
        switch (id.charAt(0)) {
        case 'M':
            return State.Staged_mod;
        case 'D':
            return State.Staged_del;
        case 'N':
            return State.Staged_new;
        }
        return null;
    }

    public String toString() {
        return "SRFile(" + name + " <" + getPath() + ">)";
    }

    public String getContents() {
        if (content != null) {
            return content;
        }
        try {
            JSONObject input = new JSONObject();
            input.put("path", getPath());
            input.put("rev", "HEAD");
            JSONObject output = sendCommand("get", input);
            if (output.has("autosaved")) {
                content = output.getString("autosaved");
            } else {
                if (!output.has("original")) {
                    content = "";
                } else {
                    Object original = output.get("original");
                    if (original instanceof String) {
                        content = (String) original;
                    } else {
                        content = "";
                    }
                }
            }
        } catch (SRException e) {
            e.printStackTrace();
            content = "";
        }
        // JSONObject decodes characters for us
        return content;
    }

    public State getState() {
        return state;
    }

    public void putContents(String content) {
        byte[] bytes;
        try {
            bytes = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            bytes = content.getBytes();
        }
        try {
            JSONObject input = new JSONObject();
            input.put("path", getPath());
            input.put("data", new String(bytes));
            sendCommand("put", input);
            if (state == State.Unmodified)
                state = State.Unstaged;
            this.content = new String(bytes, "UTF-8");
        } catch (SRException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.content = content;
        }
    }

    public Date getLastSavedDate() {
        return mtime;
    }

    public String getDiff(String text) throws SRException {
        JSONObject input = new JSONObject();
        input.put("hash", "HEAD");
        input.put("path", getPath());
        input.put("code", text);
        return sendCommand("diff", input).getString("diff");
    }

    public void stageFile(String id) throws SRException {
        // if (isStaged())
        // throw new SRException("Cannot stage already staged file");
        State state = getStateForId(id);
        if (state == null)
            throw new SRException("Unknown stage state " + id);
        JSONObject stagedFiles = project.getStagedFiles();
        stagedFiles.put(getPath(), id);
        project.putStagedFiles(stagedFiles);
        this.state = state;
    }

    public void unstageFile() throws SRException {
        if (!isStaged())
            throw new SRException("Cannot unstage an unstaged file");
        JSONObject stagedFiles = project.getStagedFiles();
        stagedFiles.remove(getPath());
        project.putStagedFiles(stagedFiles);
        state = State.Unstaged;
        content = null;
    }

    void fileCommited() {
        state = State.Unmodified;
    }

    public boolean revertChanges() throws SRException {
        if (state == State.Unmodified)
            throw new SRException("Cannot revert an unchanged file");
        JSONObject input = new JSONObject();
        JSONArray files = new JSONArray();
        files.put(getPath());
        input.put("files", files);
        input.put("revision", "HEAD");
        boolean success = sendCommand("co", input).getBoolean("success");
        // if (state == State.Unstaged) {
        // JSONObject test = new JSONObject();
        // test.put("path", getPath());
        // test.put("rev", "HEAD");
        // JSONObject output = sendCommand("get", test);
        // if (output.optBoolean("original", true) == false) {
        // JSONArray del = new JSONArray();
        // del.put(getPath());
        // // A checkout command will falsely succeed if the file was new and not staged.
        // // Just delete it as it was never added to the index.
        // delete(del);
        // }
        // }
        if (success) {
            state = State.Unmodified;
            content = null;
        }
        return success;
    }

    @Override
    public void delete() throws SRException {
        stageFile("D");
    }

    void delete(JSONArray files) throws SRException {
        JSONObject input = new JSONObject();
        input.put("files", files);
        sendCommand("del", input);
    }

    public String rename(String to) throws SRException {
        JSONObject input = new JSONObject();
        String oldPath = getPath();
        String newPath = oldPath.substring(0, oldPath.length() - getName().length()) + to;
        input.put("old-path", oldPath);
        input.put("new-path", newPath);
        JSONObject stagedFiles = project.getStagedFiles();
        stagedFiles.put(oldPath, "D");
        // if (state == State.Staged) {
        // stagedFiles.put(newPath, "N");
        // }
        project.putStagedFiles(stagedFiles);
        name = to;
        return sendCommand("mv", input).getString("message");
    }

    public boolean isStaged() {
        return state == State.Staged_del || state == State.Staged_mod || state == State.Staged_new;
    }
}
