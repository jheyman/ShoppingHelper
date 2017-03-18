package gbbtbb.com.shoppinghelper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends ArrayAdapter<gbbtbb.com.shoppinghelper.ListRowItem> {

    Context context;

    public ListAdapter(Context context, int resourceId,
                               List<gbbtbb.com.shoppinghelper.ListRowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView item;
        TextView creationDate;
        boolean alreadySpent;
        ImageView cancelIcon;
        ImageView confirmIcon;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final ListRowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.creationDate = (TextView) convertView.findViewById(R.id.listitem_creationdate);
            holder.item = (TextView) convertView.findViewById(R.id.list_item_name);
            holder.cancelIcon = (ImageView) convertView.findViewById(R.id.listitem_cancelicon);
            holder.confirmIcon = (ImageView) convertView.findViewById(R.id.listitem_confirmicon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.item.setText(rowItem.getItemName());
        holder.creationDate.setText(rowItem.getCreationDate());
        holder.alreadySpent = rowItem.getAlreadySpent();

        if (holder.alreadySpent) {
            holder.cancelIcon.setVisibility(View.GONE);
            holder.confirmIcon.setVisibility(View.GONE);
        } else  {
            holder.cancelIcon.setVisibility(View.VISIBLE);
            holder.confirmIcon.setVisibility(View.VISIBLE);
        }


        final int pos = position;
        holder.confirmIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                final Bundle extras = new Bundle();
                Intent doneIntent = new Intent();
                doneIntent.setAction(MainActivity.CONFIRMITEM_ACTION);
                extras.putInt(MainActivity.EXTRA_ITEM_POSITION, pos);
                doneIntent.putExtras(extras);
                doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
                v.getContext().sendBroadcast(doneIntent);
            }

        });

        holder.cancelIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                final Bundle extras = new Bundle();
                Intent doneIntent = new Intent();
                doneIntent.setAction(MainActivity.CANCELITEM_ACTION);
                extras.putInt(MainActivity.EXTRA_ITEM_POSITION, pos);
                doneIntent.putExtras(extras);
                doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
                v.getContext().sendBroadcast(doneIntent);
                Log.i("", "CANCel");
            }

        });

        return convertView;
    }

}
