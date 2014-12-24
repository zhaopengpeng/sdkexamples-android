package com.easemob.chatuidemo.activity;

import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMCallManager;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.widget.HorizontalListView;

public class MediaConferenceCallActivity extends BaseActivity implements OnClickListener{
//	private TextView tokenSatus = null;
	private ImageButton requireToken;
	private boolean isTalkTokenGranted = false;
	private String confId;
	private String confName;
	private ImageView ledImageview;
	private HorizontalListView usersListView;
	private TextView micInfoText;
	private Button exitBtn;
	private Chronometer chronometer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_conference_call);
		
//		tokenSatus = (TextView) findViewById(R.id.token_status);
		requireToken = (ImageButton) findViewById(R.id.btn_talk);
		ledImageview = (ImageView) findViewById(R.id.iv_led);
		exitBtn = (Button) findViewById(R.id.btn_exit);
		micInfoText = (TextView) findViewById(R.id.tv_mic_info);
		chronometer = (Chronometer) findViewById(R.id.chronometer); 
		
		usersListView = (HorizontalListView) findViewById(R.id.user_listview); 
		//设置adapter
		usersListView.setAdapter(new UserAdapter(this, 1, null));
//		usersListView.setSelected(selected)
		
		confId = getIntent().getStringExtra("confId");
		confName = getIntent().getStringExtra("confName");
		
		exitBtn.setOnClickListener(this);
		switchSpeaker(true);
		requireToken.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    final View fv = v;
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					if(!isTalkTokenGranted){
						//请求获取话语权
						requireTalkToken(new EMCallBack(){

                            @Override
                            public void onSuccess() {
                             // TODO Auto-generated method stub
                                isTalkTokenGranted = true;
                                runOnUiThread(new Runnable(){

                                    @Override
                                    public void run() {
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
                                // TODO Auto-generated method stub
                                runOnUiThread(new Runnable(){

                                    @Override
                                    public void run() {
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
				}else if(event.getAction() == MotionEvent.ACTION_UP){
				    if(isTalkTokenGranted){
				      //释放话语token
	                    releaseTalkToken(new EMCallBack(){

                            @Override
                            public void onSuccess() {
                                // TODO Auto-generated method stub
                             // TODO Auto-generated method stub
                                isTalkTokenGranted = false;
                                runOnUiThread(new Runnable(){

                                    @Override
                                    public void run() {
                                        chronometer.stop();
                                        chronometer.setVisibility(View.GONE);
                                        micInfoText.setText("");
                                        ledImageview.setImageResource(R.drawable.talk_room_led_black);
                                        fv.setPressed(false);
                                    }
                                });
                            }

                            @Override
                            public void onError(int code, String message) {
                                final String msg = message;
                                // TODO Auto-generated method stub
                                runOnUiThread(new Runnable(){

                                    @Override
                                    public void run() {
                                        Toast.makeText(MediaConferenceCallActivity.this, "failed to release the talk token" + " due to " + msg, 0).show();
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
	private void requireTalkToken(final EMCallBack callback){
		micInfoText.setText("准备中...");
		EMCallManager.getInstance().asyncRequireTalkToken(confId, callback);
	}
	
	/**
	 * 释放话语token
	 */
	private void releaseTalkToken(final EMCallBack callback){
		EMCallManager.getInstance().asyncReleaseTalkToken(confId,callback);
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
	public void onDestroy(){
	    switchSpeaker(true);
	    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        
		exitRoom();
		super.onDestroy();
	}
	
	/**
	 * 退出房间
	 */
	private void exitRoom(){
	    EMCallManager.getInstance().asyncExitMediaConferenceRoom(confId,new EMCallBack(){

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub
                
            }
	        
	    });
	}
	
	void switchSpeaker(boolean on){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if(on){
            audioManager.setSpeakerphoneOn(true);
        }else{
            audioManager.setSpeakerphoneOn(false);
        }
        // audioManager.setMode(AudioManager.MODE_NORMAL);
    }
	
	private class UserAdapter extends ArrayAdapter<String>{

		public UserAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null)
				convertView = View.inflate(getContext(), R.layout.talk_room_user_item, null);
			ImageView avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
			if(position == 0)
				avatar.setBackgroundResource(R.drawable.talk_room_avatar_item_frame);
			
			return convertView;
		}
		
		@Override
		public int getCount() {
			return 3;
		}
	}
}
