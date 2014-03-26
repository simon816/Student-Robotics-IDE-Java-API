package org.srobo.ide.api;

import java.util.ArrayList;

import org.json.JSONArray;

public class SRTeam extends Module {

    public final String id;
    public final String name;
    public final boolean readOnly;
    private RequestService con;

    SRTeam(RequestService con, String id, String name, boolean readOnly) {
        super("team", con);
        this.id = id;
        this.name = name;
        this.readOnly = readOnly;
        addRequiredData("team", id);
        this.con = con;
    }

    public ArrayList<SRProject> listProjects() throws SRException {
        JSONArray projectlist = sendCommand("list-projects").getJSONArray("team-projects");
        ArrayList<SRProject> projects = new ArrayList<SRProject>(projectlist.length());
        for (int i = 0; i < projectlist.length(); i++) {
            projects.add(new SRProject(con, this, projectlist.getString(i)));
        }
        return projects;
    }

    public String toString() {
        return "SRTeam(" + id + " [" + name + "]" + (readOnly ? "readOnly" : "") + ")";
    }

    public String getName() {
        if (!name.isEmpty())
            return name;
        return id;
    }

    public SRProject newProject(String name) throws SRException {
       SRProject project = new SRProject(con, this, name);
       project.newProject();
       return project;
    }

}
