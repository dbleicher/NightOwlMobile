package com.geofinity.wgu.nightowl.model;

import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by davidbleicher on 8/21/14.
 */
public class CommunityMessage {

    public String title;
    public String link;
    public String author;
    public String authorEmail;
    public String summary;
    public long updated;
    public int replyCount;

    public CommunityMessage(JSONObject jsonObject){
        try {
            this.title = jsonObject.getString("title");
            this.link = jsonObject.getString("link");
            this.author = jsonObject.getString("authorName");
            this.authorEmail = jsonObject.getString("authorEmail");
            this.summary = jsonObject.getString("summary");
            this.updated = jsonObject.getLong("updated");
            this.replyCount = jsonObject.getInt("replies");
        } catch (Exception e) {
            // Whatever
        }
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
