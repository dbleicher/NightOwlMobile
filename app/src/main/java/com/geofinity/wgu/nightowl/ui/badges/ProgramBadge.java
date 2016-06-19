package com.geofinity.wgu.nightowl.ui.badges;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.joanzapata.iconify.widget.IconTextView;

/**
 * Created by davidbleicher on 2/18/15.
 */
public class ProgramBadge extends LinearLayout implements IBadge {
    
    PocketPreferences prefs;
    
    private static final String badgeTitle = "Program Badge";
    
    private TextView tvProgTitle;
    private TextView tvGraduationDate;
    private TextView tvTermProgString;
    private TextView tvDegProgString;
    private TextView tvCTermEndDate;
    private ProgressBar pbTermProgBar;
    private ProgressBar pbDegProgBar;
    private IconTextView tvCurrentTerm;

    public ProgramBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = NOMApp.prefs;
        init();
    }
    
    private void init() {
        inflate(getContext(), R.layout.card_program, this);
        
        tvProgTitle      = (TextView)findViewById(R.id.tvProgTitle);
        tvGraduationDate = (TextView) findViewById(R.id.tvGraduationDate);
        tvTermProgString = (TextView) findViewById(R.id.tvTermProgString);
        tvDegProgString  = (TextView) findViewById(R.id.tvDegProgString);
        pbTermProgBar    = (ProgressBar) findViewById(R.id.pbTermProgbar);
        pbDegProgBar     = (ProgressBar) findViewById(R.id.pbDegProgbar);
        tvCurrentTerm    = (IconTextView) findViewById(R.id.tvCurrentTerm);
        tvCTermEndDate   = (TextView) findViewById(R.id.tvCTermEndDate);
     
        populateMe();
    }


    public void populateMe() {

        tvProgTitle.setText( prefs.getProgTitle() );
        tvGraduationDate.setText( prefs.getGradDate() );
        tvCurrentTerm.setText(""+prefs.getCurrentTerm());

        // Term Progress
        int tp = prefs.getTermEarned();
        pbTermProgBar.setMax(prefs.getTermTotal());
        pbTermProgBar.setProgress(tp);
        tvTermProgString.setText(prefs.getTermProgress());
        tvCTermEndDate.setText(prefs.getCurrentTermEndFF());

        // Degree Progress
        int dp = prefs.getDegEarned();
        pbDegProgBar.setMax(prefs.getDegTotal());
        pbDegProgBar.setProgress(dp);
        tvDegProgString.setText(prefs.getDegProgress());
    }
    
    public String getBadgeTitle() {
        return badgeTitle;
    }
}
