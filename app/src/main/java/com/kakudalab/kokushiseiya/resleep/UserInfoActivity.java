package com.kakudalab.kokushiseiya.resleep;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
/**
 * ユーザー情報を入力させるフレーム
 * 初期起動時のみ表示
 */
public class UserInfoActivity extends Activity implements OnClickListener, HeartRateConsentListener {
    private static String TAG = "ReSleep";
    private EditText name, old;
    private RadioButton checkedButton;
    private boolean male = false;
    private Button enterButton;
    private BandClient client = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_frame);

        name = (EditText) findViewById(R.id.nameText);
        old = (EditText) findViewById(R.id.oldText);

        RadioGroup sexRadioGroup = (RadioGroup) findViewById(R.id.sexRadioGroup); //ラジオボタンのグループ化
        sexRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedButton = (RadioButton) findViewById(checkedId);
                if (checkedButton.getText().equals("男")) {
                    male = true;//男か女かの取得
                }
            }
        });

        enterButton=(Button)findViewById(R.id.enterButton);
        enterButton.setOnClickListener(this);

        //バンドとコネクトする
        appTask task = new appTask();
        task.execute();
    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            bandService.unsetSensorListener();
        }
    }
    */

    @Override
    protected void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }


    /**
     * 心拍数センサーへのアクセス許可を行った時の動作
     * @param b アクセス許可の可否
     */
    @Override
    public void userAccepted(boolean b) {

    }

    /**
     * メッセージをログに追加するメソッド
     * @param message メッセージ
     */
    public void appendToUI(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, message);
            }
        });
    }

    /**
     * MicrosoftBandが接続されているか
     * @return 接続されていたらtrue
     * @throws InterruptedException アプリケーションの例外処理
     * @throws BandException MicrosoftBandの例外処理
     */
    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    /**
     * バンドとスマホをつなげるためのクラス
     */
    private class appTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {

                    appendToUI("Band is connected.\n");
                    if(client.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                        client.getSensorManager().requestHeartRateConsent(UserInfoActivity.this, UserInfoActivity.this);
                    }

                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

    public void onClick(View view) {

        if(view == enterButton) {
            //SleepTimeActivityに遷移
            Intent intent = new Intent(this, WakeTimeActivity.class);
            intent.putExtra("NAME", name.getText().toString());
            intent.putExtra("OLD", Integer.valueOf(old.getText().toString()));
            intent.putExtra("MALE", male);
            startActivity(intent);
            this.finish();
        }
    }

}

