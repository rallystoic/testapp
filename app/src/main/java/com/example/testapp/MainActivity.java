package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import rd.nalib.ExceptionNA;
import rd.nalib.NA;
import rd.nalib.ResponseListener;



public class MainActivity extends AppCompatActivity {



    public static final int MY_PERMISSIONS = 0x1;
    public static final int REQUEST_CHECK_SETTINGS = 0x2;
    private String NAVersion;
    private HandlerThread myThread;
    private byte[] byteRes = null;
    private boolean bReturnResponseFinish = false;
    private boolean bReturnActivityResultFinish = false;
    private boolean[] resultActivity = {false, false};
    private NA NALibs;
    private Button bt_SelectReader, bt_Read, bt_UpdateLicense, bt_Exit;
    private TextView tv_Reader, tv_Result, tv_SoftwareInfo, tv_LicenseInfo;
    private ImageView iv_Photo;
    private String mNIDReader = "/" + "NASample";
    private String RootFolder = Environment.getExternalStorageDirectory() + mNIDReader;
    private MyHandler mHandler;
    private Handler handler = new Handler();
    private int iRes = -999;
    private ArrayList<String> aRes = null;
    private String sRes = "";
    private boolean flagSetting = false;

    // test
    ThaiIDFilter _thaiIdFilter = new ThaiIDFilter();
    private String _flholder, _addressholder;
    private TextView card_id_result , flname, sex_result, dob_result, date_issue_result;
    private TextView date_exp_result, address_result;


    ResponseListener responseListener = new ResponseListener() {
        @Override
        public void onOpenLibNA(int i) {
            iRes = i;
            bReturnResponseFinish = true;
        }

        @Override
        public void onGetReaderListNA(ArrayList<String> arrayList, int i) {
            iRes = i;
            aRes = arrayList;
            bReturnResponseFinish = true;
        }

        @Override
        public void onSelectReaderNA(final int i) {
            iRes = i;
            bReturnResponseFinish = true;
        }

        @Override
        public void onGetNIDNumberNA(String s, int i) {
            iRes = i;
            sRes = s;
            bReturnResponseFinish = true;
        }

        @Override
        public void onGetNIDTextNA(final String s, int i) {
            iRes = i;
            sRes = s;
            bReturnResponseFinish = true;
        }

        @Override
        public void onGetNIDPhotoNA(byte[] bytes, int i) {
            iRes = i;
            byteRes = bytes;
            bReturnResponseFinish = true;
        }

        @Override
        public void onUpdateLicenseFileNA(int i) {
            iRes = i;
            bReturnResponseFinish = true;
        }
    };

    private int sleepTime = 10;     // = 10 ms
    private String LICFileName = "/" + "rdnidlib.dls";

    private int NA_POPUP = 0x80;
    private int NA_FIRST = 0x40;
    private int NA_SCAN = 0x10;
    private int NA_BT = 0x02;
    private int NA_USB = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            NAVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //field
        card_id_result = findViewById(R.id.card_id_result);
        flname = findViewById(R.id.flname_result);
        sex_result = findViewById(R.id.sex_result);
        dob_result = findViewById(R.id.dob_result);
        date_issue_result = findViewById(R.id.date_issue_result);
        date_exp_result = findViewById(R.id.date_exp_result);
        address_result = findViewById(R.id.address_result);


        ActionBar actionBar = getSupportActionBar();
        // removed 24/09/2021
        // actionBar.setTitle("NASample " + NAVersion);
        myThread = new HandlerThread("Worker Thread");
        myThread.start();
        Looper mLooper = myThread.getLooper();
        mHandler = new MyHandler(mLooper);
        tv_Reader = findViewById(R.id.tv_Reader);
        tv_SoftwareInfo = findViewById(R.id.tv_SoftwareInfo);
        tv_LicenseInfo = findViewById(R.id.tv_LicenseInfo);
        tv_Reader = findViewById(R.id.tv_Reader);
        bt_SelectReader = findViewById(R.id.bt_SelectReader);
        bt_SelectReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_Result.setText("");
                tv_Result.setClickable(false);
                //iv_Photo.setImageResource(R.mipmap.nas);
                Message msg = mHandler.obtainMessage();
                msg.obj = "findreader";
                mHandler.sendMessage(msg);
            }
        });

        bt_Read = findViewById(R.id.bt_Read);
        bt_Read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_Result.setText("");
                tv_Result.setClickable(false);
               // iv_Photo.setImageResource(R.mipmap.nas);
                Message msg = mHandler.obtainMessage();
                msg.obj = "read";
                mHandler.sendMessage(msg);
            }
        });
        bt_UpdateLicense = findViewById(R.id.bt_UpdateLicense);
        bt_UpdateLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_Result.setText("");
                tv_Result.setClickable(false);
                //iv_Photo.setImageResource(R.mipmap.nas);
                Message msg = mHandler.obtainMessage();
                msg.obj = "updatelicense";
                mHandler.sendMessage(msg);
            }
        });
        bt_Exit = findViewById(R.id.bt_Exit);
        bt_Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*================= Deselect Reader =================*/
                NALibs.deselectReaderNA();

                /*================= Close Lib =================*/
                NALibs.closeLibNA();
                System.exit(0);
            }
        });
        tv_Result = findViewById(R.id.tv_Result);
        iv_Photo = findViewById(R.id.iv_Photo);


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Scan bluetooth");

        NALibs = new NA(this);

        /*================= get Software Info =================*/
        clearReturnResponse();
        String[] data = new String[1];
        NALibs.getSoftwareInfoNA(data);
        if (data[0] != null) {
            tv_SoftwareInfo.setText("Software Info: " + data[0]);
        }

        NALibs.setListenerNA(responseListener);


        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS);

        /************** Location Permission for Bluetooth reader *************/
        /*           Can remove this block if use USB reader only            */

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
            dialog.setTitle("Permission");
            dialog.setMessage("Please allow Location permission if use Bluetooth reader.");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS);
                    dialog.dismiss();
                }
            });
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS);
        }

        /*********************************************************************/
    }


    public void init() {
        /*** set USB reader in-app permission ***/
        /***
         pms: 0 = Disable USB reader in-app permission (default).
         pms: 1 = Enable USB reader in-app permission.
         pms: -1 = Get current permissions state.
         ***/

        int pms = 1;
        NALibs.setPermissionsNA(pms);

        clearReturnResponse();

        writeFile(RootFolder + LICFileName, "rdnidlib.dls");                         // Write file Licence

        /*===================== Open Libs =====================*/
        NALibs.openLibNA(RootFolder + LICFileName);

        if (iRes != 0) {
            tv_Result.setClickable(false);
            tv_Result.setText("Open Lib failed; Plese restart app");
            bt_SelectReader.setEnabled(false);
            bt_Read.setEnabled(false);
            bt_UpdateLicense.setEnabled(false);
            bt_Exit.setEnabled(true);
            return;
        }

        /*================= get License Info =================*/
        clearReturnResponse();
        String[] data = new String[1];
        NALibs.getLicenseInfoNA(data);
        if (data[0] != null) {
            tv_LicenseInfo.setText("License Info: " + data[0]);
        }
        bt_SelectReader.setEnabled(true);
        bt_Read.setEnabled(true);
        bt_UpdateLicense.setEnabled(true);
        bt_Exit.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*================= Deselect Reader =================*/
        NALibs.deselectReaderNA();

        /*================= Close Lib =================*/
        NALibs.closeLibNA();
        System.exit(0);
    }

    public void setText(TextView tv, final String message) {
        tv_Result.setClickable(false);
        final TextView textView = tv;
        handler.post(new Runnable() {
            public void run() {
                textView.setText(message);
            }
        });
    }

    public void clearReturnResponse() {
        iRes = -999;
        sRes = "";
        aRes = null;
        byteRes = null;
    }

    public void setEnableButton(final boolean SelectReader, final boolean Read, final boolean UpdateLicense, final boolean Exit) {
        handler.post(new Runnable() {
            public void run() {
                bt_SelectReader.setEnabled(SelectReader);
                bt_Read.setEnabled(Read);
                bt_UpdateLicense.setEnabled(UpdateLicense);
                bt_Exit.setEnabled(Exit);
            }
        });
    }

    public void writeFile(String Path, String Filename) {
        AssetManager assetManager = getAssets();
        try {
            InputStream is = assetManager.open(Filename);
            File out = new File(Path);
            if (out.exists())
                return;
            File parent = new File(RootFolder);
            parent.mkdirs();
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(out);
            int read;

            while ((read = is.read(buffer, 0, 1024)) >= 0) {
                fos.write(buffer, 0, read);
            }

            fos.flush();
            fos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitResponse() {
        while (!bReturnResponseFinish) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void waitActivityResult() {
        while (!bReturnActivityResultFinish) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    printException(ExceptionNA.NA_STORAGE_PERMISSION_ERROR, "");
                    setEnableButton(false, false, false, true);
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        bReturnActivityResultFinish = true;
                        resultActivity[0] = true;
                        resultActivity[1] = false;
                        break;
                    case Activity.RESULT_CANCELED:
                        displayLocationRetry();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    class MyHandler extends Handler {
        MyHandler(Looper myLooper) {
            super(myLooper);
        }

        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            switch (message) {

                /*================= When Click [Find Reader Button]   =================*/
                case "findreader": {
                    int listOption = NA_POPUP + NA_FIRST + NA_SCAN + NA_BT + NA_USB;     //0xD3 USB & BT Reader
                    setEnableButton(false, false, false, false);
                    setText(tv_Reader, "Reader scanning...");

                    /*================= get Reader List =================*/
                    bReturnResponseFinish = false;
                    clearReturnResponse();

                    if ((listOption & NA_SCAN) != 0 && (listOption & NA_BT) != 0) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && displayLocationSettingsRequest()) {
                            listOption = listOption;
                        } else {
                            listOption = listOption - (NA_SCAN + NA_BT);  //remove BT Scanning
                        }
                    }

                    NALibs.getReaderListNA(listOption);

                    waitResponse();

                    printException(iRes, "");

                    if (iRes <= 0) {
                        //setText(tv_Reader, "Reader not found.");
                        setText(tv_Reader, "ไม่พบอุปกรณ์อ่านบัตร");
                        setEnableButton(true, true, true, true);
                        break;
                    }

                    String readerSelect = aRes.get(0);

                    /*================= Select Reader =================*/
                    bReturnResponseFinish = false;
                    clearReturnResponse();
                    setText(tv_Reader, "Reader Selecting...");
                    NALibs.selectReaderNA(readerSelect);
                    waitResponse();

                    printException(iRes, "");

                    setEnableButton(true, true, true, true);
                    if (iRes != ExceptionNA.NA_SUCCESS && iRes != ExceptionNA.NA_INVALID_LICENSE && iRes != ExceptionNA.NA_LICENSE_FILE_ERROR) {
                        setText(tv_Reader, "Reader not found.");
                        break;
                    }else if(iRes == ExceptionNA.NA_INVALID_LICENSE || iRes == ExceptionNA.NA_LICENSE_FILE_ERROR) {
                        setText(tv_Reader, "Reader: " + readerSelect);
                        break;
                    }

                    setText(tv_Reader, "Reader: " + readerSelect);

                    String[] data = new String[1];
                    if(NALibs.getReaderInfoNA(data) == 0){
                        setText(tv_Result,"getReaderInfoNA: " + data[0]);
                    }

                    break;
                }

                /*================= When Click [Read Button] =================*/
                case "read": {
                    long startTime = System.currentTimeMillis();
                    setEnableButton(false, false, false, false);
                    setText(tv_Result, "");
                    setText(card_id_result,"");
                    flname.setText("");
                    sex_result.setText("");
                    dob_result.setText("");
                    date_issue_result.setText("");
                    date_exp_result.setText("");
                    address_result.setText("");
                    // end

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv_Photo.setImageResource(R.mipmap.ic_launcher);
                        }
                    });

                    /*================= Connect Card =================*/
                    int result = NALibs.connectCardNA();
                    if (result != ExceptionNA.NA_SUCCESS) {
                        setEnableButton(true, true, true, true);
                        printException(result, "");
                        //setText(tv_Result, "Card connection error.");
                        break;
                    }

                    /*================= Get NID Text =================*/
                    bReturnResponseFinish = false;
                    clearReturnResponse();
                    NALibs.getNIDTextNA();

                    waitResponse();
                    printException(iRes, "");

                    if (iRes != ExceptionNA.NA_SUCCESS) {
                        setEnableButton(true, true, true, true);
                        NALibs.disconnectCardNA();
                        break;
                    }

                    //setText(tv_Result, sRes);

                    String[] _result = sRes.split("#");
                    //card_id_result.setText("?????");

                    card_id_result.setText(_result[0]);

                    _flholder =  _result[1]+ " " + _result[2] + " " + _result[3] + " " + _result[4];
                    _addressholder = _result[9] + " " +_result[10] + " " + _result[11] + " " + _result[12] + " " +
                            _result[13] + " " + _result[14] + " " + _result[15] + " " + _result[16];
                    flname.setText(_flholder);

                    sex_result.setText(_thaiIdFilter.ProvideGender(_result[17]));
                    dob_result.setText(_thaiIdFilter.FormatDate(_result[18]));
                    date_issue_result.setText(_thaiIdFilter.FormatDate(_result[20]));
                    date_exp_result.setText(_thaiIdFilter.FormatDate(_result[21]));
                    address_result.setText(_addressholder);




                   // TextView _cardid = findViewById(R.id.card_id_result);
                    //TextView _flname = findViewById(R.id.flname_result);

                   // _cardid.setText(_result[0]);

                    //_cardid.setText(_result[1] + " " + _result[2] + " " + _result[3] + " " + _result[4]);

                   // TextView _splitResult = findViewById(R.id._splitResult);
                    //setText(_splitResult,  Integer.toString(_splitResult.length()));

                    final long difference = System.currentTimeMillis() - startTime;
                    final BigDecimal bd = new BigDecimal(difference / 1000.0);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv_Photo.setImageResource(R.mipmap.nas);
                            setText(tv_Result, tv_Result.getText().toString() + "\nRead Text: " + bd.setScale(2, RoundingMode.HALF_UP) + " s");
                        }
                    });

                    /*================= Get NID Photo =================*/
                    bReturnResponseFinish = false;
                    clearReturnResponse();
                    NALibs.getNIDPhotoNA();

                    waitResponse();

                    printException(iRes, tv_Result.getText().toString());
                    if (iRes == 0) {
                        final Bitmap bMap = BitmapFactory.decodeByteArray(byteRes, 0, byteRes.length);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                iv_Photo.setImageBitmap(bMap);
                            }
                        });
                    }

                    /*================= Disconnect Card =================*/
                    NALibs.disconnectCardNA();

                    setEnableButton(true, true, true, true);

                    if (iRes >= 0) {
                        final long difference2 = System.currentTimeMillis() - startTime;
                        final BigDecimal bd2 = new BigDecimal(difference2 / 1000.0);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                setText(tv_Result, tv_Result.getText().toString() + ", Text+Photo: " + bd2.setScale(2, RoundingMode.HALF_UP) + " s");
                            }
                        });
                    }
                    break;
                }

                /*================= When Click [Update License Button] =================*/
                case "updatelicense": {
                    setEnableButton(false, false, false, false);

                    /*================= Update License File =================*/
                    bReturnResponseFinish = false;
                    clearReturnResponse();
                    NALibs.updateLicenseFileNA();
                    waitResponse();

                    /*================= Retry Update =================*/
                    if (iRes == ExceptionNA.NA_LICENSE_UPDATE_ERROR) {
                        bReturnResponseFinish = false;
                        clearReturnResponse();
                        NALibs.updateLicenseFileNA();
                        waitResponse();
                    }

                    printException(iRes, "");

                    if (iRes == ExceptionNA.NA_SUCCESS) {
                        String[] data = new String[1];
                        NALibs.getLicenseInfoNA(data);
                        if (data[0] != null) {
                           setText(tv_LicenseInfo, "License Info: " + data[0]);
                        }
                        setText(tv_Result, iRes + ": License has been successfully updated.");
                    } else if (iRes == 1) {
                        setText(tv_Result, iRes + ": The latest license has already been installed.");
                    }

                    setEnableButton(true, true, true, true);
                    break;
                }
            }
        }
    }

    private void printException(int ex, String OldText) {

        if (OldText.compareTo("") != 0) {
            OldText += "\n\n";
        }
        switch (ex) {
            case ExceptionNA.NA_INTERNAL_ERROR:
                setText(tv_Result, OldText + "-1 Internal error.");
                break;

            case ExceptionNA.NA_INVALID_LICENSE:
                setText(tv_Result, OldText + "-2 This reader is not licensed.");
                break;

            case ExceptionNA.NA_READER_NOT_FOUND:
                setText(tv_Result, OldText + "-3 Reader not found.");
                break;

            case ExceptionNA.NA_CONNECTION_ERROR:
                setText(tv_Result, OldText + "-4 Card connection error.");
                break;

            case ExceptionNA.NA_GET_PHOTO_ERROR:
                setText(tv_Result, OldText + "-5 Get photo error.");
                break;

            case ExceptionNA.NA_GET_TEXT_ERROR:
                setText(tv_Result, OldText + "-6 Get text error.");
                break;

            case ExceptionNA.NA_INVALID_CARD:
                setText(tv_Result, OldText + "-7 Invalid card.");
                break;

            case ExceptionNA.NA_UNKNOWN_CARD_VERSION:
                setText(tv_Result, OldText + "-8 Unknown card version.");
                break;

            case ExceptionNA.NA_DISCONNECTION_ERROR:
                setText(tv_Result, OldText + "-9 Disconnection error.");
                break;

            case ExceptionNA.NA_INIT_ERROR:
                setText(tv_Result, OldText + "-10 Init error.");
                break;

            case ExceptionNA.NA_READER_NOT_SUPPORTED:
                setText(tv_Result, OldText + "-11 Reader not supported.");
                break;

            case ExceptionNA.NA_LICENSE_FILE_ERROR:
                setText(tv_Result, OldText + "-12 License file error.");
                break;

            case ExceptionNA.NA_PARAMETER_ERROR:
                setText(tv_Result, OldText + "-13 Parameter error.");
                break;

            case ExceptionNA.NA_INTERNET_ERROR:
                setText(tv_Result, OldText + "-15 Internet error.");
                break;

            case ExceptionNA.NA_CARD_NOT_FOUND:
                setText(tv_Result, OldText + "-16 Card not found.");
                //setText();
                break;

            case ExceptionNA.NA_BLUETOOTH_DISABLED:
                setText(tv_Result, OldText + "-17 Bluetooth is disabled.");
                break;

            case ExceptionNA.NA_LICENSE_UPDATE_ERROR:
                setText(tv_Result, OldText + "-18 License update error.");
                break;

            case ExceptionNA.NA_STORAGE_PERMISSION_ERROR:
                setText(tv_Result, OldText + ExceptionNA.NA_STORAGE_PERMISSION_ERROR + " Storage permission error: Settings >");
                tv_Result.setClickable(true);
                tv_Result.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        if (!flagSetting) {
                            flagSetting = true;
                            startActivity(intent);
                        }
                    }
                });
                break;

            case ExceptionNA.NA_LOCATION_PERMISSION_ERROR:
                setText(tv_Result, OldText + ExceptionNA.NA_LOCATION_PERMISSION_ERROR + " Location permission error: Settings >");
                tv_Result.setClickable(true);
                tv_Result.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        if (!flagSetting) {
                            flagSetting = true;
                            startActivity(intent);
                        }
                    }
                });
                break;

            case ExceptionNA.NA_LOCATION_SERVICE_ERROR:
                setText(tv_Result, OldText + "-41 Location service error.");
                break;

            default:
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flagSetting) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            flagSetting = false;
        }
    }

    private boolean displayLocationSettingsRequest() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            bReturnActivityResultFinish = false;
            resultActivity[0] = false;
            resultActivity[1] = false;
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(MainActivity.this).addApi(LocationServices.API).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000 / 2);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            Task result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        bReturnActivityResultFinish = true;
                        resultActivity[0] = true;
                        resultActivity[1] = false;
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;

                                    resolvable.startResolutionForResult(
                                            MainActivity.this,
                                            REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException ignored) {
                                    bReturnActivityResultFinish = true;
                                    resultActivity[0] = false;
                                    resultActivity[1] = false;
                                } catch (ClassCastException ignored) {
                                    bReturnActivityResultFinish = true;
                                    resultActivity[0] = false;
                                    resultActivity[1] = false;
                                }
                                break;
                        }
                    }
                }
            });

            waitActivityResult();

            if (!resultActivity[0] && resultActivity[1]) {
                displayLocationSettingsRequest();
            }

            return resultActivity[0];

        } else {
            return true;
        }

    }

    private void displayLocationRetry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("หากไม่เปิดใช้งานบริการตำแหน่ง (Google's location service) จะใช้งานเครื่องอ่านแบบบลูทูธไม่ได้");
        builder.setCancelable(false);
        builder.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                bReturnActivityResultFinish = true;
                resultActivity[0] = false;
                resultActivity[1] = false;
            }
        });
        builder.setNegativeButton("ลองใหม่", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                bReturnActivityResultFinish = true;
                resultActivity[0] = false;
                resultActivity[1] = true;
            }
        });
        builder.show();
    }




}