package com.manco.sample.entity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manco.sample.R;
import com.manco.sample.util.WiFiHandler;

import java.util.List;

/**
 * Created by Manco on 2016/10/9.
 */
public class APAdapter extends RecyclerView.Adapter {
    private List<AccessPoint> accessPoints;
    public interface OnItemClickListener {
        void onItemClick(View view,int position);
    }
    OnItemClickListener onItemClickListener;

    public APAdapter(List<AccessPoint> aps) {
        this.accessPoints = aps;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new APViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_ap_item, null),this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        APViewHolder apViewHolder = (APViewHolder) holder;
        AccessPoint accessPoint = accessPoints.get(position);
        apViewHolder.ssid.setText(accessPoint.getSsid());
        apViewHolder.level.setText("" + accessPoint.getSignalStrength());
        apViewHolder.capabilities.setText(accessPoint.getEncryptionType());
        if (WiFiHandler.instance().isConnected(accessPoint)) {
            apViewHolder.state.setVisibility(View.VISIBLE);
            apViewHolder.state.setText("Connected");
        } else {
            apViewHolder.state.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return accessPoints == null ? 0 : accessPoints.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class APViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView ssid;
        public TextView level;
        public TextView capabilities;
        public TextView state;
        public OnItemClickListener onItemClickListener;

        public APViewHolder(View itemView,OnItemClickListener onItemClickListener) {
            super(itemView);
            ssid = (TextView) itemView.findViewById(R.id.ssid);
            level = (TextView) itemView.findViewById(R.id.level);
            capabilities = (TextView) itemView.findViewById(R.id.capabilities);
            state = (TextView) itemView.findViewById(R.id.state);
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v,getAdapterPosition());
            }
        }
    }
}
