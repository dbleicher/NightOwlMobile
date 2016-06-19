package com.geofinity.wgu.nightowl.ui.dialogs;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by davidbleicher on 1/10/15.
 */
public class DiagReplyMessage extends android.support.v4.app.DialogFragment {

    private String replyKind;
    private String messageID;
    private String threadSubject;

    private Button postButton;
    private Button cancelButton;
    private TextView tvSubject;
    private EditText etPostBody;
    
    Dialog dial;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dial = super.onCreateDialog(savedInstanceState);
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dial;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.diag_replyto_message, container, false);

        Bundle myArgs = getArguments();
        if (myArgs != null) {
            String oURL = myArgs.getString("ORIG_URL");
            String oSub = myArgs.getString("ORIG_SUBJECT");
            createAReply(oURL, oSub);
        }

        postButton = (Button) v.findViewById(R.id.btPost);
        cancelButton = (Button) v.findViewById(R.id.btCancel);
        tvSubject = (TextView) v.findViewById(R.id.tvSubjectLine);
        etPostBody = (EditText) v.findViewById(R.id.etPostBody);

        tvSubject.setText(threadSubject);

        postButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String pBody = etPostBody.getText().toString();

                if (pBody.length() >= 3) {
                    new PostAReply().execute();
                } else {
                    NOMApp.prefs.customToast(getActivity(),
                            "Subject and message must both be at least 3 characters long.",
                            Toast.LENGTH_SHORT);
                }
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }


    /**
     * Parse the original URL into pieces we can use
     * @param originalURL
     * @param originalSubject
     */
    private void createAReply(String originalURL, String originalSubject) {
        String replyKind = null;
        String currentMessageId = null;

		/* Here's what we need to capture */

        // Original URLs may look like these:
        // http://community.wgu.edu/clearspacex/message/800808
        // http://community.wgu.edu/clearspacex/message/796279?tstart=0#796279
        // http://community.wgu.edu/clearspacex/thread/55123

        // Reply URLs may look like these:
        // http://community.wgu.edu/clearspacex/post!reply.jspa?threadID=58676
        // http://community.wgu.edu/clearspacex/post!reply.jspa?messageID=800808


        Pattern patOrig  = Pattern.compile("clearspacex/(.*)/([0-9]*)");
        Pattern patReply = Pattern.compile("\\?(.*)ID=([0-9]*)");

        Matcher m;

        if (originalURL.contains("/message/") || originalURL.contains("/thread/")) {
            m = patOrig.matcher(originalURL);
            try {
                m.find();
                replyKind = m.group(1);
                currentMessageId = m.group(2);
            } catch (Exception e) {
                Log.e("REPLYKIND", "Something Puked");
            }
        } else if (originalURL.contains("messageID") || originalURL.contains("threadID")) {
            m = patReply.matcher(originalURL);
            try {
                m.find();
                replyKind = m.group(1);
                currentMessageId = m.group(2);
            } catch (Exception e) {
                Log.e("REPLYKIND", "Something Puked");
            }
        }

        if (replyKind == null || currentMessageId == null) {
            NOMApp.prefs.customToast(getActivity(), "I cannot reply to Blog, Wiki, or Doc posts.", Toast.LENGTH_LONG);
            dismiss();
            return;
        }

        this.replyKind = replyKind;
        this.messageID = currentMessageId;
        this.threadSubject = originalSubject;
    }



    /**
     * Async class to post the user created message to the WGU Community site.
     */
    private class PostAReply extends AsyncTask<Void, String, Boolean> {
        String[] myMess = {"", ""};
        private MaterialDialog pd;
        private String body = etPostBody.getText().toString();

        /**
         * Executed in the main UI thread BEFORE the task is run.
         */
        protected void onPreExecute() {
            pd = new MaterialDialog.Builder(getActivity())
                    .title("Posting A Message")
                    .content("Contacting WGU Community...")
                    .progress(true, 0)
                    .show();
        }

        /**
         * Executed in the main UI thread AFTER the task is run.
         */
        protected void onPostExecute(Boolean success) {

            try {
                pd.dismiss();

                if (success) {
                    NOMApp.prefs.customToast(getActivity(), "Message has been posted!", Toast.LENGTH_LONG);
                } else {
                    NOMApp.prefs.customToast(getActivity(), "Posting Failed!", Toast.LENGTH_LONG);
                }

                dismiss();

            } catch (Exception e) {
                // Log.e("POSTREPLY", e.getMessage());
            }
        }

        /**
         * Executed in the main UI thread to display progress updates while the
         * task is running.
         */
        protected void onProgressUpdate(String... messages) {
            pd.setContent(messages[0]);
        }

        /**
         * This is the main work of the task, and runs in a separate thread.
         *
         */
        protected Boolean doInBackground(Void... params) {

            OkHttpClient okClient = NOMApp.nlm.getOkClient();

            String idType = (replyKind.contains("message")) ? "messageID" : "threadID";

            RequestBody formBody = new FormEncodingBuilder()
                    .add( idType,               messageID)
                    .add("postedFromGUIEditor", "false")
                    .add("subject",             threadSubject)
                    .add("body",                body)
                    .add("doPost",              "Post Message")
                    .add("reply",               "true")
                    .build();

            Request req = new Request.Builder()
                    .url("http://community.wgu.edu/clearspacex/post.jspa")
                    .post(formBody)
                    .build();

            myMess[0] = "Posting the message...";
            publishProgress(myMess);

            try {
                Response resp = okClient.newCall(req).execute();
                if (!resp.isSuccessful()) {
                    resp.body().close();
                    return false;
                } else {
                    
                    if (resp.body().string().contains("MyWGU Communities: Unauthorized")) {
                        myMess[0] = "Could not post message. This community may be LOCKED.\nTap your 'Back' button to continue...";
                        publishProgress(myMess);
                        return false;
                    } else {
                        myMess[0] = "Message Posted!";
                        publishProgress(myMess);
                    }
                }
            } catch (Exception e) {
                return false;
            }

            return true;

        } // End of doInBackground

    } // End of PostAReply



}
