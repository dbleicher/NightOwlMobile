package com.geofinity.wgu.nightowl.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.geofinity.pwnet.netops.ReqCampusNews;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.TOCEntry;
import com.geofinity.wgu.nightowl.ui.ActSecWeb;
import com.geofinity.wgu.nightowl.ui.adapters.ResourcesAdapter;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * This activity was meant to be displayed as a Dialog
 * Note that it's theme in the Manifest says so!
 *
 * Created by davidbleicher on 9/14/14.
 */
public class DiagResources extends android.support.v4.app.DialogFragment {

    private ListView lvResourceList;
    private ArrayList<String> commTitles;
    private ArrayList<TOCEntry> myResources;
    private EventBus eBus = EventBus.getDefault();


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_resources, container, false);
        lvResourceList = (ListView) v.findViewById(R.id.list);
        Crashlytics.setString("CURRENT_LISTVIEW", this.getClass().getSimpleName());

        final ResourcesAdapter myAdapter = new ResourcesAdapter(getActivity());
        lvResourceList.setAdapter(myAdapter);

        lvResourceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                int pblue = getResources().getColor(R.color.wguPrimary);
                Log.e("COLOR", "PBlue is: " + pblue);

                if (myAdapter.dataSet.get(position).title.contains("TaskStream")) {
                    // Open TaskStream in External Browser
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(myAdapter.dataSet.get(position).url));
                    startActivity(i);

                } else if (myAdapter.dataSet.get(position).title.contains("Campus")) {
                    NOMApp.opEx.add(new ReqCampusNews());

                } else if (myAdapter.dataSet.get(position).title.contains("myWGU")) {

                    try {
                        Intent launchIntent = NOMApp.ac.getPackageManager().getLaunchIntentForPackage("edu.wgu.students.android");
                        startActivity(launchIntent);

                    } catch (Exception e) {
                        // Do Nothing
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=edu.wgu.students.android")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            new MaterialDialog.Builder(getActivity())
                                    .title("Cannot Launch myWGU App")
                                    .content("The myWGU app may not be available on your device.")
                                    .build()
                                    .show();
                        }
                    }

                } else {
                    // Launch SecWeb with URL
                    Intent wmi = new Intent(getActivity(), ActSecWeb.class);
                    wmi.putExtra("TARGET_URL", myAdapter.dataSet.get(position).url);
                    wmi.putExtra("TARGET_TITLE", myAdapter.dataSet.get(position).title);
                    startActivity(wmi);
                }

                DiagResources.this.dismiss();
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


}
