package com.szzcs.smartpos;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.Log;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.szzcs.smartpos.utils.DialogUtils;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.Printer;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.print.PrnAlignTypeEnum;
import com.zcs.sdk.print.PrnFontSizeTypeEnum;
import com.zcs.sdk.print.PrnSpeedTypeEnum;
import com.zcs.sdk.print.PrnStrFormat;
import com.zcs.sdk.print.PrnTextFont;
import com.zcs.sdk.print.PrnTextStyle;
import com.zcs.sdk.util.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yyzz on 2018/5/25.
 */

public class PrintFragment extends PreferenceFragment {
    private static final String TAG = "PrintFragment";
    private DriverManager mDriverManager = MyApp.sDriverManager;
    private Printer mPrinter;
    private boolean mPrintStatus = false;
    private Bitmap mBitmapDef;

    public static final String PRINT_TEXT = "本智能POS机带打印机，基于android 平台应用，整合昂贵的ECR、收银系统，伴随新型扫码支付的需求也日益突出，大屏智能安卓打印机设备，内置商户的营销管理APP，在商品管理的同时，受理客户订单支付，很好的满足了以上需求；同时便携式的要求，随着快递实名制的推行，运用在快递行业快速扫条码进件。做工精良，品质优良，是市场的最佳选择。";
    public static final String QR_TEXT = "https://www.baidu.com";
    public static final String BAR_TEXT = "6922711079066";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_print);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(Environment.getExternalStorageDirectory() + "/fonts/simsun.ttf");
                    if (file.exists()) {
                    } else {
                        AssetManager mAssetManger = getActivity().getAssets();
                        // String[] fileNames = mAssetManger.list("fonts");// 获取assets目录下的所有文件及有文件的目录名
                        InputStream in = mAssetManger.open("fonts/simsun.ttf");
                        saveFile(in, "simsun.ttf");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    File file = new File(Environment.getExternalStorageDirectory() + "/fonts/heiti.ttf");
                    if (file.exists()) {
                    } else {
                        AssetManager mAssetManger = getActivity().getAssets();
                        // String[] fileNames = mAssetManger.list("fonts");// 获取assets目录下的所有文件及有文件的目录名
                        InputStream in = mAssetManger.open("fonts/heiti.ttf");
                        saveFile(in, "heiti.ttf");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {

                    File file = new File(Environment.getExternalStorageDirectory() + "/fonts/fangzhengyouyuan.ttf");
                    if (file.exists()) {
                    } else {
                        AssetManager mAssetManger = getActivity().getAssets();
                        // String[] fileNames = mAssetManger.list("fonts");// 获取assets目录下的所有文件及有文件的目录名
                        InputStream in = mAssetManger.open("fonts/fangzhengyouyuan.ttf");
                        saveFile(in, "fangzhengyouyuan.ttf");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    int fontsStyle = 0;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDriverManager = MyApp.sDriverManager;
        mPrinter = mDriverManager.getPrinter();
        int printerStatus = mPrinter.getPrinterStatus();
        Log.d(TAG, "getPrinterStatus: " + printerStatus);
        if (printerStatus != SdkResult.SDK_OK) {
            mPrintStatus = true;
        } else {
            mPrintStatus = false;
        }

        findPreference(getString(R.string.key_paper_out)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                printPaperOut();
                return true;
            }
        });

        findPreference(getString(R.string.key_print_text)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {

                ListPreference listPreference = (ListPreference) preference;
                final int index = listPreference.findIndexOfValue((String) newValue);
                final CharSequence[] entries = listPreference.getEntries();
                if (entries[index].equals("宋体") || entries[index].equals("Song Typeface")) {
                    LogUtils.error("打印宋体");
                    fontsStyle = 0;
                    try {
                        File file = new File(Environment.getExternalStorageDirectory() + "/fonts/simsun.ttf");
                        if (file.exists()) {
                        } else {
                            AssetManager mAssetManger = getActivity().getAssets();
                            // String[] fileNames = mAssetManger.list("fonts");// 获取assets目录下的所有文件及有文件的目录名
                            InputStream in = mAssetManger.open("fonts/simsun.ttf");
                            saveFile(in, "simsun.ttf");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (entries[index].equals("默认字体") || entries[index].equals("Default Typeface")) {
                    LogUtils.error("打印默认字体");
                    fontsStyle = 1;

                   /* try {
                        File file = new File(Environment.getExternalStorageDirectory()+"/fonts/heiti.ttf");
                        if (file.exists()){
                        }else {
                            AssetManager mAssetManger = getActivity().getAssets();
                            // String[] fileNames = mAssetManger.list("fonts");// 获取assets目录下的所有文件及有文件的目录名
                            InputStream in = mAssetManger.open("fonts/heiti.ttf");
                            saveFile(in, "heiti.ttf");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                } else if (entries[index].equals("幼圆体") || entries[index].equals("Rounded Fonts")) {
                    LogUtils.error("打印圆幼体");
                    fontsStyle = 2;
                    try {

                        File file = new File(Environment.getExternalStorageDirectory() + "/fonts/fangzhengyouyuan.ttf");
                        if (file.exists()) {
                        } else {
                            AssetManager mAssetManger = getActivity().getAssets();
                            // String[] fileNames = mAssetManger.list("fonts");// 获取assets目录下的所有文件及有文件的目录名
                            InputStream in = mAssetManger.open("fonts/fangzhengyouyuan.ttf");
                            saveFile(in, "fangzhengyouyuan.ttf");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                printMatrixText();
                return true;
            }
        });


  /*      findPreference(getString(R.string.key_print_matrix_text)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                printMatrixText();
                return true;
            }
        });*/
        findPreference(getString(R.string.key_print_pic)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                printPic();
                return true;
            }
        });
        findPreference(getString(R.string.key_print_qr)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                printQr();
                return true;
            }
        });
        findPreference(getString(R.string.key_print_bar)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                printBar();
                return true;
            }
        });
       /* findPreference(getString(R.string.key_print_hybrid)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                printHybird();
                return true;
            }
        });*/
    }

    /**
     * paper out
     */
    private void printPaperOut() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                        }
                    });
                } else {
                    mPrinter.setPrintLine(10);
                }


                //  mPrinter.setPrintStart();
            }
        }).start();
    }

    private void printText() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                        }
                    });
                } else {

                    mPrinter.setPrintSpeed(PrnSpeedTypeEnum.HIGH_SPEED);
                    mPrinter.setPrintFontSize(PrnFontSizeTypeEnum.DEFAULT_SIZE);
                    mPrinter.setPrintAlign(PrnAlignTypeEnum.ALIGN_LEFT);
                    mPrinter.setPrintString(PRINT_TEXT.getBytes());
                    mPrinter.setPrintStart();
                }
            }
        }).start();
    }

    private void printMatrixText() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AssetManager asm = getActivity().getAssets();
                InputStream inputStream = null;
                try {
                    inputStream = asm.open("china_unin.bmp");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Drawable d = Drawable.createFromStream(inputStream, null);
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();

                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                        }
                    });
                } else {
                    mPrinter.setPrintAppendBitmap(bitmap, Layout.Alignment.ALIGN_CENTER);
                    PrnStrFormat format = new PrnStrFormat();
                    format.setTextSize(30);
                    format.setAli(Layout.Alignment.ALIGN_CENTER);
                    format.setStyle(PrnTextStyle.BOLD);
                    if (fontsStyle == 0) {
                        format.setFont(PrnTextFont.CUSTOM);
                        format.setPath(Environment.getExternalStorageDirectory() + "/fonts/simsun.ttf");
                    } else if (fontsStyle == 1) {
                        format.setFont(PrnTextFont.DEFAULT);
                        //  format.setPath(Environment.getExternalStorageDirectory()+"/fonts/heiti.ttf");
                    } else {
                        format.setFont(PrnTextFont.CUSTOM);
                        format.setPath(Environment.getExternalStorageDirectory() + "/fonts/fangzhengyouyuan.ttf");
                    }
                    mPrinter.setPrintAppendString(getResources().getString(R.string.pos_sales_slip), format);
                    format.setTextSize(25);
                    format.setStyle(PrnTextStyle.NORMAL);
                    format.setAli(Layout.Alignment.ALIGN_NORMAL);
                    String nomal = "";
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.merchant_name) + " Test ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.merchant_no) + " 123456789012345 ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.terminal_name) + " 12345678 ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.operator_no) + " 01 ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.card_no) + " ", format);
                    format.setAli(Layout.Alignment.ALIGN_CENTER);
                    format.setTextSize(30);
                    format.setStyle(PrnTextStyle.BOLD);
                    mPrinter.setPrintAppendString("6214 44** **** **** 7816", format);
                    format.setAli(Layout.Alignment.ALIGN_NORMAL);
                    format.setStyle(PrnTextStyle.NORMAL);
                    format.setTextSize(25);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.acq_institute), format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.iss) + " ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.trans_type) + " ", format);
                    format.setTextSize(30);
                    format.setStyle(PrnTextStyle.BOLD);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.sale) + " (C) ", format);
                    format.setAli(Layout.Alignment.ALIGN_NORMAL);
                    format.setStyle(PrnTextStyle.NORMAL);
                    format.setTextSize(25);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.exe_date) + " 2030/10  ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.batch_no) + " 000335 ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.voucher_no) + " 000002 ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.date) + " 2018/05/28 ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.time) + " 00:00:01 ", format);
                    format.setTextSize(30);
                    format.setStyle(PrnTextStyle.BOLD);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.amount) + "￥0.01", format);
                    format.setStyle(PrnTextStyle.NORMAL);
                    format.setTextSize(25);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.reference) + " ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.cardholder_signature) + " ", format);
                    mPrinter.setPrintAppendString(" ", format);

                    mPrinter.setPrintAppendString(" -----------------------------", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.print_remark) + " ", format);
                    mPrinter.setPrintAppendString(getResources().getString(R.string.cardholder_copy) + " ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    printStatus = mPrinter.setPrintStart();
                    if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                            }
                        });
                    }
                }


            }
        }).start();
    }

    public static void saveFile(InputStream inputStream, String fileName) {
        //Log.e(TAG, "保存图片");
        File appDir = new File(Environment.getExternalStorageDirectory(), "fonts");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 开始读取
            while ((len = inputStream.read(bs)) != -1) {
                fos.write(bs, 0, len);
            }

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printPic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                        }
                    });
                } else {

                    if (mBitmapDef == null) {
                        try {
                            InputStream inputStream = getActivity().getAssets().open("print_demo.bmp");
                            Drawable drawable = Drawable.createFromStream(inputStream, null);
                            mBitmapDef = ((BitmapDrawable) drawable).getBitmap();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    PrnStrFormat format = new PrnStrFormat();
                    mPrinter.setPrintAppendBitmap(mBitmapDef, Layout.Alignment.ALIGN_CENTER);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    printStatus = mPrinter.setPrintStart();
                    if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                            }
                        });
                    }

                }
            }
        }).start();
    }

    private void printQr() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                        }
                    });
                } else {

                    PrnStrFormat format = new PrnStrFormat();
                    mPrinter.setPrintAppendString(getString(R.string.show_qrcode_status1), format);
                    mPrinter.setPrintAppendQRCode(QR_TEXT, 200, 200, Layout.Alignment.ALIGN_NORMAL);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(getString(R.string.show_qrcode_status2), format);
                    mPrinter.setPrintAppendQRCode(QR_TEXT, 200, 200, Layout.Alignment.ALIGN_OPPOSITE);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(getString(R.string.show_qrcode_status3), format);
                    mPrinter.setPrintAppendQRCode(QR_TEXT, 200, 200, Layout.Alignment.ALIGN_CENTER);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    printStatus = mPrinter.setPrintStart();
                    if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                            }
                        });
                    }
                }
            }
        }).start();
    }

    private void printBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                        }
                    });
                } else {
                    PrnStrFormat format = new PrnStrFormat();
                    mPrinter.setPrintAppendString(getString(R.string.show_barcode_status1), format);
                    mPrinter.setPrintAppendBarCode(getActivity(), BAR_TEXT, 360, 100, true, Layout.Alignment.ALIGN_NORMAL, BarcodeFormat.CODE_128);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(getString(R.string.show_barcode_status2), format);
                    mPrinter.setPrintAppendBarCode(getActivity(), BAR_TEXT, 300, 80, false, Layout.Alignment.ALIGN_CENTER, BarcodeFormat.CODE_128);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(getString(R.string.show_barcode_status3), format);
                    mPrinter.setPrintAppendBarCode(getActivity(), BAR_TEXT, 300, 100, false, Layout.Alignment.ALIGN_OPPOSITE, BarcodeFormat.CODE_128);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    mPrinter.setPrintAppendString(" ", format);
                    printStatus = mPrinter.setPrintStart();
                    if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                            }
                        });
                    }

                }
            }
        }).start();
    }

    private void printHybird() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                        }
                    });
                } else {

                    PrnStrFormat format = new PrnStrFormat();
                    format.setTextSize(48);
                    format.setLetterSpacing(2.0f);
                    format.setAli(Layout.Alignment.ALIGN_CENTER);
                    format.setStyle(PrnTextStyle.BOLD);
                    format.setFont(PrnTextFont.DEFAULT);
                    mPrinter.setPrintAppendString("银联POS签购单", format);
                    //mPrinter.
                    printStatus = mPrinter.setPrintStart();
                    if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.show(getActivity(), getString(R.string.printer_out_of_paper));

                            }
                        });
                    }

                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBitmapDef != null) {
            mBitmapDef.recycle();
        }
    }
}
