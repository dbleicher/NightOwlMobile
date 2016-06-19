package com.geofinity.wgu.nightowl.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.geofinity.pwnet.models.jsonmodels.cosa.JTocEntry;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.ui.adapters.CosTocAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * This activity was meant to be displayed as a Dialog
 * Note that it's theme in the Manifest says so!
 *
 * Created by davidbleicher on 9/14/14.
 */
public class DiagCosToc extends android.support.v4.app.DialogFragment {

    private ListView lvCommList;
    private ArrayList<String> commTitles;
    private JSONArray commList;
    private String[] filtersList;
    private ArrayList<JTocEntry> tocList;
    private EventBus eBus = EventBus.getDefault();
    private String courseCode;
    private String cosURL;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_cos_toc, container, false);
        lvCommList = (ListView) v.findViewById(R.id.list);
        Crashlytics.setString("CURRENT_LISTVIEW", this.getClass().getSimpleName());

        courseCode = getArguments().getString("COURSE_CODE");
        cosURL = getArguments().getString("COS_URL");

        final Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<JTocEntry>>(){}.getType();
        tocList = gson.fromJson(NOMApp.prefs.getCosToc(courseCode), listType);

        // Updates selection based on cosURL & returns index
        int selection = findSelectedItem(tocList);

        CosTocAdapter adapter = new CosTocAdapter(getActivity(), tocList);

        lvCommList.setAdapter(adapter);
        lvCommList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                findSelectedItem(tocList);
                NOMApp.prefs.setCosToc(courseCode, gson.toJson(tocList));
                eBus.post(tocList.get(position));
                DiagCosToc.this.dismiss();
            }
        });

        // Scroll to selected item
        lvCommList.setSelection(selection);

        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dial = super.onCreateDialog(savedInstanceState);
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dial;
    }


    private int findSelectedItem(ArrayList<JTocEntry> tocList) {
        int tli = 0;
        int track = 0;
        for (JTocEntry t : tocList) {
            if (t.cosaUrl.contentEquals(cosURL)) {
                t.isBookmark = true;
                tli = track;
            } else {
                t.isBookmark = false;
            }
            track++;
        }
        return tli;
    }

//    private int findSelectedItemOld(ArrayList<JTocEntry> tocList) {
//        for (JTocEntry t : tocList) {
//            if (t.isBookmark) return tocList.indexOf(t);
//        }
//        return 0;
//    }

}
