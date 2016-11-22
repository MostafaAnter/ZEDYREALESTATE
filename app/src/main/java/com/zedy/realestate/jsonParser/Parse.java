package com.zedy.realestate.jsonParser;

import com.zedy.realestate.models.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mostafa_anter on 11/21/16.
 */

public class Parse {
    public static List<Task> parseTasks(String feed){
        JSONObject jsonObject = null;
        List<Task> taskList = new ArrayList<>();
        try {
            jsonObject = new JSONObject(feed);
            JSONArray jsonArray = jsonObject.optJSONArray("tasks");
            for (int i = 0; i < jsonArray.length() ; i++) {
                JSONObject taskObject = jsonArray.optJSONObject(i);
                String id = taskObject.optString("id");
                String project = taskObject.optString("project");
                String title = taskObject.optString("title");
                String describtion = taskObject.optString("description");
                String done = taskObject.optString("done");

                taskList.add(new Task(id, project, title, describtion, done));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return taskList;
        }

        return taskList;
    }
}
