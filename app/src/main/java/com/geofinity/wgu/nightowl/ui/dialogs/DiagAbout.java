package com.geofinity.wgu.nightowl.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.geofinity.pwnet.events.CreditURL;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.ui.adapters.AboutBoxAdapter;

import de.greenrobot.event.EventBus;

/**
 * This activity was meant to be displayed as a Dialog
 * Note that it's theme in the Manifest says so!
 *
 * Created by davidbleicher on 9/14/14.
 */
public class DiagAbout extends android.support.v4.app.DialogFragment {

    private RecyclerView rvAboutList;
    private EventBus eBus = EventBus.getDefault();


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_aboutbox, container, false);

        rvAboutList = (RecyclerView) v.findViewById(R.id.rvAboutList);
        rvAboutList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvAboutList.setAdapter(new AboutBoxAdapter(getActivity()));

        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dial = super.onCreateDialog(savedInstanceState);
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dial;
    }

    @Override
    public void onResume() {
        super.onResume();
        eBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        eBus.unregister(this);
    }

    public void onEventMainThread(CreditURL cu) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cu.url)));
    }
}
