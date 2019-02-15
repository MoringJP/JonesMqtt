package com.mqtt.com.jonesmqtt;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.codec.MessageSupport;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private EditText mEditText;
    private static final String TAG = "MQTT Service";
    private static final String CONNECTION_SUCCEED = "连接到服务器成功";
    private static final String CONNECTION_FAILURE = "无法连接到服务器";
    private static final String CONNECTION_ERROR = "建立连接出错";
    private static final String CONNECTION_CORRECT = "连接已建立";
    private static final String MESSAGE_FAILED = "消息发送成功";
    private static final String MESSAGE_ERROR = "消息发送失败";
    private BlockingConnection connection;
    ;
    private Message message;
    private String serviicePasswd;
    private String serviceName;
    private String serviceId;
    private int port;
    private String mqttService;
    private Button mBack;
    private Button mCnn;

    private void getMqttServiceEditText(View view) {
        mMqttService = (EditText) view.findViewById(R.id.mqtt_service);
        mServicePassword = (EditText) view.findViewById(R.id.service_password_editText);
        mMqttServiceName = (EditText) view.findViewById(R.id.service_name_editText);
        mMqttServicePort = (EditText) view.findViewById(R.id.service_port_edittext);
        mMqttServicePort.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mMqttServiceId = (EditText) view.findViewById(R.id.service_id_editText);
        mCnn = (Button) view.findViewById(R.id.service_conn_button);
        mBack = (Button) view.findViewById(R.id.service_conn_button);
        mqttService = mMqttService.getText().toString();
        port = new Integer(mMqttServicePort.getText().toString());
        serviceId = mMqttServiceId.getText().toString();
        serviceName = mMqttServiceName.getText().toString();
        serviicePasswd = mServicePassword.getText().toString();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    View view = View.inflate(getBaseContext(),R.layout.setting_mqtt_service,null);
                    AlertDialog.Builder mDialog = new AlertDialog.Builder(MainActivity.this);
                    mDialog.setTitle(R.string.service_setting);
                    mDialog.setView(view);
                    mDialog.show();
                    getMqttServiceEditText(view);
                    mCnn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                InitMqtt(mqttService, port);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
                    return true;
                case R.id.navigation_dashboard:
                    PublishMessage("/jones", "hello");
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    StopMqtt();
                    return true;
            }
            return false;
        }
    };
    /*
    *登入服务器的相关信息
     */
    private Handler handler;
    private EditText mMqttServiceId;
    private EditText mMqttServicePort;
    private EditText mMqttServiceName;
    private EditText mServicePassword;
    private EditText mMqttService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.textView);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        final EditText mEditText = (EditText) findViewById(R.id.et_chat_message);
        Button mButton = (Button) findViewById(R.id.btn_chat_message_send);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublishMessage("/jones/led", mEditText.getText().toString());
            }
        });

    }

    /*
    *初始化Mqtt的连接
     */
    public void InitMqtt(String address, int port) {
        MQTT mqtt = new MQTT();
        try {
            mqtt.setHost(address, port);
            Log.e(TAG, CONNECTION_SUCCEED);
        } catch (Exception e) {
            Log.e(TAG, CONNECTION_FAILURE);
            Toast.makeText(this, "登入出错！", Toast.LENGTH_SHORT).show();
        }
        connection = mqtt.blockingConnection();
        try {
            connection.connect();
            Log.e(TAG, CONNECTION_CORRECT);
        } catch (Exception e) {
            Log.e(TAG, CONNECTION_ERROR);
        }
    }

    /*
    *向指定的主题发送消息
     */
    public void PublishMessage(String Top, String Missage) {
        try {
            connection.publish(Top, Missage.getBytes(), QoS.AT_LEAST_ONCE, false);
            Log.e(TAG, MESSAGE_FAILED);
        } catch (Exception e) {
            Log.e(TAG, MESSAGE_ERROR);
        }
    }

    /*
    *关闭连接额端口
     */
    public void StopMqtt() {
        try {

            connection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "关闭连接出错");
        }
    }

    /*
    *获取指定主题的信息
     */
    public String getMessage(String top) {
        String Mess = "null";
        Topic[] topices = {new Topic(top, QoS.AT_LEAST_ONCE)};
        try {
            connection.subscribe(topices);
            Log.e(TAG, "连接到指定主题");
        } catch (Exception e) {
            Log.e(TAG, "无法连接指定主题！");
        }
        try {
            message = connection.receive();
            Log.e(TAG, "打开主题消息成功！");
        } catch (Exception e) {
            Log.e(TAG, "无法获取主题中的消息！");
        }
        byte[] paylod = message.getPayload();
        Mess = new String(paylod);
        message.ack();
        return Mess;
    }

    protected void onRestart() {
        super.onRestart();
        mTextMessage.setText(getMessage("/jones/led"));
    }
}