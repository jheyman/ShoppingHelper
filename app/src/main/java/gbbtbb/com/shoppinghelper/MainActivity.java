package gbbtbb.com.shoppinghelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    int remainingBudget = 2000;
    int totalBudget = 2000;
    int viewWidth;
    int viewHeight;

    static final int TEXT_SIZE=50;
    static final int TEXT_PADDING=20;

    public String userValueSpent;
    public String userValueTotal;

    public static final String APP_PREF_NAME = "comm.gbbtbb.shoppinghelper" ;
    public static final String TOTALVAL_PARAM = "comm.gbbtbb.shoppinghelper.total" ;
    public static final String REMAININGVAL_PARAM = "comm.gbbtbb.shoppinghelper.remaining" ;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load latest values from app's preferences
        sharedpreferences = getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        totalBudget = sharedpreferences.getInt(TOTALVAL_PARAM, 2000);
        remainingBudget = sharedpreferences.getInt(REMAININGVAL_PARAM, 2000);

        // Add the hook function on the reset button
        Button resetButton = (Button)findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setCancelable(false);
                AlertDialog dialog;
                alert.setTitle(getResources().getString(R.string.total_budget_text));
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        userValueTotal = input.getText().toString();
                        totalBudget = Integer.valueOf(userValueTotal);
                        remainingBudget = totalBudget;
                        refreshBudgetView();
                    }
                });
                alert.setNegativeButton("Cancel", null);
                dialog = alert.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
            }
        });

        // Add the hook function on the shop button
        Button shopButton = (Button)findViewById(R.id.shopButton);
        shopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setCancelable(false);
                AlertDialog dialog;
                alert.setTitle(getResources().getString(R.string.amount_spent_text));
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        userValueSpent = input.getText().toString();
                        remainingBudget = remainingBudget - Integer.valueOf(userValueSpent);
                        if (remainingBudget < 0) remainingBudget = 0;
                        refreshBudgetView();
                    }
                });
                alert.setNegativeButton("Cancel", null);
                dialog = alert.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
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

        if (remaining > 0.5*totalBudget) {
            return  Color.parseColor("#74BF00");
        } else if (remaining > 0.4*totalBudget) {
            return  Color.parseColor("#B1C200");
        }
        else if (remaining > 0.3*totalBudget) {
            return  Color.parseColor("#C69C00");
        }
        else if (remaining > 0.2*totalBudget) {
            return  Color.parseColor("#CA6200");
        }
        else if (remaining > 0.1*totalBudget) {
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

        // Fill spent budget part
        int spentPartHeight = (totalBudget-remainingBudget)*viewHeight/totalBudget;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(0,0,viewWidth,spentPartHeight,paint);

        // Print spent amount (if space allows)
        if (spentPartHeight > TEXT_SIZE+TEXT_PADDING) {
            TextPaint spentTextPaint = new TextPaint();
            spentTextPaint.setStyle(Paint.Style.FILL);
            spentTextPaint.setTextSize(TEXT_SIZE);
            spentTextPaint.setColor(Color.DKGRAY);
            spentTextPaint.setAntiAlias(true);
            spentTextPaint.setSubpixelText(true);

            String spent = getResources().getString(R.string.spent_text)+ Integer.toString(totalBudget - remainingBudget) + " €";

            textHeight = getTextHeight(spentTextPaint, "0");
            textWidth = getTextWidth(spentTextPaint, spent);

            canvas.drawText(spent, 0.5f * viewWidth - 0.5f * textWidth, textHeight + TEXT_PADDING, spentTextPaint);
        }

        // Fill remaining budget part
        int remainingPartHeigth = (remainingBudget*viewHeight)/totalBudget;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getFillColorFromRemaining(remainingBudget));
        canvas.drawRect(0,viewHeight - remainingPartHeigth,viewWidth, viewHeight, paint);

        // Print remaining amount (if space allows)
        String remaining = getResources().getString(R.string.remaining_text)+ Integer.toString(remainingBudget) + " €";

        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#FFFFFF"));
        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        textHeight = getTextHeight(textPaint, "0");
        textWidth = getTextWidth(textPaint, remaining);

        if (remainingPartHeigth > TEXT_SIZE+TEXT_PADDING) {
            canvas.drawText(remaining, 0.5f * viewWidth - 0.5f * textWidth, viewHeight - (remainingBudget * viewHeight) / totalBudget + textHeight + TEXT_PADDING, textPaint);
        } else {
            textPaint.setColor(getFillColorFromRemaining(remainingBudget));
            canvas.drawText(remaining, 0.5f * viewWidth - 0.5f * textWidth, viewHeight - (remainingBudget * viewHeight) / totalBudget - TEXT_PADDING, textPaint);
        }

        // update ImageView with this rendered bitmap
        iv.setImageBitmap(bmp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(TOTALVAL_PARAM, totalBudget);
        editor.putInt(REMAININGVAL_PARAM, remainingBudget);
        editor.commit();
    }
}
