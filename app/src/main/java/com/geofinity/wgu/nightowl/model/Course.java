package com.geofinity.wgu.nightowl.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by davidbleicher on 9/4/14.
 */
public class Course implements Comparable<Course>{

    public ArrayList<Assessment> courseAssessments;
    public String   courseName;
    public String   courseCode;
    public String   courseType;
    public String   courseNumber;
    public String   courseStatus;
    public String   courseURL;

    public String   dateStart;
    public String   dateEnd;
    public String   dateActivity;
    public String   dateGrade;
    public String   dateTermStart;
    public String   dateTermEnd;

    public int      courseCredits;
    public int      courseStatusNum;
    public int      courseTerm;
    public int      displayOrder;


    public Course() {
        super();
        courseAssessments = new ArrayList<Assessment>();
    }

    public Course(String courseName, String courseType) {
        this.courseName = courseName;
        this.courseType = courseType;
    }

    public void addAssessment(String status, String title, String type) {
        this.courseAssessments.add(new Assessment(status, title, type));
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public int compareTo(Course theOther) {
        if (this.displayOrder < theOther.displayOrder ) return -1;
        if (this.displayOrder > theOther.displayOrder ) return 1;
        return 0;
    }

    public class Assessment {
        public String status;
        public String title;
        public String type;
        public String aCode;

        public Assessment(String status, String title, String type) {
            this.status = status;
            this.title = title;
            this.type = type;
            aCode = this.title.substring(0,4);
        }
    }

    public String getKeyDate(boolean withLabel) {
        if (dateGrade != null && dateGrade.length() > 4) {
            return (withLabel) ? "Graded: "+formatYMD(dateGrade) : formatYMD(dateGrade);
        }

        if (dateEnd != null && dateEnd.length() > 4) {
            return (withLabel) ? "Due: "+formatYMD(dateEnd) : formatYMD(dateEnd);
        }

        if (dateStart != null && dateStart.length() > 4) {
            return (withLabel) ? "Start: "+formatYMD(dateStart) : formatYMD(dateStart);
        }

        if (dateActivity != null && dateActivity.length() > 4) {
            return (withLabel) ? "Added: "+formatYMD(dateActivity) : formatYMD(dateActivity);
        }

        if (dateTermStart != null && dateTermStart.length() > 4) {
            return (withLabel) ? "Term: "+formatYMD(dateTermStart) : formatYMD(dateTermStart);
        }

        return "";
    }

    private String formatYMD (String aDate) {
        if (aDate.contains("/")) {
            String[] dateParts = aDate.split("/");
            return dateParts[2]+"-"+dateParts[0]+"-"+dateParts[1];
        }
        return aDate;
    }

    //////////////////////////
    // Static Comparators   //
    //////////////////////////
    public static final Comparator<Course> CODE_ASC = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            return lhs.courseCode.compareTo(rhs.courseCode);
        }
    };

    public static final Comparator<Course> CODE_DSC = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            return rhs.courseCode.compareTo(lhs.courseCode);
        }
    };


    public static final Comparator<Course> NAME_ASC = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            return lhs.courseName.compareTo(rhs.courseName);
        }
    };

    public static final Comparator<Course> NAME_DSC = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            return rhs.courseName.compareTo(lhs.courseName);
        }
    };

    public static final Comparator<Course> DATE_ASC = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            return lhs.getKeyDate(false).compareTo(rhs.getKeyDate(false));
        }
    };

    public static final Comparator<Course> DATE_DSC = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            return rhs.getKeyDate(false).compareTo(lhs.getKeyDate(false));
        }
    };

    public static final Comparator<Course> TERM_STATNUM = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            if (lhs.courseTerm > rhs.courseTerm) {
                return 1;
            } else if (lhs.courseTerm < rhs.courseTerm) {
                return -1;
            } else {
                if (lhs.courseStatusNum > rhs.courseStatusNum) {
                    return 1;
                } else if (lhs.courseStatusNum < rhs.courseStatusNum) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    };

}
