package com.szzcs.smartpos.update;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class DownloadUtils {
	private static final String TAG = DownloadUtils.class.getSimpleName();
	// 下载更新Apk 下载任务对应的Id
	public static long downloadApkId = -1;
	public static String downloadApkFilePath;

	/**
	 * 浏览器下载APK包
	 * 
	 * @param context
	 * @param url
	 */
	public static void downloadByWeb(Context context, String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 下载更新apk包
	 * 
	 * @param context
	 * @param url
	 */
	public static void downloadAutoInstall(Context context, String url, String appName, String serverVersionName) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		try {
			Uri uri = Uri.parse(url);
			DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(uri);
			// 在通知栏中显示
			request.setVisibleInDownloadsUi(true);
			request.setTitle(appName + "_" + serverVersionName);
			if (!UpdateUtils.showNotification)
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
			String filePath = null;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				//filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
				filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
			} else {
				Log.e(TAG, "没有SD卡");
				return;
			}
			downloadApkFilePath = filePath + File.separator + appName + "_" + serverVersionName + ".apk";
			Log.d(TAG, "filePath: " + downloadApkFilePath);
			// 若存在，则删除
			Log.d(TAG, "deleteFile: " + deleteFile(downloadApkFilePath));
			Uri fileUri = Uri.fromFile(new File(downloadApkFilePath));
			request.setDestinationUri(fileUri);
			downloadApkId = downloadManager.enqueue(request);
		} catch (Exception e) {
			e.printStackTrace();
			downloadByWeb(context, url);
		}
	}

	public static void download(Context context, String url, String appName, String serverVersionName) {

	}

	private static boolean deleteFile(String fileStr) {
		File file = new File(fileStr);
		return file.delete();
	}
}
