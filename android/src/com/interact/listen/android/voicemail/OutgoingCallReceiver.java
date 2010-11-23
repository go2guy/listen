package com.interact.listen.android.voicemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

public class OutgoingCallReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle extras = intent.getExtras();

        if(extras != null)
        {
            String number = getResultData();
            if(TextUtils.isEmpty(number))
            {
                number = extras.getString(Intent.EXTRA_PHONE_NUMBER);
            }
            if(isExtension(number, 3))
            {
                String pre = ApplicationSettings.getDialPrefix(context);
                if(!TextUtils.isEmpty(pre))
                {
                    String trimmed = PhoneNumberUtils.stripSeparators(pre);
                    StringBuilder sb = new StringBuilder(trimmed);
                    if(!PhoneNumberUtils.isStartsPostDial(trimmed.charAt(trimmed.length() - 1)))
                    {
                        sb.append(PhoneNumberUtils.WAIT);
                    }
                    sb.append(number);
                    setResultData(sb.toString());
                }
            }
        }
    }

    private boolean isExtension(String number, int extensionLength)
    {
        if(number == null || number.length() != extensionLength)
        {
            return false;
        }
        for(int i = 0; i < extensionLength; ++i)
        {
            char c = number.charAt(i);
            if(c > '9' || c < '0')
            {
                return false;
            }
        }
        return true;
    }
}
