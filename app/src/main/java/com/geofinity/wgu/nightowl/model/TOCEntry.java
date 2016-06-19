package com.geofinity.wgu.nightowl.model;

/**
 * Created by davidbleicher on 9/13/14.
 */
public class TOCEntry {
    public String myLink;
    public String mySelected;
    public String myText;

    public TOCEntry(String myLink, String mySelected, String myText) {
        super();
        this.myLink = myLink;
        this.mySelected = mySelected;
        this.myText = myText;
    }

    @Override
    public String toString() {
        return String.format("%s - %s: %s", myText, myLink, mySelected);
    }
}
