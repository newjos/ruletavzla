package com.example.rouletteapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WinnerAdapter extends BaseAdapter {

    private Context context;
    private List<String> winners;
    private LayoutInflater inflater;

    public WinnerAdapter(Context context, List<String> winners) {
        this.context = context;
        this.winners = (winners != null) ? winners : new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return winners.size();
    }

    @Override
    public Object getItem(int position) {
        return winners.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Asumiendo que R.layout.list_item_winner está disponible
            int layoutId = context.getResources().getIdentifier("list_item_winner", "layout", context.getPackageName());
            convertView = inflater.inflate(layoutId, parent, false);
            holder = new ViewHolder();
            holder.tvWinnerPosition = convertView.findViewById(context.getResources().getIdentifier("tvWinnerPosition", "id", context.getPackageName()));
            holder.tvWinnerName = convertView.findViewById(context.getResources().getIdentifier("tvWinnerName", "id", context.getPackageName()));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String winnerName = winners.get(position);
        holder.tvWinnerPosition.setText((position + 1) + ".");
        holder.tvWinnerName.setText(winnerName);

        return convertView;
    }

    public void updateWinners(List<String> newWinners) {
        this.winners = (newWinners != null) ? new ArrayList<>(newWinners) : new ArrayList<>();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView tvWinnerPosition;
        TextView tvWinnerName;
    }
}
