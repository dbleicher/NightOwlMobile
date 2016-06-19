package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geofinity.pwnet.models.jsonmodels.cosa.JTocEntry;
import com.geofinity.wgu.nightowl.R;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;

/**
 * Created by davidbleicher on 9/14/14.
 */
public class CosTocAdapter extends BaseAdapter {

    private ArrayList<JTocEntry> dataSet;
    private LayoutInflater inflater;
    private boolean isSeletedRow;
    private int wguPrimary;
    private int wguBackGrey;
    private int wguTextColor;
    private int wguHeaderColor;
    private int wguWhite;

    public CosTocAdapter(Context context, ArrayList<JTocEntry> dataSet) {
        this.dataSet = dataSet;
        this.inflater = LayoutInflater.from(context);
        wguBackGrey = context.getResources().getColor(R.color.wguLightGrey);
        wguPrimary = context.getResources().getColor(R.color.wguPrimary);
        wguWhite = context.getResources().getColor(R.color.wguWhite);
        wguTextColor = context.getResources().getColor(R.color.wguPrimaryText);
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cell_diag_listitem, null);
            holder = new ViewHolder();
            holder.icon = (IconTextView) convertView.findViewById(R.id.itvBullet);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.llCellBack = (LinearLayout) convertView.findViewById(R.id.llCellBack);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Highlight the selected row
        isSeletedRow = dataSet.get(position).isBookmark;

        holder.icon.setTextColor(wguPrimary);
        if (isSeletedRow) {
            holder.icon.setText("{fa-arrow-circle-right}");
        } else {
            holder.icon.setText("{fa-circle-thin}");
        }

        // Highlight Header Rows
        LinearLayout.LayoutParams parms = (LinearLayout.LayoutParams) holder.icon.getLayoutParams();
        if (dataSet.get(position).rKind.contentEquals("topic")) {
            // parms.setMargins(96, 32, 32, 32);
            parms.leftMargin = 96;
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.tvTitle.setTextColor(wguTextColor);
            // holder.llCellBack.setBackgroundColor(wguWhite);
        } else {
            // parms.setMargins(32, 32, 32, 32);
            parms.leftMargin = 32;
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.tvTitle.setTextColor(wguPrimary);
            // holder.llCellBack.setBackgroundColor(wguBackGrey);
        }

        holder.tvTitle.setText(dataSet.get(position).title);

        return convertView;
    }

    static class ViewHolder {
        IconTextView icon;
        TextView tvTitle;
        LinearLayout llCellBack;
    }
}
