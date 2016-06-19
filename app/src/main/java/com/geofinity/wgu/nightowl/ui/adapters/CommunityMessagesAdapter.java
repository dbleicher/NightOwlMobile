package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.CommunityMessage;
import com.geofinity.wgu.nightowl.ui.ActCommunityMessage;
import com.geofinity.wgu.nightowl.ui.util.CircleTransform;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommunityMessagesAdapter
        extends RecyclerView.Adapter<CommunityMessagesAdapter.ViewHolder>
        implements View.OnClickListener {

    private ArrayList<CommunityMessage> mDataset;
    private static Context sContext;
    private SimpleDateFormat sdf;
    private StaggeredGridLayoutManager.LayoutParams viewLayoutParams;

    // Adapter's Constructor
    public CommunityMessagesAdapter(Context context, ArrayList<CommunityMessage> myDataset) {
        mDataset = myDataset;
        sContext = context;
        sdf = NOMApp.sdfDisplay;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public CommunityMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_comm_message, parent, false);

        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
        holder.itemView.setOnClickListener(CommunityMessagesAdapter.this);
        holder.itemView.setTag(holder);

        return holder;
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        CommunityMessage myMessage = mDataset.get(position);

        holder.tvTitle.setText(myMessage.title);
        holder.tvAuthorName.setText(myMessage.author);
        holder.tvUpdated.setText(sdf.format(myMessage.updated));
        holder.tvMessage.setText(Html.fromHtml(myMessage.summary).toString().trim());

        Picasso.with(sContext)
                .load(String.format("http://community.wgu.edu/clearspacex/people/%s/avatar",myMessage.authorEmail))
                .resize(48, 48)
                .centerCrop()
                .transform(new CircleTransform())
                .into(holder.nivAvatar);

        String rps;
        if (myMessage.replyCount == 0) {
            rps = " ... ";
        } else if (myMessage.replyCount == 1) {
            rps = "1 reply in thread";
        } else {
            rps = ""+myMessage.replyCount+" replies in thread";
        }
        holder.tvReplies.setText(rps);
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
        Intent cvi = new Intent(sContext, ActCommunityMessage.class);
        cvi.putExtra("MY_MESSAGE", jbod);
        sContext.startActivity(cvi);
    }

    // Provide handle to internal dataSet
    public void refreshDisplay() {
        mDataset.clear();
        try {
            JSONArray messList = new JSONArray(NOMApp.prefs.getCommMessages());

            for (int x = 0; x < messList.length(); x++)
            {
                mDataset.add(new CommunityMessage(messList.getJSONObject(x)));
            }

        } catch (Exception e) {
            // Don't know what to do
            e.printStackTrace();
        }
        this.notifyDataSetChanged();
    }



    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView nivAvatar;
        public TextView tvAuthorName;
        public TextView tvTitle;
        public TextView tvUpdated;
        public TextView tvMessage;
        public TextView tvReplies;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public ViewHolder(View v) {
            super(v);

            nivAvatar = (ImageView) v.findViewById(R.id.nivAvatar);
            tvAuthorName = (TextView) v.findViewById(R.id.tvAuthorName);
            tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvUpdated = (TextView) v.findViewById(R.id.tvUpdated);
            tvMessage = (TextView) v.findViewById(R.id.tvMessage);
            tvReplies = (TextView) v.findViewById(R.id.tvReplies);

        }
    }
}