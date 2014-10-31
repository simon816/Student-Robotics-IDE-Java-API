package org.srobo.ide.api;

public abstract class AbstractFile extends Module {

    private String name;
    private final SRTree parentTree;
    protected final SRProject project;

    AbstractFile(SRProject project, SRTree parentTree, String name) {
        super("file");
        addRequiredData("team", project.getTeam().id);
        addRequiredData("project", project.getName());
        this.parentTree = parentTree;
        this.project = project;
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return parentTree.getPath() + getName();
    }

    public SRTree getParent() {
        return parentTree;
    }

    public String getHostProject() {
        return project.getName();
    }

    public abstract void delete() throws SRException;
}
