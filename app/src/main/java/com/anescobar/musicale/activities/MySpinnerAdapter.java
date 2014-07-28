//package com.anescobar.musicale.activities;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import com.anescobar.musicale.R;
//
//public class MySpinnerAdapter extends ArrayAdapter<String> {
//
//    // CUSTOM SPINNER ADAPTER
//    private Context mContext;
//    public MySpinnerAdapter(Context context, int textViewResourceId,
//                            String[] objects) {
//        super(context, textViewResourceId, objects);
//
//        mContext = context;
//        // TODO Auto-generated constructor stub
//    }
//
//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        // TODO Auto-generated method stub
//        return getCustomView(position, convertView, parent);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // TODO Auto-generated method stub
//        return getCustomView(position, convertView, parent);
//    }
//
//    public View getCustomView(int position, View convertView,ViewGroup parent) {
//// TODO Auto-generated method stub
//// return super.getView(position, convertView, parent);
//
//
//        LayoutInflater inflater =
//                ( LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//
//        ViewHolder holder;
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.customspinneritem, null);
//            holder = new ViewHolder();
//            holder.txt01 = (TextView) convertView.findViewById(R.id.TextView01);
//            holder.txt02 = (TextView) convertView.findViewById(R.id.TextView02);
//
//            convertView.setTag(holder);
//
//        } else {
//
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        holder.txt01.setText("My Library");
//        holder.txt02.setText("ALL MUSIC");
//
//        return convertView;
//    }
//
//    class ViewHolder {
//        TextView txt01;
//        TextView txt02;
//    }
//
//
//
//} // end custom adapter