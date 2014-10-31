package org.srobo.ide.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public class SRTeam extends Module {

    public final String id;
    public final String name;
    public final boolean readOnly;
    public final SRPoll poll;

    SRTeam(String id, String name, boolean readOnly) {
        super("team");
        this.id = id;
        this.name = name;
        this.readOnly = readOnly;
        addRequiredData("team", id);
        poll = new SRPoll(this, 1000);
    }

    public List<SRProject> listProjects() throws SRException {
        JSONArray projectlist = sendCommand("list-projects").getJSONArray("team-projects");
        ArrayList<SRProject> projects = new ArrayList<SRProject>(projectlist.length());
        for (int i = 0; i < projectlist.length(); i++) {
            projects.add(new SRProject(this, projectlist.getString(i)));
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
        SRProject project = new SRProject(this, name);
        project.newProject();
        return project;
    }
}
