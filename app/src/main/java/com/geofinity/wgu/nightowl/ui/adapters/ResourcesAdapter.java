package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.ResourceItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joanzapata.iconify.widget.IconTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidbleicher on 9/14/14.
 */
public class ResourcesAdapter extends BaseAdapter {

    public ArrayList<ResourceItem> dataSet;
    private LayoutInflater inflater;

    public ResourcesAdapter(Context context) {
        this.dataSet = populateResources(context);
        notifyDataSetChanged();
        this.inflater = LayoutInflater.from(context);
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

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cell_resource_item, null);
            holder = new ViewHolder();
            holder.icon = (IconTextView) convertView.findViewById(R.id.itvBullet);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setText(dataSet.get(position).icon);
        // holder.icon.setTextColor(Color.parseColor(dataSet.get(position).color));
        holder.tvTitle.setText(dataSet.get(position).title);

        return convertView;
    }

    static class ViewHolder {
        IconTextView icon;
        TextView tvTitle;
    }

    private ArrayList<ResourceItem> populateResources(Context context) {
        String fileName = "resource_items.json";
        Type resourceListType = new TypeToken<List<ResourceItem>>(){}.getType();
        BufferedReader in = null;
        String jsonString = null;

        ArrayList<ResourceItem> items = new ArrayList<ResourceItem>();

        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(fileName);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ( (str = in.readLine()) != null ) {
                buf.append(str);
            }
            jsonString = buf.toString();

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("SOCIAL", "Error closing asset " + fileName);
                }
            }

            Gson gson = new Gson();
            return gson.fromJson(jsonString, resourceListType);
        } catch (IOException e) {
            Log.e("RESOURCES", "Error opening asset " + fileName);
        }

        return null;
    }

}
