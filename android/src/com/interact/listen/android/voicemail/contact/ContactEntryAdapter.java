package com.interact.listen.android.voicemail.contact;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.Data;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.interact.listen.android.voicemail.R;

import java.util.ArrayList;

public abstract class ContactEntryAdapter<E extends ContactEntryAdapter.Entry> extends BaseAdapter
{
    public enum ContactAction
    {
        PHONE(R.drawable.sym_action_call, -1, R.string.phoneContactHeader),
        PHONE_AND_SMS(R.drawable.sym_action_call, R.drawable.sym_action_sms, R.string.phoneContactHeader),
        EMAIL(R.drawable.sym_action_email, -1, R.string.emailContactHeader);
        
        private int primaryIcon;
        private int secondaryIcon;
        private int headerID;
        
        private ContactAction(int pID, int sID, int hID)
        {
            this.primaryIcon = pID;
            this.secondaryIcon = sID;
            this.headerID = hID;
        }
        
        public int getPrimaryIcon()
        {
            return primaryIcon;
        }
        public int getSecondaryIcon()
        {
            return secondaryIcon;
        }
        public boolean isSecondary()
        {
            return secondaryIcon > 0;
        }
        public String getHeader(Context context, String label)
        {
            return context.getString(headerID, label == null ? "" : label);
        }
        
        public static ContactAction getAction(String mime, ContactType type)
        {
            ContactMIME cm = ContactMIME.getMIME(mime);
            if(cm == ContactMIME.EMAIL)
            {
                return EMAIL;
            }
            if(cm == ContactMIME.PHONE)
            {
                if(type == null)
                {
                    return PHONE;
                }
                switch(type)
                {
                    case EXTENSION:
                    case VOICEMAIL:
                    case WORK:
                    case HOME:
                        return PHONE;
                    case MOBILE:
                    case OTHER:
                        return PHONE_AND_SMS;
                    default:
                        break;
                }
            }
            return null;
        }
    }

    protected ArrayList<ArrayList<E>> mSections;
    protected LayoutInflater mInflater;
    protected Context mContext;

    public static class Entry implements Parcelable
    {
        private ContactType type;
        private ContactAction action;
        private String label;
        private String data;
        private long id;
        private long rawID;
        private Uri uri;

        public ContactType getType()
        {
            return type;
        }

        public ContactAction getAction()
        {
            return action;
        }

        public String getLabel()
        {
            return label;
        }

        public String getData()
        {
            return data;
        }

        public long getId()
        {
            return id;
        }

        public long getRawID()
        {
            return rawID;
        }

        public Uri getUri()
        {
            return uri;
        }

        protected void fromValues(ContactType t, ContactAction a, String l, String d, long rID, long dID)
        {
            this.type = t;
            this.action = a;
            this.label = l;
            this.data = d;
            this.id = dID;
            this.rawID = rID;
            this.uri = ContentUris.withAppendedId(Data.CONTENT_URI, dID);
        }
        
        @Override
        public void writeToParcel(Parcel p, int flags)
        {
            p.writeInt(type == null ? -1 : type.ordinal());
            p.writeInt(action == null ? -1 : action.ordinal());
            p.writeString(label);
            p.writeString(data);
            p.writeParcelable(uri, 0);
            p.writeLong(id);
            p.writeLong(rawID);
        }

        protected void readFromParcel(Parcel p)
        {
            final ClassLoader loader = getClass().getClassLoader();
            final int tOrdinal = p.readInt();
            final int aOrdinal = p.readInt();
            type = tOrdinal == -1 ? null : ContactType.values()[tOrdinal];
            action = aOrdinal == -1 ? null : ContactAction.values()[aOrdinal];
            label = p.readString();
            data = p.readString();
            uri = p.readParcelable(loader);
            id = p.readLong();
            rawID = p.readLong();
        }

        @Override
        public int describeContents()
        {
            return 0;
        }
    }

    protected ContactEntryAdapter(Context context, ArrayList<ArrayList<E>> sections)
    {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSections = sections;
    }

    public final void setSections(ArrayList<ArrayList<E>> sections)
    {
        mSections = sections;
        notifyDataSetChanged();
    }

    public final int setSections(ArrayList<ArrayList<E>> sections, E entry)
    {
        mSections = sections;
        notifyDataSetChanged();

        int numSections = mSections.size();
        int position = 0;
        for(int i = 0; i < numSections; i++)
        {
            ArrayList<E> section = mSections.get(i);
            int sectionSize = section.size();
            for(int j = 0; j < sectionSize; j++)
            {
                E e = section.get(j);
                if(e.equals(entry))
                {
                    position += j;
                    return position;
                }
            }
            position += sectionSize;
        }
        return -1;
    }

    @Override
    public final int getCount()
    {
        return countEntries(mSections);
    }

    @Override
    public final boolean areAllItemsEnabled()
    {
        return true;
    }

    @Override
    public final boolean isEnabled(int position)
    {
        return true;
    }

    @Override
    public final Object getItem(int position)
    {
        return getEntry(mSections, position);
    }

    @Override
    public final long getItemId(int position)
    {
        Entry entry = getEntry(mSections, position);
        return entry == null ? -1 : entry.id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView == null ? newView(position, parent) : convertView;
        bindView(v, getEntry(mSections, position));
        return v;
    }

    protected abstract View newView(int position, ViewGroup parent);
    protected abstract void bindView(View view, E entry);
    
    public static final <T extends Entry> T getEntry(ArrayList<ArrayList<T>> sections, int position)
    {
        int pos = position;
        int numSections = sections.size();
        for(int i = 0; i < numSections; i++)
        {
            ArrayList<T> section = sections.get(i);
            if(pos < section.size())
            {
                return section.get(pos);
            }
            pos -= section.size();
        }
        return null;
    }

    public static <T extends Entry> int countEntries(ArrayList<ArrayList<T>> sections)
    {
        int count = 0;
        int numSections = sections.size();
        for(int i = 0; i < numSections; i++)
        {
            count += sections.get(i).size();
        }
        return count;
    }
}
