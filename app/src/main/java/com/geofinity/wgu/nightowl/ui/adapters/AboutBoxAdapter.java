package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geofinity.pwnet.events.CreditURL;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.AboutItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 11/30/14.
 */
public class AboutBoxAdapter extends RecyclerView.Adapter {
    private ArrayList<AboutItem> mDataset;
    private EventBus eBus = EventBus.getDefault();

    public AboutBoxAdapter(Context ctx) {
        mDataset = new ArrayList<AboutItem>();
        populateAboutList(ctx);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        View v = null;

        if (viewType == 0) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_about_pwgu, parent, false);
            holder = new VHAboutPocket(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_about_credit, parent, false);
            holder = new VHAboutCredit(v);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (mDataset.get(position).itemType == 0) {
            onBindAboutPocket( (VHAboutPocket)holder, position);
        } else {
            onBindAboutCredit( (VHAboutCredit)holder, position);
        }
    }

    private void onBindAboutPocket(final VHAboutPocket holder, final int position) {
        holder.tvAppVersion.setText("Version: "+NOMApp.getLongVer());
        holder.tvAppName.setText(mDataset.get(position).name);
        holder.tvAppCopy.setText(Html.fromHtml(mDataset.get(position).copyright));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreditURL cu = new CreditURL();
                cu.url = mDataset.get(position).url;
                eBus.post(cu);
            }
        });
    }

    private void onBindAboutCredit(VHAboutCredit holder, final int position) {
        holder.tvCreditName.setText(mDataset.get(position).name);
        holder.tvCreditCopy.setText(Html.fromHtml(mDataset.get(position).copyright));
        holder.tvCreditLicense.setText(mDataset.get(position).license);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreditURL cu = new CreditURL();
                cu.url = mDataset.get(position).url;
                eBus.post(cu);
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).itemType;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void populateAboutList(Context context) {
        String fileName = "about_credits.json";
        BufferedReader in = null;
        String jsonString = null;
        Type aboutListType = new TypeToken<List<AboutItem>>(){}.getType();

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
            mDataset = gson.fromJson(jsonString, aboutListType);

        } catch (IOException e) {
            Log.e("SOCIAL", "Error opening asset " + fileName);
        }

    }






    private static class VHAboutPocket extends RecyclerView.ViewHolder {
        TextView tvAppName;
        TextView tvAppVersion;
        TextView tvAppCopy;

        public VHAboutPocket(View itemView) {
            super(itemView);
            tvAppName = (TextView) itemView.findViewById(R.id.tvAppName);
            tvAppVersion = (TextView) itemView.findViewById(R.id.tvAppVersion);
            tvAppCopy = (TextView) itemView.findViewById(R.id.tvAppCopy);
        }
    }

    private static class VHAboutCredit extends RecyclerView.ViewHolder {
        TextView tvCreditName;
        TextView tvCreditCopy;
        TextView tvCreditLicense;

        public VHAboutCredit(View itemView) {
            super(itemView);
            tvCreditName = (TextView) itemView.findViewById(R.id.tvCreditName);
            tvCreditCopy = (TextView) itemView.findViewById(R.id.tvCreditCopy);
            tvCreditLicense = (TextView) itemView.findViewById(R.id.tvCreditLicense);
        }
    }
}
