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

import com.crashlytics.android.Crashlytics;
import com.geofinity.pwnet.events.ESocialFilter;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.ui.ActSocial;

import org.json.JSONArray;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * This activity was meant to be displayed as a Dialog
 * Note that it's theme in the Manifest says so!
 *
 * Created by davidbleicher on 9/14/14.
 */
public class DiagSocialFilter extends android.support.v4.app.DialogFragment {

    private ListView lvFilterList;
    private ArrayList<String> filterNames;
    private JSONArray commList;
    private String[] filtersList;
    private EventBus eBus = EventBus.getDefault();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_social_filter, container, false);
        lvFilterList = (ListView) v.findViewById(R.id.list);
        Crashlytics.setString("CURRENT_LISTVIEW", this.getClass().getSimpleName());

        // Fill the array
        filterNames = ActSocial.getFilterTitles();

        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, filterNames);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.cell_diag_listitem,
                R.id.tvTitle,
                filterNames
        );

        lvFilterList.setAdapter(adapter);
        lvFilterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                postSelection(position);
                DiagSocialFilter.this.dismiss();
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
        NOMApp.prefs.setSocialFilterNum(position);
        eBus.post(ESocialFilter.FILTER_CHANGE);
    }



}
