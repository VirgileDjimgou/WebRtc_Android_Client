
package com.android.gudana.apprtc.linphone;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;

import java.io.FileInputStream;

import static android.media.AudioManager.STREAM_RING;

public class Ringing_Vibrate_Manager {

	private Context mServiceContext;
	private AudioManager mAudioManager;
	private PowerManager mPowerManager;
	private String basePath;
	private boolean mAudioFocused;
	private ConnectivityManager mConnectivityManager;


	public Ringing_Vibrate_Manager(final Context c) {
		//sExited = false;
		//echoTesterIsRunning = false;
		mServiceContext = c;
		basePath = c.getFilesDir().getAbsolutePath();

		//mPrefs = LinphonePreferences.instance();
		mAudioManager = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
		mVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
		mPowerManager = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
		mConnectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		//mR = c.getResources();
		//mPendingChatFileMessage = new ArrayList<LinphoneChatMessage>();
	}


	private MediaPlayer mRingerPlayer;
	private Vibrator mVibrator;
	private int savedMaxCallWhileGsmIncall;

	private boolean isRinging;

	private void requestAudioFocus(int stream){
		if (!mAudioFocused){
			int res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT );
			// android.util.Log.d()
			android.util.Log.d("log ", "Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
			if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused=true;
		}
	}


	public  synchronized void startRinging()  {


		try {
			if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) && mVibrator != null) {
				long[] patern = {0,1000,1000};
				mVibrator.vibrate(patern, 1);
			}
			if (mRingerPlayer == null) {
				requestAudioFocus(STREAM_RING);
				mRingerPlayer = new MediaPlayer();
				mRingerPlayer.setAudioStreamType(STREAM_RING);

				// hier konnte wir eine Problem haben ....
				String ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
				try {
					if (ringtone.startsWith("content://")) {
						mRingerPlayer.setDataSource(mServiceContext, Uri.parse(ringtone));

						// mRingerPlayer.setDataSource(mServiceContext, Uri.parse("android.resource://" + mServiceContext.getPackageName() + "/raw/librem_by_feandesign_call"));
						// Uri.parse("android.resource://" + mContext.getPackageName() + "/raw/librem_by_feandesign_call");
					} else {
						FileInputStream fis = new FileInputStream(ringtone);
						mRingerPlayer.setDataSource(fis.getFD());
						fis.close();
					}
				} catch (Exception e) {
					// Log.e(e, "Cannot set ringtone");
					e.printStackTrace();
				}

				mRingerPlayer.prepare();
				mRingerPlayer.setLooping(true);
				mRingerPlayer.start();
			} else {
				//Log.w("already ringing");
				android.util.Log.d("ringing", "already ringing");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Log.e(e,"cannot handle incoming call");
		}
		isRinging = true;
	}




	public synchronized void stopRinging() {
		try{

			if (mRingerPlayer != null) {
				mRingerPlayer.stop();
				mRingerPlayer.release();
				mRingerPlayer = null;
			}
			if (mVibrator != null) {
				mVibrator.cancel();
			}

			//if (Hacks.needGalaxySAudioHack())
		    //		mAudioManager.setMode(AudioManager.MODE_NORMAL);

			isRinging = false;

		}catch(Exception ex){
			ex.printStackTrace();
		}

	}




}
