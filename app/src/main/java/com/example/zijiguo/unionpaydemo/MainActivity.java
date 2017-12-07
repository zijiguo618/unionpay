package com.example.zijiguo.unionpaydemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.text.TextUtils;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.android.volley.NetworkResponse;
import android.app.Activity;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;

import android.os.Handler;

import org.json.*;

import java.io.*;
import com.unionpay.UPPayAssistEx;
import android.app.ProgressDialog;

import java.util.HashMap;
import java.util.Calendar;
import java.util.Map;
import java.net.HttpURLConnection;

import com.android.volley.AuthFailureError;

public class MainActivity extends BaseActivity {
    @BindView(R.id.click)
    TextView Clickview;
    @BindView(R.id.textView)
    TextView Statusview;

//    private Context mContext = null;
    private int mGoodsIdx = 0;
    private Handler mHandler = null;
    private ProgressDialog mLoadingDialog = null;
    private final String mMode = "00";
    @Override
    public void doStartUnionPayPlugin(Activity activity, String tn, String mode) {
        if(UPPayAssistEx.checkInstalled(this)){
            UPPayAssistEx.startPay(activity, null, null, tn.trim(), mode);

        }else{
            Log.e("---------UPPay---------","noUppay installed");
        }

    }

    public void updateTextView(TextView tv) {
        String txt = "After";
        tv.setText(txt);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.click})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.click:
//                Log.e("onclick_test", " " + view.getTag());
//                mGoodsIdx = (Integer) view.getTag();
//
//                mLoadingDialog = ProgressDialog.show(mContext, // context
//                        "", // title
//                        "正在努力的获取tn中,请稍候...", // message
//                        true); // 进度是否是不确定的，这只和创建进度条有关
////
//                /*************************************************
//                 * 步骤1：从网络开始,获取交易流水号即TN
//                 ************************************************/
//
//                doStartUnionPayPlugin(this, "31231314",mMode);
                applytoapi();
                break;
        }
    }

    private void applytoapi() {
        if (TextUtils.isEmpty(Config.TOKEN)) {
            new AlertDialog.Builder(this)
                    .setTitle("Warn")
                    .setMessage("Need config API TOKEN")
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    //
                                    finish();
                                }
                            }).show();
            return;
        }
        Log.e("Applaytoapi", "onclick works");
        Calendar calender = Calendar.getInstance();
        RequestQueue queue = Volley.newRequestQueue(this);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(Calendar.getInstance().getTime());
        String url = "https://api.nihaopay.com/v1.2/transactions/apppay";
        final JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("amount", "100");
        jsonBody.addProperty("currency", "USD");
        jsonBody.addProperty("vendor", "unionpay");
        jsonBody.addProperty("reference", "reference1"+ dateStr.toString().trim());

        jsonBody.addProperty("ipn_url", "https://demo.nihaopay.com/ipn");
        Log.e("referencenumber","reference1"+dateStr.toString().trim());
        final String requestBody = jsonBody.toString();
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Statusview.setText("Response Success");
                        Log.e("Applaytoapi Response onres", response);
                        Log.d("-----------","-----------");
                        Log.d("substring13",response.substring(13));
                        doStartUnionPayPlugin(MainActivity.this, "663350450703226382000",mMode);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        Log.e("response zhongyu",res+"---------------------");
                        JSONObject obj = new JSONObject(res);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                }
                Log.e("Applaytoapi Response", "---------------------");
                Statusview.setText("That didn't work!");
            }

        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonBody.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
//                params.put("Content-Type", "application/json");
                headers.put("authorization", "Bearer " + Config.TOKEN);
                return headers != null ? headers : super.getHeaders();
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.e("Response Code", "-----------response:" + response.data.toString());
                int statusCode = response.statusCode;
                switch (statusCode) {
                    case HttpURLConnection.HTTP_OK:
                        Log.e("Response Code", "SUCCESSE" + statusCode);
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        Log.e("Response Code", "Not found" + statusCode);
                        break;
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        //do stuff
                        Log.e("Response Code", "HTTP_INTERNAL_ERROR" + statusCode);
                        break;
                }

                return super.parseNetworkResponse(response);
            }
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
