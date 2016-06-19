package com.geofinity.wgu.nightowl.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.geofinity.pwnet.events.ECoursesData;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.ui.FragCourses;

import org.json.JSONArray;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * This activity was meant to be displayed as a Dialog
 * Note that it's theme in the Manifest says so!
 *
 * Created by davidbleicher on 9/14/14.
 */
public class DiagCourseSort extends android.support.v4.app.DialogFragment {

    private ListView lvCourseSort;
    private TextView tvTitle;

    private ArrayList<String> sortNames;
    private JSONArray commList;
    private String[] sotingsList;
    private EventBus eBus = EventBus.getDefault();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_course_filter, container, false);
        lvCourseSort = (ListView) v.findViewById(R.id.list);
        tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvTitle.setText("Sort Courses:");

        Crashlytics.setString("CURRENT_LISTVIEW", this.getClass().getSimpleName());

        // Fill the array
        sortNames = FragCourses.getSortingTitles();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.cell_diag_listitem,
                R.id.tvTitle,
                sortNames
        );

        lvCourseSort.setAdapter(adapter);
        lvCourseSort.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                postSelection(position);
                DiagCourseSort.this.dismiss();
            }
        });
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dial = super.onCreateDialog(savedInstanceState);
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dial;
    }

    private void postSelection(int position) {
        NOMApp.prefs.setCourseSortingNum(position);
        eBus.post(ECoursesData.RETRIEVED_COURSE_DATA);
    }



}
