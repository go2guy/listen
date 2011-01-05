package com.interact.listen.android.voicemail.contact;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.R;
import com.interact.listen.android.voicemail.sync.Authority;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONObject;

public class ListenContacts
{
    private static final String TAG = Constants.TAG + "Contacts";
    
    private static final String[] PROJECTION = new String[] {RawContacts.Entity._ID,
                                                             RawContacts.DELETED,
                                                             RawContacts.SOURCE_ID,
                                                             RawContacts.Entity.DATA_ID,
                                                             RawContacts.Entity.MIMETYPE,
                                                             RawContacts.Entity.DATA1,
                                                             RawContacts.Entity.DATA2,
                                                             RawContacts.Entity.DATA3,
                                                             RawContacts.Entity.DATA15};

    private static final Uri RAW_UPDATE_URI;
    private static final Uri ENTITY_UPDATE_URI;
    private static final Uri ENTITY_SELECT_URI;
    
    static
    {
        final Uri.Builder rb = RawContacts.CONTENT_URI.buildUpon();
        RAW_UPDATE_URI = rb.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
        
        final Uri.Builder eb = Data.CONTENT_URI.buildUpon();
        eb.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true");
        ENTITY_UPDATE_URI = eb.build();
        
        ENTITY_SELECT_URI = RawContactsEntity.CONTENT_URI;
    }

    private static final String RAW_ID_SELECTION = RawContacts._ID + "=?";
    private static final String DATA_ID_SELECTION = Data._ID + "=?";
    private static final String ENTITY_SELECTION = RawContacts.ACCOUNT_TYPE + "=? AND " + RawContacts.ACCOUNT_NAME + "=?";

    private static final String PROFILE_MIME = "vnd.android.cursor.item/vnd.com.interact.listen.contacts.profile";
    
    private SortedMap<Long, ListenContact> contacts;
    
    public ListenContacts()
    {
        contacts = new TreeMap<Long, ListenContact>();
    }
    
    public Collection<ListenContact> getContacts()
    {
        return contacts.values();
    }
    
    public static void insertContactsSettings(ContentProviderClient client, String accountName) throws RemoteException
    {
        final boolean isDef = isContactsDisplaySettings(client, accountName);
        Log.i(TAG, "Contact Settings: " + isDef);
        
        if(!isDef)
        {
            ContentValues values = new ContentValues();
            values.put(Settings.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
            values.put(Settings.ACCOUNT_NAME, accountName);
            values.put(Settings.SHOULD_SYNC, 1);
            values.put(Settings.UNGROUPED_VISIBLE, 1);
            
            Uri res = client.insert(Settings.CONTENT_URI, values);
            Log.v(TAG, "inserted settings: " + res);
        }
    }

    private static boolean isContactsDisplaySettings(ContentProviderClient client, String accountName) throws RemoteException
    {
        final String selection = Settings.ACCOUNT_TYPE + "=? AND " + Settings.ACCOUNT_NAME + "=?";
        final String[] args = new String[] {Constants.ACCOUNT_TYPE, accountName};

        Cursor c = null;
        try
        {
            c = client.query(Settings.CONTENT_URI, new String[]{Settings.UNGROUPED_VISIBLE}, selection, args, null);
            return c.moveToFirst();
        }
        finally
        {
            if(c != null)
            {
                c.close();
            }
        }
    }

    public static void setContactsDisplayed(ContentResolver resolver, boolean visible)
    {
        ContentValues values = new ContentValues();
        values.put(Settings.UNGROUPED_VISIBLE, visible ? 1 : 0);
        
        final String selection = Settings.ACCOUNT_TYPE + "=?";
        final String[] args = new String[] {Constants.ACCOUNT_TYPE};

        int count = resolver.update(Settings.CONTENT_URI, values, selection, args);
        Log.v(TAG, "updated visible to " + visible + " in " + count + " accounts");
    }

    public static int deleteAll(ContentResolver resolver, String accountName)
    {
        String where;
        String[] args;
        if(accountName == null)
        {
            where = RawContacts.ACCOUNT_TYPE + "=?";
            args = new String[] {Constants.ACCOUNT_TYPE};
        }
        else
        {
            where = RawContacts.ACCOUNT_TYPE + "=? AND " + RawContacts.ACCOUNT_NAME + "=?";
            args = new String[] {Constants.ACCOUNT_TYPE, accountName};
        }
        
        final int deleted = resolver.delete(RAW_UPDATE_URI, where, args);
        Log.d(TAG, "deleted voicemails from account [" + accountName + "] = " + deleted);

        return deleted;
    }
    
    public static void insert(ListenContact server, BatchOperation ops)
    {
        Log.d(TAG, "insert " + server);
        
        final int rawIDIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(RAW_UPDATE_URI)
                 .withValue(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
                 .withValue(RawContacts.ACCOUNT_NAME, ops.getAccountName())
                 .withValue(RawContacts.SOURCE_ID, Long.toString(server.getSubscriberId()))
                 .build());

        ops.add(ContentProviderOperation.newInsert(ENTITY_UPDATE_URI)
                 .withValueBackReference(Entity.RAW_CONTACT_ID, rawIDIndex)
                 .withValue(Entity.MIMETYPE, PROFILE_MIME)
                 .withValue(Entity.DATA1, server.getSubscriberId())
                 .withValue(Entity.DATA2, ops.getString(R.string.contact_profile_action))
                 .withValue(Entity.DATA3, ops.getString(R.string.contact_view_profile))
                 .build());


        ops.add(ContentProviderOperation.newInsert(ENTITY_UPDATE_URI)
                 .withValueBackReference(Entity.RAW_CONTACT_ID, rawIDIndex)
                 .withValue(Entity.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                 .withValue(StructuredName.DISPLAY_NAME, server.getName())
                 .build());
        
        for(ContactAddress addr : server.getAddresses())
        {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ENTITY_UPDATE_URI);

            final String dialString = ops.getDialString(addr.getAddress());
            
            builder.withValueBackReference(Entity.RAW_CONTACT_ID, rawIDIndex)
                   .withValue(Entity.MIMETYPE, addr.getMIME())
                   .withValue(Entity.DATA1, dialString)
                   .withValue(Entity.DATA2, addr.getType());

            if(addr.isLabel())
            {
                builder.withValue(Entity.DATA3, addr.getLabel());
            }
            
            if(addr.isOfficeNumber())
            {
                Log.d(TAG, "office number " + addr.getAddress() + " '" + dialString + "'");
                builder.withValue(Entity.DATA15, addr.getAddress());
            }
            
            ops.add(builder.build());
        }
        
    }

    public static void remove(ListenContact local, BatchOperation ops)
    {
        Log.d(TAG, "remove " + local);
        ops.add(ContentProviderOperation.newDelete(RAW_UPDATE_URI)
                .withSelection(RAW_ID_SELECTION, idsToArgs(local.getRawID())).build());
    }

    public static void update(ListenContact local, ListenContact server, BatchOperation ops)
    {
        Log.d(TAG, "update " + server.getName() + " from " + local.getName());
        ops.add(ContentProviderOperation.newUpdate(ENTITY_UPDATE_URI)
                .withSelection(DATA_ID_SELECTION, idsToArgs(local.getNameDataID()))
                .withValue(Entity.DATA1, server.getName()).build());
    }

    public static void insert(ListenContact local, ContactAddress addr, BatchOperation ops)
    {
        Log.d(TAG, "insert " + addr);
        
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ENTITY_UPDATE_URI);

        final String dialString = ops.getDialString(addr.getAddress());

        builder.withValue(Entity.RAW_CONTACT_ID, local.getRawID())
               .withValue(Entity.MIMETYPE, addr.getMIME())
               .withValue(Entity.DATA1, dialString)
               .withValue(Entity.DATA2, addr.getType());

        if(addr.isLabel())
        {
            builder.withValue(Entity.DATA3, addr.getLabel());
        }

        if(addr.isOfficeNumber())
        {
            Log.d(TAG, "office number " + addr.getAddress() + " '" + dialString + "'");
            builder.withValue(Entity.DATA15, addr.getAddress());
        }
        
        ops.add(builder.build());
    }

    public static void remove(ListenContact local, ContactAddress address, BatchOperation ops)
    {
        Log.d(TAG, "remove " + address);
        ops.add(ContentProviderOperation.newDelete(ENTITY_UPDATE_URI)
                .withSelection(DATA_ID_SELECTION, idsToArgs(address.getDataID()))
                .build());
    }

    public static void update(ListenContact local, ContactAddress lAddr, ContactAddress sAddr, BatchOperation ops)
    {
        Log.d(TAG, "update " + lAddr + " to " + sAddr);
        
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(ENTITY_UPDATE_URI);

        final String dialString = ops.getDialString(sAddr.getAddress());

        builder.withSelection(DATA_ID_SELECTION, idsToArgs(lAddr.getDataID()))
               .withValue(Entity.MIMETYPE, sAddr.getMIME())
               .withValue(Entity.DATA1, dialString)
               .withValue(Entity.DATA2, sAddr.getType())
               .withValue(Entity.DATA3, sAddr.getLabel())
               .withValue(Entity.DATA15, sAddr.isOfficeNumber() ? sAddr.getAddress() : "");

        ops.add(builder.build());
    }
    
    private static final String UPDATE_OFFICE_WHERE;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append(RawContacts.ACCOUNT_TYPE).append("=? AND ");
        sb.append(Entity.DATA15).append(" NOT NULL AND ");
        sb.append(Entity.DATA15).append("!=? AND ");
        sb.append(Entity.MIMETYPE).append("=?");
        UPDATE_OFFICE_WHERE = sb.toString();
    }
    
    private static final String[] UPDATE_PROJECTION = new String[] {Entity._ID,
                                                                    Entity.DATA_ID,
                                                                    Entity.DATA1,
                                                                    Entity.DATA15};

    
    public static int updateOfficeNumber(Context context)
    {
        int num = 0;
        
        ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(Authority.CONTACTS.get());
        if(client == null)
        {
            throw new IllegalArgumentException("unable to get content provider client for " + Authority.CONTACTS.get());
        }
        try
        {
            BatchOperation ops = new BatchOperation(context, client, null);
            final String[] args = new String[]{Constants.ACCOUNT_TYPE, "", ContactMIME.PHONE.getMIME()};
            Cursor c = ops.getResolver().query(ENTITY_SELECT_URI, UPDATE_PROJECTION, UPDATE_OFFICE_WHERE, args, null);
            if(c == null)
            {
                Log.e(TAG, "null cursor returned");
                return num;
            }
            try
            {
                while(c.moveToNext())
                {
                    if(c.isNull(1))
                    {
                        continue;
                    }
                    long dataId = c.getLong(1);
                    String oldDial = c.getString(2);
                    String number = c.getString(3);
                    String newDial = ops.getDialString(number);
                    if(!TextUtils.equals(oldDial, newDial))
                    {
                        Log.d(TAG, "update office number from " + oldDial + " to " + newDial);
                        ops.add(ContentProviderOperation.newUpdate(ENTITY_UPDATE_URI)
                                .withSelection(DATA_ID_SELECTION, idsToArgs(dataId))
                                .withValue(Entity.DATA1, newDial).build());
                        ++num;
                    }
                    
                    if(ops.size() >= 50)
                    {
                        ops.execute();
                    }
                }
            }
            finally
            {
                c.close();
            }
            ops.execute();
        }
        catch(RemoteException e)
        {
            Log.e(TAG, "remote exception query for all office numbers", e);
        }
        finally
        {
            client.release();
        }
        return num;
    }
    
    public void add(ContentProviderClient provider, String accountName) throws RemoteException
    {
        Log.d(TAG, "finding contacts for " + accountName);
        
        final String[] args = new String[]{Constants.ACCOUNT_TYPE, accountName};
        Cursor c = provider.query(ENTITY_SELECT_URI, PROJECTION, ENTITY_SELECTION, args, null);
        if(c == null)
        {
            Log.e(TAG, "null cursor returned");
            return;
        }
        
        int total = 0;
        
        try
        {
            while(c.moveToNext())
            {
                ++total;
                long subscriberId = c.isNull(2) ? 0L : Long.parseLong(c.getString(2));
                if(subscriberId == 0)
                {
                    Log.w(TAG, "subscriber ID not set for contact");
                    continue;
                }
                
                ListenContact contact = getOrAdd(subscriberId, "");
                contact.setRawID(c.getLong(0));
                contact.setDeleted(c.isNull(1) ? false : c.getInt(1) != 0);
                
                if(!c.isNull(3))
                {
                    String mimeStr = c.getString(4);
                    String value = c.getString(8);
                    final boolean isExt = !TextUtils.isEmpty(value);
                    if(!isExt)
                    {
                        value = c.getString(5);
                        if(TextUtils.isEmpty(value))
                        {
                            Log.w(TAG, "value for contact data not set");
                            continue;
                        }
                    }
                    
                    long dataID = c.getLong(3);

                    if(StructuredName.CONTENT_ITEM_TYPE.equals(mimeStr))
                    {
                        contact.setName(dataID, value);
                        continue;
                    }

                    ContactMIME mime = ContactMIME.getMIME(mimeStr);
                    if(mime == null)
                    {
                        if(!PROFILE_MIME.equals(mimeStr))
                        {
                            Log.w(TAG, "unknown contact data MIME " + mimeStr);
                        }
                        continue;
                    }
                    
                    String label = c.isNull(7) ? "" : c.getString(7);
                    int dataType = c.getInt(6);
                    
                    ContactType type = ContactType.getContactType(mime, dataType, isExt, label);
                    if(type == null)
                    {
                        Log.w(TAG, "unknown contact type " + dataType + " [ext: " + isExt + " label: '" + label + "'] for " + mimeStr);
                        continue;
                    }
                    
                    ContactAddress addr = new ContactAddress(mime, value, type);
                    addr.setDataID(dataID);
                    contact.addAddress(addr);
                }
            }
        }
        finally
        {
            c.close();
        }
        
        Iterator<ListenContact> iter = contacts.values().iterator();
        while(iter.hasNext())
        {
            final ListenContact contact = iter.next();
            if(contact.getName().length() == 0)
            {
                Log.w(TAG, "contact with no name " + contact);
                iter.remove();
            }
            else
            {
                Log.d(TAG, "found " + contact);
            }
        }
        
        Log.d(TAG, "Returning " + contacts.size() + " contacts from " + total + " rows");
    }
    
    public boolean add(JSONObject json, ContactMIME mime)
    {
        return mime == ContactMIME.EMAIL ? addEmailAddress(json) : addPhoneNumber(json);
    }
    
    private boolean addEmailAddress(JSONObject json)
    {
        Long id = json.optLong("subscriberId");
        String name = json.optString("name");
        String address = json.optString("emailAddress");
        ContactType type = getContactType(json);

        if(id == 0L || name.length() == 0 || address.length() == 0)
        {
            return false;
        }
        
        ListenContact c = getOrAdd(id, name);
        return c.addAddress(new ContactAddress(ContactMIME.EMAIL, address, type));
    }
    
    private boolean addPhoneNumber(JSONObject json)
    {
        Long id = json.optLong("subscriberId");
        String name = json.optString("name");
        String number = json.optString("number");
        ContactType type = getContactType(json);

        if(id == 0L || name.length() == 0 || number.length() == 0)
        {
            return false;
        }

        ListenContact c = getOrAdd(id, name);
        return c.addAddress(new ContactAddress(ContactMIME.PHONE, number, type));
    }
    
    private ListenContact getOrAdd(Long id, String name)
    {
        ListenContact c = contacts.get(id);
        if(c == null)
        {
            c = new ListenContact(id, name);
            contacts.put(id, c);
        }
        return c;
    }

    private ContactType getContactType(JSONObject json)
    {
        ContactType type = ContactType.OTHER;
        try
        {
            type = ContactType.valueOf(json.optString("type", ContactType.OTHER.name()));
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "unable to parse contact type", e);
        }
        return type;
    }

    private static String[] idsToArgs(long... args)
    {
        String[] ret = new String[args.length];
        for(int i = 0; i < args.length; ++i)
        {
            ret[i] = Long.toString(args[i]);
        }
        return ret;
    }

}
