package com.geofinity.wgu.nightowl.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.geofinity.pwnet.events.ECommunityFilterDialog;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * This activity was meant to be displayed as a Dialog
 * Note that it's theme in the Manifest says so!
 *
 * Created by davidbleicher on 9/14/14.
 */
public class DiagCommunityFilter extends android.support.v4.app.DialogFragment {

    private ListView lvCommList;
    private ArrayList<String> commTitles;
    private JSONArray commList;
    private String[] filtersList;
    private EventBus eBus = EventBus.getDefault();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_community_list, container, false);
        lvCommList = (ListView) v.findViewById(R.id.list);
        Crashlytics.setString("CURRENT_LISTVIEW", this.getClass().getSimpleName());

        // Fill the array
        commTitles = new ArrayList<String>();
        try {
            commList = new JSONArray(NOMApp.prefs.getCommList());
            int ic = commList.length();
            for (int x = 0; x < ic; x++) {
                commTitles.add(((JSONObject)commList.get(x)).optString("commName"));
            }
            filtersList = commTitles.toArray(new String[commTitles.size()]);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.cell_diag_listitem, R.id.tvTitle, commTitles);
        lvCommList.setAdapter(adapter);
        lvCommList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                postSelection(position);
                DiagCommunityFilter.this.dismiss();
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
        JSONObject selComm = null;
        try {
            selComm = (JSONObject) commList.get(position);
            String commId = selComm.optString("commId");
            String commName = selComm.optString("commName");

            NOMApp.prefs.setCurrentCommId(commId);
            NOMApp.prefs.setCurrentCommName(commName);

            Log.i("COMMFILTER", "Choice: " + commId + " -- " + commName);

        } catch (JSONException e) {

        }
        eBus.post(ECommunityFilterDialog.FILTER_CHANGED);
    }



}
