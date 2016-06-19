package com.geofinity.wgu.nightowl.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geofinity.pwnet.events.EAuth;
import com.geofinity.pwnet.netops.NetLibPrefs;
import com.geofinity.pwnet.netops.RequestHelper;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;

import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 9/1/14.
 *
 * Done (b212): Reimplement as Dialog and add "Clear All" button
 *
 */
public class DiagSettings extends android.support.v4.app.DialogFragment {

    private EventBus eBus;
    private PocketPreferences prefs;
    private NetLibPrefs nPrefs;
    private RequestHelper reqHelp;
    private ProgressBar pbSpin;
    private TextView tvBadLogin;

    private EditText etUsername;
    private EditText etPassword;
    private Button btSave;
    private Button btCancel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dial = super.onCreateDialog(savedInstanceState);
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dial;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_settings, container, false);

        // Get handle to static singletons
        eBus = EventBus.getDefault();
        prefs = NOMApp.prefs;
        nPrefs = NOMApp.nlm.prefs;
        reqHelp = NOMApp.reqHelp;

        etUsername  = (EditText) v.findViewById(R.id.etUsername);
        etPassword  = (EditText) v.findViewById(R.id.etPassword);
        btSave      = (Button) v.findViewById(R.id.btSave);
        btCancel    = (Button) v.findViewById(R.id.btCancel);
        pbSpin      = (ProgressBar) v.findViewById(R.id.pbcSpin);
        tvBadLogin  = (TextView) v.findViewById(R.id.tvBadLogin);

        etUsername.setText(prefs.getUserName());
        etPassword.setText(prefs.getUserPass());

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String un = etUsername.getText().toString().trim();
                String pw = etPassword.getText().toString().trim();

                if (un.length() > 1 && pw.length() > 1) {

                    if (un.contains("@")) {
                        un = un.split("@")[0];
                    }

                    pbSpin.setVisibility(View.VISIBLE);
                    tvBadLogin.setVisibility(View.GONE);
                    btSave.setEnabled(false);
                    btCancel.setEnabled(false);

                    //Clear all previous data & Cache
                    nPrefs.resetAll(getActivity(), false);

                    //Save and authenticate
                    nPrefs.setUserName(un);
                    nPrefs.setUserPass(pw);
                    prefs.setLastAuth(new Date().getTime());
                    prefs.setLastAppLaunch();

                    reqHelp.ssoAuthenticate();
                    hideKeyboard();

                } else {
                    prefs.customToast(getActivity(), "Username and password must be at least 2 characters long.", Toast.LENGTH_LONG);
                }
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        eBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        eBus.unregister(this);
    }

    public void onEventMainThread(EAuth event) {
        switch (event) {
            case AUTH_WORKED:
                dismiss();
                // reqHelp.refreshData(true);
                break;
            case AUTH_FAILED:
                pbSpin.setVisibility(View.GONE);
                btSave.setEnabled(true);
                btCancel.setEnabled(true);
                tvBadLogin.setVisibility(View.VISIBLE);
        }
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etUsername.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
    }


}
