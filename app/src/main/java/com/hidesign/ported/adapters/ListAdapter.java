package com.hidesign.ported.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ListAdapter extends BaseAdapter  {
	
	Context c;
	private int count;
	private int resId;
	OnListAdapterAdapterListener mCallback;
	
	@SuppressWarnings("unused")
	private int mLastPosition = 1;
	
	public interface OnListAdapterAdapterListener {
		
        public void OnListAdapterAdapterCreated(ListAdapter adapter, View v, int position, ViewGroup viewGroup);
    }
	
	public void setOnListAdapterAdapterListener(OnListAdapterAdapterListener listener) {
		
		try {
            mCallback = listener;
        } catch (ClassCastException e)  {
            throw new ClassCastException(this.toString() + " must implement OnMGListAdapterAdapterListener");
        }
	}
	
	
	public ListAdapter(Context c, int count, int resId) {
		this.c = c;
		this.count = count;
		this.resId = resId;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return count;
	}

	@Override
	public Object getItem(int pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return pos;
	}

	@Override
	public View getView(int pos, View v, ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		
		ViewHolder viewHolder = null;
		
		if(v == null) {
			LayoutInflater li = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(resId, null);
			
			viewHolder = new ViewHolder();
			viewHolder.view = v;
			v.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) v.getTag();
			Log.w("ListAdapter Class", "View being reused.");
		}
		
		if(mCallback != null)
			mCallback.OnListAdapterAdapterCreated(this, viewHolder.view, pos, viewGroup);
		return v;
	}
	
	public class ViewHolder {
		
		public View view;
	}
}

