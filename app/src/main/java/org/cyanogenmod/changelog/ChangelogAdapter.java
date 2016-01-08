package org.cyanogenmod.changelog;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * Copyright (c) 2016.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {
    private static String TAG = "RVAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView subject;
        private final TextView project;
        private final TextView date;

        public ViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.subject);
            project = (TextView) itemView.findViewById(R.id.project);
            date = (TextView) itemView.findViewById(R.id.last_updated);
        }

    }

    private ArrayList<Change> mDataset;

    public ChangelogAdapter(ArrayList<Change> mDataset) {
        this.mDataset = mDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChangelogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Change change = mDataset.get(position);
        holder.project.setText(
                String.format("%s", change.getProject().replace("android_", "")));
        holder.subject.setText(
                String.format("%s", change.getSubject()));
        // parse the value of the date
        try {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            Date convertedCommitDate = simpleDateFormat.parse(change.getLastUpdate());
            holder.date.setText(simpleDateFormat.format(convertedCommitDate));
        } catch (ParseException e) {
            Log.e(TAG, "", e);
        }
        // set open in browser intent
        holder.subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String review_url =
                        String.format("http://review.cyanogenmod.org/#/c/%s", mDataset.get(position).getChangeId());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(review_url));
                Log.i(TAG, String.format("Opening %s", review_url));
                v.getContext().startActivity(browserIntent);
            }
        });

    }

    /**
     * Clear all the elements of the RecyclerView
     */
    public void clear() {
        mDataset.clear();
        notifyDataSetChanged();
    }

    /**
     * Append a set of elements to the RecyclerView
     * @param list the List we want to append.
     */
    public void addAll(List<Change> list) {
        mDataset.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Returns the size of the data set. Usually invoked by LayoutManager.
     * @return the size of the data set.
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
