package com.biao.readersex.activity;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.biao.readersex.R;
import com.biao.readersex.barcolor.SystemBarTintManager;
public class AboutActivity extends Activity {
	MenuItem item;


	@SuppressLint("SdCardPath")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
		}

		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.actionbar_color);
		ActionBar ab = getActionBar();
		setTitle("¹ØÓÚ");
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		try {
			ViewConfiguration mconfig = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);

				menuKeyField.setBoolean(mconfig, false);
			}
		} catch (Exception ex) {
		}
	}


	@SuppressWarnings("unused")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}
	
	public String getDeviceId() {
		String imei = "";
		Context ctx = getApplicationContext();
		if (ctx != null) {

			TelephonyManager telephonyManager = (TelephonyManager) ctx
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null)
				imei = telephonyManager.getDeviceId();

			if (TextUtils.isEmpty(imei))
				imei = "0";
		}
		return imei;

	}
}
