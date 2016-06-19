package com.geofinity.wgu.nightowl.ui.badges;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geofinity.pwnet.models.jsonmodels.degreeplanV6.JCourse;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by davidbleicher on 2/18/15.
 */
public class AccountableBadge extends LinearLayout implements IBadge {

    private static final String badgeTitle = "Accountability Badge";

    PocketPreferences prefs;

    private TextView tvProgTitle;
    private TextView tvSubTitle;
    private GridLayout glCourseGrid;

    private int blue  = getContext().getResources().getColor(R.color.wguPrimary);
    private int green = getContext().getResources().getColor(R.color.wguGreenDark);
    private int red   = getContext().getResources().getColor(R.color.wguRedDark);
    private int gold  = getContext().getResources().getColor(R.color.wguGold);
    
    private int labelWidth = dpToPx(52);
    private int labelPadd  = dpToPx(4);

    private Context mContext;

    public AccountableBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        prefs = NOMApp.prefs;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.card_accountable, this);
        
        tvProgTitle  = (TextView) findViewById(R.id.tvProgTitle);
        tvSubTitle   = (TextView) findViewById(R.id.tvSubtitle);
        glCourseGrid = (GridLayout) findViewById(R.id.glCourseGrid);

        populateMe();
    }
    
    @Override
    public void populateMe() {
        tvProgTitle.setText(prefs.getProgTitle());
        tvSubTitle.setText( String.format("My progress as of: %s", NOMApp.sdfShort.format(new Date().getTime())) );

        Gson gson = new Gson();
        Type listType = new TypeToken<List<JCourse>>(){}.getType();
        ArrayList<JCourse> courseList = gson.fromJson(prefs.getCourseListing(), listType);
        Collections.sort(courseList, JCourse.TERM_STATNUM);
        
        for(JCourse c : courseList) {
            if(c.courseStatusNum == 2) continue;
            glCourseGrid.addView(makeCourseLabel(c));
        }
    }
    
    private TextView makeCourseLabel(JCourse c) {
        TextView ctv = new TextView(mContext);
        ctv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        ctv.setTextColor((c.courseStatusNum == 3) ? Color.BLACK : Color.WHITE);
        ctv.setWidth(labelWidth);
        ctv.setPadding(labelPadd, labelPadd, labelPadd, labelPadd);
        ctv.setText(c.courseCode);
        ctv.setBackgroundColor(getCourseBGColor(c.courseStatusNum));
        ctv.setGravity(Gravity.CENTER);
        return ctv;        
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
    
    private int getCourseBGColor(int statusNum) {
        // 0 = Not Attempted, 1 = Complete, 2 = Not passed, 3 = Enrolled,

        switch (statusNum) {
            case 1:
                return green;
            case 2:
                return red;
            case 3:
                return gold;
            default:
                return blue;
        }
        
    }

    @Override
    public String getBadgeTitle() {
        return badgeTitle;
    }
}
