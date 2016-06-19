package com.geofinity.wgu.nightowl.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.geofinity.pwnet.models.PanoVideo;
import com.geofinity.pwnet.netops.ReqPanopto;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.ui.ActVideoPlayer;
import com.geofinity.wgu.nightowl.ui.util.PaletteTransformation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joanzapata.iconify.widget.IconButton;
import com.joanzapata.iconify.widget.IconTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class VideosAdapter
        extends RecyclerView.Adapter<VideosAdapter.VideoCellVH> {

    private static ArrayList<PanoVideo> myDataset;
    private static Context sContext;
    private SimpleDateFormat sdf;

    private Type vidListType;
    private int defFrameColor;


    // Adapter's Constructor
    public VideosAdapter(Context context, ArrayList<PanoVideo> myDataset) {
        this.myDataset = myDataset;
        sContext = context;
        sdf = NOMApp.sdfDisplay;
        vidListType = new TypeToken<List<PanoVideo>>(){}.getType();
        defFrameColor = context.getResources().getColor(R.color.wguPrimaryDark);

    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public VideoCellVH onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_video, parent, false);

        // Set the view to the ViewHolder
        VideoCellVH holder = new VideoCellVH(v);
        holder.itemView.setTag(holder);

        holder.ivThumbnail.setTag(holder);
        holder.ibFolder.setTag(holder);
        holder.ibDownload.setTag(holder);

        holder.ivThumbnail.setOnClickListener(holder);
        holder.ibFolder.setOnClickListener(holder);
        holder.ibDownload.setOnClickListener(holder);

        return holder;
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(VideoCellVH holder, int position) {
        if (position < 0 || position > 10000) return;

        int hms = this.myDataset.get(position).getDuration().intValue();

        holder.tvVideoName.setText(Html.fromHtml(myDataset.get(position).getSessionName()));
        holder.tvDate.setText("{fa-calendar-o} "+formatStartDate(myDataset.get(position).getStartTime()));
        holder.tvFolderName.setText(Html.fromHtml(myDataset.get(position).getFolderName()));
        holder.tvDuration.setText("{fa-clock-o} "+formatToHHMMSS(hms));

        final VideoCellVH h1 = holder;
        Picasso.with(sContext)
                .load("https://wgu.hosted.panopto.com" + myDataset.get(position).getThumbUrl())
                .resize(480, 0)
                .transform(PaletteTransformation.instance())
                .into(holder.ivThumbnail, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) h1.ivThumbnail.getDrawable()).getBitmap();
                        Palette pal = PaletteTransformation.getPalette(bitmap);

                        h1.llCellBack.setBackgroundColor(pal.getDarkVibrantColor(defFrameColor));
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myDataset.size();
    }


    // Provide handle to internal dataSet
    public void refreshDisplay() {
        myDataset.clear();
        try {
            Gson gson = new Gson();
            myDataset = gson.fromJson(NOMApp.prefs.getPanoContent(), vidListType);
        } catch (Exception e) {
            Log.e("PANO_LOAD", "Puked Loading Pano from Prefs: " + e.getMessage());
        }
        this.notifyDataSetChanged();
    }


    /**
     * The ViewHolder for Video Cells
     */
    public class VideoCellVH extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public ImageView ivThumbnail;
        public TextView tvVideoName;
        public IconTextView tvDate;
        public IconTextView tvDuration;
        public TextView tvFolderName;
        public FrameLayout llCellBack;

        public IconButton ibFolder;
        public IconButton ibDownload;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public VideoCellVH(View v) {
            super(v);

            llCellBack = (FrameLayout) v.findViewById(R.id.llCellBack);
            ivThumbnail = (ImageView) v.findViewById(R.id.ivThumbnail);
            tvVideoName = (TextView) v.findViewById(R.id.tvVideoName);
            tvFolderName = (TextView) v.findViewById(R.id.tvFolderName);
            tvDate = (IconTextView) v.findViewById(R.id.tvDate);
            tvDuration = (IconTextView) v.findViewById(R.id.tvDuration);

            ibFolder = (IconButton) v.findViewById(R.id.ibFolder);
            ibDownload = (IconButton) v.findViewById(R.id.ibDownload);
        }

        @Override
        public void onClick(View view) {
            VideoCellVH holder = (VideoCellVH) view.getTag();
            int position = holder.getPosition();
            int vId = view.getId();

            // This trick from:  http://stackoverflow.com/a/26940812/2259418
            if (vId == holder.ivThumbnail.getId()) {
                Intent cvi = new Intent(sContext, ActVideoPlayer.class);
                String delID = myDataset.get(position).getDeliveryID();
                cvi.putExtra("DELIVERY_ID", delID);

                ColorDrawable buttonColor = (ColorDrawable) holder.llCellBack.getBackground();
                int colorId = sContext.getResources().getColor(R.color.wguPrimary);
                if (buttonColor != null) {
                    colorId = buttonColor.getColor();
                }
                cvi.putExtra("BACKGROUND_COLOR", colorId);

                sContext.startActivity(cvi);
            } else if (vId == holder.ibFolder.getId()) {
                // Click was on the Folder
                String folderID = myDataset.get(position).getFolderID();
                String folderName = Html
                        .fromHtml(myDataset.get(position).getFolderName())
                        .toString();

                NOMApp.prefs.setPanoptoFolderId(folderID);
                NOMApp.prefs.setPanoptoFolderName(folderName);

                NOMApp.opEx.add(new ReqPanopto(null, folderID, 24));
            } else if (vId == holder.ibDownload.getId()) {

                Log.e("VID_DOWN", "Want to download");
                EventBus.getDefault().post(myDataset.get(position));

            }
        }

    }


    public String formatStartDate (String startDate) {
        if (startDate.contains("Date(")) {
            String ds = startDate.split("Date\\(")[1].split("\\)")[0];
            return NOMApp.sdfShort.format(Long.parseLong(ds));
            // return ds;
        } else {
            return startDate;
        }
    }

    public String formatToHHMMSS(int secsIn) {

        int mins = secsIn / 60;
        int secs = secsIn % 60;

        return String.format("%dmin %dsec", mins, secs);
    }
}