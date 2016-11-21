package com.zedy.realestate.models;

/**
 * Created by mostafa_anter on 11/21/16.
 */

public class Task {
    private String id;
    private String title;
    private String describtion;
    private String done;

    public Task(String id, String title, String describtion, String done) {
        this.id = id;
        this.title = title;
        this.describtion = describtion;
        this.done = done;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescribtion() {
        return describtion;
    }

    public String getDone() {
        return done;
    }
}
