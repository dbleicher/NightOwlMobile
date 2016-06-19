package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.SocialItem;
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

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 12/16/14.
 */
public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.SocialVH> {

    private ArrayList<SocialItem> myDataset;
    EventBus eventBus = EventBus.getDefault();

    public SocialAdapter () {

        myDataset = new ArrayList<SocialItem>();
        populateSocialList(NOMApp.ac);
    }


    @Override
    public SocialVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_social_item, parent, false);
        SocialVH holder = new SocialVH(v);
        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(SocialVH holder, int position) {
        SocialItem item = myDataset.get(position);

        holder.itvIcon.setText(item.icon);
        holder.tvTitle.setText(item.title);
        holder.itvIcon.setTextColor(Color.parseColor(myDataset.get(position).iconColor));

    }

    @Override
    public int getItemCount() {
        return myDataset.size();
    }


    public class SocialVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout llCardBack;
        IconTextView itvIcon;
        TextView tvTitle;

        public SocialVH(View itemView) {
            super(itemView);
            llCardBack = (LinearLayout) itemView.findViewById(R.id.llCardBack);
            itvIcon = (IconTextView) itemView.findViewById(R.id.itvIcon);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        }

        @Override
        public void onClick(View v) {
            SocialVH holder = (SocialVH) v.getTag();
            int position = holder.getPosition();
            eventBus.post(myDataset.get(position));
        }
    }

    private void populateSocialList(Context context) {
        String fileName = "social_items.json";
        BufferedReader in = null;
        String jsonString = null;
        Type socialListType = new TypeToken<List<SocialItem>>(){}.getType();

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
            ArrayList<SocialItem> tmpData = gson.fromJson(jsonString, socialListType);
            int sfp = NOMApp.prefs.getSocialFilterNum();
            if (sfp == 0) {
                myDataset = tmpData;
                return;
            } else {
                for (SocialItem si : tmpData) {

                    if (si.stateCode == sfp || si.stateCode == 100) {
                        myDataset.add(si);
                    }
                }
            }
        } catch (IOException e) {
            Log.e("SOCIAL", "Error opening asset " + fileName);
        }

    }

    public void refreshData() {
        myDataset.clear();
        populateSocialList(NOMApp.ac);
        notifyDataSetChanged();
    }


}
