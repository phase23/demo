package com.szzcs.smartpos.update;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * 常驻 service， 检查固件更新， 并发送通知
 * 
 * @author szzcsandroid
 */
public class CheckUpdateService extends Service {
	public static final int FORESERVICE_PID = 1001;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT < 18) {
			startForeground(FORESERVICE_PID, new Notification());
		} else {
			Intent innerIntent = new Intent(this, AuxiliaryService.class);
			startService(innerIntent);
			Notification notification = new Notification();
			startForeground(FORESERVICE_PID, notification);
		}
		checkUpdate();
		return super.onStartCommand(intent, flags, startId);
	}

	private void checkUpdate() {
		new CheckUpdateTask().setContext(this).execute();
	}
}
