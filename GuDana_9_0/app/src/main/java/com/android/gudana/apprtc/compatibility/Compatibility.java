package com.android.gudana.apprtc.compatibility;


import android.os.PowerManager;

//import org.linphone.mediastream.Version;

/**
 * @author Sylvain Berfini
 */
public class Compatibility {


	@SuppressWarnings("deprecation")
	public static boolean isScreenOn(PowerManager pm) {
		return  true;

	}

}
