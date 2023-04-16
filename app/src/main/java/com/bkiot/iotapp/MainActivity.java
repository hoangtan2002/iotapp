package com.bkiot.iotapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi, seekBarLabel;
    LabeledSwitch btnLED, btnPUMP;
    SeekBar adjBar;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        txtTemp = findViewById(R.id.txtTempe);
        txtHumi = findViewById((R.id.txtHumi));
        seekBarLabel = findViewById(R.id.seekBarLabel);
        btnLED = findViewById(R.id.btnLED);
        btnPUMP = findViewById(R.id.btnPUMP);
        btnLED.setOnToggledListener(new OnToggledListener() {
                                        @Override
                                        public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                                            if (isOn == true) {
                                                sendDataMQTT("duytan2002/feeds/button01", "1");
                                            } else {
                                                sendDataMQTT("duytan2002/feeds/button01", "0");
                                            }
                                        }
                                    }
            );
        btnPUMP.setOnToggledListener(new OnToggledListener() {
                                        @Override
                                        public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                                            if (isOn == true) {
                                                sendDataMQTT("duytan2002/feeds/button02", "1");
                                            } else {
                                                sendDataMQTT("duytan2002/feeds/button02", "0");
                                            }
                                        }
                                    }
        );
        adjBar=findViewById(R.id.seekBar);
        adjBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                sendDataMQTT("duytan2002/feeds/freq",Integer.toString(progressChangedValue));
                seekBarLabel.setText("Send Period: " + progressChangedValue + " s");
                Log.d("TEST",Integer.toString(progressChangedValue));
            }
        });
        adjBar.setMax(30);
        adjBar.setProgress(10);
        startMQTT();
    }

        public void sendDataMQTT(String topic, String value){
            MqttMessage msg = new MqttMessage();
            msg.setId(1234);
            msg.setQos(0);
            msg.setRetained(false);

            byte[] b = value.getBytes(Charset.forName("UTF-8"));
            msg.setPayload(b);

            try {
                mqttHelper.mqttAndroidClient.publish(topic, msg);
            }catch (MqttException e){
            }
        }
    public void startMQTT(){
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Log.d("TEST", topic + "***" + message.toString());
                if(topic.contains("sensor01")){
                    txtTemp.setText("Temp\n"+message.toString() + "℃");
                } else if (topic.contains("sensor02")) {
                    txtHumi.setText("Humidity\n"+message.toString() + "%");
                } else if (topic.contains("button01")){
                    if(message.toString().equals("1")){
                        btnLED.setOn(true);
                    }else{
                        btnLED.setOn(false);
                    }
                } else if (topic.contains("button02")){
                    if(message.toString().equals("1")){
                        btnPUMP.setOn(true);
                    }else{
                        btnPUMP.setOn(false);
                    }
                } else if (topic.contains("freq")){
                    Log.d("FREQ",message.toString());
                    adjBar.setMax(30);
                    adjBar.setProgress(Integer.parseInt(message.toString()));
                    seekBarLabel.setText("Send Period: "+message+" s");
                }
                else if (topic.contains("sensor03")){
                    Toast.makeText(MainActivity.this, "Status from Gateway: " + message.toString()
                            , Toast.LENGTH_SHORT).show();
                }
                else if (topic.contains("ai")){
                    Toast.makeText(MainActivity.this, "Recognized: " + message.toString()
                            , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}