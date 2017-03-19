package gbbtbb.com.shoppinghelper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        LinearLayout layout;
        TextView amount;
        TextView item;
        TextView date;
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
            holder.layout = (LinearLayout) convertView.findViewById(R.id.list_item_layout);
            holder.amount = (TextView) convertView.findViewById(R.id.listitem_amount);
            holder.item = (TextView) convertView.findViewById(R.id.list_item_name);
            holder.date = (TextView) convertView.findViewById(R.id.listitem_date);
            holder.cancelIcon = (ImageView) convertView.findViewById(R.id.listitem_cancelicon);
            holder.confirmIcon = (ImageView) convertView.findViewById(R.id.listitem_confirmicon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.amount.setText(Integer.toString(rowItem.getAmount())+"€");
        holder.item.setText(rowItem.getItemName());
        holder.date.setText(rowItem.getDate());
        holder.alreadySpent = rowItem.getAlreadySpent();
        final int pos = position;

        // If the item is already bought, hide the cancel/confirm icon and unregister their click handlers
        // Note: instead of making the icons invisible (which would remove the ImageView from the layout, and
        // mess up with the nice alignments), a blank/transparent icon of same size if displayed instead.
        if (holder.alreadySpent) {
            holder.cancelIcon.setImageResource(R.drawable.blankicon);
            holder.confirmIcon.setImageResource(R.drawable.blankicon);
            holder.layout.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.spent));
            holder.confirmIcon.setOnClickListener(null);
            holder.cancelIcon.setOnClickListener(null);
        // else, show cancel/confirm buttons and register their click handlers
        } else  {
            holder.cancelIcon.setImageResource(R.drawable.cancel);
            holder.confirmIcon.setImageResource(R.drawable.confirm);
            holder.layout.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.futurespend));

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
                }

            });
        }

        return convertView;
    }

}
