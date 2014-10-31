package org.srobo.ide.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SRFile extends AbstractFile {
    // public static enum State {
    // Unmodified, Unstaged, Staged_del, Staged_mod, Staged_new;
    // }

    public static enum State {
        /**
         * The file is not in the master repo
         */
        NEW,

        /**
         * The file is different to the master repo
         */
        MODIFIED,

        /**
         * The file is the same as the master repo
         */
        UNCHANGED
    }

    public static enum StagedType {
        /**
         * The file is not staged
         */
        UNSTAGED('U'),

        /**
         * The file is staged for the modifications
         */
        MODIFY('M'),

        /**
         * The file is staged for creation
         */
        CREATE('N'),

        /**
         * The file is staged for deletion
         */
        DELETE('D');

        private final char id;

        StagedType(char id) {
            this.id = id;
        }

        public final String getId() {
            return new String(new char[] { id });
        }

        public static final StagedType fromId(char id) {
            for (StagedType type : values()) {
                if (id == type.id)
                    return type;
            }
            return null;
        }

        public static final StagedType fromId(String string) {
            return fromId(string.charAt(0));
        }
    }

    private Date mtime;
    private State state;
    private StagedType staging;

    public SRFile(SRProject project, SRTree tree, String name, Long autosave) {
        super(project, tree, name);

        if (autosave != null) {
            JSONObject staged;
            try {
                staged = project.getStagedFiles();
            } catch (SRException e) {
                e.printStackTrace();
                staged = new JSONObject();
            }
            mtime = new Date(autosave * 1000);
            if (staged.has(getPath())) {
                staging = StagedType.fromId(staged.getString(getPath()));
                if (staging == StagedType.CREATE)
                    state = State.NEW;
                else
                    state = State.MODIFIED;
            } else {
                staging = StagedType.UNSTAGED;
                if (autosave == 0) {
                    state = State.UNCHANGED;
                } else {
                    // TODO This is messy
                    try {
                        if (getContents(false) == null)
                            state = State.NEW;
                        else
                            state = State.MODIFIED;
                    } catch (SRException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            state = State.NEW;
            staging = StagedType.UNSTAGED;
            mtime = new Date(0);
        }
    }

    public String toString() {
        return "SRFile(" + getName() + " <" + getPath() + ">)";
    }

    public String getContents(boolean useAutosave) throws SRException {
        String content;
        JSONObject input = new JSONObject();
        input.put("path", getPath());
        input.put("rev", "HEAD");
        JSONObject output = sendCommand("get", input);
        if (useAutosave && output.has("autosaved")) {
            content = output.getString("autosaved");
        } else {
            if (!output.has("original")) {
                content = null;
            } else {
                Object orig = output.get("original");
                if (orig instanceof String)
                    content = (String) orig;
                else
                    content = null;
            }
        }

        return content;
    }

    public String getContents() throws SRException {
        return getContents(true);
    }

    public State getState() {
        return state;
    }

    public StagedType getStagedType() {
        return staging;
    }

    public boolean isStaged() {
        return staging != StagedType.UNSTAGED;
    }

    public boolean isDifferent() {
        return state != State.UNCHANGED;
    }

    public void putContents(String content) throws SRException {
        JSONObject input = new JSONObject();
        input.put("path", getPath());
        input.put("data", content);
        sendCommand("put", input);
        // TODO Note: this is expensive
        String masterContent = getContents(false);
        if (content.equals(masterContent)) {
            state = State.UNCHANGED;
        } else if (masterContent != null) {
            state = State.MODIFIED;
        } else {
            state = State.NEW;
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

    public void setStaged(StagedType type) throws SRException {
        JSONObject stagedFiles = project.getStagedFiles();
        if (type == StagedType.UNSTAGED) {
            if (stagedFiles.has(getPath()))
                stagedFiles.remove(getPath());
        } else {
            stagedFiles.put(getPath(), type.getId());
        }
        project.putStagedFiles(stagedFiles);
        this.staging = type;
    }

    void markCommited() {
        state = State.UNCHANGED;
        staging = StagedType.UNSTAGED;
    }

    public boolean revertChanges() throws SRException {
        if (state == State.UNCHANGED)
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
        // // A checkout command will falsely succeed if the file was new and
        // not staged.
        // // Just delete it as it was never added to the index.
        // delete(del);
        // }
        // }
        if (success) {
            state = State.UNCHANGED;
        }
        return success;
    }

    @Override
    public void delete() throws SRException {
        setStaged(StagedType.DELETE);
    }

    void actuallyDeleteFiles(JSONArray files) throws SRException {
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
        project.putStagedFiles(stagedFiles);
        setName(to);
        return sendCommand("mv", input).getString("message");
    }

    public SRFileLog getLog(int number, int offset, String user) throws SRException {
        JSONObject input = new JSONObject();
        input.put("path", getPath());
        input.put("number", number);
        input.put("offset", offset);
        input.put("user", user);
        return new SRFileLog(sendCommand("log", input));
    }

    public SRFileLog getLog(int number, int offset, SRUser user) throws SRException {
        return getLog(number, offset, user != null ? user.getName() + " <" + user.getEmail() + ">" : null);
    }

    public SRFileLog getLog(int number) throws SRException {
        return getLog(number, 0, (String) null);
    }

    public SRFileLog getLog() throws SRException {
        return getLog(10, 0, (String) null);
    }

    public List<PythonError> lint(String revision, boolean useAutosave) throws SRException {
        if (!this.getName().endsWith(".py"))
            throw new SRException("Only Python files can be 'linted'");
        JSONObject input = new JSONObject();
        input.put("path", getPath());
        input.put("rev", revision);
        input.put("autosave", useAutosave);
        JSONArray errors = sendCommand("lint", input).getJSONArray("errors");
        List<PythonError> lintErrors = new ArrayList<PythonError>();
        for (int i = 0, len = errors.length(); i < len; i++) {
            lintErrors.add(new PythonError(errors.getJSONObject(i)));
        }
        return lintErrors;
    }

    public List<PythonError> lint() throws SRException {
        return lint(null, true);
    }
}
