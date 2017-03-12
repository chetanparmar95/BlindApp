package com.example.appforblind;

import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;
import android.widget.Toast;

public class ReadIncomingSms extends Service { 

	   TextToSpeech ttobj;
	 
	   String name,body;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){  //initailze TTS
		
	      ttobj=new TextToSpeech(getApplicationContext(), 
	      new TextToSpeech.OnInitListener() {
	      @Override
	      public void onInit(int status) {
	         if(status != TextToSpeech.ERROR){
	             ttobj.setLanguage(Locale.UK);
	            }				
	         }
	      });
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) { //get name and data from message
		if(intent!=null){
			if(intent.hasExtra("call")){
				if(intent.getBooleanExtra("call",false)){
					name = intent.getStringExtra("name");
					speakCallerName();
				}
			}else{
			  name=intent.getStringExtra("Name");
			  body=intent.getStringExtra("Body");
			  Toast.makeText(getApplicationContext(), "Name : "+name, Toast.LENGTH_SHORT).show();
			  speakT();
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	private void speakCallerName(){
		Handler handler1 = new Handler();

		handler1.postDelayed(new Runnable() {
			public void run() {
				ttobj.playSilence(2000, TextToSpeech.QUEUE_ADD, null);
				ttobj.speak("You are having a call from "+ name ,TextToSpeech.QUEUE_FLUSH,null);
			}
		}, 500);
	}
	
	public void speakT(){   // read sms 
		 Handler handler1 = new Handler();

	        handler1.postDelayed(new Runnable() {
	            public void run() {
	              // ttobj.playSilence(1000, TextToSpeech.QUEUE_ADD, null);
	               
	              // ttobj.speak("SMS from "+name, TextToSpeech.QUEUE_ADD, null);
	     	       
	     	      // Toast.makeText(getApplicationContext(),"TTS Engine initialized & Ready  op : "+op,Toast.LENGTH_LONG).show();
	     	       
	     	       ttobj.playSilence(2000, TextToSpeech.QUEUE_ADD, null);
	     	       
	     	       ttobj.speak("SMS FROM "+ name +" SMS content are "+body,TextToSpeech.QUEUE_FLUSH,null);
	            }
	        }, 1000);
        
	   }
	
	@Override public void onDestroy(){
	      if(ttobj !=null){
	         ttobj.stop();
	         ttobj.shutdown();
	      }
	      super.onDestroy();
	}
	

}
