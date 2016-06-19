package com.geofinity.wgu.nightowl.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.events.FullMessEvent;
import com.geofinity.pwnet.models.UserProfileHtml;
import com.geofinity.pwnet.netops.ReqFullMessageThread;
import com.geofinity.pwnet.netops.ReqUserProfile;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.CommunityMessage;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagReplyMessage;
import com.geofinity.wgu.nightowl.ui.util.CircleTransform;
import com.google.gson.Gson;
import com.joanzapata.iconify.widget.IconButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.HttpCookie;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 9/11/14.
 */
public class ActCommunityMessage extends ActionBarActivity {

    private TextView tvAuthorName;
    private TextView tvUpdated;
    private ImageView nivAvatar;
    private TextView tvReplies;
    private WebView wvSummary;
    private CommunityMessage myMessage;
    private MaterialDialog pd;

    private IconButton ibReply;
    private IconButton ibToggle;
    private boolean pointsBack;

    private File fullFile;

    private EventBus eBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.act_community_message);

        tvAuthorName = (TextView) findViewById(R.id.tvAuthorName);
        tvUpdated = (TextView) findViewById(R.id.tvUpdated);
        wvSummary = (WebView) findViewById(R.id.wvSummary);
        tvReplies = (TextView) findViewById(R.id.tvReplies);
        nivAvatar = (ImageView) findViewById(R.id.nivAvatar);
        ibToggle  = (IconButton) findViewById(R.id.ibToggle);
        ibReply  = (IconButton) findViewById(R.id.ibReply);


        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null) {
            Gson gson = new Gson();
            String mmJson = (String) myExtras.get("MY_MESSAGE");
            try {
                myMessage = gson.fromJson(mmJson, CommunityMessage.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Something went wrong!
        if (myMessage == null) {
            finish();
        }


        ibReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepForReply(myMessage.link, myMessage.title);
            }
        });

        ibToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pointsBack) {
                    // The button points left
                    // Change to "down" and reload the message
                    ibToggle.setText("{fa-users}");
                    pointsBack = false;
                    prepDisplayMessage();
                } else {
                    // The button points down
                    // Change to "left" and load full message thread
                    ibToggle.setText("{fa-angle-left}");
                    pointsBack = true;
                    fetchAndPrepFullThread();
                }
            }
        });


        nivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String profURL = String.format(String.format("http://community.wgu.edu/clearspacex/people/%s",myMessage.authorEmail));
                fetchAndPrepProfile(profURL);
            }
        });


        tvAuthorName.setText(myMessage.author);
        tvUpdated.setText(NOMApp.sdfDisplay.format(myMessage.updated));

        Picasso.with(getApplicationContext())
                .load(String.format("http://community.wgu.edu/clearspacex/people/%s/avatar",myMessage.authorEmail))
                .centerCrop()
                .error(R.drawable.owl5)
                .transform(new CircleTransform())
                .resize(40, 40)
                .into(nivAvatar);

        String rps;
        if (myMessage.replyCount == 0) {
            rps = " ... ";
        } else if (myMessage.replyCount == 1) {
            rps = "1 reply in thread";
        } else {
            rps = ""+myMessage.replyCount+" replies in thread";
        }
        tvReplies.setText(rps);

        //////////////////////////////////
        //  Setup the WebView Here      //
        //////////////////////////////////
        WebSettings settings = wvSummary.getSettings();

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);  //Use zoom capability
        settings.setDisplayZoomControls(false); //Don't display the controls

        settings.setDomStorageEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        wvSummary.setWebChromeClient(new WebChromeClient());
        wvSummary.setWebViewClient(new MyThreadWebViewClient());

        // Put the Auth Cookie into the WebView
        String loginToken = "";
        List<HttpCookie> clist = NOMApp.cookieMan.getCookieStore().getCookies();
        for (HttpCookie c : clist) {
            if (c.getName().contains("iPlanet")) {
                loginToken = c.getValue();
                break;
            }
        }
        // CookieSyncManager.createInstance(NOMApp.ac);
        CookieManager cookieManager = CookieManager.getInstance();
        String cookieString = "iPlanetDirectoryPro=" + loginToken + "; domain=.wgu.edu";
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(wvSummary, true);
        }
        cookieManager.setCookie("wgu.edu", cookieString);
        // CookieSyncManager.getInstance().sync();

        prepDisplayMessage();
    }


    //////////////////////////////////
    //  Add WebClient Stuff Here!   //
    //////////////////////////////////
    /**
     * Custom WebViewClient to mangle various links.
     *
     */
    private class MyThreadWebViewClient extends WebViewClient {
        //		Pattern patReply = Pattern.compile("\\?(.*)ID=([0-9]*)");
        Pattern patEdit = Pattern.compile("message/([0-9]*)/edit");
        Matcher m;

        boolean scaleChangedRunnablePending = false;

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (scaleChangedRunnablePending) return;
                view.postDelayed(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        wvSummary.evaluateJavascript(
                                "document.body.style.width = window.innerWidth+'px';",
                                null
                        );
                        scaleChangedRunnablePending = false;
                    }
                }, 100);
            } else {
                super.onScaleChanged(view, oldScale, newScale);
            }
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Log.i("WebView-URL", url);

            // Here's a REPLY link
            // http://community.wgu.edu/clearspacex/post!reply.jspa?messageID=597740
            if (url.contains("post!reply")) {
                prepForReply(url, myMessage.title);
                return true;
            }

            // Here's an EDIT link
            // https://community.wgu.edu/clearspacex/message/597761/edit
            if (url.matches(".*message/[0-9]*/edit$")) {
                m = patEdit.matcher(url);
                m.find();
                NOMApp.prefs.customToast(ActCommunityMessage.this, "Sorry, I don't yet support editing.", Toast.LENGTH_LONG);
                return true;
            }

            if (MailTo.isMailTo(url)) {
                MailTo mt = MailTo.parse(url);
                NOMApp.prefs.composeEmail(ActCommunityMessage.this, mt.getTo(), false);
                return true;
            }

            if (url.contains("community.wgu.edu/clearspacex/people/")) {
                fetchAndPrepProfile(url);
                return true;
            }


            if (!url.contains("wgu.")) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(myIntent);
                return true;
            }

            return false;
        }
    }


    //////////////////////
    //  More Overrides  //
    //////////////////////
    @Override
    protected void onStart() {
        super.onStart();
        eBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        eBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fullFile != null && fullFile.exists()) {
            fullFile.delete();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // NavUtils.navigateUpTo(this, new Intent(this, ActMain.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    ///////////////////////////
    // Preppers & Fetchers   //
    ///////////////////////////

    public void prepForReply(String url, String subject) {
        DiagReplyMessage drm = new DiagReplyMessage();
        Bundle b = new Bundle();
        b.putString("ORIG_URL", url);
        b.putString("ORIG_SUBJECT", subject);
        drm.setArguments(b);
        drm.show(getSupportFragmentManager(), "replyMessageDiag");
    }


    public void prepDisplayMessage() {
        String messText = String.format(
                "<h3 style=\"color:#4674A3;\">%s</h3><span >%s</span>",
                myMessage.title,
                myMessage.summary
        );

        wvSummary.loadDataWithBaseURL("http://community.wgu.edu/", messText, "text/html", "UTF-8", null);
    }

    public void fetchAndPrepFullThread() {
        if (fullFile != null && fullFile.exists()) {
            wvSummary.loadUrl("file://"+fullFile.getPath());
        } else {

            pd = new MaterialDialog.Builder(this)
                    .title("Retrieving All Messages in Thread")
                    .content("please wait...")
                    .progress(true, 0)
                    .show();

            NOMApp.opEx.add(new ReqFullMessageThread(myMessage.link));
        }
    }

    public void fetchAndPrepProfile (String url) {
        pd = new MaterialDialog.Builder(this)
                .title("Retrieving User Profile")
                .content("please wait...")
                .progress(true, 0)
                .show();
        
        NOMApp.opEx.add(new ReqUserProfile(url));
        ibToggle.setText("{fa-angle-left}");
        pointsBack = true;
    }


    //////////////////////
    // Event Handlers   //
    //////////////////////

    public void onEventMainThread(UserProfileHtml uph) {
        wvSummary.loadDataWithBaseURL("http://community.wgu.edu/", uph.userProfileHtml, "text/html", "UTF-8", null);
        if (pd != null) pd.dismiss();
    }

    public void onEventMainThread(FullMessEvent e) {
        if (e.didSucceed) {
            fullFile = new File(e.message);
            wvSummary.loadUrl("file://"+fullFile.getPath());
            if (pd != null) pd.dismiss();
        } else {
            pd.setContent("Failed loading message thread. Press 'Back'");
        }
    }


}
