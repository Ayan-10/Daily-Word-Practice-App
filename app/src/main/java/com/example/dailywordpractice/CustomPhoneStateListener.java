package com.example.dailywordpractice;

import android.content.Context;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CustomPhoneStateListener extends PhoneStateListener {
    private final Context context;

    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
    }
    boolean mWasPlayingWhenCalled = false;

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);



        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                //when Idle i.e no call
                if( mWasPlayingWhenCalled )
                {
                    start(am);
                    mWasPlayingWhenCalled = false;
                }
                Log.d("htht", "onCallStateChanged: aa");
                Toast.makeText(context, "Phone state Idle", Toast.LENGTH_LONG).show();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //when Off hook i.e in call
                //Make intent and start your service here
                Log.d("htht", "onCallStateChanged: aw");
                Toast.makeText(context, "Phone state Off hook", Toast.LENGTH_LONG).show();
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //when Ringing
                if( mWasPlayingWhenCalled )
                {
                    stop(am);
                    mWasPlayingWhenCalled = true;
                }

                Log.d("htht", "onCallStateChanged: ay");
                Toast.makeText(context, "Phone state Ringing", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    public void  stop(AudioManager am){
        am.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
        am.setMode(AudioManager.MODE_IN_CALL);
        am.setStreamMute(AudioManager.STREAM_MUSIC, true);

    }
    public void  start(AudioManager am){
        am.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
        am.setMode(AudioManager.MODE_NORMAL);
        am.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }


}
