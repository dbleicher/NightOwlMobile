package com.geofinity.wgu.nightowl.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.models.jsonmodels.mentors.Mentor;
import com.geofinity.pwnet.models.jsonmodels.mentors.Shift;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.google.gson.Gson;
import com.joanzapata.iconify.widget.IconButton;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * PocketPreferences - App-wide Singleton to manage all use of SharedPreferences with
 * convenience getters/setters, and constants for SP names.
 *
 * Created by davidbleicher on 8/10/14.
 */
public class PocketPreferences {

    // Global stuff
    private static final String prefName = "PocketWGU";
    private static final int prefMode = Context.MODE_PRIVATE;
    private static SharedPreferences prefs;

    // All downloaded files are stored into an External directory called "PocketWGU"
    public static final String myExternalDir = Environment.getExternalStorageDirectory().toString()+"/PocketWGU";

    public static final String UA_IPHONE        = "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D201 Safari/9537.53";
    public static final String UA_IPAD          = "Mozilla/5.0 (iPad; CPU OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53";
    public static final String UA_AND_PHONE     = "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
    public static final String UA_AND_TABLET    = "Mozilla/5.0 (Linux; Android 5.0; Nexus 9 Build/LRX21F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.509 Safari/537.36";
    public static final String UA_KINDLE_FIRE   = "Mozilla/5.0 (Linux; U; Android 4.2.2; en-us; KFTHWI Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Silk/3.22 like Chrome/34.0.1847.137 Safari/537.36";
    public static final String UA_GAL5_LOLLI    = "Mozilla/5.0 (Linux; Android 5.0.1; en-us; SAMSUNG-SM-G900A Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/1.6 Chrome/40.0.0.0 Mobile Safari/537.36";


    // Names for all the preferences
    private static final String USER_NAME = "userName";
    private static final String USER_PASS = "userPass";
    private static final String LAST_AUTH = "lastAuthentication";
    private static final String STUDENT_NAME = "studentName";
    private static final String STUDENT_ID   = "studentID";

    private static final String GRAD_DATE = "graduationDate";
    private static final String PROG_TITLE = "programTitle";
    private static final String CURRENT_TERM = "currentTerm";
    private static final String CURRENT_TERM_START = "currentTermStart";
    private static final String CURRENT_TERM_END = "currentTermEnd";


    private static final String TERM_EARNED = "termEarned";
    private static final String TERM_TOTAL = "termTotal";
    private static final String TERM_PERCENT = "termPercent";

    private static final String DEG_EARNED = "degEarned";
    private static final String DEG_TOTAL = "degTotal";
    private static final String DEG_PERCENT = "degPercent";

    private static final String MENTOR_NAME = "mentorName";
    private static final String MENTOR_EMAIL = "mentorEmail";
    private static final String STUDENT_MENTOR_JSON = "studentMentorJSON";

    private static final String COMM_LIST = "communityList";
    private static final String COMM_ID_CURRENT = "currentCommId";
    private static final String COMM_NAME_CURRENT = "currentCommName";
    private static final String COMM_SEARCH_CURRENT = "currentCommSearchFor";

    private static final String COMM_POSTS_TODAY = "postsToday";
    private static final String COMM_MESSAGES = "communityMessages";

    private static final String UNREAD_EMAIL_COUNT = "unreadEmailCount";

    private static final String COURSE_LISTING = "courseListing";
    private static final String COURSE_FILTERNUM = "courseFilterNum";
    private static final String COURSE_SORTINGNUM = "courseSortingNum";

    private static final String LAST_CORE_DATA = "lastCoreDataUpdate";

    private static final String PANO_LAST_FETCH  = "panoptoLastFetch";
    private static final String PANO_SEARCH_TERM = "panoptoSearchTerm";
    private static final String PANO_CONTENT     = "panoptoContent";
    private static final String PANO_TOTAL_HITS  = "panoptoTotalHits";
    private static final String PANO_RESULTS_RETURNED = "panoptoResultsReturned";

    private static final String PANO_FOLDER_ID = "panoptoFolderID";
    private static final String PANO_FOLDER_NAME = "panoptoFolderName";

    private static final String CAMPUS_NEWS = "campusNews";

    private static final String SOCIAL_FILTER_NUM = "socialFilterNum";

    public static final String cssSFStyles = "<style>" +
            "h2, h3 {color: #467096; margin-top:4px; margin-bottom:4px;}"+
            "a:link, a:visited, a:hover, a:active {color: #467096;}"+

            ".person{margin:4px 0}\n" +
            ".person:after{content:'';display:table;clear:both}\n" +
            ".person p{margin-bottom:0}\n" +
            ".person__avatar{margin-right:10px;position:relative;float:left;clear:both;margin-bottom:7px}\n" +
            ".person__avatar:after{" +
            "   position:absolute;bottom:-5px;right:-5px;content:'ST';" +
            "   background-color:#1f56a1;" +
            "   background-image:-webkit-gradient(linear,left top,left bottom,from(#2480c1),to(#1f56a1));" +
            "   background-image:-webkit-linear-gradient(top,#2480c1,#1f56a1);" +
            "   background-image:-moz-linear-gradient(top,#2480c1,#1f56a1);" +
            "   background-image:-ms-linear-gradient(top,#2480c1,#1f56a1);" +
            "   background-image:-o-linear-gradient(top,#2480c1,#1f56a1);" +
            "   background-image:linear-gradient(top,#2480c1,#1f56a1);" +
            "   filter:progid:DXImageTransform.Microsoft.gradient(GradientType=0,StartColorStr='#2480c1',EndColorStr='#1f56a1');" +
            "   width:17px;height:15px;font-size:10px;color:#fff;text-align:center;border-radius:3px;font-weight:300;" +
            "   padding-top:3px}\n" +
            ".person__name,.person__email{font-size:14px;color:#0ea1dc;line-height:16px}\n" +
            ".person__email{font-size:12px}\n" +
            ".person__like{font-size:12px;color:#808080;margin-top:4px;text-transform:capitalize}\n" +
            ".person__content{display:block;overflow:hidden}\n" +
            ".person__date{font-size:12px;color:#808080;margin-top:2px}\n" +
            ".person__type{text-transform:uppercase;font-weight:normal;color:#8b8b8b;font-size:12px}\n" +
            ".person__message{white-space:normal;margin-top:3px;margin-bottom:5px;}\n" +
            ".person__comments{font-size:12px;color:#808080;margin-top:2px}\n" +
            ".person__status{font-size:12px}\n" +
            ".person__attachment{min-height:50px;display:block;margin:5px 0;*zoom:1}\n" +
            ".person__attachment:before,.person__attachment:after{content:\" \";display:table}\n" +
            ".person__attachment:after{clear:both}\n" +
            ".person__attachment .icon{font-size:58px;float:left;position:relative;top:-7px;color:#c5c7c9}\n" +
            ".person__attachment img{float:left;margin-right:10px;max-width:65px;border:1px solid #bbbbbc}\n" +
            ".person__attachment a{color:#2a58a8 !important;display:block}\n" +
            ".person--name-only .person__name,.person--name-only .person__email{padding-top:5px}\n" +
            ".person--mentor>.person__avatar:after{content:'CM'}\n" +
            ".person--me .person__avatar{float:right;margin-left:10px;margin-right:0}\n" +
            ".person--me .person__content{float:right;color:#2356ad}\n" +
            ".pocketSFLinkDiv {background-color: #4674A3; text-align: center; color:#FFFFFF; padding:5px; font-weight: bold;}\n" +
            ".pocketSFLink {text-decoration: none; color: white;}\n" +
            "div.activity-right, div.activity-right-top {text-align:center;}"+

            ".pocketCPS {font-size:0.8em; color:#AAAAAA; margin:4px;}"+
            "div.activity-content {"+
            "list-style: none; margin-top: 4px; padding-left:8px; padding-right:8px;"+
            "border: 1px solid #b4b3b8;"+
            "-webkit-border-radius: 5px 5px 5px 5px;"+
            "-moz-border-radius: 5px 5px 5px 5px;"+
            "border-radius: 5px 5px 5px 5px;"+
            "overflow: hidden;}"+

            "</style>";


    public static final String cosHead =
            "<html>"
                    +"<head>"
                    +"<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=yes\" />"
                    +"<style type='text/css'>"
                    +"body {padding-top:4px; font-family: Helvetica;}"
                    +".myHidden { display: block}"
                    +".myUnhidden { display: block}"
                    +".btn-toggle, .skip-btn-toggle {"
                    +"margin-right: 10px;"
                    +"margin-top: 9px;"
                    +"margin-left: 10px;"
                    +"background: url(\"../images/courses-sprite.png\") no-repeat;"
                    +"background-position: 0 -95px;"
                    +"cursor: pointer;"
                    +"float: left;"
                    +"height: 21px;"
                    +"text-decoration: none;"
                    +"width: 21px;"
                    +"}"

                    +"table {border-collapse: collapse;}"
                    +"table, td, th {\n" +
                    "    border: 1px solid #DDDDDD;\n" +
                    "}"

                    +".cosProgressDone {\n" +
                    "  -webkit-border-radius: 28;\n" +
                    "  -moz-border-radius: 28;\n" +
                    "  border-radius: 28px;\n" +
                    "  border-width:0px;\n"+
                    "  font-family: Arial;\n" +
                    "  color: #ffffff;\n" +
                    "  font-size: 12px;\n" +
                    "  background: #467096;\n" +
                    "  padding: 6px 10px 6px 10px;\n" +
                    "  text-decoration: none;\n" +
                    "}"
                    +".cosProgressDone:hover {\n" +
                    "  background: #5e96c7;\n" +
                    "  text-decoration: none;\n" +
                    "  outline:0;"+
                    "}"
                    +".cosProgressDone:focus {\n" +
                    "  outline:0;"+
                    "}"


                    +".cosProgressNot, .cpnBut {\n" +
                    "  -webkit-border-radius: 28;\n" +
                    "  -moz-border-radius: 28;\n" +
                    "  border-radius: 28px;\n" +
                    "  border-width:0px;\n"+
                    "  font-family: Arial;\n" +
                    "  color: #000000;\n" +
                    "  font-size: 12px;\n" +
                    "  background: #e1e1e1;\n" +
                    "  padding: 6px 10px 6px 10px;\n" +
                    "  text-decoration: none;\n" +
                    "}\n"

                    +".cosProgressNot:hover {\n" +
                    "  background: #ededed;\n" +
                    "  text-decoration: none;\n" +
                    "  outline:0;"+
                    "}"
                    +".cosProgressNot:focus {\n" +
                    "  outline:0;"+
                    "}"

                    +"hr.cfade {\n" +
                    "    border: 0;\n" +
                    "    height: 1px;\n" +
                    "    background-image: linear-gradient(to right, rgba(70, 112, 150, 0), rgba(70, 112, 150, 0.50), rgba(70, 112, 150, 0));\n" +
                    "}"

                    +"a:link, a:visited, a:hover, a:active "
                    +"{color: #467096; margin-top:4px; margin-bottom:4px; text-decoration:none;}"
                    +"div.title {margin-top:32px; margin-bottom:2px; font-size:0.8em;}"
                    +"h1 {color: #467096; margin-top:4px; margin-bottom:4px; font-size:1.4em;}"
                    +"h2, h3 {color: #467096; margin-top:4px; margin-bottom:4px;}"

                    +".pocketCPS {font-size:0.8em; color:#AAAAAA; padding:4px; margin:4px;}"
                    +".navBut {font-size:0.8em; vertical-align:baseline; background-color:#F0F0F0; padding:4px; margin:4px;}"

                    +"div.activity-content {"
                    +"list-style: none; margin-top: 4px; padding-left:8px;"
                    +"padding-right:8px; margin-right:4px;"
                    +"border: 1px solid #b4b3b8;"
                    +"-webkit-border-radius: 5px 5px 5px 5px;"
                    +"-moz-border-radius: 5px 5px 5px 5px;"
                    +"border-radius: 5px 5px 5px 5px;"
                    +"overflow: hidden;}"
                    +"div.activity-right, div.activity-right-top {text-align:center;}"
                    +"</style>"
                    +"<script type=\"text/javascript\">\n" +
                    "    function cosProgressChoose(status, cCode, rKind, spId, subId, topId, actId, uatId) {\n" +
                    "        CosProgress.cosProgressChoose(status, cCode, rKind, spId, subId, topId, actId, uatId);\n" +
                    "    }\n" +
                    "</script>"
                    +"</head><body style=\"font-size:1.05em;\">";




    // Instance of this Singleton Class
    private static PocketPreferences sInstance = null;

    /**
     * Private constructor for creating a singleton
     * @param ac Application Context
     */
    private PocketPreferences (Context ac){
        prefs = ac.getSharedPreferences(prefName, prefMode);
    }

    /**
     * Factory method to return singleton instance
     */
    public static PocketPreferences getInstance(Context ac) {
        if (sInstance == null) {
            sInstance = new PocketPreferences(ac);
        }
        return sInstance;
    }

    //////////////////////////////////////
    // Methods that need to be "filed"  //
    //////////////////////////////////////
    // DONE: Make an "isFirstRun() method

    public boolean isFirsRun () {
        if (this.getLastAppLaunch() < 0 || this.getLastAppLaunch() < NOMApp.getLastAppUpdate()) {
            // Log.e("NOMApp","First Run!!");
            this.setLastAppLaunch();
            return true;
        } else {
            // Log.e("NOMApp","NOT FIRST RUN");
            this.setLastAppLaunch();
            return false;
        }
    }
    public void setLastAppLaunch () {
        prefs.edit().putLong("LAST_APP_LAUNCH", new Date().getTime()).apply();
    }
    public long getLastAppLaunch() {
        return prefs.getLong("LAST_APP_LAUNCH", -1);
    }


    // Custom Toast Display
    public static void customToast(Context ctx, String message, int duration) {
        Toast toast = Toast.makeText(ctx, message, duration);
        toast.getView().setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.rounded_rect));
        toast.show();
    }

    public String getWebmailUrl() {
        return "https://mail.google.com/a/wgu.edu/?account_id=" + getUserName() + "@wgu.edu";
    }


    //////////////////////////////
    // New CoS Handling Methods //
    //////////////////////////////
    public String getStudyPlan (String courseCode) {
        return prefs.getString(courseCode+"_StudyPlan", "{}");
    }

    public String getSPBookmark (String courseCode) {
        return prefs.getString(courseCode+"_SPBookmark", "");
    }
    public void setSPBookmark (String courseCode, String spBookmark) {
        prefs.edit().putString(courseCode+"_SPBookmark", spBookmark).apply();
    }

    public String getAssessDetails (String courseCode) {
        return prefs.getString(courseCode+"_AssessDetails", "{}");
    }

    //////////////////////////////
    // Overall Mentors Methods  //
    //////////////////////////////

    public String getStudentMentorJSON() {
        return prefs.getString(STUDENT_MENTOR_JSON, "{}");
    }

    public String getCourseMentorJSON(String courseCode) {
        return prefs.getString(courseCode+"_CMENTOR_JSON", "{}");
    }


    ///////////////////////////
    // New SalesForce Stuff  //
    ///////////////////////////
    public String getTipsAnnsJSON (String courseCode) {
        return prefs.getString(courseCode+"_TipsAnns", "{}");
    }
    public long getTipsAnnsLast (String courseCode) {
        return prefs.getLong(courseCode + "_TipsAnnsLast", 0);
    }

    public String getChatterJSON (String courseCode) {
        return prefs.getString(courseCode + "_Chatter", "{}");
    }
    public long getChatterLast (String courseCode) {
        return prefs.getLong(courseCode + "_ChatterLast", 0);
    }

    ///////////////////////
    // TaskStream Stuff  //
    ///////////////////////
    public void setTaskStreamUrl(String courseCode, String assessTitle) {
        String tsUrl = "https://cos.wgu.edu/courses/task-stream/"+assessTitle.substring(0, 4);
        prefs.edit().putString(courseCode+"_TaskStreamUrl", tsUrl).apply();
    }

    public String getCosPDFUrl(String courseCode) {
        return prefs.getString(courseCode+"_CosPDFUrl", "");
    }
    public void setCosPDFUrl(String courseCode, String cosPDFUrl) {
        prefs.edit().putString(courseCode+"_CosPDFUrl", cosPDFUrl).apply();
    }



    /// Deal with TOC for a CoS page
    public String getCosToc(String courseCode) {
        return prefs.getString(courseCode+"_COSTOC", "[]");
    }
    public void setCosToc(String courseCode, String cosToc) {
        prefs.edit().putString(courseCode+"_COSTOC", cosToc ).apply();
    }

    //////////////////////////
    // Getters and Setters  //
    //////////////////////////


    public String getUserName(){
        return prefs.getString(USER_NAME, "");
    }

    public String getUserPass(){
        return prefs.getString(USER_PASS, "");
    }

    public void setLastAuth(long lastAuth){
        prefs.edit().putLong(LAST_AUTH, lastAuth).apply();
    }



    public String getStudentName() {
        return prefs.getString(STUDENT_NAME, "WGU Student");
    }

    public String getStudentID() {
        return prefs.getString(STUDENT_ID, "---");
    }


    public String getGradDate(){
        return prefs.getString(GRAD_DATE, "");
    }


    public String getProgTitle(){
        return prefs.getString(PROG_TITLE, "Welcome to PocketWGU");
    }


    public int getCurrentTerm(){
        return prefs.getInt(CURRENT_TERM, -1);
    }

    public String getCurrentTermStart() {
        return prefs.getString(CURRENT_TERM_START, "");
    }

    public String getCurrentTermEnd() {
        return prefs.getString(CURRENT_TERM_END, "");
    }
    public String getCurrentTermEndFF() {
        try {
            String cte = prefs.getString(CURRENT_TERM_END, "");
            Date cted = NOMApp.sdfYMD.parse(cte);
            return NOMApp.sdfNice.format(cted);
        } catch (Exception e) {
            return getCurrentTermEnd();
        }
    }

    // Term Progress Methods

    public int getTermEarned(){
        return prefs.getInt(TERM_EARNED, -1);
    }

    public int getTermTotal(){
        return prefs.getInt(TERM_TOTAL, -1);
    }

    public int getTermPercent(){
        return prefs.getInt(TERM_PERCENT, -1);
    }

    public String getTermProgress(){
        return String.format(
                "%d of %d (%d%%)",
                getTermEarned(),
                getTermTotal(),
                getTermPercent()
        );
    }


    // Degree Progress Methods

    public int getDegEarned(){
        return prefs.getInt(DEG_EARNED, -1);
    }

    public int getDegTotal(){
        return prefs.getInt(DEG_TOTAL, -1);
    }

    public int getDegPercent(){
        return prefs.getInt(DEG_PERCENT, -1);
    }

    public String getDegProgress(){
        return String.format(
                "%d of %d (%d%%)",
                getDegEarned(),
                getDegTotal(),
                getDegPercent()
        );
    }

    // Mentor Info Methods
    public String getMentorName() { return prefs.getString(MENTOR_NAME, ""); }
    public String getMentorEmail() { return prefs.getString(MENTOR_EMAIL, ""); }


    public String getCommList() {
        return prefs.getString(COMM_LIST, "[]");
    }

    public String getCurrentCommId() { return prefs.getString(COMM_ID_CURRENT, "0000"); }
    public void setCurrentCommId(String currentCommId) {
        prefs.edit().putString(COMM_ID_CURRENT, currentCommId).apply();
    }

    public String getCurrentCommName() { return prefs.getString(COMM_NAME_CURRENT, "All WGU Communities"); }
    public void setCurrentCommName(String currentCommName) {
        prefs.edit().putString(COMM_NAME_CURRENT, currentCommName).apply();
    }

    public String getCommSearchCurrent() { return prefs.getString(COMM_SEARCH_CURRENT, ""); }
    public void setCommSearchCurrent(String commSearchCurrent) {
        prefs.edit().putString(COMM_SEARCH_CURRENT, commSearchCurrent).apply();
    }

   

    public int getCommPostsToday() {
        return prefs.getInt(COMM_POSTS_TODAY, 0);
    }

    public String getCommMessages() { return prefs.getString(COMM_MESSAGES,  "[]"); }

    public boolean isCommunityLocked(String commId) {
        return prefs.getBoolean("COMM_LOCKED_"+commId, false);        
    }

    public int getUnreadEmailCount() {
        return prefs.getInt(UNREAD_EMAIL_COUNT, 0);
    }


    //////////////////////////////////
    // Course Listing and FilterNum //
    //////////////////////////////////
    public String getCourseListing() { return prefs.getString(COURSE_LISTING, "[]"); }

    public int getCourseFilterNum() { return prefs.getInt(COURSE_FILTERNUM, 0); }
    public void setCourseFilterNum(int courseFilterNum) {
        prefs.edit().putInt(COURSE_FILTERNUM, courseFilterNum).apply();
    }

    public int getCourseSortingNum() { return prefs.getInt(COURSE_SORTINGNUM, 0); }
    public void setCourseSortingNum(int courseSortingNum) {
        prefs.edit().putInt(COURSE_SORTINGNUM, courseSortingNum).apply();
    }


    //////////////////////////////
    // Dates of last refresh    //
    //////////////////////////////
    public long getLastCoreData() {
        return prefs.getLong(LAST_CORE_DATA, 0l);
    }

    //////////////////////////
    // Panopto Video Stuff  //
    //////////////////////////
    public long getPanoLastFetch() { return prefs.getLong(PANO_LAST_FETCH, 0l); }

    public String getPanoSearchTerm() { return prefs.getString(PANO_SEARCH_TERM, ""); }
    public void setPanoSearchTerm(String panoSearchTerm) {
        prefs.edit().putString(PANO_SEARCH_TERM, panoSearchTerm).apply();
    }

    public String getPanoContent() { return prefs.getString(PANO_CONTENT, "[]"); }
    public int getPanoptoTotalHits() { return prefs.getInt(PANO_TOTAL_HITS, 0); }

    public int getPanoptoResultCount() {
        return prefs.getInt(PANO_RESULTS_RETURNED, 0);
    }

    public String getPanoptoFolderId () {
        return prefs.getString(PANO_FOLDER_ID, "");
    }
    public void setPanoptoFolderId (String panoptoFolderId) {
        prefs.edit().putString(PANO_FOLDER_ID, panoptoFolderId).apply();
    }

    public String getPanoptoFolderName () {
        return prefs.getString(PANO_FOLDER_NAME, "");
    }
    public void setPanoptoFolderName (String panoptoFolderName) {
        prefs.edit().putString(PANO_FOLDER_NAME, panoptoFolderName).apply();
    }


    public String getCampusNews() {
        return prefs.getString(CAMPUS_NEWS, "<html><body><h2>No Current News</h2></body></html>");
    }

    public int getSocialFilterNum() {
        return prefs.getInt(SOCIAL_FILTER_NUM, 1);
    }
    public void setSocialFilterNum(int socialFilterNum) {
        prefs.edit().putInt(SOCIAL_FILTER_NUM, socialFilterNum).apply();
    }


    public void showMentorPage (final Context ctx, String mentorJSON) {

        Gson gson = new Gson();
        final Mentor mentor = gson.fromJson(mentorJSON, Mentor.class);

        String dTitle = "Course Mentor Group";
        if (mentor.courseCode != null && mentor.fullName != null) {
            dTitle = mentor.courseCode+" Course Mentor";
        } else if (mentor.courseCode != null) {
            dTitle = mentor.courseCode+" Course Mentor Group";
        } else {
            dTitle = "My Student Mentor";
        }

        MaterialDialog mentDiag = new MaterialDialog.Builder(ctx)
                .title(dTitle)
                .customView(R.layout.diag_mentor_view, true)
                .positiveText("OK")
                .build();

        View cv = mentDiag.getCustomView();

        // Deal with mentor Name
        TextView mentorName = (TextView) cv.findViewById(R.id.tvMentorName);
        if (mentor.fullName != null) {
            mentorName.setText(mentor.fullName);
        } else if (mentor.courseTitle != null) {
            mentorName.setText(mentor.courseTitle);
        } else {
            mentorName.setText("Course Mentor Group");
        }

        // Deal with Email
        if (mentor.emailAddress != null) {
            TextView tvMentorEmail = (TextView) cv.findViewById(R.id.tvEmailAddress);
            IconButton btSendEmail = (IconButton) cv.findViewById(R.id.btSendEmail);
            tvMentorEmail.setText(mentor.emailAddress);
            btSendEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("MENTOR", "Tried to email my mentor");
                    composeEmail(ctx, mentor.emailAddress, true);
                }
            });
        }

        // Deal with Phone
        LinearLayout llMentPhone = (LinearLayout) cv.findViewById(R.id.llMentorPhone);
        if (mentor.phoneNumber != null) {
            TextView tvMentorPhone = (TextView) cv.findViewById(R.id.tvPhoneNumber);
            IconButton btDialPhone = (IconButton) cv.findViewById(R.id.btDialPhone);
            tvMentorPhone.setText(mentor.phoneNumber);
            btDialPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("MENTOR", "Tried to CALL my mentor");
                    String pn = mentor.phoneNumber.replaceAll("\\D+","");
                    Log.i("MENTOR", pn);
                    if(pn.startsWith("1") && pn.length() > 11) {
                        customToast(
                                ctx,
                                "After connecting, dial extension: " + pn.substring(11, pn.length()),
                                Toast.LENGTH_LONG
                        );
                        pn = pn.substring(0, 11) + ",," + pn.substring(11, pn.length());
                    } else if (pn.length() > 10) {
                        customToast(
                                ctx,
                                "After connecting, dial extension: " + pn.substring(10, pn.length()),
                                Toast.LENGTH_LONG
                        );
                        pn = pn.substring(0, 10) + ",," + pn.substring(10, pn.length());
                    }
                    ctx.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(pn))));
                }
            });
        } else {
            llMentPhone.setVisibility(View.GONE);
        }

        // Deal with Office Hours
        LinearLayout llOffice = (LinearLayout) cv.findViewById(R.id.llMentorOffice);
        if (mentor.officeHours != null
                && mentor.officeHours.shift != null
                && mentor.officeHours.shift.size() > 0) {
            try {
                TextView tvOH = (TextView) cv.findViewById(R.id.tvOfficeHours);
                TextView tvOHTitle = (TextView) cv.findViewById(R.id.tvOHTitle);
                String toht = String.format(
                        "Office Hours:\n(%s)",
                        TimeZone.getDefault().getDisplayName(Locale.getDefault())
                );
                tvOHTitle.setText(toht);
                // TimeZone.getDefault().getDisplayName(Locale.getDefault())

                StringBuilder sb = new StringBuilder();
                for (Shift s : mentor.officeHours.shift) {
                    Date sd = NOMApp.sdfShiftDTIn.parse(s.startDT.timeDT+" "+s.offset);
                    Date ed = NOMApp.sdfShiftDTIn.parse(s.endDT.timeDT+" "+s.offset);
                    sb.append(String.format(
                            "%s %s - %s\n",
                            s.startDT.dayDT,
                            NOMApp.sdfShiftDTOut.format(sd),
                            NOMApp.sdfShiftDTOut.format(ed)
                    ));
                }
                tvOH.setText(sb.toString());
            } catch (Exception e) {
                llOffice.setVisibility(View.GONE);
            }
        } else {
            llOffice.setVisibility(View.GONE);
        }

        // Deal with Mentor Bio
        LinearLayout llBio = (LinearLayout) cv.findViewById(R.id.llMentorBio);
        if (mentor.bio != null && mentor.bio.length() > 10) {
            TextView tvBio = (TextView) cv.findViewById(R.id.tvBio);
            tvBio.setText(mentor.bio);
        } else {
            llBio.setVisibility(View.GONE);
        }

        mentDiag.show();
    }

    public void composeEmail (Context ctx, String toAddress, Boolean isMentor) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        String emailBody = "Hello, \n\n\n\n";
        String intentTitle = "Send Email...";

        if (isMentor && toAddress.endsWith("wgu.edu")) {
            intentTitle = "Email My Mentor...";
            emailBody = String.format(
                    "Hi, <p>&nbsp;</p><p>&nbsp;</p><p>&nbsp;</p><b>%s</b><br/>%s<br/><b>Student ID:</b> %s<br/><b>Email:</b> %s@wgu.edu<br/><b>Timezone:</b> %s",
                    PocketPreferences.this.getStudentName(),
                    PocketPreferences.this.getProgTitle(),
                    PocketPreferences.this.getStudentID(),
                    PocketPreferences.this.getUserName(),
                    TimeZone.getDefault().getDisplayName(Locale.getDefault())
            );
        } else if (toAddress.endsWith("wgu.edu")) {
            intentTitle = "Send Email...";
            emailBody = String.format(
                    "Hi, <p>&nbsp;</p><p>&nbsp;</p><p>&nbsp;</p><b>%s</b><br/>%s<br/><b>Timezone:</b> %s",
                    PocketPreferences.this.getStudentName(),
                    PocketPreferences.this.getProgTitle(),
                    TimeZone.getDefault().getDisplayName(Locale.getDefault())
            );
        }

        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{toAddress});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I have a question...");
        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(emailBody));
        ctx.startActivity(Intent.createChooser(emailIntent, intentTitle));
    }


}
