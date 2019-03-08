package com.advantech.edgexdeploy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private DeployService mContext;
    private List<DeviceItem> mDevice;


    private static class ViewHolder
    {
        private TextView macAddr;
        private TextView status;
        private TextView result;
    }
    ListAdapter(DeployService context, List<DeviceItem> deviceItem) {
        super();
        mContext = context;
        mDevice = deviceItem;
        mInflater = mContext.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mDevice.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevice.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.listview_item, parent, false);
            holder = new ViewHolder();
            holder.macAddr = convertView.findViewById(R.id.macaddr);
            holder.status = convertView.findViewById(R.id.status);
            holder.result = convertView.findViewById(R.id.result);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        final DeviceItem deviceItem = mDevice.get(position);
        holder.macAddr.setText(deviceItem.getMacAddr());
        holder.status.setText(deviceItem.getStatus());
        if(deviceItem.getStatus().equals("online")){
            holder.status.setTextColor(mContext.getResources().getColor(R.color.green));
        }else{
            holder.status.setTextColor(mContext.getResources().getColor(R.color.red));
        }
        holder.result.setText(deviceItem.getResult());
        if(deviceItem.getResult().equals("succeed")){
            holder.result.setTextColor(mContext.getResources().getColor(R.color.green));
        }else{
            holder.result.setTextColor(mContext.getResources().getColor(R.color.red));
        }
        return convertView;
    }
}
