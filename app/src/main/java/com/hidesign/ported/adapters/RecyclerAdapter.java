package com.hidesign.ported.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private OnRecyclerAdapterListener mCallback;
    public interface OnRecyclerAdapterListener {
        void onRecyclerAdapterCreated(RecyclerAdapter adapter, ViewHolder v, int position);
    }

    public void setOnRecyclerAdapterListener(OnRecyclerAdapterListener listener) {

        try {
            mCallback = listener;
        } catch (ClassCastException e)  {
            throw new ClassCastException(this.toString() + " must implement OnRecyclerAdapterListener");
        }
    }

    private int resId;
    private int count;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View view;
        ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public RecyclerAdapter(int count, int resId) {
        this.count = count;
        this.resId = resId;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.mTextView.setText(mDataset[position]);
        if(mCallback != null) {
            mCallback.onRecyclerAdapterCreated(this, holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }
}
