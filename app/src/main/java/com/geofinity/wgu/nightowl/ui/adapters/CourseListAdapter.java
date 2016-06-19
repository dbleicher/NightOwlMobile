package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geofinity.pwnet.events.ECoursesData;
import com.geofinity.pwnet.models.jsonmodels.degreeplanV6.JCourse;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.ui.ActCoS;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joanzapata.iconify.widget.IconTextView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CourseListAdapter
        extends RecyclerView.Adapter<CourseListAdapter.ViewHolder>
        implements View.OnClickListener {

    private ArrayList<JCourse> mDataset;
    private static Context sContext;
    private StaggeredGridLayoutManager.LayoutParams viewLayoutParams;

    private PocketPreferences prefs;

    private int red;
    private int green;
    private int blue;
    private int gold;
    private int white;

    private int paleGreen;
    private int paleRed;
    private int paleBlue;
    private int paleGold;

    // Adapter's Constructor
    public CourseListAdapter(Context context) {
        sContext = context;
        prefs = NOMApp.prefs;

        mDataset = new ArrayList<JCourse>();
        processCourseList(mDataset);

        red = sContext.getResources().getColor(R.color.wguRedDark);
        green = sContext.getResources().getColor(R.color.wguGreenDark);
        blue = sContext.getResources().getColor(R.color.wguPrimary);
        gold = sContext.getResources().getColor(R.color.wguGold);
        white = sContext.getResources().getColor(R.color.wguWhite);

        paleGreen = sContext.getResources().getColor(R.color.paleGreen);
        paleRed = sContext.getResources().getColor(R.color.paleRed);
        paleBlue = sContext.getResources().getColor(R.color.palePrimary);
        paleGold = sContext.getResources().getColor(R.color.paleGold);

    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public CourseListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_course, parent, false);

        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
                holder.itemView.setOnClickListener(CourseListAdapter.this);
                holder.itemView.setTag(holder);

        return holder;
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        JCourse item = mDataset.get(position);
        holder.tvCourseTitle.setText(item.title);
        holder.tvCourseCode.setText(item.courseCode);
        holder.tvCourseStatus.setText(item.courseStatus);
        holder.tvCourseCredits.setText("Units: "+(int)item.competencyUnits);
        holder.tvCourseTerm.setText("Term: "+item.courseTerm);
        holder.tvKeyDate.setText(item.getKeyDate(true));

        int highColor = white;
        int lowColor = paleBlue;

        // Set icon and color for the Course
        switch (item.courseStatusNum) {
            case 1:
                highColor = green;
                lowColor = paleGreen;
                holder.imageItem.setText("{fa-check-circle}");
                break;
            case 2:
                highColor = red;
                lowColor = paleRed;
                holder.imageItem.setText("{fa-times-circle-o}");
                break;
            case 3:
                highColor = gold;
                lowColor = paleGold;
                holder.imageItem.setText("{fa-arrow-circle-right}");
                break;
            default:
                highColor = blue;
                lowColor = paleBlue;
                holder.imageItem.setText("{fa-circle-o}");
        }
        holder.imageItem.setTextColor(highColor);
        holder.llTitleBack.setBackgroundColor(lowColor);
        // holder.llDateBack.setBackgroundColor(lowColor);
        holder.vSeparator.setBackgroundColor(highColor);

        // holder.llStatBack.setBackgroundColor(lowColor);
        // holder.tvCourseTitle.setTextColor(highColor);
        // holder.tvKeyDate.setTextColor(highColor);
        // holder.tvCourseStatus.setTextColor(highColor);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String jbod = mDataset.get(holder.getPosition()).toString();
        // Log.e("ONE_COURSE", jbod);

        Gson gson = new Gson();
        JCourse mc = gson.fromJson(jbod, JCourse.class);
        if (mc.courseVersionId == 0) {
            EventBus.getDefault().post(ECoursesData.NO_COURSE_VERSION_ID);
            return;
        }

        Intent cvi = new Intent(sContext, ActCoS.class);
        cvi.putExtra("MY_COURSE", jbod);
        sContext.startActivity(cvi);
    }

    // Provide handle to internal dataSet
    public void refreshDisplay() {
        processCourseList(mDataset);
        notifyDataSetChanged();
    }


    /**
     * Gets the courseList from prefs, applies filtering and sorting.
     *
     * @param mDataset
     * @return
     */
    public ArrayList<JCourse> processCourseList(ArrayList<JCourse> mDataset) {
        mDataset.clear();
        ArrayList<JCourse> rawList;

        Gson gson = new Gson();
        Type listType = new TypeToken<List<JCourse>>(){}.getType();
        rawList = gson.fromJson(prefs.getCourseListing(), listType);

        int currTerm = prefs.getCurrentTerm();
        int filterNum = prefs.getCourseFilterNum();
        int sortNum = prefs.getCourseSortingNum();

        switch (filterNum) {

            // Current and Future Terms
            case 1:
                for (JCourse c : rawList) {
                    if (c.courseTerm >= currTerm) mDataset.add(c);
                }
                break;

            // Current Term Only
            case 2:
                for (JCourse c : rawList) {
                    if (c.courseTerm == currTerm) mDataset.add(c);
                }
                break;

            // Previous Terms Only
            case 3:
                for (JCourse c : rawList) {
                    if (c.courseTerm < currTerm) mDataset.add(c);
                }
                break;

            // Enrolled Courses Only
            case 4:
                for (JCourse c : rawList) {
                    if (c.courseStatus.startsWith("Enroll")) mDataset.add(c);
                }
                break;

            // Passed Courses Only
            case 5:
                for (JCourse c : rawList) {
                    if (c.courseStatus.startsWith("Passed")) mDataset.add(c);
                }
                break;

            // Not Attempted Courses Only
            case 6:
                for (JCourse c : rawList) {
                    if (c.courseStatus.startsWith("Not Attempted")) mDataset.add(c);
                }
                break;

            // Not Passed Courses Only
            case 7:
                for (JCourse c : rawList) {
                    if (c.courseStatus.startsWith("Not Pass")) mDataset.add(c);
                }
                break;

            // Any other value, no filtering at all
            default:
                for (JCourse c : rawList) {
                    mDataset.add(c);
                }
                break;
        }

        switch (sortNum) {
            case 1:
                Collections.sort(mDataset, JCourse.DATE_ASC);
                break;
            case 2:
                Collections.sort(mDataset, JCourse.DATE_DSC);
                break;
            case 3:
                Collections.sort(mDataset, JCourse.CODE_ASC);
                break;
            case 4:
                Collections.sort(mDataset, JCourse.CODE_DSC);
                break;
            case 5:
                Collections.sort(mDataset, JCourse.NAME_ASC);
                break;
            case 6:
                Collections.sort(mDataset, JCourse.NAME_DSC);
                break;
            default:
                Collections.sort(mDataset);
                break;
        }

        return mDataset;
    }


    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llCellBack;
        LinearLayout llStatBack;
        LinearLayout llDateBack;
        LinearLayout llTitleBack;

        View vSeparator;

        TextView tvCourseTitle;
        TextView tvCourseCode;
        TextView tvCourseStatus;
        TextView tvCourseCredits;
        TextView tvCourseTerm;
        TextView tvKeyDate;
        IconTextView imageItem;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public ViewHolder(View v) {
            super(v);

            llDateBack = (LinearLayout) v.findViewById(R.id.llDateBack);
            llStatBack = (LinearLayout) v.findViewById(R.id.llStatBack);
            llCellBack = (LinearLayout) v.findViewById(R.id.llCellBack);
            llTitleBack = (LinearLayout) v.findViewById(R.id.llTitleBack);

            vSeparator = (View) v.findViewById(R.id.vSeparator);

            tvCourseTitle = (TextView) v.findViewById(R.id.tvCourseTitle);
            tvCourseCode = (TextView) v.findViewById(R.id.tvCourseCode);
            tvCourseStatus = (TextView) v.findViewById(R.id.tvCourseStatus);
            tvCourseCredits = (TextView) v.findViewById(R.id.tvCourseCredits);
            tvCourseTerm = (TextView) v.findViewById(R.id.tvCourseTerm);
            tvKeyDate = (TextView) v.findViewById(R.id.tvKeyDate);
            imageItem = (IconTextView) v.findViewById(R.id.itvCourseIcon);

        }
    }
}