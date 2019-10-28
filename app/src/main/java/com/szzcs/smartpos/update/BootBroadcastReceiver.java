package com.szzcs.smartpos.update;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * 监听系统开机启动广播，并定期启动检查固件更新的常驻Service
 * 
 * @author szzcsandroid
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	private static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	private static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	private static final String HARDWARE_PICTURE_ACTION = "android.hardware.action.NEW_PICTURE";
	private static final String CAMEAR_PICTURE_ACTION = "com.android.camera.NEW_PICTURE";
	private static final long ALARM_INTERVAL_MILLS = 4 * 60 * 60;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(BOOT_ACTION) || action.equals(CONNECTIVITY_ACTION) || action.equals(CAMEAR_PICTURE_ACTION)
				|| action.equals(HARDWARE_PICTURE_ACTION)) {
			Log.e("BootBroadcastReceiver", intent.getAction());
			// 启动Service
			Intent service = new Intent(context, CheckUpdateService.class);
			PendingIntent alarmSender = PendingIntent.getService(context, 0, service, 0);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(alarmSender);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
					ALARM_INTERVAL_MILLS, alarmSender);
		}
	}

}
