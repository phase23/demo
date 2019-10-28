package com.szzcs.smartpos.update;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuxiliaryService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startForeground(CheckUpdateService.FORESERVICE_PID, new Notification());
		stopSelf();
		stopForeground(true);
		return super.onStartCommand(intent, flags, startId);
	}
}
