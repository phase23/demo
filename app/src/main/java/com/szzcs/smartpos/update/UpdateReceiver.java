package com.szzcs.smartpos.update;

import java.io.File;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// 处理下载完成
		Cursor c = null;

		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
			if (DownloadUtils.downloadApkId >= 0) {
				long downloadId = DownloadUtils.downloadApkId;
				DownloadManager.Query query = new DownloadManager.Query();
				query.setFilterById(downloadId);
				DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				c = downloadManager.query(query);
				if (c.moveToFirst()) {
					int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
					if (status == DownloadManager.STATUS_FAILED) {
						downloadManager.remove(downloadId);
					} else if (status == DownloadManager.STATUS_SUCCESSFUL) {
						if (DownloadUtils.downloadApkFilePath != null) {
							Intent i = new Intent(Intent.ACTION_VIEW);
							File apkFile = new File(DownloadUtils.downloadApkFilePath);
							i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(i);
						}
					}
				}
				c.close();
			}
		}
	}
}
