package com.example.appforblind;

import android.content.BroadcastReceiver;



import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver{
	final SmsManager sms = SmsManager.getDefault();
	String senderNum,message;
	WifiManager mainWifi;
	private String contactName = "";

	@Override
	public void onReceive(Context context, Intent intent) {  // wait for incomming msg
		
		final Bundle bundle = intent.getExtras();
		 
		try {
		     
		    if (bundle != null) { 
		         
		        final Object[] pdusObj = (Object[]) bundle.get("pdus");
		         
		        for (int i = 0; i < pdusObj.length; i++) {  
		             
		            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
		            String phoneNumber = currentMessage.getDisplayOriginatingAddress();
		             
		            senderNum = phoneNumber;
		            message = currentMessage.getDisplayMessageBody();
		            
		           
		        } // end for loop
		        //Toast.makeText(context, "SMS from "+makeCall(context, senderNum)+"Msg : "+message, Toast.LENGTH_LONG).show();
				Intent i1=new Intent(context,ReadIncomingSms.class);
				//i1.setClassName("com.example.appforblind","com.example.appforblind.Read");
			   // i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i1.putExtra("Name" ,  getContactName(senderNum,context));
				i1.putExtra("Body" , message);
				//context.getApplicationContext().startActivity(i1);
				context.startService(i1);
			
				
		      } // bundle is null
		        
		 
		} catch (Exception e) {  
		    //Log.e("SmsReceiver", "Exception smsReceiver" +e);
			e.printStackTrace();
		     Toast.makeText(context, "Exception ", Toast.LENGTH_SHORT).show();  
		}
	}
	
	 public String makeCall(Context ctx,String str){ //return name from contacts
	    	Cursor cursor = null;
	        try {
	            cursor = ctx.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
	          //  int contactIdIdx = cursor.getColumnIndex(Phone._ID);
	            int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
	            int phoneNumberIdx = cursor.getColumnIndex(Phone.NUMBER);
	         //   int photoIdIdx = cursor.getColumnIndex(Phone.PHOTO_ID);
	            cursor.moveToFirst();
	            do {
	               // String idContact = cursor.getString(contactIdIdx);
	                String name = cursor.getString(nameIdx);
	                String phoneNumber = cursor.getString(phoneNumberIdx);
	                
	                if(phoneNumber.equals(str)){
	                	return name;
	                }
	                
	                //...
	            } while (cursor.moveToNext());  
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (cursor != null) {
	                cursor.close();
	            }
	        }



	        return "0";
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

