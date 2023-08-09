package ph.com.api.sms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;

import android.os.Handler;
import android.telephony.SmsManager;

import android.util.Log;
import android.view.Menu;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    Button sendBtn;
    Button senderBtn;
    EditText txtPhone;
    EditText txtMessage;
    String phoneNo;
    String message;
    JSONObject resJSONObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn = (Button) findViewById(R.id.btnSendSMS);
        senderBtn = (Button) findViewById(R.id.btnSender);
        txtPhone = (EditText) findViewById(R.id.editText);
        txtMessage = (EditText) findViewById(R.id.editText2);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendSMSMessage();
            }
        });

        senderBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                doSend();
            }
        });

        doSend();

    }

    protected void delay(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                senderBtn.performClick();
            }
        }, 10000);
    }

    protected void doUpdate(int id) {
        //
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://connect.api.com.ph/sms/update?id="+id;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (null != response) {
                            resJSONObj = response;
                            Toast.makeText(getApplicationContext(), String.valueOf(resJSONObj), Toast.LENGTH_LONG).show(); // DEBUG
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }

    protected void doSend() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://connect.api.com.ph/sms/get";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (null != response) {
                            resJSONObj = response;
                            //
                            try {
                                if(response.getString("status").compareTo("200")==0){
                                    JSONArray jArray = resJSONObj.getJSONArray("data");
                                    for (int i=0; i < jArray.length(); i++)
                                    {
                                        try {
                                            JSONObject oneObject = jArray.getJSONObject(i);
                                            String phone = oneObject.getString("sms_number");
                                            String sms = oneObject.getString("sms_message").replace("\\n", System.getProperty("line.separator"));
                                            // SET DATA
                                            txtPhone.setText(phone);
                                            txtMessage.setText(sms);
                                            //
                                            phoneNo = txtPhone.getText().toString();
                                            message = txtMessage.getText().toString();
                                            Toast.makeText(getApplicationContext(), phoneNo+":"+message, Toast.LENGTH_LONG).show(); // DEBUG
                                            // SEND DATA
                                            sendBtn.performClick();
                                            // UPDATE DATA
                                            doUpdate(Integer.parseInt(oneObject.getString("sms_id")));
                                        } catch (JSONException e) {
                                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show(); // DEBUG
                                        }
                                    }
                                    delay();
                                }else{
                                    Toast.makeText(getApplicationContext(), response.getString("status").toString(), Toast.LENGTH_LONG).show(); // DEBUG
                                    delay();
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                senderBtn.performClick();
            }
        });
        queue.add(request);
    }

    protected void sendSMSMessage() {
        //
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }else{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

}