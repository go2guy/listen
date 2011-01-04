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
import java.util.Arrays;
import java.util.List;

public final class VoicemailHelper
{
    private static final String TAG = Constants.TAG + "Helper";

    public static final long OLD_VOICEMAIL        = 7 * 24 * 3600000; // week ago
    public static final long STALE_DOWNLOAD       =      4 * 3600000; // 4 hours
    public static final long FORCE_RETRY_DOWNLOAD =            60000; // 1 minute
    public static final long SYNC_RETRY_DOWNLOAD  = 1 * 24 * 3600000; // once a day
    public static final long OLD_DOWNLOAD         = 1 * 24 * 3600000; // day old

    // don't move _ID or LEFT_BY in list
    private static final String[] VOICEMAIL_LIST_PROJECTION =
        new String[]{Voicemails._ID, Voicemails.LEFT_BY, Voicemails.LEFT_BY_NAME, Voicemails.DATE_CREATED, Voicemails.TRANSCRIPT,
                     Voicemails.IS_NEW, Voicemails.HAS_NOTIFIED};

    public static final int VOICEMAIL_LIST_PROJECT_IS_NEW = 5;
    public static final int VOICEMAIL_LIST_PROJECT_NOTIFIED = 6;
    
    private static final String LIST_VOICEMAILS_VIEW_ORDER;
    private static final String LIST_VOICEMAILS_CONTROL_ORDER;

    static
    {
        StringBuffer viewSB = new StringBuffer();
        viewSB.append(Voicemails.USER_NAME);
        viewSB.append(',').append(Voicemails.DATE_CREATED).append(" DESC");
        viewSB.append(',').append(Voicemails.VOICEMAIL_ID);
        LIST_VOICEMAILS_VIEW_ORDER = viewSB.toString();
        
        StringBuffer syncSB = new StringBuffer().append(Voicemails.USER_NAME).append(',').append(Voicemails.VOICEMAIL_ID);
        LIST_VOICEMAILS_CONTROL_ORDER = syncSB.toString();
    }

    public static Cursor getVoicemailListInfoCursor(ContentResolver resolver)
    {
        String selection = Voicemails.LABEL + "!=?";
        String[] args = new String[]{Voicemail.Label.TRASH.name()};
        return resolver.query(Voicemails.CONTENT_URI, VOICEMAIL_LIST_PROJECTION, selection, args, LIST_VOICEMAILS_VIEW_ORDER);
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

    public static List<Voicemail> getUpdatedVoicemails(ContentProviderClient resolver, String userName)
        throws RemoteException
    {
        List<String> selectionList = new ArrayList<String>();
        String selection = listVoicemailsSelection(userName, 0, true, selectionList);
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

    public static int insertVoicemails(ContentProviderClient resolver, List<Voicemail> voicemails) throws RemoteException
    {
        if(voicemails == null || voicemails.size() == 0)
        {
            return 0;
        }
        ContentValues[] values = new ContentValues[voicemails.size()];
        int i = 0;
        for (Voicemail vm : voicemails)
        {
            values[i++] = vm.getInsertValues();
        }
        Log.i(TAG, "bulk inserting " + values.length + " voicemails");
        return resolver.bulkInsert(Voicemails.CONTENT_URI, values);
    }

    public static void deleteVoicemail(ContentProviderClient resolver, Voicemail voicemail) throws RemoteException
    {
        voicemail.setTrashed();
        resolver.delete(voicemail.getUri(), null, null);
    }

    public static int deleteVoicemails(ContentProviderClient resolver, List<Voicemail> voicemails) throws RemoteException
    {
        if(voicemails.isEmpty())
        {
            return 0;
        }
        
        final int deleteMax = Math.min(voicemails.size(), 25);
        final String orId = " OR " + Voicemails._ID + "=?";
        
        StringBuilder where = new StringBuilder(Voicemails._ID + "=?");
        
        for(int i = 1; i < deleteMax; ++i)
        {
            where.append(orId);
        }
        String selection = where.toString();
        
        String[] args = new String[deleteMax];
        Arrays.fill(args, "-1");
        
        int deleted = 0;
        
        int idx = 0;
        for(Voicemail voicemail : voicemails)
        {
            voicemail.setTrashed();
            args[idx++] = Integer.toString(voicemail.getId());
            if(idx >= deleteMax)
            {
                deleted += resolver.delete(Voicemails.CONTENT_URI, selection, args);
                Arrays.fill(args, "-1");
                idx = 0;
            }
        }
        if(idx > 0)
        {
            deleted += resolver.delete(Voicemails.CONTENT_URI, selection, args);
        }
        
        Log.i(TAG, "batch deleted " + deleted + " out of " + voicemails.size());
        
        return deleted;
    }

    public static Voicemail getVoicemailFromServerID(ContentProviderClient resolver, Voicemail vm) throws RemoteException
    {
        final String selection = Voicemails.VOICEMAIL_ID + "=? AND " + Voicemails.USER_NAME + "=?";
        final String[] args = new String[] {Long.toString(vm.getVoicemailId()), vm.getUserName()};
        
        Cursor c = resolver.query(vm.getUri(), Voicemail.ALL_PROJECTION, selection, args, null);
        try
        {
            return c.moveToFirst() ? Voicemail.create(c, Voicemail.ALL_PROJECTION) : null;
        }
        finally
        {
            if(c != null)
            {
                c.close();
            }
        }
    }

    public static Voicemail getVoicemailFromServerID(ContentResolver resolver, Voicemail vm)
    {
        final String selection = Voicemails.VOICEMAIL_ID + "=? AND " + Voicemails.USER_NAME + "=?";
        final String[] args = new String[] {Long.toString(vm.getVoicemailId()), vm.getUserName()};
        
        Cursor c = resolver.query(vm.getUri(), Voicemail.ALL_PROJECTION, selection, args, null);
        try
        {
            return Voicemail.create(c, Voicemail.ALL_PROJECTION);
        }
        finally
        {
            if(c != null)
            {
                c.close();
            }
        }
    }

    public static void moveVoicemailToTrash(ContentResolver resolver, Voicemail voicemail)
    {
        ContentValues values = voicemail.markDeleted();
        resolver.update(voicemail.getUri(), values, null, null);
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

    public static int setVoicemailsNotified(ContentResolver resolver, int[] ids)
    {
        int updates = 0;
        ContentValues values = new ContentValues();
        values.put(Voicemails.HAS_NOTIFIED, true);
        if(ids == null)
        {
            updates = resolver.update(Voicemails.CONTENT_URI, values, Voicemails.HAS_NOTIFIED + "=0", null);
        }
        else
        {
            for (int id : ids)
            {
                updates += resolver.update(getVoicemailUri(id), values, null, null);
            }
        }
        return updates;
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

    public static int deleteVoicemails(ContentResolver resolver, String userName)
    {
        int updates = 0;
        if(userName == null)
        {
            updates = resolver.delete(Voicemails.CONTENT_URI, null, null);
            Log.i(TAG, "cleared out voicemails in database: " + updates);
        }
        else
        {
            updates = resolver.delete(Voicemails.CONTENT_URI, Voicemails.USER_NAME + "=?", new String[]{userName});
            Log.i(TAG, "cleared out voicemails for " + userName + ": " + updates);
        }
        return updates;
    }
    
    public static int deleteOldAudio(ContentProviderClient resolver, String userName) throws RemoteException
    {
        final long now = System.currentTimeMillis();
        final long oldVoicemail  = now - OLD_VOICEMAIL;
        final long oldDownload   = now - OLD_DOWNLOAD;
        final long staleDownload = now - STALE_DOWNLOAD;

        // clear old downloads from old voicemails and stuff that appears stuck in downloading

        ContentValues values = Voicemail.getClearedDownloadValues();
        
        List<String> selectionArgs = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();

        if(userName != null)
        {
            sb.append(Voicemails.USER_NAME).append("=? AND (");
            selectionArgs.add(userName);
        }

        sb.append('(');
        sb.append(Voicemails.AUDIO_STATE).append("=? AND ");
        sb.append(Voicemails.DATE_CREATED).append("<? AND ");
        sb.append(Voicemails.AUDIO_DATE).append("<?) OR (");
        sb.append(Voicemails.AUDIO_STATE).append("=? AND ");
        sb.append(Voicemails.AUDIO_DATE).append("<?)");

        if(userName != null)
        {
            sb.append(')');
        }
        
        selectionArgs.add(Integer.toString(Voicemail.getAudioDownloadedState()));
        selectionArgs.add(Long.toString(oldVoicemail));
        selectionArgs.add(Long.toString(oldDownload));
        selectionArgs.add(Integer.toString(Voicemail.getAudioDownloadingState()));
        selectionArgs.add(Long.toString(staleDownload));

        int updates = resolver.update(Voicemails.CONTENT_URI, values, sb.toString(), selectionArgs.toArray(new String[0]));
        Log.d(TAG, "Audio clean updates: " + updates);
        
        return updates;
    }

    public static boolean shouldAttemptDownload(Voicemail v, boolean force)
    {
        long now = System.currentTimeMillis();
        if(force)
        {
            return v.needsDownload(now, FORCE_RETRY_DOWNLOAD, STALE_DOWNLOAD);
        }
        return v.needsDownload(now, SYNC_RETRY_DOWNLOAD, STALE_DOWNLOAD) && !v.isOlderThan(now - OLD_VOICEMAIL);
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
    
    private VoicemailHelper()
    {
    }

}
