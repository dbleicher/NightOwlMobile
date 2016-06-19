package com.geofinity.wgu.nightowl.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.ui.badges.AccountableBadge;
import com.geofinity.wgu.nightowl.ui.badges.CUsByTermBadge;
import com.geofinity.wgu.nightowl.ui.badges.EBadgeList;
import com.geofinity.wgu.nightowl.ui.badges.IBadge;
import com.geofinity.wgu.nightowl.ui.badges.ProgramBadge;
import com.geofinity.wgu.nightowl.ui.badges.TermPieBadge;
import com.joanzapata.iconify.widget.IconButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by davidbleicher on 1/3/15.
 * Updating for Publication
 */
public class ActBadge extends ActionBarActivity {
    private PocketPreferences prefs;
    
    private FrameLayout flBadgeFrame;
    private EditText etFilename;
    private Button btSaveMe;
    
    private IconButton btPrevious;
    private TextView tvBadgeName;
    private IconButton btNext;
    private String badgeTitle;

    private View pbv;
    
    private String dateSuf;
    private EBadgeList currentBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.act_badge);

        prefs = NOMApp.prefs;
        dateSuf = NOMApp.sdfYMD.format(new Date());

        flBadgeFrame = (FrameLayout) findViewById(R.id.flBadgeFrame);
        etFilename = (EditText) findViewById(R.id.etFileName);
        btSaveMe = (Button) findViewById(R.id.btSaveMe);
        tvBadgeName = (TextView) findViewById(R.id.tvBadgeName);

        btPrevious = (IconButton) findViewById(R.id.btPrevious);
        btNext = (IconButton) findViewById(R.id.btNext);

        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentBadge) {
                    case PROGRAM_BADGE:
                        currentBadge = EBadgeList.CUS_BY_TERM_BADGE;
                        break;
                    case CUS_BY_TERM_BADGE:
                        currentBadge = EBadgeList.TERM_PIE_BADGE;
                        break;
                    case TERM_PIE_BADGE:
                        currentBadge = EBadgeList.ACCOUNTABLE_BADGE;
                        break;
                    case ACCOUNTABLE_BADGE:
                        currentBadge = EBadgeList.PROGRAM_BADGE;
                        break;
                }
                setActiveBadge(currentBadge);
            }
        });

        btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentBadge) {
                    case PROGRAM_BADGE:
                        currentBadge = EBadgeList.ACCOUNTABLE_BADGE;
                        break;
                    case CUS_BY_TERM_BADGE:
                        currentBadge = EBadgeList.PROGRAM_BADGE;
                        break;
                    case TERM_PIE_BADGE:
                        currentBadge = EBadgeList.CUS_BY_TERM_BADGE;
                        break;
                    case ACCOUNTABLE_BADGE:
                        currentBadge = EBadgeList.TERM_PIE_BADGE;
                        break;
                }
                setActiveBadge(currentBadge);
            }
        });


        btSaveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Let's pester the user for Storage permission
                if (ContextCompat.checkSelfPermission(ActBadge.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    NOMApp.reqHelp.nagAboutStorage(ActBadge.this);

                } else {

                    etFilename.setText(etFilename.getText().toString().replaceAll("[^\\w\\-]", "_"));
                    saveProgressBadge();
                }
            }
        });

        currentBadge = EBadgeList.PROGRAM_BADGE;
        setActiveBadge(currentBadge);
        
    }

    
    public void setActiveBadge(EBadgeList badge) {
        switch (badge) {
            case PROGRAM_BADGE:
                pbv = new ProgramBadge(this, null);
                badgeTitle = ((IBadge)pbv).getBadgeTitle();
                tvBadgeName.setText(badgeTitle);
                
                etFilename.setText(badgeTitle+"-"+dateSuf);
                
                flBadgeFrame.removeAllViews();
                flBadgeFrame.addView(pbv);
                break;
            case CUS_BY_TERM_BADGE:
                pbv = new CUsByTermBadge(this, null);
                badgeTitle = ((IBadge)pbv).getBadgeTitle();
                tvBadgeName.setText(badgeTitle);

                etFilename.setText(badgeTitle+"-"+dateSuf);

                flBadgeFrame.removeAllViews();
                flBadgeFrame.addView(pbv);
                break;
            case TERM_PIE_BADGE:
                pbv = new TermPieBadge(this, null);
                badgeTitle = ((IBadge)pbv).getBadgeTitle();
                tvBadgeName.setText(badgeTitle);

                etFilename.setText(badgeTitle+"-"+dateSuf);

                flBadgeFrame.removeAllViews();
                flBadgeFrame.addView(pbv);
                break;
            case ACCOUNTABLE_BADGE:
                pbv = new AccountableBadge(this, null);
                badgeTitle = ((IBadge)pbv).getBadgeTitle();
                tvBadgeName.setText(badgeTitle);

                etFilename.setText(badgeTitle+"-"+dateSuf);

                flBadgeFrame.removeAllViews();
                flBadgeFrame.addView(pbv);
                break;
        }
        
    }
    

    public boolean saveProgressBadge() {
        final String fileName;

        try {
            fileName = etFilename.getText().toString()+".png";
            
            // Will write to global external directory location
            File myDirectory = new File(prefs.myExternalDir);

            // If myDirectory doesn't exist, create it
            if (! myDirectory.exists()) {
                myDirectory.mkdirs();
            }

            File pb = new File(myDirectory+"/"+fileName);
          
            pbv.setDrawingCacheEnabled(true);
            Bitmap b = pbv.getDrawingCache();
            b.compress(Bitmap.CompressFormat.PNG, 95, new FileOutputStream(pb));
            pbv.destroyDrawingCache();

            final Uri result = Uri.fromFile(pb);
            String[] myPaths = {result.getPath()};
            String[] myMimes = {"image/png"};

            MediaScannerConnection.scanFile(
                    this,
                    myPaths,
                    myMimes,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            if (uri != null) {
                                Log.i("MEDIA", "Scanned: " + uri);
                            }
                        }
                    }
            );


            new MaterialDialog.Builder(this)
                    .title("Progress Badge Saved")
                    .content("The badge was saved to your \"My Saved Files\" section.\n\n" +
                            "Would you like to share it via email or social networks?")
                    .positiveText("Share")
                    .negativeText("No Thanks")
                    .positiveColorRes(R.color.wguPrimary)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, result);
                            shareIntent.setType("image/png");
                            startActivity(Intent.createChooser(shareIntent, "Share Progress Badge with:"));
                        }
                    })
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    private void askShareFile(String fileName, final Uri uri) {
        new MaterialDialog.Builder(this)
                .title("Progress Badge Saved")
                .content("The badge was saved to your \"My Saved Files\" section.\n\n" +
                        "Would you like to share it via email or social networks?")
                .positiveText("Share")
                .negativeText("No Thanks")
                .positiveColorRes(R.color.wguPrimary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.setType("image/png");
                        startActivity(Intent.createChooser(shareIntent, "Share Progress Badge with:"));
                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        finish();
                    }
                })
                .show();
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
