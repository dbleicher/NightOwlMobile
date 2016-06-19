package com.geofinity.wgu.nightowl.netops;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.netops.ReqCosProgressUpdate;
import com.geofinity.wgu.nightowl.NOMApp;

/**
 * Created by davidbleicher on 10/17/15.
 */
public class CosProgressInterface {

    Context mContext;

    public CosProgressInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void cosProgressChoose(final int status,
                                  final String cCode,
                                  final String rKind,
                                  final long spId,
                                  final long subId,
                                  final long topId,
                                  final long actId,
                                  final long uatId) {

        // Status Codes:
        //  0 = Incomplete
        //  1 = Skipped
        //  2 = Completed

        String[] statusCodes = {"Incomplete", "Skipped", "Completed"};

        String dialTitle = "Mark this Activity:";
        if (rKind.contentEquals("subject")) {
            dialTitle = "Mark all Activities in this Subject:";
        } else if (rKind.contentEquals("topic")) {
            dialTitle = "Mark all Activities in this Topic:";
        }

        new MaterialDialog.Builder(mContext)
                .title(dialTitle)
                .items(statusCodes)
                .itemsCallbackSingleChoice(status, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (status == which) {
                            // No change, nothing to do
                        } else {
                            NOMApp.opEx.add(new ReqCosProgressUpdate(which, cCode, rKind, spId, subId, topId, actId, uatId));
                            Log.e("COS-UPDATE", "Was " + status + " and user chose " + which);
                        }
                        return true;
                    }
                })
                .positiveText("Update")
                .negativeText("Cancel")
                .show();

    }

}
