package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geofinity.wgu.nightowl.R;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;

/**
 * Created by davidbleicher on 8/30/14.
 */
public class NavDrawerAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<NavDrawerItem> myNavItems = new ArrayList<NavDrawerItem>();

    public NavDrawerAdapter(Context context, String longVer) {
        this.context = context;

//        myNavItems.add(new NavDrawerItem("Refresh My Data", "Refresh WGU data now", "{fa-refresh}"));
//        myNavItems.add(new NavDrawerItem("AutoSync Settings", "Enable automatic refresh", "{fa-sliders}"));

        myNavItems.add(new NavDrawerItem("WGU WebMail",     "", "{fa-envelope}"));      // 0
        myNavItems.add(new NavDrawerItem("Progress Badge",  "", "{fa-tachometer}"));    // 1
        myNavItems.add(new NavDrawerItem("Change My Login", "", "{fa-lock}"));          // 2
        myNavItems.add(new NavDrawerItem("My Saved Files",  "", "{fa-files-o}"));       // 3
        myNavItems.add(new NavDrawerItem("Sign Out",        "", "{fa-sign-out}"));      // 4

        myNavItems.add(new NavDrawerItem("My Mentor"));                                 // 5
        myNavItems.add(new NavDrawerItem("My Mentor Info",  "", "{fa-star-o}"));         // 6
        myNavItems.add(new NavDrawerItem("Email My Mentor", "", "{fa-envelope-o}"));    // 7

        myNavItems.add(new NavDrawerItem("PocketWGU Version: " + longVer));             // 8
        myNavItems.add(new NavDrawerItem("About PocketWGU", "", "{fa-info-circle}"));   // 9
        myNavItems.add(new NavDrawerItem("Send Us Feedback", "", "{fa-thumbs-o-up}"));  // 10
        notifyDataSetChanged();

    }

    @Override
    public int getViewTypeCount() {
        return(2);
    }

    @Override
    public int getItemViewType(int position) {
        if (myNavItems.get(position).isHeader) {
            return 0;
        }
        return(1);
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
        if (getItemViewType(position) == 0) {
            return(getHeaderView(position, convertView, parent));
        }


        // Declare Variables
        TextView txtTitle;
        TextView txtSubTitle;
        IconTextView imgIcon;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.cell_navdrawer_item, parent, false);

        // Locate the TextViews in cell_navdrawer_item.xmlxml
        txtTitle = (TextView) itemView.findViewById(R.id.title);
        // txtSubTitle = (TextView) itemView.findViewById(R.id.subtitle);

        // Locate the ImageView in cell_navdrawer_item.xmlxml
        imgIcon = (IconTextView) itemView.findViewById(R.id.icon);

        // Set the results into TextViews
        txtTitle.setText(myNavItems.get(position).getMyTitle());
        // txtSubTitle.setText(myNavItems.get(position).getMySubTitle());

        // Set the results into ImageView
        imgIcon.setText(myNavItems.get(position).getMyFAIcon());

        return itemView;
    }

    private View getHeaderView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.cell_navdrawer_header, parent, false);
        }

        TextView label = (TextView) row.findViewById(R.id.headerTitle);
        label.setText(myNavItems.get(position).getMyTitle());

        return (row);
    }


    private class NavDrawerItem {
        private String myTitle;
        private String mySubTitle;
        private String myFAIcon;
        private boolean isHeader;

        public NavDrawerItem(String myTitle, String mySubTitle, String myFAIcon) {
            super();
            this.myTitle = myTitle;
            this.mySubTitle = mySubTitle;
            this.myFAIcon = myFAIcon;
            this.isHeader = false;
        }

        public NavDrawerItem(String myTitle)
        {
            super();
            this.myTitle = myTitle;
            isHeader = true;
        }

        public String getMyTitle() {
            return myTitle;
        }

        public String getMySubTitle() {
            return mySubTitle;
        }

        public String getMyFAIcon() {
            return myFAIcon;
        }

        @SuppressWarnings("unused")
        public boolean isHeader() {
            return isHeader;
        }

    }

}
