package com.geofinity.wgu.nightowl.ui.badges;

import android.content.Context;
import android.util.AttributeSet;
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
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * Created by davidbleicher on 2/18/15.
 */
public class CUsByTermBadge extends LinearLayout implements IBadge {

    private static final String badgeTitle = "CU by Term Badge";
    
    PocketPreferences prefs;
    
    private TextView tvProgTitle;
    private TextView tvSubTitle;
    private ColumnChartView chart;
    private ColumnChartData data;
        
    
    public CUsByTermBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = NOMApp.prefs;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.card_cus_by_term, this);
        
        tvProgTitle = (TextView) findViewById(R.id.tvProgTitle);
        tvSubTitle  = (TextView) findViewById(R.id.tvSubtitle);
        chart       = (ColumnChartView) findViewById(R.id.chart);
        
        populateMe();
    }
    
    @Override
    public void populateMe() {
        int blue  = getContext().getResources().getColor(R.color.palePrimary);
        int green = getContext().getResources().getColor(R.color.wguGreenDark);
        int red   = getContext().getResources().getColor(R.color.wguRedDark);
        int gold  = getContext().getResources().getColor(R.color.wguGold);
        
        tvProgTitle.setText(prefs.getProgTitle());
        tvSubTitle.setText("Progress by Term - Current Term "+prefs.getCurrentTerm());

        Gson gson = new Gson();
        Type listType = new TypeToken<List<JCourse>>(){}.getType();
        ArrayList<JCourse> courseList = gson.fromJson(prefs.getCourseListing(), listType);
        Collections.sort(courseList, JCourse.TERM_STATNUM);

        HashMap<Integer, Integer[]> dataSet = new HashMap<Integer, Integer[]>();
        for (JCourse c : courseList) {
            if (!dataSet.containsKey(c.courseTerm)) {
                dataSet.put(c.courseTerm, new Integer[4]);
                dataSet.get(c.courseTerm)[0] = 0;
                dataSet.get(c.courseTerm)[1] = 0;
                dataSet.get(c.courseTerm)[2] = 0;
                dataSet.get(c.courseTerm)[3] = 0;
            }
            dataSet.get(c.courseTerm)[c.courseStatusNum] += (int)c.competencyUnits;
        }

//        dataSet.get(1)[1] = 8;
//        dataSet.get(2)[1] = 9;
//        dataSet.get(3)[1] = 12;
//        dataSet.get(4)[1] = 8;
//        dataSet.get(5)[0] = 3;
//        dataSet.get(5)[1] = 4;
//        dataSet.get(5)[3] = 5;

        // 0 = Not Attempted, 1 = Complete, 2 = Not passed, 3 = Enrolled,
        List<Column> columns = new ArrayList<Column>();
        ArrayList<AxisValue> xLabels = new ArrayList<AxisValue>();

        int start = (dataSet.containsKey(0)) ? 0 : 1;
        
        for (int x = start; true; x++) {
            if (!dataSet.containsKey(x)) break;

            ArrayList<SubcolumnValue>scValues = new ArrayList<SubcolumnValue>();
            
            // We only want to add subcolumns that have values
            // And we don't want to display failed courses ([2])
            
            if (dataSet.get(x)[0] > 0)
                scValues.add(new SubcolumnValue(dataSet.get(x)[0], blue));
            if (dataSet.get(x)[1] > 0)
                scValues.add(new SubcolumnValue(dataSet.get(x)[1], green));
//            if (dataSet.get(x)[2] > 0)
//                scValues.add(new SubcolumnValue(dataSet.get(x)[2], red));
            if (dataSet.get(x)[3] > 0)
                scValues.add(new SubcolumnValue(dataSet.get(x)[3], gold));


            columns.add(new Column(scValues).setHasLabels(true));
            xLabels.add(new AxisValue(x - start, ("T" + x).toCharArray()));

//            Log.i("ATB", "Term: " + x + " has "
//                    + dataSet.get(x)[0]
//                    + ", " + dataSet.get(x)[1]
//                    + ", " + dataSet.get(x)[2]
//                    + ", " + dataSet.get(x)[3]);
        }


        data = new ColumnChartData(columns);
        data.setStacked(true);

        Axis axisX = new Axis();
        axisX.setValues(xLabels);
        Axis axisY = new Axis().setHasLines(true);        
        
        axisX.setName("Term");
        axisY.setName("Competency Units");
        
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        
        chart.setColumnChartData(data);
    }

    @Override
    public String getBadgeTitle() {
        return badgeTitle;
    }
}
