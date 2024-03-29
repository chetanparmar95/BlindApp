package com.example.appforblind;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    String phoneNumber = "";
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    static String[] msgArr = new String[5];
    static int no = -5;
    static int text = 0;
    static int contacts = 0;
    private TextToSpeech tts;

    private final String TAG = MainActivity.class.getSimpleName();
    private double lat=0.0,lng=0.0;

    static String name = "", number = "";
//    static String saveContact[] = new String[15];
//    String msgContent[] = {"i am busy", "not feeling well", "talk to you later", "come home right now", "lost my wallet"};
    private final int PERMISSION_REQUEST_CODE = 5000;
    private SessionManager sessionManager;
    private boolean helpDone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {  // initailze audio and content provider
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, this);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        sessionManager = new SessionManager(MainActivity.this);

//        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);



        if(!checkLocationPermission()){
            requestPermission();
        }else{
            GPSCurrentLocation currentLocation = new GPSCurrentLocation(MainActivity.this);
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "GPS isEnable");
            } else {
                tts.speak("Please turn on the GPS to get the location", TextToSpeech.QUEUE_FLUSH, null);
                currentLocation.showSettingsAlert();
                Log.d(TAG, "GPS isNotEnable");
            }
            boolean canGetLocation = currentLocation.canGetLocation();
            if(canGetLocation){
                lat = currentLocation.getLatitude();
                lng = currentLocation.getLongitude();
            }else{
                tts.speak("Not able to get location try again", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        checkContactPermisson();
    }


    private boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void checkContactPermisson() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 5005);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 5005);
                }
            }
        }
    }



    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {  // volume key press events
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(helpDone) {
                        promptSpeechInput();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(helpDone){
                        promptSpeechInput();
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() { //open  goggle speech input
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //get data and process from what user spoke
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    txtSpeechInput.setText(result.get(0));

                    tts.speak(result.get(0), TextToSpeech.QUEUE_FLUSH, null);


                    if (result.get(0).contains("add contact")) {
                        contacts = 2;
                        sessionManager.addContactCount(2);
                        Log.i("Contacts ", "value" + contacts);
                        Toast.makeText(this, "Command " + result.get(0), Toast.LENGTH_SHORT).show();

                    }
                    if (result.get(0).contains("name")) {
                        if(sessionManager.getCount() == 2){
                            if(result.get(0).length() > 5){
                                name = result.get(0).substring(5);
                                contacts = 3;
                                sessionManager.addContactCount(3);
                                Log.i("Contacts ", "name :" + name + "value" + contacts);
                                Toast.makeText(this, "Command " + name, Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            tts.speak("Please say add contact before name",TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                    if (!name.equals(" ") && result.get(0).contains("number")) {
                        if(sessionManager.getCount() == 3){
                            if(result.get(0).length() > 7){
                                number = result.get(0).substring(7);
                                number = number.trim();
                                contacts = 4;
                                sessionManager.addContactCount(4);

                                if (number.length() < 6) {
                                    contacts = 3;
                                    sessionManager.addContactCount(3);
                                    tts.speak("Please provide valid number",TextToSpeech.QUEUE_FLUSH,null);
                                }
                                Log.i("Contacts ", "number :" + number.trim() + "value" + contacts);
                                Toast.makeText(this, "Command " + number, Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            tts.speak("Please say name and users actual name before number",TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                    if (result.get(0).contains("save contact")) {
                        if(sessionManager.getCount() == 4){
                            if(!name.equals("") && !number.equals("")){
                                boolean saved = UserContact.addUserToContactList(MainActivity.this,name,number);
                                if(saved){
                                    sessionManager.addContactCount(0);
                                    tts.speak("Contact has been successfully added to your contact list",TextToSpeech.QUEUE_FLUSH,null);
                                }else{
                                    tts.speak("Sorry permission is not given to add contact",TextToSpeech.QUEUE_FLUSH,null);
                                }
                            }else{
                                tts.speak("Please provide name and number",TextToSpeech.QUEUE_FLUSH,null);
                            }
                        }else{
                            tts.speak("Please provide name and number",TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                    if (result.get(0).contains("call")) {
                        name = result.get(0).substring(5);

                        Intent intent = new Intent(Intent.ACTION_CALL);
                        Toast.makeText(this, UserContact.getContactByName(MainActivity.this, name), Toast.LENGTH_SHORT).show();
                        intent.setData(Uri.parse("tel:" + UserContact.getContactByName(MainActivity.this,name)));
//                        Log.i("Calling ", "name " + name + " number : " + makeCall(name));
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "Please give permission to make calls", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivity(intent);
                        name="";
                	
                    }
                
                    if(result.get(0).contains("message")){
                    // msgArr=result.get(0).toString().split(" ");
                     name=result.get(0).substring(8);

                     text=1;
                     Toast.makeText(this, "text "+name+"=========>"+text, Toast.LENGTH_SHORT).show();
                    }

                    if(text==1 && result.get(0).contains("content")){
                        number=result.get(0).substring(8);
                        //for(int i=0;i<5;i++){
                        //	if(msgContent[i].contains(number)){
                                //msgArr[2]=String.valueOf(i);
                        //		no=i;
                        Toast.makeText(this, "content "+number+"=NAME========>"+name, Toast.LENGTH_SHORT).show();
                        //	}
                        //}
                    }
                    if(result.get(0).contains("send") && !number.equals(" ")){
                        SmsManager sms=SmsManager.getDefault();
                        System.out.println("name"+name);
                        makeCall(name);
                        Toast.makeText(getApplicationContext(),
                                "Name : "+name+" Number : "+phoneNumber+" Content -> : "+number,
                                Toast.LENGTH_LONG).show();
                        System.out.println(phoneNumber);
                        sms.sendTextMessage(makeCall(name), null, number, null, null);

                        name=" ";
                        number=" ";
                        text=0;
                        //no=0;
                    }

                    if(result.get(0).equalsIgnoreCase("where am i")){
                        if(!checkLocationPermission()){
                            tts.speak("Permission is not given to this application to use location service", TextToSpeech.QUEUE_FLUSH, null);
                        }else {
                            getAddress(new GeocoderHandler());
                        }
                    }



                
            }
            break;
        }
            case PERMISSION_REQUEST_CODE:
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                break;
 
        }
    }


    private void getAddress(final Handler handler){
        if(lat != 0.0 && lng != 0.0) {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            String result = null;
            try {
                List<Address> addressList = geocoder.getFromLocation(
                        lat, lng, 1);
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i)).append("\n");
                    }
                    sb.append(address.getLocality()).append("\n");
                    sb.append(address.getPostalCode()).append("\n");
                    sb.append(address.getCountryName());
                    result = sb.toString();
                }
            } catch (IOException e) {
                Log.e(TAG, "Unable connect to Geocoder", e);
            } finally {
                Message message = Message.obtain();
                message.setTarget(handler);
                if (result != null) {
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    result = "Latitude: " + lat + " Longitude: " + lng +
                            "\n\nAddress:\n" + result;
                    bundle.putString("address", result);
                    message.setData(bundle);
                } else {
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    result = "Latitude: " + lat + " Longitude: " + lng +
                            "\n Unable to get address for this lat-long.";
                    bundle.putString("address", result);
                    message.setData(bundle);
                }
                message.sendToTarget();
            }
        }else{
            tts.speak("Not able to find location please try again",TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            Toast.makeText(MainActivity.this, locationAddress, Toast.LENGTH_SHORT).show();
            tts.speak(locationAddress, TextToSpeech.QUEUE_FLUSH, null);

        }
    }
    
    public String makeCall(String str){ //return phone number from contacts
    	Cursor cursor = null;
        try {
            cursor = getApplicationContext().getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
            int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
            int phoneNumberIdx = cursor.getColumnIndex(Phone.NUMBER);
            cursor.moveToFirst();
            do {
                String name = cursor.getString(nameIdx);
                 phoneNumber = cursor.getString(phoneNumberIdx);  
                if(name.toLowerCase().equals(str)){  
                	
                 return phoneNumber;
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
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
			 
	        int result = tts.setLanguage(Locale.US);

	        if (result == TextToSpeech.LANG_MISSING_DATA
	                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	            Log.e("TTS", "This Language is not supported");
	        } else {
	        	tts.speak("Welcome to Blind Application", TextToSpeech.QUEUE_FLUSH, null);

                //check previous session
                if(sessionManager.hasPreviousSession()){
                    helpDone = true;
                }else{
                    sessionManager.addSession();
                    help();
                }

	        }

	    } else {
	        Log.e("TTS", "Initilization Failed!");
	    }
	}
	@Override
	public void onDestroy() {
	    // Don't forget to shutdown tts!
	    if (tts != null) {
	        tts.stop();
	        tts.shutdown();
	    }
	    super.onDestroy();
	}


    public void help(){
        tts.speak("To Add new contact say add contact",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("then name and user name for example name manan",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("then number and users number for example number 9867 541 235",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("then say save contact",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("To call someone say call and user name for example call manan",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("to send message say message and user name for example message manan",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("then say content and message for example content how are you",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("then say send",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("to listen current location say where am i",TextToSpeech.QUEUE_FLUSH,null);
        waitThread();
        tts.speak("to listen this again say help",TextToSpeech.QUEUE_FLUSH,null);
    }

    public void waitThread(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
