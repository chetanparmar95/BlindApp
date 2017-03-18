package com.example.appforblind;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;


class UserContact {


    static boolean addUserToContactList(Context context, String contactName, String contactNumber){


        ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>();
        op_list.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                //.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());

        // first and last names
        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contactName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, "")
                .build());

        // Name
//        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//        .withValue(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, contactName).build());

        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, op_list);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    public static String getNameByContact(Context context,String contact){


        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[] {ContactsContract.Data._ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.LABEL}, ContactsContract.CommonDataKinds.Phone.NUMBER + "=?", new String[]{contact}, null);

        if(null != c){
            String data = "";
            while(c.moveToNext()){
                if(c.getString(3) != null){
                    data = c.getString(0)+" "+c.getString(1)+" "+c.getString(2)+" "+c.getString(3)+"\n";
                    Log.d("User Contacts", "getContactByName: "+data);
                    //position 0 - id
                    //position 1 - contact
                    //position 2 - type
                    //position 3 - label/name
                    return c.getString(3);
                }
            }
            c.close();
        }
        return null;
    }

    public static String getContactByName(Context context,String name){


        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[] {ContactsContract.Data._ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.Data.DISPLAY_NAME}, ContactsContract.Data.DISPLAY_NAME + "=?", new String[]{name}, null);

        if(null != c){
            String data = "";
            while(c.moveToNext()){
                if(c.getString(3) != null){
                    data = c.getString(0)+" "+c.getString(1)+" "+c.getString(2)+" "+c.getString(3)+"\n";
                    Log.d("User Contacts", "getContactByName: "+data);
                    //position 0 - id
                    //position 1 - contact
                    //position 2 - type
                    //position 3 - label/name
                    return c.getString(3);
                }
            }
            c.close();
        }
        return null;
    }

    public static boolean updateBuddyInContactList(Context context,String name,String newPhoneNumber){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";

        String[] params = new String[] {name,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, params)
                .withValue(ContactsContract.CommonDataKinds.Phone.DATA, newPhoneNumber)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

        public static boolean deleteBuddyFromContactList(Context context,String name){
            ContentResolver cr = context.getContentResolver();
            String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
            String[] params = new String[] {name};

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(where, params)
                    .build());
            try {
                cr.applyBatch(ContactsContract.AUTHORITY, ops);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    public static boolean contactExists(Context context, String name) {
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(name));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }


}
