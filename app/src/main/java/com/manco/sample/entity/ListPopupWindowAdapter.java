package com.manco.sample.entity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.manco.sample.R;

import java.util.ArrayList;

/**
 * Created by Manco on 2016/10/10.
 */
public class ListPopupWindowAdapter extends BaseAdapter{
    private ArrayList<String> mArrayList;
    private Context mContext;

    public ListPopupWindowAdapter(ArrayList<String> list, Context context) {
        super();
        this.mArrayList = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        if (mArrayList == null) {
            return 0;
        } else {
            return this.mArrayList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mArrayList == null) {
            return null;
        } else {
            return this.mArrayList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_list_menu_item, null, false);
            holder.itemTextView = (TextView) convertView.findViewById(R.id.wifi_operation);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.mArrayList != null) {
            final String itemName = this.mArrayList.get(position);
            if (holder.itemTextView != null) {
                holder.itemTextView.setText(itemName);
            }
        }

        return convertView;
    }

    private class ViewHolder {
        TextView itemTextView;
    }
}
