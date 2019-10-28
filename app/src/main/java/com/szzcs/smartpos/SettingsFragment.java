package com.szzcs.smartpos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.szzcs.smartpos.qr.QRTestActivity;
import com.szzcs.smartpos.update.Contants;
import com.szzcs.smartpos.update.HttpUtil;
import com.szzcs.smartpos.update.UpdateUtils;
import com.szzcs.smartpos.utils.DialogUtils;
import com.szzcs.smartpos.utils.SDK_Result;
import com.szzcs.smartpos.utils.SystemInfoUtils;
import com.zcs.sdk.Beeper;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.Led;
import com.zcs.sdk.LedLightModeEnum;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.Sys;
import com.zcs.sdk.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by yyzz on 2018/5/16.
 */

public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = "SettingsFragment";
    private DriverManager mDriverManager = MyApp.sDriverManager;
    private Sys mBaseSysDevice;
    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private Beeper mBeeper;
    private Led mLed;
    private boolean isflag = true;
    public static final int BEEP_FREQUENCE = 4000;
    public static final int BEEP_TIME = 600;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.pref_settings);
        mActivity = getActivity();

        initSdk();
    }

    String title = "测试";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LogUtils.setDebugEnable(true);
        findPreference(getString(R.string.key_os)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                title = getString(R.string.pref_os);
                switchFragment(SettingsFragment.this, new InfoFragment());
                return true;
            }
        });
        findPreference(getString(R.string.key_card)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                title = getString(R.string.pref_card);
                switchFragment(SettingsFragment.this, new CardFragment());
                return true;
            }
        });


        findPreference(getString(R.string.key_print)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                title = getString(R.string.pref_print);
                switchFragment(SettingsFragment.this, new PrintFragment());
                return true;
            }
        });

        findPreference(getString(R.string.key_pinPad)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                title = getString(R.string.pref_pinPad);
                switchFragment(SettingsFragment.this, new PinpadFragment());
                return true;
            }
        });


        findPreference(getString(R.string.key_scan)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                title = getString(R.string.pref_scan);
                startActivity(new Intent(getActivity(), QRTestActivity.class));

                return true;
            }
        });
        findPreference(getString(R.string.key_whole_engine_test)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //   title = getString(R.string.pref_scan);
                startActivity(new Intent(getActivity(), TestActivity.class));
                return true;
            }
        });
        findPreference(getString(R.string.key_update_app)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //   title = getString(R.string.pref_scan);
                checkUpdate();
                return true;
            }
        });


        // set beep
        findPreference(getString(R.string.key_beep)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mBeeper == null) {
                    mBeeper = mDriverManager.getBeeper();
                }

                if (isflag) {
                    isflag = false;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int beep = mBeeper.beep(BEEP_FREQUENCE, BEEP_TIME);
                            Log.e(TAG, "set beep:\t" + beep);
                            /*if (beep != SdkResult.SDK_OK) {
                                DialogUtils.show(getActivity(), SDK_Result.obtainMsg(getActivity(), beep));
                            }*/
                            try {
                                Thread.currentThread().sleep(300);//毫秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            isflag = true;
                        }
                    }).start();
                }

                return true;
            }
        });
        // set led light
        findPreference(getString(R.string.key_led)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                if (mLed == null)
                    mLed = mDriverManager.getLedDriver();
                ListPreference listPreference = (ListPreference) preference;
                final int index = listPreference.findIndexOfValue((String) newValue);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mLed.setLed(LedLightModeEnum.ALL, false);
                        if (index == 0) {
                            mLed.setLed(LedLightModeEnum.RED, true);

                        } else if (index == 1) {
                            mLed.setLed(LedLightModeEnum.GREEN, true);
                        } else if (index == 2) {
                            mLed.setLed(LedLightModeEnum.YELLOW, true);
                        } else if (index == 3) {
                            mLed.setLed(LedLightModeEnum.BLUE, true);
                        } else if (index == 4) {
                            mLed.setLed(LedLightModeEnum.ALL, true);
                        }

                    }
                }).start();
                return true;
            }
        });

        findPreference(getString(R.string.key_fingerprint)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(mActivity, FingerprintActivity.class));
                return true;
            }
        });
    }

    /**
     * 更新app
     */
    private void checkUpdate() {
        // String appName = UpdateUtils.getAppName(this);
        String appName = "smartpos";
        int versionCode = UpdateUtils.getversionCode(getActivity());
        String versionName = UpdateUtils.getversionName(getActivity());
        if (versionName.length() > 5) {
            versionName = versionName.substring(0, 5);
            Log.d("versionName", versionName + "");
        }
        versionName = "V" + versionName;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("appName", appName);
            jsonObject.put("appVersion", versionName);
            jsonObject.put("sysType", "Android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String json = jsonObject.toString();
        new Thread(new Runnable() {
            public void run() {
                String response = HttpUtil.postHttpResponseText(Contants.BASE_URL + Contants.UPDATE_APP_URL, json);
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if ("0".equals(jsonObject.get("checkState"))) {
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.what = 999;
                    msg.obj = response;
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    Toast.makeText(getActivity(), "Printing now,pls wait for a moment", Toast.LENGTH_LONG).show();
                    break;
                case 999:
                    update((String) msg.obj);
                    break;
                case 1001:
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    Toast.makeText(getActivity(), getString(R.string.init_success), Toast.LENGTH_SHORT).show();
                    break;
                case 1002:
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    Toast.makeText(getActivity(), getString(R.string.init_failed), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private void update(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            int code = Integer.parseInt(jsonObject.getString("checkState"));
            String fileUrl = jsonObject.getString("fileUrl");
            String fileDesc = jsonObject.getString("fileDesc");
            if (TextUtils.isEmpty(fileUrl)) {
                return;
            }
            fileUrl = Contants.BASE_URL + fileUrl;
            UpdateUtils updateUtils = UpdateUtils.from(getActivity()).checkBy(UpdateUtils.CHECK_BY_NO).updateInfo(fileDesc)
                    .apkPath(fileUrl);
            if (code == 1) {
                updateUtils.isForce(false).update();
            } else if (code == 3) {
                updateUtils.isForce(true).update();
            } else if (code == 2) {
                Log.d(TAG, "已经是最新版本");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initSdk() {
        // Config the SDK base info
        mBaseSysDevice = mDriverManager.getBaseSysDevice();
        mBaseSysDevice.showLog(getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.key_show_log), true));
        mProgressDialog = (ProgressDialog) DialogUtils.showProgress(mActivity, getString(R.string.title_waiting), getString(R.string.msg_init));
        new Thread(new Runnable() {
            @Override
            public void run() {
                int statue = mBaseSysDevice.getFirmwareVer(new String[1]);
                if (statue != SdkResult.SDK_OK) {
                    int sysPowerOn = mBaseSysDevice.sysPowerOn();
                    Log.e(TAG, "sysPowerOn: " + sysPowerOn);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                final int i = mBaseSysDevice.sdkInit();
                if (i == SdkResult.SDK_OK) {
                    setDeviceInfo();
                }
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog != null)
                                mProgressDialog.dismiss();
                            String initRes = (i == SdkResult.SDK_OK) ? getString(R.string.init_success) : SDK_Result.obtainMsg(mActivity, i);

                            Toast.makeText(getActivity(), initRes, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void setDeviceInfo() {
        //String[] pid = new String[1];
        //int getPid = mBaseSysDevice.getPid(pid);
        //if (getPid != SdkResult.SDK_OK) {
        //    return;
        //}
        Map<String, String> map = SystemInfoUtils.getImeiAndMeid(mActivity.getApplicationContext());
        String imei1 = map.get("imei1");
        String imei2 = map.get("imei2");
        String meid = map.get("meid");
        //String msg = "pid:" + pid[0] + "\t" + "imei1:" + imei1 + "\t" + "imei2:" + imei2 + "\t" + "meid:" + meid;
        String msg = "IMEI1:" + imei1 + "\t" + "IMEI2:" + imei2 + "\t" + "MEID:" + meid;
        Log.e(TAG, "DeviceInfo: " + msg);
        byte[] info = new byte[1000];
        byte[] infoLen = new byte[2];
        int getInfo = mBaseSysDevice.getDeviceInfo(info, infoLen);
        Log.e(TAG, "getDeviceInfo: " + getInfo);
        if (getInfo == SdkResult.SDK_OK) {
            int len = infoLen[0] * 256 + infoLen[1];
            byte[] newInfo = new byte[len];
            System.arraycopy(info, 0, newInfo, 0, len);
            Log.e(TAG, "getDeviceInfo: " + getInfo + "\t" + len + "\t" + new String(newInfo));
            if (!new String(newInfo).equals(msg)) {
                byte[] bytes = msg.getBytes();
                int setInfo = mBaseSysDevice.setDeviceInfo(bytes, bytes.length);
                Log.e(TAG, "setDeviceInfo: " + setInfo);
            }
        } else {
            byte[] bytes = msg.getBytes();
            int setInfo = mBaseSysDevice.setDeviceInfo(bytes, bytes.length);
            Log.e(TAG, "setDeviceInfo: " + setInfo);
        }
    }

    private void switchFragment(Fragment from, Fragment to) {
        if (!to.isAdded()) {
            getFragmentManager().beginTransaction().addToBackStack(null).hide(from).add(R.id.frame_container, to).commit();
        } else {
            getFragmentManager().beginTransaction().addToBackStack(null).hide(from).show(to).commit();
        }
        MainActivity mainactivity = (MainActivity) mActivity;
        mainactivity.setActionBarTitle(title);
    }
}
