package gbbtbb.com.shoppinghelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    int remainingBudget = 1000;
    int totalBudget = 1000;
    int spentBudget = 0;
    int futureSpend = 0;

    int viewWidth;
    int viewHeight;

    static final int TEXT_SIZE=40;
    static final int MIN_HORIZONTAL_TEXT_PADDING=10;
    static final int MIN_VERTICAL_TEXT_PADDING=15;
    static final int MIN_REMAINING_SLIDE_WIDTH=5;

    public String userValueSpent;
    public String userValueFutureSpend;
    public String userValueTotal;
    public String userItemDescription;

    public static String CONFIRMITEM_ACTION = "com.gbbtbb.shoppinghelper.CONFIRMITEM_ACTION";
    public static String CANCELITEM_ACTION = "com.gbbtbb.shoppinghelper.CANCELITEM_ACTION";

    public static String EXTRA_ITEM_POSITION = "com.gbbtbb.shoppinghelper.itemposition";

    public static final String APP_PREF_NAME = "com.gbbtbb.shoppinghelper" ;
    public static final String TOTALVAL_PARAM = "com.gbbtbb.shoppinghelper.total" ;
    public static final String REMAININGVAL_PARAM = "com.gbbtbb.shoppinghelper.remaining" ;
    public static final String SPENTVAL_PARAM = "com.gbbtbb.shoppinghelper.spent" ;
    public static final String FUTURSPENDVAL_PARAM = "com.gbbtbb.shoppinghelper.futurespend" ;
    public static final String LIST_PARAM = "com.gbbtbb.shoppinghelper.list" ;

    SharedPreferences sharedpreferences;

    List<ListRowItem> listItems;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(CONFIRMITEM_ACTION);
        filter.addAction(CANCELITEM_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(ListBroadcastReceiver, filter);

        listItems = new ArrayList<>();
        ListView listView = (ListView)findViewById(R.id.item_list);
        adapter = new ListAdapter(this, R.layout.list_item, listItems);
        listView.setAdapter(adapter);

        // Load latest values from app's preferences
        sharedpreferences = getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        totalBudget = sharedpreferences.getInt(TOTALVAL_PARAM, 2000);
        remainingBudget = sharedpreferences.getInt(REMAININGVAL_PARAM, 2000);
        spentBudget = sharedpreferences.getInt(SPENTVAL_PARAM, 0);
        futureSpend = sharedpreferences.getInt(FUTURSPENDVAL_PARAM, 0);
        deserializeItems(sharedpreferences.getString(LIST_PARAM,""));

        // Add the hook function on the reset button
        Button resetButton = (Button)findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setCancelable(false);
            final AlertDialog dialog;

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText amountBox = new EditText(MainActivity.this);
            amountBox.setInputType(InputType.TYPE_CLASS_NUMBER);
            amountBox.setHint(getResources().getString(R.string.amount_input_text));
            layout.addView(amountBox);

            alert.setView(layout);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing intentionally: this is to allow a specific click handler
                // that will perform checks on the values and keep the dialog open if needed
                }
            });

            alert.setNegativeButton("Cancel", null);
            dialog = alert.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();

            // Register the click listener on the OK button, that will CHECK entered values before accepting to close.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    userValueTotal = amountBox.getText().toString();
                    if (!userValueTotal.equals("")) {
                        int value = Integer.valueOf(userValueTotal);

                        if (value > 0) {
                            totalBudget = value;
                            remainingBudget = totalBudget;
                            futureSpend = 0;
                            spentBudget = 0;
                            refreshBudgetView();
                            listItems.clear();
                            dialog.dismiss();
                        }
                        else
                            Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.wrong_amount_text), Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.empty_amount_text), Toast.LENGTH_SHORT).show();
                }
            });
            }
        });

        // Add the hook function on the pre-shop button
        Button preShopButton = (Button)findViewById(R.id.preshopButton);
        preShopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setCancelable(false);
            final AlertDialog dialog;

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText amountBox = new EditText(MainActivity.this);
            amountBox.setInputType(InputType.TYPE_CLASS_NUMBER);
            amountBox.setHint(getResources().getString(R.string.amount_input_text));
            layout.addView(amountBox);

            final EditText itemNameBox = new EditText(MainActivity.this);
            itemNameBox.setHint(getResources().getString(R.string.name_input_text));
            layout.addView(itemNameBox);

            alert.setView(layout);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing intentionally: this is to allow a specific click handler
                    // that will perform checks on the values and keep the dialog open if needed.
                }
            });

            alert.setNegativeButton("Cancel", null);
            dialog = alert.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();

            // Register the click listener on the OK button, that will CHECK entered values before accepting to close.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    userValueFutureSpend = amountBox.getText().toString();
                    if (!userValueFutureSpend.equals("")) {

                        userItemDescription = itemNameBox.getText().toString();
                        int value = Integer.valueOf(userValueFutureSpend);

                        if (value > 0) {
                            futureSpend = futureSpend + Integer.valueOf(userValueFutureSpend);
                            remainingBudget = remainingBudget - Integer.valueOf(userValueFutureSpend);
                            refreshBudgetView();

                            ListRowItem lri = new ListRowItem(Integer.valueOf(userValueFutureSpend), userItemDescription, "-",  false);
                            addFuturePurchase(lri);

                            dialog.dismiss();
                        }
                        else
                            Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.wrong_amount_text), Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.empty_amount_text), Toast.LENGTH_SHORT).show();
                }
            });
            }
        });

        // Add the hook function on the shop button
        Button shopButton = (Button)findViewById(R.id.shopButton);
        shopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setCancelable(false);
            final AlertDialog dialog;

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText amountBox = new EditText(MainActivity.this);
            amountBox.setInputType(InputType.TYPE_CLASS_NUMBER);
            amountBox.setHint(getResources().getString(R.string.amount_input_text));
            layout.addView(amountBox);

            final EditText itemNameBox = new EditText(MainActivity.this);
            itemNameBox.setHint(getResources().getString(R.string.name_input_text));
            layout.addView(itemNameBox);

            alert.setView(layout);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing intentionally: this is to allow a specific click handler
                    // that will perform checks on the values and keep the dialog open if needed.
                }
            });

            alert.setNegativeButton("Cancel", null);
            dialog = alert.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();

            // Register the click listener on the OK button, that will CHECK entered values before accepting to close.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    userValueSpent = amountBox.getText().toString();
                    if (!userValueSpent.equals("")) {

                        userItemDescription = itemNameBox.getText().toString();
                        int value = Integer.valueOf(userValueSpent);

                        if (value > 0) {
                            remainingBudget = remainingBudget - value;
                            spentBudget = spentBudget + Integer.valueOf(userValueSpent);
                            refreshBudgetView();

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
                            sdf.setTimeZone(TimeZone.getDefault());
                            String date = sdf.format(new Date());
                            ListRowItem lri = new ListRowItem(Integer.valueOf(userValueSpent), userItemDescription, date, true);
                            addPurchase(lri);

                            dialog.dismiss();
                        }
                        else
                            Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.wrong_amount_text), Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.empty_amount_text), Toast.LENGTH_SHORT).show();
                }
            });
            }
        });

        // Implement hook on global layout completion, to get valid view sizes
        final ImageView BudgetIV = (ImageView)findViewById(R.id.budgetRemainingView);
        ViewTreeObserver viewTreeObserver = BudgetIV.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    BudgetIV.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    viewWidth = BudgetIV.getWidth();
                    viewHeight = BudgetIV.getHeight();
                    refreshBudgetView();
                }
            });
        }
    }

    private static float getTextWidth(Paint p, String text) {
        return p.measureText(text);
    }

    private static float getTextHeight(Paint p, String text) {
        Rect bounds = new Rect();
        p.getTextBounds(text, 0, 1, bounds);
        return bounds.height();
    }

    private int getFillColorFromRemaining(float remaining) {

        // Custom color gradient from green to orange to red
        if (remaining > 0.4*totalBudget) {
            return  Color.parseColor("#74BF00");
        } else if (remaining > 0.3*totalBudget) {
            return  Color.parseColor("#B1C200");
        }
        else if (remaining > 0.2*totalBudget) {
            return  Color.parseColor("#C69C00");
        }
        else if (remaining > 0.1*totalBudget) {
            return  Color.parseColor("#CA6200");
        }
        else if (remaining > 0.05*totalBudget) {
            return  Color.parseColor("#CE2600");
        } else {
            return  Color.parseColor("#D20017");
        }
    }

    private void refreshBudgetView() {
        float textHeight;
        float textWidth;
        ImageView iv = (ImageView)findViewById(R.id.budgetRemainingView);

        Bitmap bmp = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        //Log.i("MainActivity","spent="+Integer.toString(spentBudget)+", futureSpent="+Integer.toString(futureSpend) + ", remaining="+Integer.toString(remainingBudget));

        // Fill spent budget part
        int spentDisplayed = spentBudget <= totalBudget ? spentBudget : totalBudget;

        int spentPartWidth = (spentDisplayed)*viewWidth/totalBudget;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(this, R.color.spent));
        canvas.drawRect(0,0, spentPartWidth, viewHeight, paint);

        // Fill future spend part
        int futureSpentDisplayed = futureSpend <= (totalBudget - spentDisplayed) ? futureSpend : totalBudget - spentDisplayed;
        int futureSpendPartWidth = (futureSpentDisplayed*viewWidth)/totalBudget;
        paint.setColor(ContextCompat.getColor(this, R.color.futurespend));
        canvas.drawRect(spentPartWidth, 0, spentPartWidth+futureSpendPartWidth, viewHeight, paint);

        // Fill remaining budget part
        paint.setColor(getFillColorFromRemaining(remainingBudget));
        int startX = spentPartWidth+futureSpendPartWidth;
        if (startX > viewWidth - MIN_REMAINING_SLIDE_WIDTH) startX = viewWidth - MIN_REMAINING_SLIDE_WIDTH;
        canvas.drawRect(startX, 0, viewWidth, viewHeight, paint);

        // Print spent amount (if space allows)
        TextPaint spentTextPaint = new TextPaint();
        spentTextPaint.setStyle(Paint.Style.FILL);
        spentTextPaint.setTextSize(TEXT_SIZE);
        spentTextPaint.setColor(Color.WHITE);
        spentTextPaint.setAntiAlias(true);
        spentTextPaint.setSubpixelText(true);

        String spent = getResources().getString(R.string.spent_text) + " " + Integer.toString(spentBudget) + "€";
        textHeight = getTextHeight(spentTextPaint, "0");

        if (spentBudget >= 0) {
            canvas.drawText(spent, MIN_HORIZONTAL_TEXT_PADDING, textHeight + MIN_VERTICAL_TEXT_PADDING, spentTextPaint);
        }

        // Print spent amount (if space allows)
        TextPaint futureSpentTextPaint = new TextPaint();
        futureSpentTextPaint.setStyle(Paint.Style.FILL);
        futureSpentTextPaint.setTextSize(TEXT_SIZE);
        futureSpentTextPaint.setColor(Color.WHITE);
        futureSpentTextPaint.setAntiAlias(true);
        futureSpentTextPaint.setSubpixelText(true);

        String futureSpent = "(+"+Integer.toString(futureSpend) + "€ " + getResources().getString(R.string.futureSpent_text)+ ")";

        textHeight = getTextHeight(spentTextPaint, "0");
        textWidth = getTextWidth(spentTextPaint, futureSpent);

        if (futureSpend > 0) {
            if (spentPartWidth + 0.5f * futureSpendPartWidth - 0.5f * textWidth < 0) {
                canvas.drawText(futureSpent, 10, 0.5f * viewHeight + 0.5f * textHeight, futureSpentTextPaint);
            }
            else if (spentPartWidth + 0.5f * futureSpendPartWidth - 0.5f * textWidth < viewWidth - textWidth) {
                canvas.drawText(futureSpent, spentPartWidth + 0.5f * futureSpendPartWidth - 0.5f * textWidth, 0.5f * viewHeight + 0.5f * textHeight, futureSpentTextPaint);
            } else {
                canvas.drawText(futureSpent, viewWidth - textWidth - MIN_HORIZONTAL_TEXT_PADDING, 0.5f * viewHeight + 0.5f * textHeight, futureSpentTextPaint);
            }
        }

        // Print remaining amount (if space allows)
        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(remainingBudget > 0 ? Color.parseColor("#FFFFFF"): Color.parseColor("#FF0000"));
        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        String remaining = getResources().getString(R.string.remaining_text) + " " +Integer.toString(remainingBudget) + "€";

        textHeight = getTextHeight(textPaint, "0");
        textWidth = getTextWidth(textPaint, remaining);

        canvas.drawText(remaining,  viewWidth - textWidth - MIN_HORIZONTAL_TEXT_PADDING, viewHeight - 0.5f * textHeight, textPaint);

        // update ImageView with this rendered bitmap
        iv.setImageBitmap(bmp);
    }

    private final BroadcastReceiver ListBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (action.equals(CONFIRMITEM_ACTION)) {

                int pos = intent.getIntExtra(EXTRA_ITEM_POSITION, 0);
                Log.i("TEST", "CONFIRM pos=" + Integer.toString(pos));

                ListRowItem lri = listItems.get(pos);
                lri.setAlreadySpent(true);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
                sdf.setTimeZone(TimeZone.getDefault());
                lri.setDate(sdf.format(new Date()));

                // remove it from the list
                listItems.remove(pos);

                futureSpend = futureSpend - lri.getAmount();
                spentBudget = spentBudget + lri.getAmount();

                // re-insert it at the end of the purchased list
                addPurchase(lri);

                // Refresh status bar
                refreshBudgetView();
            }
            else if (action.equals(CANCELITEM_ACTION)) {

                int pos = intent.getIntExtra(EXTRA_ITEM_POSITION, 0);
                Log.i("TEST", "CANCEL pos=" + Integer.toString(pos));

                ListRowItem lri = listItems.get(pos);

                // re-credit cancelled spend
                futureSpend = futureSpend - lri.getAmount();
                remainingBudget = remainingBudget + lri.getAmount();

                // remove it from the list
                listItems.remove(pos);

                // Refresh status bar & list
                refreshBudgetView();
                adapter.notifyDataSetChanged();
            }
        }
    };

    private void addFuturePurchase(ListRowItem lri) {
        listItems.add(lri);
        adapter.notifyDataSetChanged();
    }

    private void addPurchase(ListRowItem lri) {
        // Insert item to the end of the list of already spent items
        int i;
        for (i=0; i<listItems.size(); i++) {
            if (!listItems.get(i).getAlreadySpent())
                break;
        }

        if (i<listItems.size()) {
            listItems.add(i, lri);
        } else {
            listItems.add(lri);
        }

        adapter.notifyDataSetChanged();
    }

    String serializeItems() {
        String data="";
        for (int i=0; i<listItems.size(); i++) {
            ListRowItem lri = listItems.get(i);
            data += String.valueOf(lri.getAmount())+";";
            data += lri.getItemName()+";";
            data += lri.getDate()+";";
            data += String.valueOf(lri.getAlreadySpent())+"#";
        }
        return data;
    }

    void deserializeItems(String data) {

        if (data != "") {
            String[] stringlist = data.split("#");
            for (int i = 0; i < stringlist.length; i++) {
                String item = stringlist[i];
                String[] parts = item.split(";");

                int amount = Integer.valueOf(parts[0]);
                String itemName = parts[1];
                String date = parts[2];
                boolean alreadySpent = Boolean.valueOf(parts[3]);

                ListRowItem lri = new ListRowItem(amount, itemName, date, alreadySpent);
                listItems.add(lri);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void saveCurrentState() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(TOTALVAL_PARAM, totalBudget);
        editor.putInt(REMAININGVAL_PARAM, remainingBudget);
        editor.putInt(FUTURSPENDVAL_PARAM, futureSpend);
        editor.putInt(SPENTVAL_PARAM, spentBudget);
        editor.putString(LIST_PARAM, serializeItems());
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(ListBroadcastReceiver);
        saveCurrentState();
    }
}
