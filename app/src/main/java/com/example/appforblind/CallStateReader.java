package com.example.appforblind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallStateReader extends BroadcastReceiver {


    static PhonecallStartEndDetector listener;
    String contactName =  "";
    Context context;

    public CallStateReader() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (listener == null) {
            listener = new PhonecallStartEndDetector();

        }

        TelephonyManager telephony = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    // Deals with actual events
    public class PhonecallStartEndDetector extends PhoneStateListener {
        int lastState = TelephonyManager.CALL_STATE_IDLE;
        boolean isIncoming;
        String savedNumber; // because the passed incoming is only valid in ringing

        public PhonecallStartEndDetector() {
        }


        // Incoming call- goes from IDLE to RINGING when it rings, to OFFHOOK
        // when it's answered, to IDLE when its hung up
        // Outgoing call- goes from IDLE to OFFHOOK when it dials out, to IDLE
        // when hung up
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            // state is changed

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //an incoming call has been started

                    isIncoming = true;
                    savedNumber = incomingNumber;
                    contactName = getContactName(incomingNumber,CallStateReader.this.context);

                    Intent i1=new Intent(context,ReadIncomingSms.class);
                    //i1.setClassName("com.example.appforblind","com.example.appforblind.Read");
                    // i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i1.putExtra("name" ,  contactName);
                    i1.putExtra("call" , true);
                    //context.getApplicationContext().startActivity(i1);
                    context.startService(i1);

                    break;
            }

        }
    }

    private String getContactName(String number, Context context) {

        // // define the columns I want the query to return
        String[] projection = new String[] {
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.HAS_PHONE_NUMBER };

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        // query time
        Cursor cursor = context.getContentResolver().query(contactUri,
                projection, null, null, null);
        // querying all contacts = Cursor cursor =
        // context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
        // projection, null, null, null);

        assert cursor != null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();
        if(contactName.equalsIgnoreCase("")){
            return number;
        }else{
            String temp = contactName;
            contactName = "";
            return temp;
        }

    }
}