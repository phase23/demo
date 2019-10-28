package com.szzcs.smartpos;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.szzcs.smartpos.utils.DialogUtils;
import com.szzcs.smartpos.utils.SDK_Result;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.Led;
import com.zcs.sdk.LedLightModeEnum;
import com.zcs.sdk.SdkData;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.card.CardInfoEntity;
import com.zcs.sdk.card.CardReaderManager;
import com.zcs.sdk.card.CardReaderTypeEnum;
import com.zcs.sdk.card.CardSlotNoEnum;
import com.zcs.sdk.card.ICCard;
import com.zcs.sdk.card.MagCard;
import com.zcs.sdk.card.RfCard;
import com.zcs.sdk.listener.OnSearchCardListener;
import com.zcs.sdk.util.LogUtils;
import com.zcs.sdk.util.StringUtils;
import android.content.Intent;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.szzcs.smartpos.utils.SDK_Result.obtainCardInfo;


/**
 * Created by yyzz on 2018/5/24.
 */

public class CardFragment extends PreferenceFragment {
    public static String keyS = "FFFFFFFFFFFF";
    public static byte keyType = 0x00;
    public static boolean issetM1 = false;

    public static String keyMf_plus = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
    public static byte[] Mf_plus_adress = {0x40, 0x00};
    public static boolean issetMF_plus = false;

    private static final String TAG = "CardFragment";
    private static DriverManager mDriverManager = MyApp.sDriverManager;
    private CardHandler mHandler;
    private static CardReaderManager mCardReadManager;
    private static Led sLed = mDriverManager.getLedDriver();
    private ProgressDialog mProgressDialog;

    public static final int READ_TIMEOUT = 60 * 1000;
    public static final int MSG_CARD_OK = 2001;
    public static final int MSG_CARD_ERROR = 2002;
    public static final int MSG_CARD_APDU = 2003;
    public static final int MSG_RF_CARD_APDU = 2007;
    public static final int MSG_CARD_M1 = 2004;
    public static final int MSG_CARD_MF_PLUS = 2005;
    public static final int MSG_CARD_FELICA = 2006;

    public static final byte[] APDU_SEND_IC = {0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0X00};
    public static final byte[] APDU_SEND_RF = {0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00};
    public static final byte[] APDU_SEND_RANDOM = {0x00, (byte) 0x84, 0x00, 0x00, 0x08};
    // 10 06 01 2E 45 76 BA C5 45 2B 01 09 00 01 80 00
    public static final byte[] APDU_SEND_FELICA = {0x10, 0x06, 0x01, 0x2E, 0x45, 0x76, (byte) 0xBA, (byte) 0xC5, 0x45, 0x2B, 0x01, 0x09, 0x00, 0x01, (byte) 0x80, 0x00};
    private static final String KEY_APDU = "APDU";
    private static final String KEY_RF_CARD_TYPE = "RF_CARD_TYPE";
    private byte[] mReceivedData = new byte[300];
    private int[] mReceivedDataLength = new int[1];
    private static final byte SLOT_USERCARD = 0x00;
    private static final byte SLOT_PSAM1 = 0x01;
    private static final byte SLOT_PSAM2 = 0x02;
    public boolean ifSearch = true;

    boolean isShowDialog = true;

    private boolean isMF_Plus = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_card);
        mHandler = new CardHandler(this);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // search card and read, just wait a moment
        findPreference(getString(R.string.key_read_all)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clickCardType = CardReaderTypeEnum.MAG_IC_RF_CARD;
                readCard1(CardReaderTypeEnum.MAG_IC_RF_CARD, READ_TIMEOUT, (byte) (SdkData.RF_TYPE_A | SdkData.RF_TYPE_B | SdkData.RF_TYPE_FELICA | SdkData.RF_TYPE_N24G));
                return true;
            }
        });

        // read mag card
        findPreference(getString(R.string.key_magnetic)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clickCardType = CardReaderTypeEnum.MAG_CARD;
                readCard(CardReaderTypeEnum.MAG_CARD, READ_TIMEOUT, (byte) 0);
                return true;
            }
        });

        // ic card
        findPreference(getString(R.string.key_ic)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clickCardType = CardReaderTypeEnum.IC_CARD;
                readCard1(CardReaderTypeEnum.IC_CARD, READ_TIMEOUT, (byte) 0);
                return true;
            }
        });

        findPreference(getString(R.string.key_rf)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clickCardType = CardReaderTypeEnum.RF_CARD;
                readCard1(CardReaderTypeEnum.RF_CARD, READ_TIMEOUT, (byte) (SdkData.RF_TYPE_A | SdkData.RF_TYPE_B | SdkData.RF_TYPE_FELICA | SdkData.RF_TYPE_N24G));
                return true;
            }
        });
        findPreference(getString(R.string.key_m1)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                isMF_Plus = false;
                clickCardType = CardReaderTypeEnum.RF_CARD;
                readCard1(CardReaderTypeEnum.RF_CARD, READ_TIMEOUT, SdkData.RF_TYPE_A);
                return true;
            }
        });
        findPreference(getString(R.string.key_mf_plus)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                isMF_Plus = true;
                clickCardType = CardReaderTypeEnum.RF_CARD;
                readCard1(CardReaderTypeEnum.RF_CARD, READ_TIMEOUT, SdkData.RF_TYPE_A);
                return true;
            }
        });
        findPreference(getString(R.string.key_psam1)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clickCardType = CardReaderTypeEnum.PSIM1;
                readCard1(CardReaderTypeEnum.PSIM1, READ_TIMEOUT, (byte) 0);
                return true;
            }
        });

        findPreference(getString(R.string.key_psam2)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clickCardType = CardReaderTypeEnum.PSIM2;
                readCard1(CardReaderTypeEnum.PSIM2, READ_TIMEOUT, (byte) 0);
                return true;
            }
        });

        findPreference(getString(R.string.key_external_ic)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ExternalPortActivity.class));
                return true;
            }
        });

        findPreference(getString(R.string.key_sle4442)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SLE4442Activity.class));
                return true;
            }
        });
        findPreference(getString(R.string.key_sle4428)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SLE4428Activity.class));
                return true;
            }
        });
        findPreference(getString(R.string.key_emv)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), EmvActivity.class));
                return true;
            }
        });
    }

    private void readCard(final CardReaderTypeEnum cardType, final int timeout, final byte cardTypepa) {


        if (mCardReadManager == null) {
            mCardReadManager = mDriverManager.getCardReadManager();
        }
        mCardReadManager.cancelSearchCard();
        switch (cardType) {

            case MAG_CARD:
                showSearchCardDialog(R.string.title_waiting, R.string.msg_mag_card);
                break;

        }
        ifSearch = true;
        mCardReadManager.searchCard(cardType, timeout, cardTypepa, new OnSearchCardListener() {
            @Override
            public void onCardInfo(CardInfoEntity cardInfoEntity) {
                Log.e(TAG, "searchCard thread: " + Thread.currentThread().getName());
                Message msg = Message.obtain();
                CardReaderTypeEnum cardTypeNew = cardInfoEntity.getCardExistslot();
                switch (cardTypeNew) {
                    case MAG_CARD:
                        readMagCard(msg);
                        break;
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (ifSearch) {
                    // searchCardDia(cardType);
                    mCardReadManager.searchCard(cardType, timeout, cardTypepa, this);
                }
            }

            @Override
            public void onError(int i) {
                Log.e(TAG, "search card onError: " + i);
                mHandler.sendEmptyMessage(i);

            }

            @Override
            public void onNoCard(CardReaderTypeEnum cardReaderTypeEnum, boolean b) {

            }
        });
    }

    CardReaderTypeEnum mCardType;
    byte endCardType = 0;

    private void readCard1(final CardReaderTypeEnum cardType, final int timeout, final byte cardTypepa) {

        endCardType = cardTypepa;

        isShowDialog = true;
        if (mCardReadManager == null) {
            mCardReadManager = mDriverManager.getCardReadManager();
        }
        mCardReadManager.cancelSearchCard();
        switch (cardType) {
            case MAG_IC_RF_CARD:
                sLed.setLed(LedLightModeEnum.BLUE, true);
                showSearchCardDialog(R.string.title_waiting, R.string.msg_bank_card);
                break;

            case RF_CARD:
                sLed.setLed(LedLightModeEnum.BLUE, true);
                if (endCardType == SdkData.RF_TYPE_A) {
                    //showSearchCardDialog(R.string.title_waiting, R.string.msg_m1_card);
                } else {
                    showSearchCardDialog(R.string.title_waiting, R.string.msg_rf_card);
                }

                break;
            case IC_CARD:
                showSearchCardDialog(R.string.title_waiting, R.string.msg_ic_card);
                break;
            case PSIM1:
            case PSIM2:
                showSearchCardDialog(R.string.title_waiting, R.string.msg_reading);
                break;
        }
        mCardType = cardType;
        ifSearch = true;
        if (cardType == CardReaderTypeEnum.RF_CARD && endCardType == SdkData.RF_TYPE_A) {
            if (!isMF_Plus) {
                if (issetM1) {
                    showSearchCardDialog(R.string.title_waiting, R.string.msg_m1_card);
                    mCardReadManager.searchCard(cardType, timeout, cardTypepa, mListener);
                } else {
                    m1Dialog(cardType, timeout, cardTypepa);
                }
            } else {
                if (issetMF_plus) {
                    showSearchCardDialog(R.string.title_waiting, R.string.msg_mf_puls_card);
                    mCardReadManager.searchCard(cardType, timeout, cardTypepa, mListener);
                } else {
                    MF_plusDialog(cardType, timeout, cardTypepa);
                }
            }

        } else {
            mCardReadManager.searchCard(cardType, timeout, cardTypepa, mListener);
        }

    }

    OnSearchCardListener mListener = new OnSearchCardListener() {
        @Override
        public void onCardInfo(CardInfoEntity cardInfoEntity) {

            Log.e(TAG, "searchCard thread: " + Thread.currentThread().getName());
            Message msg = Message.obtain();
            CardReaderTypeEnum cardTypeNew = cardInfoEntity.getCardExistslot();

            Log.e(TAG, "rfCardType: " + cardTypeNew);

            if (isShowDialog) {
                switch (cardTypeNew) {
                    case RF_CARD:
                        sLed.setLed(LedLightModeEnum.YELLOW, true);

                        if (endCardType == SdkData.RF_TYPE_A) {
                            isShowDialog = false;
                            if (!isMF_Plus) {

                                byte[] rfCardType = cardInfoEntity.getRfCardType();
                                Log.e(TAG, "rfCardType: " + rfCardType[0]);

                                readM1Card(mCardType, READ_TIMEOUT, msg);    //M1?
                            } else {
                                readMF_PlusCard(mCardType, READ_TIMEOUT, msg);    //MF_Plus?
                            }

                        } else {
                            isShowDialog = false;
                            //
                            byte[] rfCardType = cardInfoEntity.getRfCardType();
                            Log.e(TAG, "rfCardType: " + rfCardType[0]);
                            readRfCard(mCardType, rfCardType[0], READ_TIMEOUT, msg);

                        }
                        break;
                    case MAG_CARD:
                        isShowDialog = false;
                        readMagCard(msg);
                        break;
                    case IC_CARD:
                        isShowDialog = false;
                        readICCard(mCardType, READ_TIMEOUT, msg, CardSlotNoEnum.SDK_ICC_USERCARD);

                        break;
                    case PSIM1:
                        isShowDialog = false;
                        readICCard(mCardType, READ_TIMEOUT, msg, CardSlotNoEnum.SDK_ICC_SAM1);

                        break;
                    case PSIM2:
                        isShowDialog = false;
                        readICCard(mCardType, READ_TIMEOUT, msg, CardSlotNoEnum.SDK_ICC_SAM2);

                        break;
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mCardReadManager.searchCard(mCardType, READ_TIMEOUT, endCardType, mListener);
            }


        }

        @Override
        public void onError(int i) {
            sLed.setLed(LedLightModeEnum.RED, true);
            Log.e(TAG, "search card onError: " + i);
            mHandler.sendEmptyMessage(i);
             /*   try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (ifSearch)
                    mCardReadManager.searchCard(cardType, timeout, this);*/
        }

        @Override
        public void onNoCard(CardReaderTypeEnum cardReaderTypeEnum, boolean b) {
            if (cardReaderTypeEnum == mCardType) {
                isShowDialog = true;

            }
        }
    };

    private void readICCard(CardReaderTypeEnum cardType, int timeout, Message msg, CardSlotNoEnum slotNo) {
        ICCard iccCard = mCardReadManager.getICCard();
        int icCardReset = iccCard.icCardReset(slotNo);
        Log.e(TAG, "icCardReset: " + icCardReset);

        if (icCardReset == com.zcs.sdk.SdkResult.SDK_OK) {
            int icRes;
            byte[] apdu;
            if (slotNo.getType() == SLOT_PSAM1 || slotNo.getType() == SLOT_PSAM2) {
                // test random
                icRes = iccCard.icExchangeAPDU(slotNo, APDU_SEND_RANDOM, mReceivedData, mReceivedDataLength);
                apdu = APDU_SEND_RANDOM;
            } else {
                icRes = iccCard.icExchangeAPDU(slotNo, APDU_SEND_IC, mReceivedData, mReceivedDataLength);
                apdu = APDU_SEND_IC;
            }
            Log.e(TAG, "icRes: " + icRes);
            if (icRes == com.zcs.sdk.SdkResult.SDK_OK) {
                msg.what = MSG_CARD_APDU;
                msg.arg1 = icRes;
                Log.e(TAG, "iccCard res: " + StringUtils.convertBytesToHex(mReceivedData));
                msg.obj = StringUtils.convertBytesToHex(mReceivedData).substring(0, mReceivedDataLength[0] * 2);
                Bundle icBundle = new Bundle();
                icBundle.putByteArray(KEY_APDU, apdu);
                msg.setData(icBundle);
                mHandler.sendMessage(msg);

            } else {
                mHandler.sendEmptyMessage(icRes);
            }
        } else {
            mHandler.sendEmptyMessage(icCardReset);
        }
        int icCardPowerDown = iccCard.icCardPowerDown(CardSlotNoEnum.SDK_ICC_USERCARD);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ifSearch) {
            iscontinue = true;

            while (iscontinue) {
                //??
                iccCard.setCardType(true);
                if (slotNo.getType() == SLOT_USERCARD) {

                    int i = iccCard.getIcCardStatus(CardSlotNoEnum.SDK_ICC_USERCARD);
                    if (i == SdkResult.SDK_ICC_NO_CARD) {
                        iscontinue = false;
                    }
                } else {
                    iscontinue = false;
                }
            }

            if (slotNo.getType() == SLOT_USERCARD) {
                if (ifSearch) {
                    mCardReadManager.searchCard(cardType, timeout, endCardType, mListener);
                }
            }


        }


    }

    private void setAllLedLieht() {
        sLed.setLed(LedLightModeEnum.GREEN, true);
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sLed.setLed(LedLightModeEnum.ALL, false);
    }

    private void showSearchCardDialog(@StringRes int title, @StringRes int msg) {
        mProgressDialog = (ProgressDialog) DialogUtils.showProgress(getActivity(), getString(title), getString(msg), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mCardReadManager.cancelSearchCard();
            }
        });
    }


    private void readMagCard(Message msg) {
        CardInfoEntity cardInfo;
        MagCard magCard = mCardReadManager.getMAGCard();
        cardInfo = magCard.getMagReadData();
        if (cardInfo.getResultcode() == SdkResult.SDK_OK) {
            msg.what = MSG_CARD_OK;
            msg.arg1 = cardInfo.getResultcode();
            msg.obj = cardInfo;
            mHandler.sendMessage(msg);
        } else {
            mHandler.sendEmptyMessage(cardInfo.getResultcode());
        }
        magCard.magCardClose();

    }

    boolean iscontinue = true;

    private void readRfCard(final CardReaderTypeEnum cardType, byte rfCardType, final int timeout, Message msg) {
        final RfCard rfCard = mCardReadManager.getRFCard();
        int resetStatus = rfCard.rfReset(new byte[300], new int[10]);
        if (resetStatus != SdkResult.SDK_OK) {
            mHandler.sendEmptyMessage(resetStatus);
        } else {
            if (rfCardType == SdkData.RF_TYPE_FELICA) { // felica card
                rfCardSend(msg, rfCard, APDU_SEND_FELICA, rfCardType);
            } else {
                rfCardSend(msg, rfCard, APDU_SEND_RF, rfCardType);
            }
        }
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ifSearch && cardType == CardReaderTypeEnum.RF_CARD) {
            iscontinue = true;

            while (iscontinue) {
                //??
                byte[] outCardType = new byte[1];
                byte[] uid = new byte[300];
                rfCard.setCardType(true);
                int i = rfCard.rfSearchCard(endCardType, outCardType, uid);
                if (i == SdkResult.SDK_RF_ERR_NOCARD) {
                    iscontinue = false;
                }
            }
            if (ifSearch) {
                mCardReadManager.searchCard(cardType, timeout, endCardType, mListener);
            }
        }
    }

    /**
     * rf card send apdu
     *
     * @param msg
     * @param rfCard
     * @param apduSend
     */
    private void rfCardSend(Message msg, RfCard rfCard, byte[] apduSend, byte rfCardType) {
        int rfRes = rfCard.rfExchangeAPDU(apduSend, mReceivedData, mReceivedDataLength);
        int powerDownRes = rfCard.rfCardPowerDown();
        if (rfRes != SdkResult.SDK_OK) {
            mHandler.sendEmptyMessage(rfRes);
        } else {
            String recv = StringUtils.convertBytesToHex(mReceivedData).substring(0, mReceivedDataLength[0] * 2);
            Log.e(TAG, "rfAPUDRes: " + rfRes);
            Log.e(TAG, "mReceivedData: " + recv);
            msg.what = MSG_RF_CARD_APDU;
            msg.arg1 = rfRes;
            msg.obj = recv;
            Bundle rfBundle = new Bundle();
            rfBundle.putByteArray(KEY_APDU, apduSend);
            rfBundle.putByte(KEY_RF_CARD_TYPE, rfCardType);
            msg.setData(rfBundle);
            mHandler.sendMessage(msg);
            setAllLedLieht();
        }
    }

    String m1_message = "";

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l/2];
        for (int i = 0; i < l; i += 2) {
            data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }



    public static String hexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        char[] hexData = hex.toCharArray();
        for (int count = 0; count < hexData.length - 1; count += 2) {
            int firstDigit = Character.digit(hexData[count], 16);
            int lastDigit = Character.digit(hexData[count + 1], 16);
            int decimal = firstDigit * 16 + lastDigit;
            sb.append((char)decimal);
        }
        return sb.toString();
    }

    public static String convertHexToStringValue(String hex) {
        StringBuilder stringbuilder = new StringBuilder();
        char[] hexData = hex.toCharArray();
        for (int count = 0; count < hexData.length - 1; count += 2) {
            int firstDigit = Character.digit(hexData[count], 16);
            int lastDigit = Character.digit(hexData[count + 1], 16);
            int decimal = firstDigit * 16 + lastDigit;
            stringbuilder.append((char)decimal);
        }
        return stringbuilder.toString();
    }

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static String unHex(String arg) {

        String str = "";
        for(int i=0;i<arg.length();i+=2)
        {
            String s = arg.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            str = str + (char) decimal;
        }
        return str;
    }

    public String convertUtf8NumbersToString(String[] numbers){
        int length = numbers.length;
        byte[] data = new byte[length];

        for(int i = 0; i< length; i++){
            data[i] = Byte.parseByte(numbers[i]);
        }
        return new String(data, Charset.forName("UTF-8"));
    }


    public static int toLittleEndian(final String hex) {
        int ret = 0;
        String hexLittleEndian = "";
        if (hex.length() % 2 != 0) return ret;
        for (int i = hex.length() - 2; i >= 0; i -= 2) {
            hexLittleEndian += hex.substring(i, i + 2);
        }
        ret = Integer.parseInt(hexLittleEndian, 16);
        return ret;
    }

    public static String reverseHex(String originalHex) {
        // TODO: Validation that the length is even
        int lengthInBytes = originalHex.length() / 2;
        char[] chars = new char[lengthInBytes * 2];
        for (int index = 0; index < lengthInBytes; index++) {
            int reversedIndex = lengthInBytes - 1 - index;
            chars[reversedIndex * 2] = originalHex.charAt(index * 2);
            chars[reversedIndex * 2 + 1] = originalHex.charAt(index * 2 + 1);
        }
        return new String(chars);
    }



    private void readM1Card(CardReaderTypeEnum cardType, int timeout, Message msg) {

        RfCard rfCard = mCardReadManager.getRFCard();

        byte[] key = StringUtils.convertHexToBytes(keyS);
        m1_message = "";
        int status = rfCard.m1VerifyKey((byte) 4, keyType, key);
        if (status == SdkResult.SDK_OK) {
            //m1_message += "???0????:";
            for (int i = 0; i < 4; i++) {
                byte[] out = new byte[16];
                if (rfCard.m1ReadBlock((byte) (4 * 1 + i), out) == SdkResult.SDK_OK) {
                   // m1_message += com.zcs.sdk.util.StringUtils.convertBytesToHex(out);
                } else {

                }
                /*
                String b = m1_message;
                byte[] bytes = hexStringToByteArray(b);
                String st = new String(bytes, StandardCharsets.UTF_8);
                System.out.println(st);
                    */
            }
          /*  m1_message  +=" \n ???"+0+"??....";
            for (int i = 0; i < 3; i ++){
                byte[] input = com.zcs.sdk.util.StringUtils.convertHexToBytes("0123456789ABCDEF0123456789ABCDEF");
                rfCard.m1WirteBlock((byte) (4*1+i),input);

            }
            m1_message  +=" \n ???????"+0+"????:";
            for (int i = 0; i < 4; i ++){
                byte[] out = new byte[16];
                rfCard.m1ReadBlock((byte)(4*1+i),out);
                m1_message += com.zcs.sdk.util.StringUtils.convertBytesToHex(out);
            }*/




        }

        int status1 = rfCard.m1VerifyKey((byte) (0 * 8), keyType, key);
        if (status1 == SdkResult.SDK_OK) {
            //m1_message += " \n ?" + 3 + "????:";
            for (int i = 0; i < 8; i++) {
                byte[] out = new byte[16];
                rfCard.m1ReadBlock((byte) (0 * 8 + i), out);
               m1_message += com.zcs.sdk.util.StringUtils.convertBytesToHex(out);
            }
          /*  m1_message  +=" \n ???"+3+"??....";
            for (int i = 0; i < 3; i ++){
                byte[] input = com.zcs.sdk.util.StringUtils.convertHexToBytes("0123456789ABCDEF0123456789ABCDEF");
                rfCard.m1WirteBlock((byte) (2*4 +i),input);

            }
            m1_message  +=" \n ???????"+3+"????:";
            for (int i = 0; i < 4; i ++){
                byte[] out = new byte[16];
                rfCard.m1ReadBlock((byte) (2*4 +i) ,out);
                m1_message += com.zcs.sdk.util.StringUtils.convertBytesToHex(out);
            }*/

            String myacci = unHex(m1_message);
            Log.d("myacci : to /",  myacci);
            Log.d("irawit : to /",  m1_message);
            String myuid = m1_message.substring(0,8);
            Log.d("UID/",  myuid);
            String littleedian = reverseHex(myuid);
            Log.d("UID/",  littleedian);
            //int decimal=Integer.parseInt(littleedian,16);
            BigInteger decimal = new BigInteger(littleedian, 16);
            System.out.println("UID/: " + decimal);
            String struid = decimal.toString();
            //String struid = new String(decimal.toByteArray());

            Log.d("UID str/",  struid);
            m1_message = struid;
            //int nxtuid  = toLittleEndian(littleedian);
            //Log.d("uid", "uid: " + nxtuid);

            //4002013134
            // 4002013134
           //4001761022



        }
        rfCard.rfCardPowerDown();
        if (status == SdkResult.SDK_OK && status1 == SdkResult.SDK_OK) {
            msg.what = MSG_CARD_M1;
            msg.obj = m1_message;
            mHandler.sendMessage(msg);
            setAllLedLieht();

        } else {
            if (status == SdkResult.SDK_OK) {
                mHandler.sendEmptyMessage(status1);
            } else {
                mHandler.sendEmptyMessage(status);
            }


        }
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ifSearch && cardType == CardReaderTypeEnum.RF_CARD) {
            iscontinue = true;

            while (iscontinue) {
                //??
                byte[] outCardType = new byte[1];
                byte[] uid = new byte[300];
                rfCard.setCardType(true);
                int i = rfCard.rfSearchCard(endCardType, outCardType, uid);
                if (i == SdkResult.SDK_RF_ERR_NOCARD) {
                    iscontinue = false;
                }
            }
            if (ifSearch) {
                mCardReadManager.searchCard(cardType, timeout, endCardType, mListener);
            }
        }
    }


    String m1_mf_puls = "";

    private void readMF_PlusCard(CardReaderTypeEnum cardType, int timeout, Message msg) {

        RfCard rfCard = mCardReadManager.getRFCard();
        int resetStatus = rfCard.rfReset(new byte[300], new int[10]);
        if (resetStatus == SdkResult.SDK_OK) {
            byte[] key = StringUtils.convertHexToBytes(keyMf_plus);
            m1_mf_puls = "";
            int status = rfCard.mFPlusFirstAuthen(Mf_plus_adress, key);
            if (status == SdkResult.SDK_OK) {
                m1_mf_puls += "???0????:";
                Log.e("sangtian", "======" + status);
                byte[] outdata = new byte[64];
                if (rfCard.mFPlusL3Read(StringUtils.convertHexToBytes("0000"), (byte) 4, outdata) == SdkResult.SDK_OK) {
                    m1_mf_puls += com.zcs.sdk.util.StringUtils.convertBytesToHex(outdata);
                } else {
                }
          /*  for (int i = 0; i < 4; i++) {
                byte[] out = new byte[16];
                if (rfCard.m1ReadBlock((byte) (4 * 1 + i), out) == SdkResult.SDK_OK) {
                    m1_mf_puls += com.zcs.sdk.util.StringUtils.convertBytesToHex(out);
                } else {
                }
            }*/
            }

            rfCard.rfCardPowerDown();
            if (status == SdkResult.SDK_OK) {
                msg.what = MSG_CARD_MF_PLUS;
                msg.obj = m1_mf_puls;
                mHandler.sendMessage(msg);
                setAllLedLieht();

            } else {
                mHandler.sendEmptyMessage(status);
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ifSearch && cardType == CardReaderTypeEnum.RF_CARD) {
                iscontinue = true;

                while (iscontinue) {
                    //??
                    byte[] outCardType = new byte[1];
                    byte[] uid = new byte[300];
                    rfCard.setCardType(true);
                    int i = rfCard.rfSearchCard(endCardType, outCardType, uid);
                    if (i == SdkResult.SDK_RF_ERR_NOCARD) {
                        iscontinue = false;
                    }

                }
                if (ifSearch) {
                    mCardReadManager.searchCard(cardType, timeout, endCardType, mListener);
                }

            }

        } else {

        }
    }

    public void m1Dialog(final CardReaderTypeEnum cardType, final int timeout, final byte cardTypepa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View contentView = getActivity().getLayoutInflater().inflate(
                R.layout.activity_m1_dialog, null);
        final EditText password = contentView.findViewById(R.id.password);
        final EditText key_type = contentView.findViewById(R.id.key_type);

        builder.setTitle("M1 Card Password Input:");
        builder.setView(contentView);
        builder.setPositiveButton(getString(R.string.set_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                keyS = password.getText().toString().trim();
                String keyType1 = key_type.getText().toString().trim();
                keyType = StringUtils.convertHexToBytes(keyType1)[0];
                issetM1 = true;
                showSearchCardDialog(R.string.title_waiting, R.string.msg_m1_card);
                mCardReadManager.searchCard(cardType, timeout, cardTypepa, mListener);
            }
        }).setNegativeButton(getString(R.string.set_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                issetM1 = false;
                showSearchCardDialog(R.string.title_waiting, R.string.msg_m1_card);
                mCardReadManager.searchCard(cardType, timeout, cardTypepa, mListener);
            }
        }).create().show();
    }

    public void MF_plusDialog(final CardReaderTypeEnum cardType, final int timeout, final byte cardTypepa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View contentView = getActivity().getLayoutInflater().inflate(
                R.layout.activity_mf_plus_dialog, null);
        final EditText password = contentView.findViewById(R.id.password);
        final EditText key_address = contentView.findViewById(R.id.key_address);

        builder.setTitle("MF plus Card Password Input:");
        builder.setView(contentView);
        builder.setPositiveButton(getString(R.string.set_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                keyMf_plus = password.getText().toString().trim();
                Mf_plus_adress = StringUtils.convertHexToBytes(key_address.getText().toString().trim());
                issetMF_plus = true;
                showSearchCardDialog(R.string.title_waiting, R.string.msg_mf_puls_card);
                mCardReadManager.searchCard(cardType, timeout, cardTypepa, mListener);
            }
        }).setNegativeButton(getString(R.string.set_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                issetMF_plus = false;
                showSearchCardDialog(R.string.title_waiting, R.string.msg_mf_puls_card);
                mCardReadManager.searchCard(cardType, timeout, cardTypepa, mListener);
            }
        }).create().show();
    }


    @Override
    public void onDestroy() {
        LogUtils.error("??activity");
        ifSearch = false;
        iscontinue = false;
        if (mCardReadManager != null) {
            mCardReadManager.cancelSearchCard();
            mCardReadManager.closeCard();
        }
        super.onDestroy();
    }

    public void closeSearch() {
        LogUtils.error("closeSearch");
        ifSearch = false;
        iscontinue = false;
        if (mCardReadManager != null) {

            mCardReadManager.cancelSearchCard();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCardReadManager.cancelSearchCard();
        }
    }

    static CardReaderTypeEnum clickCardType;
    private Dialog mCardInfoDialog;

    @SuppressLint("HandlerLeak")
    class CardHandler extends Handler implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
        WeakReference<Fragment> mFragment;
        int code = 9999;


        CardHandler(Fragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            CardFragment fragment = (CardFragment) mFragment.get();
            if (fragment == null || !fragment.isAdded())
                return;
            if (mCardInfoDialog != null) {
                mCardInfoDialog.dismiss();
            }
            if (fragment.mProgressDialog != null) {
                fragment.mProgressDialog.dismiss();
            }
            switch (msg.what) {
                case MSG_CARD_OK:
                    CardInfoEntity cardInfoEntity = (CardInfoEntity) msg.obj;
                    MyApp.cardInfoEntity = cardInfoEntity;
                    /*
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_card), obtainCardInfo(fragment.getActivity(), cardInfoEntity),
                            "OK", this, this);

                     */

                    obtainCardInfo(fragment.getActivity(), cardInfoEntity);

                    //Intent intent = new Intent(CardFragment.this,cvv.class);
                    Intent intent = new Intent(fragment.getActivity(), cvv.class);
                    //startActivity(intent);

                    break;
                case MSG_CARD_APDU:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_apdu),
                            SDK_Result.appendMsg("Code", msg.arg1 + "", "APDU send", StringUtils.convertBytesToHex(msg.getData().getByteArray(CardFragment.KEY_APDU)), "APDU response", (String) msg.obj),
                            "OK", this, this);
                    break;
                case MSG_RF_CARD_APDU:
                    byte rfCardType = msg.getData().getByte(KEY_RF_CARD_TYPE);
                    String type = handleRfCardType(rfCardType);
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(), type,
                            SDK_Result.appendMsg("Code", msg.arg1 + "", "Send", StringUtils.convertBytesToHex(msg.getData().getByteArray(CardFragment.KEY_APDU)), "Response", (String) msg.obj),
                            "OK", this, this);
                    break;
                case MSG_CARD_M1:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_card), (String) msg.obj,
                            "OK", this, this);

                    Log.i("right response", (String) msg.obj);

                    break;

                case MSG_CARD_MF_PLUS:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_card), (String) msg.obj,
                            "OK", this, this);
                    break;
                case MSG_CARD_FELICA:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(), "Felica card", "Search card success", "OK", this, this);
                    break;
                default:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_error),
                            SDK_Result.obtainMsg(fragment.getActivity(), msg.what),
                            "OK", this, this);
                    break;
            }
        }

        @NonNull
        private String handleRfCardType(byte rfCardType) {
            //public static final byte RF_TYPE_A = 1;
            //public static final byte RF_TYPE_B = 2;
            //public static final byte RF_TYPE_M1 = 4;
            //public static final byte RF_TYPE_FELICA = 8;
            //public static final byte RF_TYPE_N24G = 16;
            String type = "";
            switch (rfCardType) {
                case SdkData.RF_TYPE_A:
                    type = "RF_TYPE_A";
                    break;
                case SdkData.RF_TYPE_B:
                    type = "RF_TYPE_B";
                    break;
                case SdkData.RF_TYPE_M1:
                    type = "RF_TYPE_M1";
                    break;
                case SdkData.RF_TYPE_FELICA:
                    type = "RF_TYPE_FELICA";
                    break;
                case SdkData.RF_TYPE_N24G:
                    type = "RF_TYPE_N24G";
                    break;
            }
            return type;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            CardFragment fragment = (CardFragment) mFragment.get();
            if (fragment != null && fragment.isAdded()) {
                Log.e(TAG, "onCancel: " + fragment.ifSearch);
                fragment.ifSearch = false;
                closeSearch();
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            CardFragment fragment = (CardFragment) mFragment.get();
            if (fragment != null && fragment.isAdded()) {
                Log.e(TAG, "onClick: " + fragment.ifSearch);
                fragment.ifSearch = false;
                closeSearch();
            }
        }
    }
}
