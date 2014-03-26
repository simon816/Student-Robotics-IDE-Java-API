package org.srobo.ide.api;

public abstract class AbstractFile extends Module {

    protected String name;
    protected SRTree parentTree;
    protected SRProject project;

    public AbstractFile(RequestService con, SRProject project, SRTree parentTree, String name) {
        super("file", con);
        addRequiredData("team", project.getTeam().id);
        addRequiredData("project", project.getName());
        this.parentTree = parentTree;
        this.project = project;
        if (name != null)
            setName(name);
    }

    public final String getName() {
        return name;
    }

    public String getPath() {
        return parentTree.getPath() + getName();
    }

    public SRTree getParent() {
        return parentTree;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getHostProject() {
        return project.getName();
    }

    public abstract void delete() throws SRException;

    // public abstract void delete() throws SRException;
}
