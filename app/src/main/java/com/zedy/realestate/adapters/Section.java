package com.zedy.realestate.adapters;

/**
 * Created by bpncool on 2/23/2016.
 */
public class Section {

    private final String name;
    private final String id;
    private final boolean isTaskDone;

    public boolean isExpanded;

    public Section(String id, String name, boolean isTaskDones) {
        this.id = id;
        this.name = name;
        isExpanded = true;
        this.isTaskDone = isTaskDones;
    }

    public String getName() {
        return name;
    }

    public boolean isTaskDone() {
        return isTaskDone;
    }

    public String getId() {
        return id;
    }
}
