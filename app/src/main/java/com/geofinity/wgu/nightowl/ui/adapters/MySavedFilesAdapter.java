package com.geofinity.wgu.nightowl.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geofinity.pwnet.events.EMSFActions;
import com.geofinity.pwnet.events.MySavedFilesEvent;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.joanzapata.iconify.widget.IconButton;
import com.joanzapata.iconify.widget.IconTextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 12/31/14.
 */
public class MySavedFilesAdapter extends RecyclerView.Adapter<MySavedFilesAdapter.VHDownFile> {

    public ArrayList<File> mDataset;

    public MySavedFilesAdapter() {
        File med = new File(NOMApp.prefs.myExternalDir);
        if (!med.exists()) {
            med.mkdir();
        }
        mDataset = new ArrayList<File>(Arrays.asList(med.listFiles()));
        Collections.sort(mDataset);
    }


    @Override
    public VHDownFile onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_mysavedfile, parent, false);
        VHDownFile holder = new VHDownFile(v);

        holder.ibFileShare.setTag(holder);
        holder.ibFilePlay.setTag(holder);
        holder.ibFileDelete.setTag(holder);

        holder.ibFileShare.setOnClickListener(holder);
        holder.ibFilePlay.setOnClickListener(holder);
        holder.ibFileDelete.setOnClickListener(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(VHDownFile holder, int position) {

        holder.tvFileDate.setText(formatFileDate(mDataset.get(position)));
        holder.tvFileSize.setText(formatFileSize(mDataset.get(position)));

        holder.tvFilename.setText(mDataset.get(position).getName());
        holder.itvFileType.setText(chooseIcon(mDataset.get(position)));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public static class VHDownFile extends RecyclerView.ViewHolder implements View.OnClickListener {
        IconTextView itvFileType;
        IconButton ibFileShare;
        IconButton ibFilePlay;
        IconButton ibFileDelete;
        TextView tvFilename;
        TextView tvFileDate;
        TextView tvFileSize;

        public VHDownFile(View itemView) {
            super(itemView);
            itvFileType = (IconTextView) itemView.findViewById(R.id.itvFileType);
            ibFileShare = (IconButton) itemView.findViewById(R.id.ibFileShare);
            ibFilePlay = (IconButton) itemView.findViewById(R.id.ibFileView);
            ibFileDelete = (IconButton) itemView.findViewById(R.id.ibFileDelete);
            tvFilename = (TextView) itemView.findViewById(R.id.tvFileName);
            tvFileDate = (TextView) itemView.findViewById(R.id.tvFileDate);
            tvFileSize = (TextView) itemView.findViewById(R.id.tvFileSize);
        }

        @Override
        public void onClick(View v) {
            VHDownFile holder = (VHDownFile) v.getTag();
            int position = holder.getPosition();
            int vId = v.getId();

            if (vId == holder.ibFileShare.getId()) {
                EventBus.getDefault().post(new MySavedFilesEvent(EMSFActions.FILE_SHARE, position));
                return;
            }

            if (vId == holder.ibFilePlay.getId()) {
                EventBus.getDefault().post(new MySavedFilesEvent(EMSFActions.FILE_PLAY, position));
                return;
            }

            if (vId == holder.ibFileDelete.getId()) {
                EventBus.getDefault().post(new MySavedFilesEvent(EMSFActions.FILE_DELETE, position));
                return;
            }
        }
    }

    private String chooseIcon(File theFile) {
        String fName = theFile.getName();
        String myIcon = "{fa-file-o}";

        if (fName.toLowerCase().endsWith(".pdf")) return "{fa-file-pdf-o}";
        if (fName.toLowerCase().endsWith(".mp4")) return "{fa-film}";
        if (fName.toLowerCase().endsWith(".png")) return "{fa-image}";

        return myIcon;
    }

    private String formatFileDate(File theFile) {
        return NOMApp.sdfDisplay.format(theFile.lastModified());
    }

    private String formatFileSize(File theFile) {
        long fSize = theFile.length();

        if (fSize > 1000000) {
            return String.format("Size: %sMB", new DecimalFormat("#,###").format((fSize / 1000000.0)));
        } else if (fSize > 1000) {
            return String.format("Size: %sKB", new DecimalFormat("#,###").format((fSize / 1000.0)));
        } else {
            return String.format("Size: %dB", fSize);
        }
    }
}
