package com.szzcs.smartpos.update;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.szzcs.smartpos.R;

public class UpdateUtils {
	private final String TAG = UpdateUtils.class.getSimpleName();
	public static final int CHECK_BY_VERSION_NAME = 1001;
	public static final int CHECK_BY_VERSION_CODE = 1002;
	public static final int CHECK_BY_NO = 1003;
	public static final int DOWNLOAD_BY_APP = 1004;
	public static final int DOWNLOAD_BY_BROWSER = 1005;

	private Activity mActivity;
	private int checkBy = CHECK_BY_VERSION_CODE;
	private int downloadBy = DOWNLOAD_BY_APP;
	private int serverVersionCode = 0;
	private int localVersionCode = 0;
	private String apkPath = "";
	private String appName = "";
	private String serverVersionName = "";
	private String localVersionName = "";
	private String updateInfo = "";
	private boolean isForce = false; // 是否强制更新
	private boolean isAutoInstall = true; // 是否自动安装
	public static boolean showNotification = true;
	private final String NO_WIFI;
	private final String BTN_CANCEL;
	private final String BTN_UPDATE;
	private final String BTN_POSITIVE;
	private final String FOREC_UPDATE_TITLE;
	private final String FOREC_UPDATE_MSG;
	private final String UPDATE_INFO;
	private String title;

	public ProgressDialog mProgressDialog;

	private UpdateUtils(Activity activity) {
		this.mActivity = activity;
		NO_WIFI = activity.getResources().getString(R.string.no_wifi);
		BTN_CANCEL = activity.getResources().getString(R.string.btn_cancel);
		BTN_UPDATE = activity.getResources().getString(R.string.btn_upadte);
		BTN_POSITIVE = activity.getResources().getString(R.string.btn_positive);
		FOREC_UPDATE_TITLE = activity.getResources().getString(R.string.force_update_title);
		FOREC_UPDATE_MSG = activity.getResources().getString(R.string.force_update_msg);
		UPDATE_INFO = activity.getResources().getString(R.string.update_info);
		title = activity.getResources().getString(R.string.update_title);
		init(activity);
	}

	private void init(Context ctx) {
		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			int labelRes = info.applicationInfo.labelRes;
			appName = ctx.getResources().getString(labelRes);
			localVersionName = info.versionName; // 版本名
			localVersionCode = info.versionCode; // 版本号
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static UpdateUtils from(Activity activity) {
		return new UpdateUtils(activity);
	}

	public UpdateUtils checkBy(int checkBy) {
		this.checkBy = checkBy;
		return this;
	}

	public UpdateUtils apkPath(String apkPath) {
		this.apkPath = apkPath;
		return this;
	}

	public UpdateUtils downloadBy(int downloadBy) {
		this.downloadBy = downloadBy;
		return this;
	}

	public UpdateUtils showNotification(boolean showNotification) {
		UpdateUtils.showNotification = showNotification;
		return this;
	}

	public UpdateUtils updateTitle(String title) {
		this.title = title;
		return this;
	}

	public UpdateUtils updateInfo(String updateInfo) {
		this.updateInfo = updateInfo;
		return this;
	}

	public UpdateUtils serverVersionCode(int serverVersionCode) {
		this.serverVersionCode = serverVersionCode;
		return this;
	}

	public UpdateUtils serverVersionName(String serverVersionName) {
		this.serverVersionName = serverVersionName;
		return this;
	}

	public UpdateUtils isForce(boolean isForce) {
		this.isForce = isForce;
		return this;
	}

	public String getAppName() {
		return appName;
	}

	public String getVersionName() {
		return localVersionName;
	}

	public int getVersionCoe() {
		return localVersionCode;
	}

	public static String getAppName(Context ctx) {
		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			int labelRes = info.applicationInfo.labelRes;
			return ctx.getResources().getString(labelRes);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getversionName(Context ctx) {
		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			return info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getversionCode(Context ctx) {
		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			return info.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return 1;
	}

	public void update() {

		switch (checkBy) {
		case CHECK_BY_VERSION_CODE:
			if (serverVersionCode > localVersionCode) {
				realUpdate();
			} else {
				Log.d(TAG, "当前版本是最新版本" + serverVersionCode + "/" + serverVersionName);
			}
			break;
		case CHECK_BY_VERSION_NAME:
			if (!serverVersionName.equals(localVersionName)) {
				realUpdate();
			} else {
				Log.d(TAG, "当前版本是最新版本" + serverVersionCode + "/" + serverVersionName);
			}
		case CHECK_BY_NO:
			realUpdate();
			break;
		default:
			break;
		}

	}

	private void realUpdate() {
		String content = UPDATE_INFO;
		if (!TextUtils.isEmpty(updateInfo)) {
			content = content + "\n" + updateInfo;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity).setTitle(title).setMessage(content)
				.setPositiveButton(BTN_UPDATE, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (downloadBy == DOWNLOAD_BY_APP) {
							if (isWifiConnected(mActivity)) {
								download();
							} else {
								AlertDialog wifiDialog = new AlertDialog.Builder(mActivity).setMessage(NO_WIFI)
										.setPositiveButton(BTN_POSITIVE, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												download();
											}
										}).setNegativeButton(BTN_CANCEL, new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {

											}
										}).create();
								wifiDialog.setCancelable(false);
								wifiDialog.show();
							}
						} else if (downloadBy == DOWNLOAD_BY_BROWSER) {
							DownloadUtils.downloadByWeb(mActivity, apkPath);
						}
					}
				}).setNegativeButton(BTN_CANCEL, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();

	}


	private void download() {
		if (isForce) {
			mProgressDialog = ProgressDialog.show(mActivity, FOREC_UPDATE_TITLE, FOREC_UPDATE_MSG, false, false);
		}
		if (isAutoInstall) {
			DownloadUtils.downloadAutoInstall(mActivity, apkPath, appName, serverVersionName);
		} else {
			DownloadUtils.download(mActivity, apkPath, appName, serverVersionName);
		}
	}

	/**
	 * 检测wifi是否连接
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				return true;
			}
		}
		return false;
	}
}
