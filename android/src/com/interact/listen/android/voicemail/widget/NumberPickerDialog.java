package com.interact.listen.android.voicemail.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.interact.listen.android.voicemail.R;

/**
 * A dialog that prompts the user for the time of day using a {@link NumberPicker}.
 */
public final class NumberPickerDialog extends AlertDialog implements OnClickListener
{

    /**
     * The callback interface used to indicate the user is done filling in value (they clicked on the 'Set' button).
     */
    public interface OnNumberSetListener
    {
        void onNumberSet(NumberPicker view, int value);
    }

    public static final class Builder
    {
        private Context context;
        private OnNumberSetListener callBack;
        private int theme;
        private int initialValue;
        private int start, end;
        private int mTitleMultResID, mTitleSingResID;
        private String positiveButton, negativeButton;
        private String details;
        
        public Builder(Context context)
        {
            this.context = context;
            callBack = null;
            theme = android.R.style.Theme_Dialog;
            initialValue = 0;
            start = Integer.MIN_VALUE;
            end = Integer.MAX_VALUE;
            mTitleMultResID = 0;
            mTitleSingResID = 0;
            positiveButton = context.getString(R.string.generic_confirm);
            negativeButton = context.getString(R.string.generic_cancel);
            details = "";

        }
        public void setCallBack(OnNumberSetListener callBack)
        {
            this.callBack = callBack;
        }
        public void setTheme(int theme)
        {
            this.theme = theme;
        }
        public void setInitialValue(int initialValue)
        {
            this.initialValue = initialValue;
        }
        public void setRange(int low, int high)
        {
            start = low;
            end = high;
        }
        public void setButtons(int confirmResID, int cancelResID)
        {
            setButtons(context.getString(confirmResID), context.getString(cancelResID));
        }
        public void setButtons(String confirm, String cancel)
        {
            positiveButton = confirm;
            negativeButton = cancel;
        }
        public void setTitleResIDs(int singleResID, int multipleResID) // should include one %d in them
        {
            mTitleSingResID = singleResID;
            mTitleMultResID = multipleResID;
        }
        public void setDetails(int detailResID)
        {
            details = context.getString(detailResID);
        }
        public void setDetails(String details)
        {
            this.details = details;
        }

        public NumberPickerDialog create()
        {
            NumberPickerDialog d = new NumberPickerDialog(context, theme, callBack, initialValue, start, end,
                                                          positiveButton, negativeButton,
                                                          mTitleSingResID, mTitleMultResID, details);
            
            return d;
        }
        
    }
    
    private static final String VALUE = "value";

    private final NumberPicker mNumberPicker;
    private final OnNumberSetListener mCallBack;

    private final int mTitleMultResID;
    private final int mTitleSingResID;

    private int mValue;
    
    private NumberPickerDialog(Context context, int theme, OnNumberSetListener callBack, int initialValue,
                               int start, int end, String confirm, String cancel,
                               int noSResID, int withSResID, String details)
    {
        super(context, theme);
        mCallBack = callBack;
        mValue = initialValue;

        mTitleMultResID = withSResID;
        mTitleSingResID = noSResID;
        
        setButton(BUTTON_POSITIVE, confirm, this);
        setButton(BUTTON_NEGATIVE, cancel, (OnClickListener)null);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_widget, null);
        setView(view);

        mNumberPicker = (NumberPicker)view.findViewById(R.id.pickedNumber);
        mNumberPicker.setRange(start, end);
        mNumberPicker.setCurrent(mValue);
        
        mNumberPicker.setOnChangeListener(new NumberPicker.OnChangedListener()
        {
            @Override
            public void onNumberChanged(NumberPicker picker, int oldVal, int newVal)
            {
                updateView();
            }
        });

        TextView detailText = (TextView)view.findViewById(R.id.numberPickerDetails);
        detailText.setText(details);
        
        updateView();
    }

    public void setRange(int start, int end)
    {
        mNumberPicker.setRange(start, end);
    }

    public void updateNumber(int current)
    {
        mNumberPicker.setCurrent(current);
        updateView();
    }

    public void onClick(DialogInterface dialog, int which)
    {
        if(mCallBack != null)
        {
            mNumberPicker.clearFocus();
            mCallBack.onNumberSet(mNumberPicker, mNumberPicker.getCurrent());
        }
    }

    @Override
    public Bundle onSaveInstanceState()
    {
        Bundle state = super.onSaveInstanceState();
        state.putInt(VALUE, mNumberPicker.getCurrent());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        int current = savedInstanceState.getInt(VALUE);
        updateNumber(current);
    }
    
    private void updateView()
    {
        int current = mNumberPicker.getCurrent();
        if (current == 1)
        {
            if(mTitleSingResID != 0)
            {
                setTitle(getContext().getString(mTitleSingResID, current));
            }
        }
        else if(mTitleMultResID != 0)
        {
            setTitle(getContext().getString(mTitleMultResID, current));
        }
    }
}
