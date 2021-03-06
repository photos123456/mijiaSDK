package com.mi.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mi.test.R;
import com.mi.utils.BaseActivity;
import com.mi.utils.TestConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.miot.api.CommonHandler;
import com.miot.api.CompletionHandler;
import com.miot.api.MiotManager;

import com.miot.common.device.Device;
import com.miot.common.exception.MiotException;
import com.miot.common.share.ShareStatus;
import com.miot.common.share.SharedRequest;
import com.miot.common.voice.VoiceCommand;
import com.miot.common.voice.VoiceResult;
import com.miot.common.voice.VoiceSession;

public class MainActivity extends BaseActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    @InjectView(R.id.btn_account)
    Button btnAccount;
    @InjectView(R.id.btn_device)
    Button btnDevice;
    @InjectView(R.id.btn_request)
    Button mBtnRequest;
    @InjectView(R.id.btn_accept)
    Button mBtnAccept;
    @InjectView(R.id.btn_security_conn)
    Button mBtnSConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MiotManager.getPeopleManager().isLogin()) {
                    showToast(R.string.account_not_login);
                    return;
                }

                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivity(intent);
            }
        });

        mBtnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querySharedRequests();
            }
        });

        mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                acceptSharedRequest();
                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent);
            }
        });
        mBtnSConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConnActivity.class);
                startActivity(intent);
            }
        });
    }

    private SharedRequest mRequest;

    private void querySharedRequests() {
        try {
            MiotManager.getDeviceManager().querySharedRequests(new CommonHandler<List<SharedRequest>>() {
                @Override
                public void onSucceed(List<SharedRequest> result) {
                    for (SharedRequest request : result) {
                        mRequest = request;
                        logSharedRequest(request);
                    }
                }

                @Override
                public void onFailed(int errCode, String description) {
                    Log.e(TAG, "querySharedRequests: " + errCode + description);
                }
            });
        } catch (MiotException e) {
            e.printStackTrace();
        }
    }

    private void acceptSharedRequest() {
        mRequest.setShareStatus(ShareStatus.accept);
        try {
            MiotManager.getDeviceManager().replySharedRequest(mRequest, new CompletionHandler() {
                @Override
                public void onSucceed() {
                    Log.e(TAG, "replySharedRequest onSucceed");
                }

                @Override
                public void onFailed(int errCode, String description) {
                    Log.e(TAG, "replySharedRequest: " + errCode + description);
                }
            });
        } catch (MiotException e) {
            e.printStackTrace();
        }
    }

    private void logSharedRequest(SharedRequest request) {
        StringBuilder sb = new StringBuilder();
        Device device = request.getSharedDevice();
        sb.append("invitedId=").append(request.getInvitedId())
                .append("  messageId=").append(request.getMessageId())
                .append("  status=").append(request.getShareStatus().toString())
                .append("  deviceId=").append(device.getDeviceId())
                .append("  owner=").append(device.getOwnerInfo().getUserId())
                .append("  ").append(device.getOwnerInfo().getUserName());
        Log.d(TAG, "shareRequest: " + sb.toString());
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case TestConstants.ACTION_BIND_SERVICE_SUCCEED:
                    showToast(R.string.bind_succeed);
                    break;

                case TestConstants.ACTION_BIND_SERVICE_FAILED:
                    showToast(R.string.bind_failed);
                    Log.d(TAG, "bind failed");
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TestConstants.ACTION_BIND_SERVICE_SUCCEED);
        filter.addAction(TestConstants.ACTION_BIND_SERVICE_FAILED);
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}