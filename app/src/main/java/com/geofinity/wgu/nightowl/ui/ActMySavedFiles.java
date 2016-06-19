package com.geofinity.wgu.nightowl.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.events.MySavedFilesEvent;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.ui.adapters.MySavedFilesAdapter;

import java.io.File;
import java.net.URLConnection;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 12/31/14.
 */
public class ActMySavedFiles extends ActionBarActivity {

    private int columnCount;
    private RecyclerView sgv;
    private StaggeredGridLayoutManager sglm;
    private MySavedFilesAdapter adapter;

    private EventBus eBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.act_mysavedfiles);

        columnCount = getResources().getInteger(R.integer.sgv_column_count);

        //////////////////////////////////////////////
        //  Grab the StaggeredGrid & LayoutManager  //
        //////////////////////////////////////////////
        sgv = (RecyclerView) findViewById(R.id.rvDownList);
        sgv.setHasFixedSize(true);

        sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        sgv.setLayoutManager(sglm);

        try {
            adapter = new MySavedFilesAdapter();
            sgv.setAdapter(adapter);

        } catch (Exception e) {
            Log.e("STORAGE", e.getMessage());
            NOMApp.prefs.customToast(this, "Can't access local storage.", Toast.LENGTH_LONG);
            return;
        }

        if(adapter.mDataset.size() < 1) {
            new MaterialDialog.Builder(this)
                    .title("Nothing to see here, move along...")
                    .content("You don't have any saved files at the moment.  You may want to:\n" +
                            "  - Download a Video or two\n" +
                            "  - Grab a Course of Study PDF\n" +
                            "  - Create a Progress Badge\n\n" +
                            "It's up to you, of course!")
                    .positiveText("OK")
                    .positiveColorRes(R.color.wguPrimary)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            finish();
                        }
                    })
                    .show();
        }

    }


    public void onEventMainThread(final MySavedFilesEvent event) {
        final File targetFile = adapter.mDataset.get(event.position);
        Uri uri = Uri.fromFile(targetFile);
        String mimeType = URLConnection.guessContentTypeFromName(uri.toString());
        if(mimeType == null) mimeType = "application/octet-stream";

        switch (event.action) {
            case FILE_SHARE:
                Log.e("MD_SHARE", uri.toString());
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType(mimeType);
                startActivity(Intent.createChooser(shareIntent, "Share File with:"));
                break;
            case FILE_PLAY:
                Log.e("MD_PLAY", uri.toString());
                Intent playIntent = new Intent(Intent.ACTION_VIEW);
                playIntent.setDataAndType(uri, mimeType);
                startActivity(Intent.createChooser(playIntent, "Open File with:"));
                break;
            case FILE_DELETE:
                Log.e("MD_DELETE", uri.toString());
                new MaterialDialog.Builder(this)
                        .title("Delete this file?")
                        .content(targetFile.getName())
                        .positiveText("Delete")
                        .negativeText("Cancel")
                        .positiveColorRes(R.color.wguPrimary)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                targetFile.delete();
                                adapter.mDataset.remove(event.position);
                                adapter.notifyItemRemoved(event.position);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                NOMApp.prefs.customToast(ActMySavedFiles.this, "Cancelled...", Toast.LENGTH_SHORT);
                            }
                        })
                        .show();
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        eBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eBus.unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
