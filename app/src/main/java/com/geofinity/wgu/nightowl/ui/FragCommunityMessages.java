package com.geofinity.wgu.nightowl.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.events.ECommunityFilterDialog;
import com.geofinity.pwnet.events.ECommunityMessages;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.CommunityMessage;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.ui.adapters.CommunityMessagesAdapter;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagCommunityFilter;
import com.geofinity.wgu.nightowl.ui.util.EMainFragments;
import com.geofinity.wgu.nightowl.ui.util.IMainActivity;
import com.geofinity.wgu.nightowl.ui.util.QRBarDecoration;
import com.geofinity.wgu.nightowl.ui.util.TargetedSwipeRefreshLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconButton;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by David Bleicher on 8/20/14.
 */
public class FragCommunityMessages extends Fragment  {

    private TargetedSwipeRefreshLayout swiper;
    private RecyclerView sgv;
    private StaggeredGridLayoutManager sglm;
    private PocketPreferences prefs;

    private LinearLayout commToolBar;
    private FloatingActionButton fab;
    
    private TextView tvCommunityName;
    private IconButton ibFilter;

    private MenuItem searchItem;

    private IMainActivity myActivity;
    private EventBus eBus;
    private CommunityMessagesAdapter cmAdapter;
    private ArrayList<CommunityMessage> myDataset;

    private int columnCount;
    private int qrBarHeight;

    private Animation inAnim;
    private Animation outAnim;
    private Animation fabinAnim;
    private Animation faboutAnim;
    
    private int fabState = 0;  // 0 neutral, 1 locked, 2 postable
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        eBus = EventBus.getDefault();
        prefs = NOMApp.prefs;
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_comm_messages, container, false);

        //////////////////////////////
        //  Setup Community Toolbar //
        //////////////////////////////
        inAnim  = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_top);
        outAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_top);
        
        fabinAnim  = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_bottom);
        faboutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_bottom);
        
//        inAnim.setDuration(600);
//        outAnim.setDuration(600);

        commToolBar = (LinearLayout) rootView.findViewById(R.id.commToolBar);
        tvCommunityName = (TextView) rootView.findViewById(R.id.tvCommunityName);
        ibFilter = (IconButton) rootView.findViewById(R.id.ibFilter);

        tvCommunityName.setText( myActivity.displayCommFilter() );

        ibFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DiagCommunityFilter cfl = new DiagCommunityFilter();
                cfl.show(getFragmentManager(), "choosecommunitydialog");
            }
        });


        //////////////////////////////////////////
        //  Setup Floating Action Button (FAB)  //
        //////////////////////////////////////////
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setImageDrawable( getFabDrawable() );
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (fabState) {
                    case 0:
                        new MaterialDialog.Builder(getActivity())
                                .title("Select an Individual Community")
                                .content("To create a new message thread, you must choose an individual community.")
                                .positiveText("Choose Community")
                                .negativeText("Cancel")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        DiagCommunityFilter cfl = new DiagCommunityFilter();
                                        cfl.show(getFragmentManager(), "choosecommunitydialog");
                                    }
                                })
                                .show();
                        break;
                    case 1:
                        new MaterialDialog.Builder(getActivity())
                                .title("Community is Locked")
                                .content("WGU has disabled creating new messages in this community.")
                                .positiveText("OK")
                                .show();
                        break;
                    case 2:
                        MaterialDialog md = new MaterialDialog.Builder(getActivity())
                                .title("Create a New Message")
                                .customView(R.layout.mddiag_newpost_message, false)
                                .positiveText("Post Message")
                                .negativeText("Cancel")
                                .autoDismiss(false)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(final MaterialDialog dialog) {
                                        EditText subj = (EditText) dialog.findViewById(R.id.etSubjectLine);
                                        EditText body = (EditText) dialog.findViewById(R.id.etPostBody);
                                        if (subj.getText().length() > 3 && body.getText().length() > 3) {
                                            new PostAMessage().execute( new String[]{
                                                    subj.getText().toString(),
                                                    body.getText().toString(),
                                                    prefs.getCurrentCommId()}
                                            );
                                            dialog.dismiss();
                                        } else {
                                            dialog.setTitle("Subject and/or message are too short!");
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    dialog.setTitle("Create a New Message");
                                                }
                                            }, 3000);
                                        }
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        dialog.dismiss();
                                    }
                                })
                                .build();
                        TextView tvCommName = (TextView) md.getCustomView().findViewById(R.id.tvCommName);
                        tvCommName.setText(prefs.getCurrentCommName());
                        md.show();
                        break;
                }
            }
        });



        //////////////////////////////
        //  Setup Swipe To Refresh  //
        //////////////////////////////
        swiper = (TargetedSwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swiper.setSize(TargetedSwipeRefreshLayout.LARGE);
        swiper.setColorSchemeResources(
                R.color.wguGold,
                R.color.wguRedDark,
                R.color.wguPrimaryAccent,
                R.color.wguPrimaryDark
        );

        swiper.setOnRefreshListener(new TargetedSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PocketPreferences.customToast(getActivity(), "Refeshing Community Messages...", Toast.LENGTH_SHORT);
                NOMApp.reqHelp.performCommunityMessageRefresh();
            }
        });

        //////////////////////////////////////////////
        // Get number of columns and toolBar height //
        //////////////////////////////////////////////
        columnCount = getResources().getInteger(R.integer.sgv_column_count);
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            qrBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        //////////////////////////////////////////////
        //  Grab the StaggeredGrid & LayoutManager  //
        //////////////////////////////////////////////
        sgv = (RecyclerView) rootView.findViewById(R.id.rvMessageList);
        sgv.addItemDecoration(new QRBarDecoration(columnCount, qrBarHeight));
        sgv.setHasFixedSize(true);

        sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        sgv.setLayoutManager(sglm);

        //////////////////////////////
        //  Setup Adapter & DataSet //
        //////////////////////////////
        myDataset = new ArrayList<CommunityMessage>();
        cmAdapter = new CommunityMessagesAdapter(getActivity(), myDataset);

        sgv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 3) {
                    if (commToolBar.getVisibility() == View.VISIBLE)
                        hideCommToolBar();

                } else if (dy < -3) {

                    if (commToolBar.getVisibility() == View.GONE)
                        showCommToolBar();
                }
            }
        });

        try {
            JSONArray messList = new JSONArray(prefs.getCommMessages());

            for (int x = 0; x < messList.length(); x++)
            {
                myDataset.add(new CommunityMessage(messList.getJSONObject(x)));
            }

        } catch (Exception e) {
            //
        }

        sgv.setAdapter(cmAdapter);

        // Set swiper's target!
        swiper.setTargetScrollableView(sgv);

        return rootView;

    }

    private Drawable getFabDrawable() {
        // fabState: 0 neutral, 1 locked, 2 postable
        // FabType: 0 normal, 1 mini
        
        if (prefs.getCurrentCommId().equals("0000")) {
            fabState = 0;  
            fab.setColorNormalResId(R.color.wguGold);
            fab.setType(1);
            return new IconDrawable(getActivity(), FontAwesomeIcons.fa_circle_o)
                    .colorRes(R.color.wguWhite)
                    .sizeDp(24);
        } else {
            if ( prefs.isCommunityLocked(prefs.getCurrentCommId()) ) {
                fabState = 1;
                fab.setColorNormalResId(R.color.wguRedDark);
                fab.setType(1);
                return new IconDrawable(getActivity(), FontAwesomeIcons.fa_lock)
                        .colorRes(R.color.wguWhite)
                        .sizeDp(24);
            } else {
                fabState = 2;
                fab.setColorNormalResId(R.color.wguPrimary);
                fab.setType(0);
                return new IconDrawable(getActivity(), FontAwesomeIcons.fa_plus)
                        .colorRes(R.color.wguWhite)
                        .sizeDp(28);
            }
        }
        
    }
    
    public void onEventMainThread(ECommunityFilterDialog event) {
        if (event == ECommunityFilterDialog.FILTER_CHANGED) {
            tvCommunityName.setText(prefs.getCurrentCommName());
            prefs.setCommSearchCurrent("");
            swiper.setRefreshing(true);
            NOMApp.reqHelp.performCommunityMessageRefresh();
        }
    }

    public void onEventMainThread(ECommunityMessages event) {
        switch (event) {
            case RETRIEVE_SUCCESS:
                tvCommunityName.setText( myActivity.displayCommFilter() );
                fab.setImageDrawable( getFabDrawable() );
                cmAdapter.refreshDisplay();
                swiper.setRefreshing(false);
                break;
            case RETRIEVE_FAILURE:
                swiper.setRefreshing(false);
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Get handle to Activity Interface for
        // convenience calls to parent.
        myActivity = (IMainActivity) activity;
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.goHome:
                myActivity.navigateToPage(EMainFragments.HOME_FRAG);
                break;
            case R.id.goDegree:
                myActivity.navigateToPage(EMainFragments.COURSES_FRAG);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_community, menu);

        menu.findItem(R.id.goHome).setIcon(
                new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_home)
                        .colorRes(R.color.wguWhite)
                        .sizeDp(20)
        );

        menu.findItem(R.id.goDegree).setIcon(
                new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_graduation_cap)
                        .colorRes(R.color.wguWhite)
                        .sizeDp(20)
        );

        searchItem = menu.findItem(R.id.comm_act_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search for...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() != 0) {
                    // handle search here
                    swiper.setRefreshing(true);
                    NOMApp.prefs.setCommSearchCurrent(query);
                    tvCommunityName.setText( myActivity.displayCommFilter() );
                    NOMApp.reqHelp.performCommunityMessageRefresh();
                    MenuItemCompat.collapseActionView(searchItem);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

    private void showCommToolBar() {

        commToolBar.startAnimation(inAnim);
        commToolBar.setVisibility(View.VISIBLE);
        fab.startAnimation(fabinAnim);
        fab.setVisibility(View.VISIBLE);
    }

    private void hideCommToolBar() {

        commToolBar.startAnimation(outAnim);
        commToolBar.setVisibility(View.GONE);
        fab.startAnimation(faboutAnim);
        fab.setVisibility(View.GONE);
    }

    /**
     * Async class to post the user created message to the WGU Community site.
     */
    private class PostAMessage extends AsyncTask<String, String, Boolean> {
        String[] myMess = {"", ""};
        private MaterialDialog pd;
        
        private String commNum;
        private String subject;
        private String body;

        /**
         * Executed in the main UI thread BEFORE the task is run.
         */
        protected void onPreExecute() {
            pd = new MaterialDialog.Builder(getActivity())
                    .title("Posting Your Message")
                    .content("Contacting WGU Community...")
                    .progress(true, 0)
                    .show();
        }

        /**
         * Executed in the main UI thread AFTER the task is run.
         */
        protected void onPostExecute(Boolean success) {
            if (success) {
                pd.setContent("Your message has been posted");
                PocketPreferences.customToast(getActivity(), "Refeshing Community Messages...", Toast.LENGTH_SHORT);
                NOMApp.reqHelp.performCommunityMessageRefresh();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                }, 500);
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
        protected Boolean doInBackground(String... params) {
            OkHttpClient okClient = NOMApp.nlm.getOkClient();
            subject = params[0];
            body = params[1];
            commNum = params[2];
            
            RequestBody formBody = new FormEncodingBuilder()
                    .add("postedFromGUIEditor", "false")
                    .add("subject",             subject)
                    .add("body",                body)
                    .add("doPost",              "Post Message")
                    .add("reply",               "false")
                    .add("communityID",         commNum)
                    .build();

            Request req = new Request.Builder()
                    .url("https://community.wgu.edu/clearspacex/post.jspa")
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
