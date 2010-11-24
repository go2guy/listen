package com.interact.listen.android.voicemail.provider;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.Voicemail;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public final class VoicemailHelper
{
    private static final String TAG = Constants.TAG + "Helper";

    public static final long OLD_VOICEMAIL  = 7 * 24 * 3600000; // week ago
    public static final long STALE_DOWNLOAD =      4 * 3600000; // 4 hours
    public static final long RETRY_DOWNLOAD =            60000; // 1 minute
    public static final long OLD_DOWNLOAD   = 1 * 24 * 3600000; // day old

    private static final String[] VOICEMAIL_LIST_PROJECTION =
        new String[]{Voicemails._ID, Voicemails.LEFT_BY, Voicemails.DATE_CREATED, Voicemails.TRANSCRIPT,
                     Voicemails.IS_NEW, Voicemails.HAS_NOTIFIED};

    public static final int VOICEMAIL_LIST_PROJECT_IS_NEW = 4;
    public static final int VOICEMAIL_LIST_PROJECT_NOTIFIED = 5;
    
    private static final String LIST_VOICEMAILS_VIEW_ORDER;
    private static final String LIST_VOICEMAILS_CONTROL_ORDER;

    static
    {
        StringBuffer viewSB = new StringBuffer();
        viewSB.append(Voicemails.USER_NAME);
        viewSB.append(',').append(Voicemails.DATE_CREATED).append(" DESC");
        viewSB.append(',').append(Voicemails.VOICEMAIL_ID);
        LIST_VOICEMAILS_VIEW_ORDER = viewSB.toString();
        
        StringBuffer syncSB = new StringBuffer().append(Voicemails.USER_NAME).append(',').append(Voicemails.DATE_CREATED);
        LIST_VOICEMAILS_CONTROL_ORDER = syncSB.toString();
    }

    public static Cursor getVoicemailListInfoCursor(ContentResolver resolver)
    {
        return resolver.query(Voicemails.CONTENT_URI, VOICEMAIL_LIST_PROJECTION, null, null, LIST_VOICEMAILS_VIEW_ORDER);
    }
    
    public static Cursor getVoicemailDetailsCursor(ContentResolver resolver, int id)
    {
        return resolver.query(getVoicemailUri(id), Voicemail.ALL_PROJECTION, null, null, null);
    }
    public static Voicemail getVoicemailDetailsFromCursor(Cursor c)
    {
        return Voicemail.create(c, Voicemail.ALL_PROJECTION);
    }
    
    public static Voicemail getVoicemail(ContentResolver resolver, int id)
    {
        Cursor c = getVoicemailDetailsCursor(resolver, id);
        if(c == null || !c.moveToFirst())
        {
            if(c != null)
            {
                c.close();
            }
            return null;
        }
        Voicemail v = getVoicemailDetailsFromCursor(c);
        c.close();
        return v;
    }

    public static final Uri getVoicemailUri(int id)
    {
        return ContentUris.withAppendedId(Voicemails.CONTENT_URI, id);
    }
    
    private static String listVoicemailsSelection(String userName, long afterMS, boolean includeDeleted, List<String> selectionArgs)
    {
        StringBuffer sb = new StringBuffer();
        if (userName != null)
        {
            sb.append(Voicemails.USER_NAME).append("=? ");
            selectionArgs.add(userName);
        }
        if (afterMS != 0)
        {
            if(!selectionArgs.isEmpty())
            {
                sb.append("AND ");
            }
            sb.append(Voicemails.DATE_CREATED).append(">=? ");
            selectionArgs.add(Long.toString(afterMS));
        }
        if (!includeDeleted)
        {
            if(!selectionArgs.isEmpty())
            {
                sb.append("AND ");
            }
            sb.append(Voicemails.LABEL).append("!=? ");
            selectionArgs.add(Voicemail.Label.TRASH.name());
        }
        return selectionArgs.isEmpty() ? null : sb.toString();
    }
    
    private static List<Voicemail> getVoicemailList(Cursor cursor)
    {
        List<Voicemail> voicemails = new ArrayList<Voicemail>();
        if(cursor != null)
        {
            while(cursor.moveToNext())
            {
                voicemails.add(Voicemail.create(cursor, Voicemail.ALL_PROJECTION));
            }
        }
        
        cursor.close();

        Log.i(TAG, "got voicemails: " + voicemails.size());
        
        return voicemails;
    }

    public static List<Voicemail> getVoicemails(ContentProviderClient resolver, String userName, long afterMS,
                                                boolean includeDeleted) throws RemoteException
    {
        List<String> selectionList = new ArrayList<String>();
        String selection = listVoicemailsSelection(userName, afterMS, includeDeleted, selectionList);
        String[] selectionArgs = selection == null ? null : selectionList.toArray(new String[selectionList.size()]);
        Cursor cursor = resolver.query(Voicemails.CONTENT_URI, Voicemail.ALL_PROJECTION,
                                       selection, selectionArgs, LIST_VOICEMAILS_CONTROL_ORDER);
        return getVoicemailList(cursor);
    }

    public static List<Voicemail> getUpdatedVoicemails(ContentProviderClient resolver, String userName, long afterMS)
        throws RemoteException
    {
        List<String> selectionList = new ArrayList<String>();
        String selection = listVoicemailsSelection(userName, afterMS, true, selectionList);
        selection += " AND " + Voicemails.STATE + " != 0";

        String[] selectionArgs = selection == null ? null : selectionList.toArray(new String[selectionList.size()]);

        Cursor cursor = resolver.query(Voicemails.CONTENT_URI, Voicemail.ALL_PROJECTION,
                                       selection, selectionArgs, LIST_VOICEMAILS_CONTROL_ORDER);
        return getVoicemailList(cursor);
    }

    public static Uri insertVoicemail(ContentProviderClient resolver, Voicemail voicemail) throws RemoteException
    {
        ContentValues values = voicemail.getInsertValues();
        Uri uri = resolver.insert(Voicemails.CONTENT_URI, values);
        if(uri != null)
        {
            int id = Integer.parseInt(uri.getLastPathSegment());
            Log.i(TAG, "inserted voicemail: " + id);
            voicemail.setIdFromInsert(id);
        }
        return uri;
    }

    public static void insertVoicemails(ContentProviderClient resolver, List<Voicemail> voicemails) throws RemoteException
    {
        ContentValues[] values = new ContentValues[voicemails.size()];
        int i = 0;
        for (Voicemail vm : voicemails)
        {
            values[i++] = vm.getInsertValues();
        }
        resolver.bulkInsert(Voicemails.CONTENT_URI, values);
    }

    public static void deleteVoicemail(ContentProviderClient resolver, Voicemail voicemail) throws RemoteException
    {
        voicemail.setTrashed();
        resolver.delete(voicemail.getUri(), null, null);
    }
    
    public static void moveVoicemailToTrash(ContentResolver resolver, Voicemail voicemail)
    {
        ContentValues values = voicemail.markDeleted();
        moveVoicemailToTrash(resolver, voicemail.getId());
        resolver.update(voicemail.getUri(), values, null, null);
    }

    public static void moveVoicemailToTrash(ContentResolver resolver, int id)
    {
        resolver.update(getVoicemailUri(id), Voicemail.getDeletedValues(), null, null);
    }

    public static void markVoicemailRead(ContentResolver resolver, Voicemail voicemail, boolean isRead)
    {
        ContentValues values = voicemail.markRead(isRead);
        resolver.update(voicemail.getUri(), values, null, null);
    }

    public static void clearMarkedRead(ContentProviderClient resolver, Voicemail voicemail) throws RemoteException
    {
        ContentValues values = voicemail.clearMarkedRead();
        resolver.update(voicemail.getUri(), values, null, null);
    }

    public static void setVoicemailsNotified(ContentResolver resolver, int[] ids)
    {
        ContentValues values = new ContentValues();
        values.put(Voicemails.HAS_NOTIFIED, true);
        for (int id : ids)
        {
            resolver.update(getVoicemailUri(id), values, null, null);
        }
    }

    public static void setVoicemailsNotified(ContentResolver resolver)
    {
        ContentValues values = new ContentValues();
        values.put(Voicemails.HAS_NOTIFIED, true);
        resolver.update(Voicemails.CONTENT_URI, values, Voicemails.HAS_NOTIFIED + "==0", null);
    }

    public static boolean updateVoicemail(ContentProviderClient resolver, Voicemail dest, Voicemail source) throws RemoteException
    {
        ContentValues values = dest.updateValues(source);
        
        if(values.size() == 0)
        {
            Log.i(TAG, "no updates to voicemail values " + source.getVoicemailId());
            return false;
        }
        Log.i(TAG, "voicemail was updated on the server");
        return resolver.update(dest.getUri(), values, null, null) > 0;
    }
    
    public static void setDownloadCancelled(ContentResolver resolver, int id)
    {
        ContentValues values = Voicemail.getClearedDownloadValues();
        values.putNull(Voicemails.DATA);
        resolver.update(getVoicemailUri(id), values, null, null);
    }

    public static void refreshCache(ContentResolver contentResolver)
    {
        int rows = contentResolver.delete(Voicemails.CONTENT_URI, null, null);
        Log.i(TAG, "cleared out voicemails in database: " + rows);
    }
    
    public static int deleteOldAudio(ContentProviderClient resolver, List<Voicemail> voicemails) throws RemoteException
    {
        long now = System.currentTimeMillis();
        long oldVoicemail = now - VoicemailHelper.OLD_VOICEMAIL;
        long oldDownload = now - VoicemailHelper.OLD_DOWNLOAD;

        int updates = 0;
        
        if(voicemails != null)
        {
            // clear downloaded based on the provided list of voicemails
            for(Voicemail vm : voicemails)
            {
                if(vm.isDownloaded() && !vm.getIsNew() && vm.isOlderThan(oldVoicemail) && vm.isAudioOlderThan(oldDownload))
                {
                    Log.i(TAG, "removing voicemail audio for " + vm.getId());
                    ContentValues values = vm.clearDownloaded();
                    values.putNull(Voicemails.DATA);
                    updates += resolver.update(vm.getUri(), values, null, null);
                }
            }
        }
        else
        {
            // clear downloaded based on what's in the database
            ContentValues values = Voicemail.getClearedDownloadValues();

            StringBuffer sb = new StringBuffer();
            sb.append(Voicemails.AUDIO_STATE).append(" = ").append(Voicemail.getAudioDownloadedState()).append(" AND ");
            sb.append(Voicemails.IS_NEW).append(" = 0");
            sb.append(" AND ").append(Voicemails.DATE_CREATED).append(" < ").append(oldVoicemail);
            sb.append(" AND ").append(Voicemails.AUDIO_DATE).append(" < ").append(oldDownload);

            updates += resolver.update(Voicemails.CONTENT_URI, values, sb.toString(), null);
            
            // clear old stuff that may be stuck in downloading
            long staleDownload = System.currentTimeMillis() - OLD_DOWNLOAD;
            sb = new StringBuffer();
            sb.append(Voicemails.AUDIO_STATE).append(" = ").append(Voicemail.getAudioDownloadingState());
            sb.append(" AND ").append(Voicemails.AUDIO_DATE).append(" < ").append(staleDownload);
            updates += resolver.update(Voicemails.CONTENT_URI, values, sb.toString(), null);
        }
        
        return updates;
    }

    public static boolean shouldAttemptDownload(Voicemail v, boolean force)
    {
        long now = System.currentTimeMillis();
        return v.needsDownload(now, RETRY_DOWNLOAD, STALE_DOWNLOAD) && (force || !v.isOlderThan(now - OLD_VOICEMAIL));
    }
    
    public static ParcelFileDescriptor getDownloadStream(ContentResolver resolver, Voicemail voicemail)
    {
        try
        {
            return resolver.openFileDescriptor(voicemail.getUri(), "wt");
        }
        catch(FileNotFoundException e)
        {
            Log.i(TAG, "resolver - went to get download stream, seems like it's being downloaded now " + voicemail.getId(), e);
            return null;
        }
    }
    
    public static ParcelFileDescriptor getDownloadStream(ContentProviderClient resolver, Voicemail voicemail)
        throws RemoteException
    {
        try
        {
            return resolver.openFile(voicemail.getUri(), "wt");
        }
        catch(FileNotFoundException e)
        {
            Log.i(TAG, "client - went to get download stream, seems like it's being downloaded now " + voicemail.getId(), e);
            return null;
        }
    }
    
    public static void deleteUserAccount(ContentResolver resolver, String username)
    {
        resolver.delete(Voicemails.CONTENT_URI, Voicemails.USER_NAME + "=?", new String[]{username});
    }
    
    private VoicemailHelper()
    {
    }

}
