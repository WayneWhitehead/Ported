package com.hidesign.ported.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(private val count: Int, private val resId: Int) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var mCallback: OnRecyclerAdapterListener? = null

    interface OnRecyclerAdapterListener {
        fun onRecyclerAdapterCreated(adapter: RecyclerAdapter?, v: ViewHolder?, position: Int)
    }

    fun setOnRecyclerAdapterListener(listener: OnRecyclerAdapterListener?) {
        mCallback = try {
            listener
        } catch (e: ClassCastException) {
            throw ClassCastException("$this must implement OnRecyclerAdapterListener")
        }
    }

    inner class ViewHolder internal constructor(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(resId, parent, false)
        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.mTextView.setText(mDataset[position]);
        if (mCallback != null) {
            mCallback!!.onRecyclerAdapterCreated(this, holder, position)
        }
    }

    override fun getItemCount(): Int {
        return count
    }
}