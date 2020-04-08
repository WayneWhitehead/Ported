package com.hidesign.ported.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ListAnimatedAdapter extends BaseAdapter  {
	
	private Context c;
	private int count;
	private int resId;
	private OnListAnimatedAdapter mCallback;

	@SuppressWarnings("unused")
	private int mLastPosition = 1;
	
	public interface OnListAnimatedAdapter {
		
        public void OnMGListAnimatedAdapterCreated(ListAnimatedAdapter adapter, View v, int position, ViewGroup viewGroup);
    }
	
	public void setOnListAnimatedAdapter(OnListAnimatedAdapter listener) {
		
		try {
            mCallback = listener;
        } catch (ClassCastException e)  {
            throw new ClassCastException(this.toString() + " must implement OnMGListAnimatedAdapter");
        }
	}
	
	
	public ListAnimatedAdapter(Context c, int count, int resId) {
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
			Log.w("adapter", "View being reused.");
		}
		
		if(mCallback != null)
			mCallback.OnMGListAnimatedAdapterCreated(this, viewHolder.view, pos, viewGroup);

		return v;
	}
	
	public class ViewHolder {
		
		public View view;
	}
}

