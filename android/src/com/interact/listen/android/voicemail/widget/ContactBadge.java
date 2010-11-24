package com.interact.listen.android.voicemail.widget;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.QuickContactBadge;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.R;

import java.io.ByteArrayInputStream;

public class ContactBadge extends QuickContactBadge
{
    public interface OnComplete
    {
        void onComplete(Data info);
    }

    public static final class Data implements Comparable<Data>
    {
        private String mContactPhone;
        private Uri mContactUri;
        private Uri mContactPhoto;
        private String mContactName;
        private byte[] mPhotoData;

        private Data()
        {
            clear();
        }
        
        private Data(Data i)
        {
            set(i);
        }

        private void set(Data i)
        {
            this.mContactPhone = i.mContactPhone;
            this.mContactUri = i.mContactUri;
            this.mContactPhoto = i.mContactPhoto;
            this.mContactName = i.mContactName;
            this.mPhotoData = i.mPhotoData;
        }
        
        private void clear()
        {
            mContactPhone = null;
            mContactUri = null;
            mContactPhoto = null;
            mContactName = null;
            mPhotoData = null;
        }
        
        public String getContactName()
        {
            return mContactName;
        }
        
        public String getPhoneNumber()
        {
            return mContactPhone;
        }

        @Override
        public int compareTo(Data cbi)
        {
            if(mContactPhone == null)
            {
                return cbi.mContactPhone == null ? 0 : -1;
            }
            return cbi.mContactPhone == null ? 1 : mContactPhone.compareTo(cbi.mContactPhone);
        }
        
        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof Data ? compareTo((Data)obj) == 0 : false;
        }
        
        @Override
        public int hashCode()
        {
            return mContactPhone == null ? 0 : mContactPhone.hashCode();
        }
    }
    
    private static final String TAG = Constants.TAG + "ContactBadge";
    
    private static final int TOKEN_LOOKUP = 1;
    private static final int TOKEN_ICON = 2;
    
    private static final String[] LOOKUP_PROJECTION = new String[] {PhoneLookup._ID, PhoneLookup.LOOKUP_KEY,
                                                                    PhoneLookup.DISPLAY_NAME};
    private static final int ID_INDEX = 0;
    private static final int LOOKUP_INDEX = 1;
    private static final int NAME_INDEX = 2;
    
    private static final String[] ICON_PROJECTION = new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO};

    private QueryHandler mQueryHandler;
    private Data info;
    private OnComplete updateListener;
    
    public ContactBadge(Context context)
    {
        super(context);
        mQueryHandler = new QueryHandler(context.getContentResolver());
        info = new Data();
        updateListener = null;
    }

    public ContactBadge(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mQueryHandler = new QueryHandler(context.getContentResolver());
        info = new Data();
    }
    
    public ContactBadge(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mQueryHandler = new QueryHandler(context.getContentResolver());
        info = new Data();
    }

    public void onDestroy()
    {
        mQueryHandler.cancelOperation(TOKEN_LOOKUP);
        mQueryHandler.cancelOperation(TOKEN_ICON);
        updateListener = null;
    }
    
    public void setOnCompleteListener(OnComplete listener)
    {
        updateListener = listener;
    }

    public void assignFromInfo(Data cbi)
    {
        if(info.mContactUri == cbi.mContactUri && info.mContactPhoto == cbi.mContactPhoto &&
            info.mPhotoData == cbi.mPhotoData && TextUtils.equals(info.mContactPhone, cbi.mContactPhone))
        {
            return;
        }
        info.set(cbi);

        if(info.mContactUri == null)
        {
            super.assignContactFromPhone(info.mContactPhone, true);
        }
        else
        {
            super.assignContactUri(info.mContactUri);
        }

        if(info.mPhotoData == null && info.mContactPhoto != null)
        {
            mQueryHandler.startQuery(TOKEN_ICON, null, info.mContactPhoto, ICON_PROJECTION, null, null, null);
        }
        else
        {
            setPhoto(info.mPhotoData);
        }
    }
    
    @Override
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup)
    {
        Log.v(TAG, "Assign Phone('" + phoneNumber + "', " + lazyLookup + ") current=" + info.mContactPhone);
        
        if(!TextUtils.equals(phoneNumber, info.mContactPhone))
        {
            super.assignContactFromPhone(phoneNumber, true);
        }
        info.mContactPhone = phoneNumber;
        if(!lazyLookup)
        {
            mQueryHandler.startQuery(TOKEN_LOOKUP, null, Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, info.mContactPhone),
                                     LOOKUP_PROJECTION, null, null, null);
        }
    }
    
    @Override
    public void assignContactFromEmail(String email, boolean lazyLookup)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void assignContactUri(Uri contactUri)
    {
        throw new UnsupportedOperationException();
    }
    
    public void clearInfo()
    {
        if(info.mContactUri != null || info.mContactPhone != null || info.mContactPhoto != null)
        {
            super.assignContactFromPhone(null, true);
            setPhoto(null);
        }
        info.clear();
    }
    
    private void setPhoto(byte[] data)
    {
        if(data == null || data.length == 0)
        {
            setImageResource(R.drawable.ic_contact_picture);
        }
        else
        {
            Drawable drawable = new BitmapDrawable(getResources(), new ByteArrayInputStream(info.mPhotoData));
            setImageDrawable(drawable);
        }
    }
    
    private class QueryHandler extends AsyncQueryHandler
    {
        public QueryHandler(ContentResolver resolver)
        {
            super(resolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor)
        {
            try
            {
                switch(token)
                {
                    case TOKEN_LOOKUP:
                        if(cursor != null && cursor.moveToFirst() && !cursor.isNull(ID_INDEX))
                        {
                            long contactID = cursor.getLong(ID_INDEX);
                            String key = cursor.getString(LOOKUP_INDEX);
                            info.mContactName = cursor.getString(NAME_INDEX);
                            info.mContactUri = Contacts.getLookupUri(contactID, key);
                            Uri contact = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactID);
                            info.mContactPhoto = Uri.withAppendedPath(contact, Photo.CONTENT_DIRECTORY);
                            Log.v(TAG, "found contact " + info.mContactName);
                        }
                        else
                        {
                            info.mContactUri = null;
                            if(updateListener != null)
                            {
                                updateListener.onComplete(new Data(info));
                                updateListener = null;
                            }
                            return;
                        }
                        break;
                    case TOKEN_ICON:
                        if(cursor != null && cursor.moveToFirst() && !cursor.isNull(0))
                        {
                            info.mPhotoData = cursor.getBlob(0);
                            Log.v(TAG, "got photo data of size " + info.mPhotoData.length + " for " + info.mContactName);
                        }
                        else
                        {
                            Log.v(TAG, "no photo data for " + info.mContactName);
                            info.mPhotoData = new byte[0];
                        }
                        setPhoto(info.mPhotoData);
                        if(updateListener != null)
                        {
                            updateListener.onComplete(new Data(info));
                            updateListener = null;
                        }
                        return;
                    default:
                        return;
                }
            }
            finally
            {
                if(cursor != null)
                {
                    cursor.close();
                }
            }
            
            ContactBadge.super.assignContactUri(info.mContactUri);

            mQueryHandler.startQuery(TOKEN_ICON, null, info.mContactPhoto, ICON_PROJECTION, null, null, null);
        }
    }

}
