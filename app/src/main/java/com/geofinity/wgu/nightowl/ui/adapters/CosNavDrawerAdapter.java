package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.ECosNavActions;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;

/**
 * Created by davidbleicher on 11/30/14.
 */
public class CosNavDrawerAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<CosNavDrawerItem> myNavItems = new ArrayList<CosNavDrawerItem>();

    public CosNavDrawerAdapter(Context context, String courseName) {
        this.context = context;

        myNavItems.add(new CosNavDrawerItem(courseName, 0));
        myNavItems.add(new CosNavDrawerItem(ECosNavActions.BACK_TO_COS));
        myNavItems.add(new CosNavDrawerItem(ECosNavActions.ASSESS_DETAILS));
        myNavItems.add(new CosNavDrawerItem(ECosNavActions.COURSE_MENTORS));
        myNavItems.add(new CosNavDrawerItem(ECosNavActions.COURSE_VIDEOS));
        notifyDataSetChanged();
    }

    public void addTaskStream(String aCode) {
        CosNavDrawerItem cni = new CosNavDrawerItem(ECosNavActions.TASKSTREAM);
        cni.myTitle = "Tasks & Rubric ("+aCode+")";
        myNavItems.add(cni);
        CosNavDrawerItem cni2 = new CosNavDrawerItem(ECosNavActions.TASKSTREAM_SCORE);
        cni2.myTitle = "Score & Queue ("+aCode+")";
        myNavItems.add(cni2);
        notifyDataSetChanged();
    }
    
    public boolean addAction(ECosNavActions action) {
        for (CosNavDrawerItem cndi : myNavItems) {
            if (action == cndi.myAction) return false;
        }
        myNavItems.add(new CosNavDrawerItem(action));
        notifyDataSetChanged();
        return true;
    }

    public boolean addHeader(String title) {
        for (CosNavDrawerItem cndi : myNavItems) {
            if (title.equals(cndi.myTitle)) return false;
        }
        myNavItems.add(new CosNavDrawerItem(title, 1));
        notifyDataSetChanged();
        return true;
    }


    @Override
    public int getViewTypeCount() {
        return(3);
    }

    @Override
    public int getItemViewType(int position) {
        return myNavItems.get(position).myType;
    }

    @Override
    public int getCount() {
        return myNavItems.size();
    }

    @Override
    public Object getItem(int position) {
        return myNavItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = null;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        switch (getItemViewType(position)) {
            case 0: // Main Title
                itemView = inflater.inflate(R.layout.cell_cosnav_title, parent, false);
                TextView tvName = (TextView) itemView.findViewById(R.id.tvCourseName);
                tvName.setText(myNavItems.get(position).myTitle);
                break;
            case 1: // Header
                itemView = inflater.inflate(R.layout.cell_cosnav_header, parent, false);
                TextView label = (TextView) itemView.findViewById(R.id.headerTitle);
                label.setText(myNavItems.get(position).myTitle);
                break;
            case 2: // Action
                itemView = inflater.inflate(R.layout.cell_navdrawer_item, parent, false);
                TextView txtTitle = (TextView) itemView.findViewById(R.id.title);
                IconTextView imgIcon = (IconTextView) itemView.findViewById(R.id.icon);
                txtTitle.setText(myNavItems.get(position).myTitle);
                imgIcon.setText(myNavItems.get(position).myFAIcon);
                break;
        }
                
        return itemView;
    }


    public class CosNavDrawerItem {
        public String myTitle;
        public String myFAIcon;
        public ECosNavActions myAction;
        public int myType;

        public CosNavDrawerItem(ECosNavActions myAction) {
            this.myAction = myAction;
            this.myType = 2;
            switch (myAction) {
                case BACK_TO_COS:
                    myTitle = "Course of Study";
                    myFAIcon = "{fa-book}";
                    break;
                case ASSESS_DETAILS:
                    myTitle = "Assessment Details";
                    myFAIcon = "{fa-crosshairs}";
                    break;
                case COURSE_MENTORS:
                    myTitle = "Course Mentors";
                    myFAIcon = "{fa-users}";
                    break;
                case COURSE_VIDEOS:
                    myTitle = "Videos for Course";
                    myFAIcon = "{fa-film}";
                    break;
                case TASKSTREAM:
                    myTitle = "TaskStream";
                    myFAIcon = "{fa-file-text-o}";
                    break;
                case TASKSTREAM_SCORE:
                    myTitle = "TaskStream Score";
                    myFAIcon = "{fa-flag-checkered}";
                    break;
                case COS_PDF:
                    myTitle = "Download the PDF";
                    myFAIcon = "{fa-file-pdf-o}";
                    break;
                case SF_CHATTER:
                    myTitle = "Chatter";
                    myFAIcon = "{fa-comment-o}";
                    break;
                case SF_NEWS:
                    myTitle = "Announcements";
                    myFAIcon = "{fa-bullhorn}";
                    break;
                case SF_TIPS:
                    myTitle = "Tips";
                    myFAIcon = "{fa-check}";
                    break;
                case SF_SEARCH:
                    myTitle = "Search";
                    myFAIcon = "{fa-search}";
                    break;
            }
        }

        public CosNavDrawerItem(String myTitle, int myType)
        {
            this.myTitle = myTitle;
            this.myType = myType;
        }

    }

}
