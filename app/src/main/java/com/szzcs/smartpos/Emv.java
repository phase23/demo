package com.szzcs.smartpos;

import android.content.Context;
import android.util.Log;

import com.zcs.sdk.DriverManager;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.card.CardInfoEntity;
import com.zcs.sdk.card.CardReaderManager;
import com.zcs.sdk.card.CardReaderTypeEnum;
import com.zcs.sdk.card.CardSlotNoEnum;
import com.zcs.sdk.card.ICCard;
import com.zcs.sdk.card.RfCard;
import com.zcs.sdk.emv.EmvApp;
import com.zcs.sdk.emv.EmvData;
import com.zcs.sdk.emv.EmvHandler;
import com.zcs.sdk.emv.EmvResult;
import com.zcs.sdk.emv.EmvTermParam;
import com.zcs.sdk.emv.EmvTransParam;
import com.zcs.sdk.emv.OnEmvListener;
import com.zcs.sdk.listener.OnSearchCardListener;
import com.zcs.sdk.pin.PinAlgorithmMode;
import com.zcs.sdk.pin.pinpad.PinPadManager;
import com.zcs.sdk.pin.pinpad.PinPadManager.OnPinPadInputListener;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yyzz on 19.3.21.
 */
public class Emv {

    private DriverManager mDriverManager = DriverManager.getInstance();
    private CardReaderManager mCardReadManager = mDriverManager.getCardReadManager();
    private PinPadManager mPinPadManager = mDriverManager.getPadManager();
    private EmvHandler emvHandler = EmvHandler.getInstance();
    private ICCard mICCCard;
    private RfCard mRFCard;
    private CardReaderTypeEnum realCardType;
    private int iRet;
    private CountDownLatch mLatch;
    private Context mContext;
    private byte[] pinBlock;
    private int inputPINResult;

    private void searchCard(final CardReaderTypeEnum cardType) {
        OnSearchCardListener listener = new OnSearchCardListener() {
            @Override
            public void onError(int resultCode) {
                mCardReadManager.closeCard();
            }

            @Override
            public void onCardInfo(CardInfoEntity cardInfoEntity) {
                realCardType = cardInfoEntity.getCardExistslot();
                switch (realCardType) {
                    case RF_CARD:
                        RfCard rfCard = mCardReadManager.getRFCard();
                        byte resetData[] = new byte[EmvData.BUF_LEN];
                        int datalength[] = new int[1];
                        iRet = rfCard.rfReset(resetData, datalength);
                        if (iRet != 0) {
                            Log.d("Debug", "rf reset error!");
                            return;
                        }
                        break;
                    case MAG_CARD:
                        Log.d("Debug", "MAG_CARD");
                        getMagData();
                        break;
                    case IC_CARD:
                        Log.d("Debug", "ICC_CARD");
                        ICCard iccCard = mCardReadManager.getICCard();
                        iRet = iccCard.icCardReset(CardSlotNoEnum.SDK_ICC_USERCARD);
                        if (iRet != 0) {
                            Log.d("Debug", "ic reset error!");
                            return;
                        }
                        break;
                    default:
                        break;
                }
                if (iRet == 0 && realCardType != CardReaderTypeEnum.MAG_CARD) {
                    emv(realCardType);
                }
            }

            @Override
            public void onNoCard(CardReaderTypeEnum arg0, boolean arg1) {
            }
        };
        mCardReadManager.searchCard(cardType, 0, listener);
    }


    private void cancelSearchCard() {
        mCardReadManager.cancelSearchCard();
    }


    private void loadVisaAIDs(EmvHandler emvHandle) {
        // Visa Credit/Debit
        EmvApp ea = new EmvApp();

        ea.setAid("A0000000031010");
        ea.setSelFlag((byte) 0);
        ea.setTargetPer((byte) 0x00);
        ea.setMaxTargetPer((byte) 0);
        ea.setFloorLimit(1000);
        ea.setOnLinePINFlag((byte) 1);
        ea.setThreshold(0);
        ea.setTacDefault("0000000000");
        ea.setTacDenial("0000000000");
        ea.setTacOnline("0000000000");
        ea.settDOL("0F9F02065F2A029A039C0195059F3704");
        ea.setdDOL("039F3704");
        ea.setVersion("008C");
        ea.setClTransLimit("000000015000");
        ea.setClOfflineLimit("000000008000");
        ea.setClCVMLimit("000000005000");
        ea.setEcTTLVal("000000100000");

        emvHandle.addApp(ea);


        // Visa Electron
        ea = new EmvApp();

        ea.setAid("A0000000032010");
        ea.setSelFlag((byte) 0);
        ea.setTargetPer((byte) 0x00);
        ea.setMaxTargetPer((byte) 0);
        ea.setFloorLimit(1000);
        ea.setOnLinePINFlag((byte) 1);
        ea.setThreshold(0);
        ea.setTacDefault("0000000000");
        ea.setTacDenial("0000000000");
        ea.setTacOnline("0000000000");
        ea.settDOL("0F9F02065F2A029A039C0195059F3704");
        ea.setdDOL("039F3704");
        ea.setVersion("008C");
        ea.setClTransLimit("000000015000");
        ea.setClOfflineLimit("000000008000");
        ea.setClCVMLimit("000000005000");
        ea.setEcTTLVal("000000100000");

        emvHandle.addApp(ea);

        // Visa Plus
        ea = new EmvApp();

        ea.setAid("A0000000038010");
        ea.setSelFlag((byte) 0);
        ea.setTargetPer((byte) 0x00);
        ea.setMaxTargetPer((byte) 0);
        ea.setFloorLimit(1000);
        ea.setOnLinePINFlag((byte) 1);
        ea.setThreshold(0);
        ea.setTacDefault("0000000000");
        ea.setTacDenial("0000000000");
        ea.setTacOnline("0000000000");
        ea.settDOL("0F9F02065F2A029A039C0195059F3704");
        ea.setdDOL("039F3704");
        ea.setVersion("008C");
        ea.setClTransLimit("000000015000");
        ea.setClOfflineLimit("000000008000");
        ea.setClCVMLimit("000000005000");
        ea.setEcTTLVal("000000100000");

        emvHandle.addApp(ea);
    }


    private void emv(CardReaderTypeEnum cardType) {
        // 1. copy aid and capk to '/sdcard/emv/' as the default aid and capk
        try {
            if (!new File(EmvTermParam.emvParamFilePath).exists()) {
                //FileUtils.doCopy(Emv.this, "emv", EmvTermParam.emvParamFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. set params
        final EmvTransParam emvTransParam = new EmvTransParam();
        if (cardType == CardReaderTypeEnum.IC_CARD) {
            emvTransParam.setTransKernalType(EmvData.KERNAL_EMV_PBOC);
        } else if (cardType == CardReaderTypeEnum.RF_CARD) {
            emvTransParam.setTransKernalType(EmvData.KERNAL_CONTACTLESS_ENTRY_POINT);
        }

        final EmvTermParam emvTermParam = new EmvTermParam();
        // 3. add aid or capk
        //loadVisaAIDs(emvHandler);
        //emvHandler.addApp();
        //emvHandler.addCapk()
        emvHandler.kernelInit(emvTermParam);

        // 4. transaction
        byte[] pucIsEcTrans = new byte[1];
        byte[] pucBalance = new byte[6];
        byte[] pucTransResult = new byte[1];

        OnEmvListener onEmvListener = new OnEmvListener() {
            @Override
            public int onSelApp(String[] appLabelList) {
                Log.d("Debug", "onSelApp");
                return iRet;
            }

            @Override
            public int onConfirmCardNo(String cardNo) {
                Log.d("Debug", "onConfirmCardNo");
                String[] track2 = new String[1];
                final String[] pan = new String[1];
                emvHandler.getTrack2AndPAN(track2, pan);
                int index = 0;
                if (track2[0].contains("D")) {
                    index = track2[0].indexOf("D") + 1;
                } else if (track2[0].contains("=")) {
                    index = track2[0].indexOf("=") + 1;
                }
                final String exp = track2[0].substring(index, index + 4);
                return 0;
            }

            @Override
            public int onInputPIN(byte pinType) {
                if (emvTransParam.getTransKernalType() == EmvData.KERNAL_CONTACTLESS_ENTRY_POINT) {
                    String[] track2 = new String[1];
                    final String[] pan = new String[1];
                    emvHandler.getTrack2AndPAN(track2, pan);
                    int index = 0;
                    if (track2[0].contains("D")) {
                        index = track2[0].indexOf("D") + 1;
                    } else if (track2[0].contains("=")) {
                        index = track2[0].indexOf("=") + 1;
                    }
                    final String exp = track2[0].substring(index, index + 4);
                }
                Log.d("Debug", "onInputPIN");
                int iRet = 0;
                iRet = inputPIN(pinType);
                Log.d("Debug", "iRet=" + iRet);
                if (iRet == EmvResult.EMV_OK) {
                    emvHandler.setPinBlock(pinBlock);
                }
                return iRet;
            }

            @Override
            public int onCertVerify(int certType, String certNo) {
                Log.d("Debug", "onCertVerify");
                return 0;
            }

            @Override
            public int onlineProc() {
                Log.d("Debug", "onOnlineProc");
                return 0;
            }

            @Override
            public byte[] onExchangeApdu(byte[] send) {
                Log.d("Debug", "onExchangeApdu");
                if (realCardType == CardReaderTypeEnum.IC_CARD) {
                    mICCCard = mCardReadManager.getICCard();
                    byte[] bytes = mICCCard.icExchangeAPDU(CardSlotNoEnum.SDK_ICC_USERCARD, send);
                    return bytes;
                } else if (realCardType == CardReaderTypeEnum.RF_CARD) {
                    mRFCard = mCardReadManager.getRFCard();
                    byte[] bytes = mRFCard.rfExchangeAPDU(send);
                    return bytes;
                }
                return null;
            }
        };
        // for the emv result, plz refer to emv doc.
        int ret = emvHandler.emvTrans(emvTransParam, onEmvListener, pucIsEcTrans, pucBalance, pucTransResult);
        String str = "Decline";
        if (pucTransResult[0] == EmvData.APPROVE_M) {
            str = "Approve";
        } else if (pucTransResult[0] == EmvData.ONLINE_M) {
            str = "Online";
        } else if (pucTransResult[0] == EmvData.DECLINE_M) {
            str = "Decline";
        }
        if (ret == 0) {
            getEmvData();
        }
        mCardReadManager.closeCard();
    }

    private void getMagData() {
        CardInfoEntity magReadData = mCardReadManager.getMAGCard().getMagReadData();
        MyApp.cardInfoEntity = magReadData;
        if (magReadData.getResultcode() == SdkResult.SDK_OK) {
            String tk1 = magReadData.getTk1();
            String tk2 = magReadData.getTk2();
            String tk3 = magReadData.getTk3();
            String expiredDate = magReadData.getExpiredDate();
            String cardNo = magReadData.getCardNo();
        }

    }

    int[] tags = {
            0x9F26,
            0x9F27,
            0x9F10,
            0x9F37,
            0x9F36,
            0x95,
            0x9A,
            0x9C,
            0x9F02,
            0x5F2A,
            0x82,
            0x9F1A,
            0x9F03,
            0x9F33,
            0x9F34,
            0x9F35,
            0x9F1E,
            0x84,
            0x9F09,
            0x9F41,
            0x9F63,
            0x5F24
    };

    private void getEmvData() {
        byte[] field55 = emvHandler.packageTlvList(tags);
    }

    /**
     * To use the pin pad, you must register the activity for pin. Plz refer to the lib doc.
     *
     * @param pinType
     * @return
     */
    public int inputPIN(byte pinType) {
        final byte InputPinType = pinType;
        mLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OnPinPadInputListener onPinPadInputListener = new OnPinPadInputListener() {

                        @Override
                        public void onSuccess(byte[] pinBlock) {
                            System.arraycopy(pinBlock, 0, Emv.this.pinBlock, 0, pinBlock.length);
                            Emv.this.pinBlock[pinBlock.length] = 0x00;

                            String encryptedPin = emvHandler.bytesToHexString(Emv.this.pinBlock);
                            Log.d("Debug", "encryptedPin=" + encryptedPin);

                            if (encryptedPin.length() == 0) {
                                inputPINResult = EmvResult.EMV_NO_PASSWORD;
                            } else {// pin length =0
                                inputPINResult = EmvResult.EMV_OK;
                            }
                            mLatch.countDown();
                        }

                        @Override
                        public void onError(int backCode) {
                            Log.d("Debug", "backCode=" + backCode);
                            if (backCode == SdkResult.SDK_PAD_ERR_NOPIN) {
                                inputPINResult = EmvResult.EMV_NO_PASSWORD;
                            } else if (backCode == SdkResult.SDK_PAD_ERR_TIMEOUT) {
                                inputPINResult = EmvResult.EMV_TIME_OUT;
                            } else if (backCode == SdkResult.SDK_PAD_ERR_CANCEL) {
                                inputPINResult = EmvResult.EMV_USER_CANCEL;
                            } else {
                                inputPINResult = EmvResult.EMV_NO_PINPAD_OR_ERR;
                            }

                            mLatch.countDown();
                        }
                    };

                    Log.d("Debug", "InputPinType=" + InputPinType);
                    if (InputPinType == EmvData.ONLINE_ENCIPHERED_PIN)

                    {
                        String track2[] = new String[1];
                        String pan[] = new String[1];
                        iRet = emvHandler.getTrack2AndPAN(track2, pan);
                        mPinPadManager.inputOnlinePin(mContext, (byte) 4, (byte) 12, 60, true, pan[0], (byte) 0, PinAlgorithmMode.ANSI_X_9_8, onPinPadInputListener);
                    } else

                    {
                        mPinPadManager.inputOfflinePin(mContext, (byte) 4, (byte) 12, 60, true, onPinPadInputListener);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inputPINResult;
    }
}
