package com.szzcs.smartpos.update;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.szzcs.smartpos.MyApp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

public class CheckUpdateTask extends AsyncTask<Void, Void, String> {
	private static final int UPDATE_NOTIFICATION_ID = 101;
	public static final String EXTRA_UPDATE_URL = "fileUrl";

	private Context mContext;

	public CheckUpdateTask setContext(Context context) {
		mContext = context;
		return this;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

	}

	@Override
	protected String doInBackground(Void... params) {
		String[]  verq = new String[1];
		MyApp.sDriverManager.getBaseSysDevice().getBaseSdkVer(verq);
		String ver = verq[0];

		String[]  firm  = new String[1];
		MyApp.sDriverManager.getBaseSysDevice().getFirmwareVer(firm);
		String firmWareName = firm[0];

		String[]  pid1  = new String[1];
		MyApp.sDriverManager.getBaseSysDevice().getPid(pid1);
		String pid = pid1[0];
		if (ver == null || firmWareName == null) {
			return null;
		}
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		param.put("firmWareName", firmWareName.substring(0, firmWareName.indexOf("V")));// 型号
		param.put("firmVersion", ver.substring(0, 6));// 版本号
		param.put("pid",pid );// pid
		return HttpUtil.postHttpResponseText(Contants.BASE_URL + Contants.UPDATE_URL,new Gson().toJson(param));
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (TextUtils.isEmpty(result))
			return;
		Map res = new Gson().fromJson(result, Map.class);
		if (res == null) {
			return;
		}
		String state = String.valueOf(res.get("checkState"));
		if (state.equals("1") || state.equals("3")) {
			String fileUrl = String.valueOf(res.get("fileUrl"));
			/*Intent updateIntent = new Intent(mContext, UpdateFirmwareActivity.class);
			updateIntent.putExtra(EXTRA_UPDATE_URL, fileUrl);
			sendNotification(mContext, updateIntent, mContext.getResources().getString(R.string.update_notification),
					state, R.drawable.ic_launcher);*/
		}
	}


	private void sendNotification(Context context, Intent updateIntent, String title, String content, int iconId) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent updatePendingIntent = PendingIntent.getActivity(context, 0, updateIntent, 0);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(iconId).setContentTitle(title)
				.setContentText(content).setContentIntent(updatePendingIntent).setAutoCancel(true).build();
		notificationManager.notify(UPDATE_NOTIFICATION_ID, notification);
	}
}
