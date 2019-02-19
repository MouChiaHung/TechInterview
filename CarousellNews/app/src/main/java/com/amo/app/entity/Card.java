package com.amo.app.entity;
import com.orhanobut.logger.Logger;

public class Card {
    //auto-build sql table should include static final fields
    /*
    public static final String DEFAULT_ID          = "";
    public static final String DEFAULT_TITLE       = "";
    public static final String DEFAULT_DESCRIPION  = "";
    public static final String DEFAULT_BANNER_URL  = "";
    public static final int DEFAULT_TIME_CREATED  = 0;
    public static final int DEFAULT_RANK          = 0;
    */

    /**
     * name of fields are same as the name of columns in the table representing a SQL database storing cards
     */
    private String id;
    private String title;
    private String description;
    private String banner_url;
    private int time_created; //in sec
    private int rank;

    public Card() {
        //Logger.d(">>> constructor accepting null");
    }

    public Card(String id, String title, String description, String banner_url, int time_created, int rank) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.banner_url = banner_url;
        this.time_created = time_created;
        this.rank = rank;
    }

    /**
     * getter
     */
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBannerUrl() {
        return banner_url;
    }

    public int getTimeCreated() {
        return time_created;
    }

    public int getRank() {
        return rank;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBannerUrl(String banner_url) {
        this.banner_url = banner_url;
    }

    public void setTimeCreated(int time_created) {
        this.time_created = time_created;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * setter
     */


    public void printCardData(){
        if (false) return;
        String log = "... "
                + "id:" + getId()
                + "\n"
                + "title :" + getTitle()
                + "\n"
                + "description:" + getDescription()
                + "\n"
                + "banner_url:" + getBannerUrl()
                + "\n"
                + "time_created:" + getTimeCreated()
                + "\n"
                + "rank:" + getRank()
                + "\n";
        Logger.d(log);
    }
}
