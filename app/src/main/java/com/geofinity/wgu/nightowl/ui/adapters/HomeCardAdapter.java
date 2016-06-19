package com.geofinity.wgu.nightowl.ui.adapters;

import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geofinity.pwnet.events.EHCActions;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.HCCommMail;
import com.geofinity.wgu.nightowl.model.HCMentor;
import com.geofinity.wgu.nightowl.model.HCNavigate;
import com.geofinity.wgu.nightowl.model.HCProgram;
import com.geofinity.wgu.nightowl.model.HCUpdated;
import com.geofinity.wgu.nightowl.model.IHomeCard;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.joanzapata.iconify.widget.IconButton;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 12/11/14.
 */
public class HomeCardAdapter extends RecyclerView.Adapter {

    public static final int HC_PROGRAM = 0;
    public static final int HC_COMM_MAIL = 1;
    public static final int HC_NAVIGATE = 2;
    public static final int HC_MENTOR = 3;
    public static final int HC_UPDATED = 4;

    public static PocketPreferences prefs = NOMApp.prefs;
    public static EventBus eBus = EventBus.getDefault();

    public ArrayList<IHomeCard> mDataSet;

    public HomeCardAdapter () {
        mDataSet = new ArrayList<IHomeCard>();
        mDataSet.add(new HCProgram());
        mDataSet.add(new HCCommMail());
        mDataSet.add(new HCNavigate());
        mDataSet.add(new HCMentor());
        mDataSet.add(new HCUpdated());
    }

    public void addCard(IHomeCard card) {
        int pos = mDataSet.size();
        mDataSet.add(card);
        notifyItemInserted(pos);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View v = null;

        switch (viewType) {
            case HC_PROGRAM:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_program, parent, false);
                holder = new VHProgramCard(v);
                break;
            case HC_COMM_MAIL:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_mail_community, parent, false);
                holder = new VHCommMailCard(v);
                break;
            case HC_NAVIGATE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_navigate, parent, false);
                holder = new VHNavigateCard(v);
                break;
            case HC_MENTOR:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_mentor, parent, false);
                holder = new VHMentorCard(v);
                break;
            case HC_UPDATED:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_updated, parent, false);
                holder = new VHUpdatedCard(v);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (mDataSet.get(position).getType()) {
            case HC_PROGRAM:
                onBindVHProgramCard( (VHProgramCard) holder, position);
                break;
            case HC_COMM_MAIL:
                onBindVHCommMailCard( (VHCommMailCard) holder, position);
                break;
            case HC_NAVIGATE:
                onBindVHNavigateCard( (VHNavigateCard) holder, position);
                break;
            case HC_MENTOR:
                onBindVHMentorCard( (VHMentorCard) holder, position);
                break;
            case HC_UPDATED:
                onBindVHUpdatedCard( (VHUpdatedCard) holder, position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void onBindVHProgramCard(final VHProgramCard holder, int position) {
        holder.tvProgTitle.setText( prefs.getProgTitle() );
        holder.tvGraduationDate.setText( prefs.getGradDate() );
        holder.tvCurrentTerm.setText(""+prefs.getCurrentTerm());

        // Term Progress
        int tp = prefs.getTermEarned();
        holder.pbTermProgBar.setMax(prefs.getTermTotal());
        holder.tvTermProgString.setText(prefs.getTermProgress());
        holder.tvCTermEndDate.setText(prefs.getCurrentTermEndFF());

        ValueAnimator termP = ValueAnimator.ofInt(0, tp);
        termP.setDuration(500);
        termP.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                holder.pbTermProgBar.setProgress((Integer) animation.getAnimatedValue());
            }
        });
        termP.start();

        // Degree Progress
        int dp = prefs.getDegEarned();
        holder.pbDegProgBar.setMax(prefs.getDegTotal());
        holder.pbDegProgBar.setProgress(prefs.getDegEarned());
        holder.tvDegProgString.setText(prefs.getDegProgress());
        
        ValueAnimator degP = ValueAnimator.ofInt(0, dp);
        degP.setDuration(500);
        degP.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                holder.pbDegProgBar.setProgress((Integer) animation.getAnimatedValue());
            }
        });
        degP.start();
    }

    public void onBindVHCommMailCard(final VHCommMailCard holder, int position) {

        String ccn = prefs.getCurrentCommName();
        String ccs = prefs.getCommSearchCurrent();
        if (ccs.equals("")) {
            holder.tvCommunityName.setText(ccn);
        } else {
            holder.tvCommunityName.setText(String.format("Search for \"%s\" in %s", ccs, ccn));
        }

        int cpt = prefs.getCommPostsToday();
        if (cpt > 24) {
            holder.itvPostsToday.setText(cpt + "+ {fa-comments}");
        } else {
            holder.itvPostsToday.setText(cpt + " {fa-comments}");
        }

        // Unread email maxes out at 20
        int uec = prefs.getUnreadEmailCount();
        if (uec > 19) {
            holder.itvUnreadEmail.setText("{fa-envelope} "+uec+"+");
        } else {
            holder.itvUnreadEmail.setText("{fa-envelope} "+uec);
        }

//        // Animate Color Change
//        int fromColor = NOMApp.ac.getResources().getColor(R.color.wguGold);
//        int toColor = NOMApp.ac.getResources().getColor(R.color.wguPrimary);
//
//        final ValueAnimator atc = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
//        atc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                holder.itvPostsToday.setTextColor((Integer)animation.getAnimatedValue());
//                holder.itvUnreadEmail.setTextColor((Integer)animation.getAnimatedValue());
//            }
//        });
//        atc.setDuration(1000);
//        atc.start();

    }

    public void onBindVHNavigateCard(VHNavigateCard card, int position) {
        // NO-OP for Consistency / Future use
    }

    public void onBindVHMentorCard(VHMentorCard card, int position) {
        // NO-OP for Consistency / Future use
        card.tvMentorName.setText("My Mentor:\n"+prefs.getMentorName());
    }

    public void onBindVHUpdatedCard(VHUpdatedCard card, int position) {
        card.tvLastRefreshed.setText(
                "Last Update: "+NOMApp.sdfDisplay.format(new Date(prefs.getLastCoreData()))
        );
    }

    /**
     * ViewHolder for Program Card
     */
    public static class VHProgramCard extends RecyclerView.ViewHolder {

        private final TextView tvProgTitle;
        private final TextView tvGraduationDate;
        private final TextView tvTermProgString;
        private final TextView tvDegProgString;
        private final TextView tvCTermEndDate;
        private final ProgressBar pbTermProgBar;
        private final ProgressBar pbDegProgBar;
        private final IconTextView tvCurrentTerm;
        private final IconTextView itvFinishFlag;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public VHProgramCard(View v) {
            super(v);

            tvProgTitle = (TextView) v.findViewById(R.id.tvProgTitle);
            tvGraduationDate = (TextView) v.findViewById(R.id.tvGraduationDate);
            tvTermProgString = (TextView) v.findViewById(R.id.tvTermProgString);
            tvDegProgString = (TextView) v.findViewById(R.id.tvDegProgString);
            tvCTermEndDate = (TextView) v.findViewById(R.id.tvCTermEndDate);
            pbTermProgBar = (ProgressBar) v.findViewById(R.id.pbTermProgbar);
            pbDegProgBar = (ProgressBar) v.findViewById(R.id.pbDegProgbar);
            tvCurrentTerm = (IconTextView) v.findViewById(R.id.tvCurrentTerm);
            itvFinishFlag = (IconTextView) v.findViewById(R.id.itvProgessIcon2);
            
            tvCurrentTerm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eBus.post(EHCActions.HC_GOTO_COURSES);
                }
            });
            
            itvFinishFlag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eBus.post(EHCActions.HC_GOTO_BADGE);
                }
            });
        }
    }

    /**
     * ViewHolder for CommMail Card
     */
    public static class VHCommMailCard extends RecyclerView.ViewHolder {

        private final TextView tvCommunityName;
        private final IconButton itvPostsToday;
        private final IconButton itvUnreadEmail;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public VHCommMailCard(View v) {
            super(v);

            tvCommunityName = (TextView) v.findViewById(R.id.tvCommunityName);
            itvPostsToday = (IconButton) v.findViewById(R.id.itvPostsToday);
            itvUnreadEmail = (IconButton) v.findViewById(R.id.itvUnreadEmail);

            itvPostsToday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eBus.post(EHCActions.HC_LAUNCH_COMMUNITY);
                }
            });

            itvUnreadEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eBus.post(EHCActions.HC_LAUNCH_WEBMAIL);
                }
            });

        }
    }

    /**
     * ViewHolder for Navigate Card
     */
    public static class VHNavigateCard extends RecyclerView.ViewHolder {

        private final IconButton itvResources;
        private final IconButton itvSocial;
        private final IconButton itvVideos;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public VHNavigateCard(View v) {
            super(v);

            itvResources = (IconButton) v.findViewById(R.id.itvResources);
            itvResources.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Post to Activity
                    eBus.post(EHCActions.HC_LAUNCH_RESOURCES);
                }
            });

            itvSocial = (IconButton) v.findViewById(R.id.itvSocial);
            itvSocial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Post to Activity
                    eBus.post(EHCActions.HC_LAUNCH_SOCIAL);
                }
            });

            itvVideos = (IconButton) v.findViewById(R.id.itvVideos);
            itvVideos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Post to Activity
                    eBus.post(EHCActions.HC_LAUNCH_VIDEOLIST);
                }
            });

        }
    }


    /**
     * ViewHolder for Mentor Card
     */
    public static class VHMentorCard extends RecyclerView.ViewHolder {

        private final TextView tvMentorName;
        private final IconButton itvMentorCall;
        private final IconButton itvMentorEmail;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public VHMentorCard(View v) {
            super(v);

            tvMentorName = (TextView) v.findViewById(R.id.tvMentorName);
            itvMentorCall = (IconButton) v.findViewById(R.id.itvMentorPhone);
            itvMentorCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Post to Activity
                    eBus.post(EHCActions.HC_MENTOR_INFO);
                }
            });
            itvMentorEmail = (IconButton) v.findViewById(R.id.itvMentorEmail);
            itvMentorEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Post to Activity
                    eBus.post(EHCActions.HC_EMAIL_MENTOR);
                }
            });
        }
    }


    /**
     * ViewHolder for Mentor Card
     */
    public static class VHUpdatedCard extends RecyclerView.ViewHolder {

        private final TextView tvLastRefreshed;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public VHUpdatedCard(View v) {
            super(v);
            tvLastRefreshed = (TextView) v.findViewById(R.id.tvLastRefresh);

        }
    }


}
