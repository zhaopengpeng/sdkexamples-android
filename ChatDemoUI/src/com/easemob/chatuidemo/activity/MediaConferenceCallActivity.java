package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.List;

import u.aly.ac;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMCallManager;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMediaConferenceCallListener;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.widget.HorizontalListView;
import com.easemob.exceptions.EaseMobException;

public class MediaConferenceCallActivity extends BaseActivity implements OnClickListener {
    // private TextView tokenSatus = null;
    private ImageButton requireToken;
    private boolean isTalkTokenGranted = false;
    private String confId;
    private String confName;
    private ImageView ledImageview;
    private HorizontalListView usersListView;
    private TextView micInfoText;
    private TextView roomInfo;
    private Button exitBtn;
    private Chronometer chronometer;

    private List<String> members = new ArrayList<String>();
    private UserAdapter adapter;
    private ProgressBar progressBar;

    private String currentTalkUser;
    private TextView membersCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_conference_call);

        // tokenSatus = (TextView) findViewById(R.id.token_status);
        requireToken = (ImageButton) findViewById(R.id.btn_talk);
        ledImageview = (ImageView) findViewById(R.id.iv_led);
        exitBtn = (Button) findViewById(R.id.btn_exit);
        micInfoText = (TextView) findViewById(R.id.tv_mic_info);
        membersCountText = (TextView) findViewById(R.id.count_tv);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        roomInfo = (TextView) findViewById(R.id.tv_title);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        usersListView = (HorizontalListView) findViewById(R.id.user_listview);

        // usersListView.setSelected(selected)

        confId = getIntent().getStringExtra("confId");
        confName = getIntent().getStringExtra("confName");
        roomInfo.setText(confName);
        exitBtn.setOnClickListener(this);
        
        currentTalkUser = getCurrentSpeaker();
        // 显示members
        showGroupMembers();
        // 设置监听
        addCallListen();
        switchSpeaker(true);

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("releasing token");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);

        requireToken.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final View fv = v;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isTalkTokenGranted) {
                        // 请求获取话语权
                        requireTalkToken(new EMCallBack() {

                            @Override
                            public void onSuccess() {
                                // TODO Auto-generated method stub
                                isTalkTokenGranted = true;
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        //当前说话人置为自己
                                        currentTalkUser = EMChatManager.getInstance().getCurrentUser();
                                        if(adapter != null)
                                            adapter.notifyDataSetChanged();
                                        
                                        chronometer.setVisibility(View.VISIBLE);
                                        chronometer.setBase(SystemClock.elapsedRealtime());
                                        // 开始记时
                                        chronometer.start();
                                        micInfoText.setText("说话中...");
                                        // TODO Auto-generated method stub
                                        ledImageview.setImageResource(R.drawable.talk_room_led_green);
                                        fv.setPressed(true);
                                    }

                                });
                            }

                            @Override
                            public void onError(int code, String message) {
                                isTalkTokenGranted = false;
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        micInfoText.setText("");
                                        if(currentTalkUser != null)
                                            micInfoText.setText(currentTalkUser);
                                        Toast.makeText(MediaConferenceCallActivity.this, "failed to require the talk token", 0).show();
                                    }

                                });
                            }

                            @Override
                            public void onProgress(int progress, String status) {
                                // TODO Auto-generated method stub

                            }

                        });
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isTalkTokenGranted) {
                        pd.show();
                        // 释放话语token
                        releaseTalkToken(new EMCallBack() {

                            @Override
                            public void onSuccess() {
                                // TODO Auto-generated method stub
                                // TODO Auto-generated method stub
                                isTalkTokenGranted = false;
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        chronometer.stop();
                                        chronometer.setVisibility(View.GONE);
                                        micInfoText.setText("");
                                        ledImageview.setImageResource(R.drawable.talk_room_led_black);
                                        fv.setPressed(false);
                                        pd.dismiss();
                                    }
                                });
                            }

                            @Override
                            public void onError(int code, String message) {
                                final String msg = message;
                                // TODO Auto-generated method stub
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(MediaConferenceCallActivity.this,
                                                "failed to release the talk token" + " due to " + msg, 0).show();
                                        pd.dismiss();
                                    }

                                });
                            }

                            @Override
                            public void onProgress(int progress, String status) {
                                // TODO Auto-generated method stub

                            }

                        });
                    }
                    return true;
                }
                return false;
            }

        });
    }

    /**
     * 请求talk token
     */
    private void requireTalkToken(final EMCallBack callback) {
        micInfoText.setText("准备中...");
        EMCallManager.getInstance().asyncRequireTalkToken(confId, callback);
    }

    /**
     * 释放话语token
     */
    private void releaseTalkToken(final EMCallBack callback) {
        currentTalkUser = null;
        if(adapter != null)
            adapter.notifyDataSetChanged();
        EMCallManager.getInstance().asyncReleaseTalkToken(confId, callback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_exit:
            finish();
            break;

        default:
            break;
        }
    }

    @Override
    public void onDestroy() {
        switchSpeaker(true);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);

        exitRoom();
        super.onDestroy();
    }

    /**
     * 退出房间
     */
    private void exitRoom() {
        EMCallManager.getInstance().asyncExitMediaConferenceRoom(confId);
    }

    void switchSpeaker(boolean on) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (on) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
        }
        // audioManager.setMode(AudioManager.MODE_NORMAL);
    }

    /**
     * 会议成员adpater
     *
     */
    private class UserAdapter extends ArrayAdapter<String> {

        public UserAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(getContext(), R.layout.talk_room_user_item, null);
            String username = getItem(position);

            ImageView avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            TextView nick = (TextView) convertView.findViewById(R.id.tv_nickname);
            if (username.equals(currentTalkUser))
                avatar.setBackgroundResource(R.drawable.talk_room_avatar_item_frame);
            else
                avatar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            nick.setText(username);
            return convertView;
        }

    }

    /**
     * 设置监听
     */
    private void addCallListen() {
        EMCallManager.getInstance().addMediaConferenceCallListener(new EMMediaConferenceCallListener() {

            @Override
            public void onReleasedToken(String actor) {
                currentTalkUser = null;
                Message msg = handler.obtainMessage(RELEASE, actor);
                handler.sendMessage(msg);
            }

            @Override
            public void onJoinedRoom(String actor) {
                if (members != null && !members.contains(actor)) {
                    members.add(actor);

                    Message msg = handler.obtainMessage(JOIN, actor);
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onExitedRoom(String actor) {
                if (members != null && members.contains(actor)) {
                    members.remove(actor);

                    Message msg = handler.obtainMessage(EXIT, actor);
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onAcquiredToken(String actor) {
                currentTalkUser = actor;
                Message msg = handler.obtainMessage(ACQUIRE, actor);
                handler.sendMessage(msg);
            }
        });
    }

    private static final int JOIN = 0;
    private static final int ACQUIRE = 1;
    private static final int RELEASE = 2;
    private static final int EXIT = 3;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            String username = (String) msg.obj;
            if(!EMChatManager.getInstance().getCurrentUser().equals(username) && adapter != null){
                adapter.notifyDataSetChanged();
            }
            switch (msg.what) {
            case JOIN:
                Toast.makeText(getApplicationContext(), username+"加入房间", 0).show();
                membersCountText.setText(String.valueOf(members.size()));
                break;
            case ACQUIRE:
                //正在讲话被人抢掉话语权，置为false
                if(!EMChatManager.getInstance().getCurrentUser().equals(currentTalkUser)){
                    isTalkTokenGranted = false;
                    ledImageview.setVisibility(View.INVISIBLE);
                }
                micInfoText.setText(username);
                chronometer.setVisibility(View.VISIBLE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                // 开始记时
                chronometer.start();
                break;
            case RELEASE:
                micInfoText.setText("");
                chronometer.stop();
                chronometer.setVisibility(View.GONE);
                break;
            case EXIT:
                Toast.makeText(getApplicationContext(), username+"退出房间", 0).show();
                membersCountText.setText(String.valueOf(members.size()));
                break;

            default:
                break;
            }
        };
    };

    private void showGroupMembers() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMGroup group = EMGroupManager.getInstance().getGroupFromServer(confId);
                    String owner = group.getOwner();
                    members.addAll(group.getMembers());
                    if(owner != null && members.contains(owner))
                        members.remove(group.getOwner());
                    runOnUiThread(new Runnable() {

                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            membersCountText.setText(String.valueOf(members.size()));
                            // 设置adapter
                            adapter = new UserAdapter(MediaConferenceCallActivity.this, 1, members);
                            usersListView.setAdapter(adapter);
                        }
                    });

                } catch (EaseMobException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "获取群成员失败", 0).show();
                        }
                    });
                }
            }
        }).start();

    }
    
    /**
     * 获取当前说话人
     * @return
     */
    private String getCurrentSpeaker(){
        return EMCallManager.getInstance().getMediaConfCallCurrentSpeaker();
    }
}
