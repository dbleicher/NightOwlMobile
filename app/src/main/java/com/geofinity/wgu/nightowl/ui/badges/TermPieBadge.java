package com.geofinity.wgu.nightowl.ui.badges;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by davidbleicher on 2/18/15.
 */
public class TermPieBadge extends LinearLayout implements IBadge {

    private static final String badgeTitle = "Current Term Badge";

    PocketPreferences prefs;

    private TextView tvProgTitle;
    private TextView tvSubTitle;
    private PieChartView chart;
    private PieChartData data;
    private TextView tvTermNumber;

    private TextView tvLegendComplete;
    private TextView tvLegendEnrolled;
    private TextView tvLegendPending;
    
    private int currentTerm;

    public TermPieBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = NOMApp.prefs;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.card_term_pie, this);
        
        tvProgTitle  = (TextView) findViewById(R.id.tvProgTitle);
        tvSubTitle   = (TextView) findViewById(R.id.tvSubtitle);
        tvTermNumber = (TextView) findViewById(R.id.tvTermNumber);

        tvLegendComplete = (TextView) findViewById(R.id.tvLegendComplete);
        tvLegendEnrolled = (TextView) findViewById(R.id.tvLegendEnrolled);
        tvLegendPending  = (TextView) findViewById(R.id.tvLegendPending);
        
        chart = (PieChartView) findViewById(R.id.chart);
        
        populateMe();
    }
    
    @Override
    public void populateMe() {
        int blue  = getContext().getResources().getColor(R.color.palePrimary);
        int green = getContext().getResources().getColor(R.color.wguGreenDark);
        int red   = getContext().getResources().getColor(R.color.wguRedDark);
        int gold  = getContext().getResources().getColor(R.color.wguGold);
        
        tvProgTitle.setText(prefs.getProgTitle());
        currentTerm = prefs.getCurrentTerm();
        
        tvTermNumber.setText(""+currentTerm);

        tvSubTitle.setText(
                String.format("%s through %s",
                prefs.getCurrentTermStart(),
                prefs.getCurrentTermEnd())
        );

        Gson gson = new Gson();
        Type listType = new TypeToken<List<JCourse>>(){}.getType();
        ArrayList<JCourse> courseList = gson.fromJson(prefs.getCourseListing(), listType);
        Collections.sort(courseList, JCourse.TERM_STATNUM);

        boolean showPending = false;
        boolean showComplete = false;
        boolean showEnrolled = false;
        int done = 0;
        int tot = 0;

        List<SliceValue> values = new ArrayList<SliceValue>();
        for (JCourse c : courseList) {
            if (c.courseTerm != currentTerm) continue;

            // 0 = Not Attempted, 1 = Complete, 2 = Not passed, 3 = Enrolled,
            if (c.courseStatusNum == 0) {
                showPending = true;
                tot += c.competencyUnits;
                values.add(new SliceValue(c.competencyUnits, blue));
            } else if (c.courseStatusNum == 1) {
                showComplete = true;
                done += c.competencyUnits;
                tot += c.competencyUnits;
                values.add(new SliceValue(c.competencyUnits, green));
            } else if (c.courseStatusNum == 3) {
                showEnrolled = true;
                tot += c.competencyUnits;
                values.add(new SliceValue(c.competencyUnits, gold));
            }
        }
        
        if(!showComplete) tvLegendComplete.setVisibility(View.GONE);
        if(!showEnrolled) tvLegendEnrolled.setVisibility(View.GONE);
        if(!showPending) tvLegendPending.setVisibility(View.GONE);

        data = new PieChartData(values);
        
        data.setHasLabels(true);
        data.setHasCenterCircle(true);
        
        if (done == 0 || tot == 0) {
            data.setCenterText1("0%");
        } else {
            data.setCenterText1( ""+Math.round(done * 100.0/tot)+"%" );
        }
        data.setCenterText1Color(green);
        data.setCenterText1FontSize(32);

        chart.setPieChartData(data);
    }

    @Override
    public String getBadgeTitle() {
        return badgeTitle;
    }
}
