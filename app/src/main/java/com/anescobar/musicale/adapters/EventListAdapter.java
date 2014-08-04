package com.anescobar.musicale.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;

import java.util.ArrayList;

/**
 * Created by Andres Escobar on 8/3/14.
 */
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<String> mDataset;
    private static Context mContext;

    // Adapter's Constructor
    public EventListAdapter(Context context, ArrayList<String> myDataset) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public EventListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
        holder.mExampleTextView.setOnClickListener(EventListAdapter.this);

        holder.mExampleTextView.setTag(holder);

        return holder;
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Get element from your dataset at this position and set the text for the specified element
        holder.mExampleTextView.setText(mDataset.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Implement OnClick listener. The clicked item text is displayed in a Toast message.
    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (view.getId() == holder.mExampleTextView.getId()) {
            Toast.makeText(mContext, holder.mExampleTextView.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mExampleTextView;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public ViewHolder(View v) {
            super(v);

            mExampleTextView = (TextView) v.findViewById(R.id.eventCard_textView_eventTitle);
        }
    }
}
