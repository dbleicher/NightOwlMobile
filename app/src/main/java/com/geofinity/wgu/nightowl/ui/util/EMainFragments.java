package com.geofinity.wgu.nightowl.ui.util;

/**
 * Created by davidbleicher on 8/31/14.
 */
public enum EMainFragments {
    HOME_FRAG("PocketWGU Home"),
    COURSES_FRAG("My Courses"),
    COMMUNITIES_FRAG("My Communities");

    final String display;

    private EMainFragments(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }
}
