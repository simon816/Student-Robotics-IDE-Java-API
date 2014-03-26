package org.srobo.ide.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class SRTree extends AbstractFile {

    private ArrayList<AbstractFile> tree;
    private RequestService con;

    public SRTree(RequestService con, SRProject project, SRTree parentTree, String name) {
        super(con, project, parentTree, name);
        this.con = con;
        tree = new ArrayList<AbstractFile>();
    }

    public AbstractFile[] getEntries() {
        return tree.toArray(new AbstractFile[tree.size()]);
    }

    public AbstractFile[] listFiles() throws SRException {
        JSONObject input = new JSONObject();
        input.put("path", getPath());
        JSONArray files = sendCommand("list", input).getJSONArray("files");
        tree = new ArrayList<AbstractFile>();
        for (int i = 0; i < files.length(); i++) {
            String fileName = files.getString(i);
            if (fileName.endsWith("/")) {
                tree.add(new SRTree(con, project, this, fileName));
            } else {
                tree.add(new SRFile(con, project, this, fileName, null));
            }
        }
        return getEntries();
    }

    public void addFile(AbstractFile file) {
        tree.add(file);
    }

    @Override
    public String toString() {
        return "SRTree(" + name + " <" + getPath() + "> " + tree + ")";
    }

    @Override
    public String getPath() {
        if (isRoot())
            return "";
        return super.getPath() + "/";
    }

    public boolean isRoot() {
        return parentTree == null;
    }

    public ArrayList<SRFile> getStagedChanges() {
        ArrayList<SRFile> staged = new ArrayList<SRFile>();
        for (AbstractFile entry : tree) {
            if (entry instanceof SRFile) {
                if (((SRFile) entry).isStaged()) {
                    staged.add((SRFile) entry);
                }
            } else if (entry instanceof SRTree) {
                staged.addAll(((SRTree) entry).getStagedChanges());
            }
        }
        return staged;
    }

    public JSONObject listAllFiles() throws Exception {
        if (isRoot()) {
            JSONObject input = new JSONObject();
            input.put("rev", "HEAD");
            return sendCommand("compat-tree");
        }
        throw new Exception("Cannot call listAllFiles from within tree");
    }

    @Override
    public void delete() throws SRException {
        for (AbstractFile file : tree) {
            file.delete();
            tree.remove(file);
        }
    }

    public SRTree makeNewTree(String name) throws SRException {
        JSONObject input = new JSONObject();
        input.put("path", getPath() + name + "/");
        if (sendCommand("mkdir", input).getInt("success") == 1) {
            return new SRTree(con, project, this, name);
        }
        return null;
    }

    public SRFile makeNewFile(String name) throws SRException {
        JSONObject input = new JSONObject();
        input.put("path", getPath() + name);
        sendCommand("new", input);
        SRFile file = new SRFile(con, project, this, name, null);
        file.putContents(""); // The command 'new' does not seem to work, the 'put' command must be called
        addFile(file);
        return file;
    }
}
