package com.zedy.realestate.adapters;

/**
 * Created by bpncool on 2/23/2016.
 */
public class Section {

    private final String name;

    private final boolean isTaskDone;

    public boolean isExpanded;

    public Section(String name, boolean isTaskDones) {
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
}
