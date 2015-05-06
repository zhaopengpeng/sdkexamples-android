package com.easemob.widget.chatrow;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.uidemo.R;
import com.easemob.util.EMLog;
import com.easemob.widget.activity.ContextMenu;

public class EMChatRowVoiceWidget extends EMChatRowWidget {
	
	private static final String TAG = "EMChatRowVoiceWidget";

	public EMChatRowVoiceWidget(Context context, EMMessage message, int position, ViewGroup parent) {
		super(context);
		setupView(message, position, parent);
	}

	@Override
	public void setupView(EMMessage message, int position, ViewGroup parent) {
		convertView = inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
				R.layout.em_row_received_voice : R.layout.em_row_sent_voice, this);
		
		holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
		holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
		holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
		holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
		holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
		holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
		holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
	}

	public void updateView(final EMMessage message, final int position, ViewGroup parent) {
		setAvatar(message, position, convertView, holder);
		updateAckDelivered(message, position, convertView, holder);
		setResendListener(message, position, convertView, holder);
		setOnBlackList(message, position, convertView, holder);
		
		handleVoiceMessage(message, position, convertView, holder);
	}

	@Override
	public void updateSendedView(EMMessage message, ViewHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgress(EMMessage message, ViewHolder holder, int progress,
			String status) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 语音消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleVoiceMessage(final EMMessage message, final int position, View convertView, final ViewHolder holder) {
		VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
		holder.tv.setText(voiceBody.getLength() + "\"");
		holder.iv.setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status,
				chatWidget.getAdapter(), chatWidget.getActivity(), chatWidget.getToChatUsername()));
		holder.iv.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				chatWidget.getActivity().startActivityForResult(
						(new Intent(context, ContextMenu.class)).putExtra("position", position).putExtra("type",
								EMMessage.Type.VOICE.ordinal()), REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});
		if (VoicePlayClickListener.playMsgId != null
				&& VoicePlayClickListener.playMsgId.equals(message
						.getMsgId())&& VoicePlayClickListener.isPlaying) {
			AnimationDrawable voiceAnimation;
			if (message.direct == EMMessage.Direct.RECEIVE) {
				holder.iv.setImageResource(R.anim.voice_from_icon);
			} else {
				holder.iv.setImageResource(R.anim.voice_to_icon);
			}
			voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
			voiceAnimation.start();
		} else {
			if (message.direct == EMMessage.Direct.RECEIVE) {
				holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
			} else {
				holder.iv.setImageResource(R.drawable.chatto_voice_playing);
			}
		}
		
		
		if (message.direct == EMMessage.Direct.RECEIVE) {
			if (message.isListened()) {
				// 隐藏语音未听标志
				holder.iv_read_status.setVisibility(View.INVISIBLE);
			} else {
				holder.iv_read_status.setVisibility(View.VISIBLE);
			}
			EMLog.d(TAG, "it is receive msg");
			if (message.status == EMMessage.Status.INPROGRESS) {
				holder.pb.setVisibility(View.VISIBLE);
				EMLog.d(TAG, "!!!! back receive");
				((FileMessageBody) message.getBody()).setDownloadCallback(new EMCallBack() {

					@Override
					public void onSuccess() {
						chatWidget.getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								holder.pb.setVisibility(View.INVISIBLE);
								chatWidget.getAdapter().notifyDataSetChanged();
							}
						});

					}

					@Override
					public void onProgress(int progress, String status) {
					}

					@Override
					public void onError(int code, String message) {
						chatWidget.getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								holder.pb.setVisibility(View.INVISIBLE);
							}
						});

					}
				});

			} else {
				holder.pb.setVisibility(View.INVISIBLE);

			}
			return;
		}

		// until here, deal with send voice msg
		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			holder.pb.setVisibility(View.VISIBLE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		default:
			sendMsgInBackground(message, holder);
		}
	}
	
	public static class VoicePlayClickListener implements View.OnClickListener {
		public static String playMsgId;
		public static boolean isPlaying = false;
		public static VoicePlayClickListener currentPlayListener;

		EMMessage message;
		VoiceMessageBody voiceBody;
		ImageView voiceIconView;

		private AnimationDrawable voiceAnimation = null;
		MediaPlayer mediaPlayer = null;
		ImageView iv_read_status;
		Activity activity;
		private ChatType chatType;
		private BaseAdapter adapter;
		
		/**
		 * 
		 * @param message
		 * @param v
		 * @param iv_read_status
		 * @param context
		 * @param activity
		 * @param user
		 * @param chatType
		 */
		public VoicePlayClickListener(EMMessage message, ImageView v, ImageView iv_read_status, BaseAdapter adapter, Activity activity,
				String username) {
			this.message = message;
			voiceBody = (VoiceMessageBody) message.getBody();
			this.iv_read_status = iv_read_status;
			this.adapter = adapter;
			voiceIconView = v;
			this.activity = activity;
			this.chatType = message.getChatType();
			voiceIconView.setOnClickListener(this);
		}

		public void stopPlayVoice() {
			voiceAnimation.stop();
			if (message.direct == EMMessage.Direct.RECEIVE) {
				voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
			} else {
				voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
			}
			// stop play voice
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
			}
			VoicePlayClickListener.isPlaying = false;
			playMsgId = null;
			adapter.notifyDataSetChanged();
			currentPlayListener = null;
		}

		public void playVoice(String filePath) {
			if (!(new File(filePath).exists())) {
				return;
			}
			currentPlayListener = this;
			playMsgId = message.getMsgId();
			AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

			mediaPlayer = new MediaPlayer();
			// TODO: EMWidget
//			if (HXSDKHelper.getInstance().getModel().getSettingMsgSpeaker()) {
			if (true) {
				audioManager.setMode(AudioManager.MODE_NORMAL);
				audioManager.setSpeakerphoneOn(true);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			} else {
				audioManager.setSpeakerphoneOn(false);// 关闭扬声器
				// 把声音设定成Earpiece（听筒）出来，设定为正在通话中
				audioManager.setMode(AudioManager.MODE_IN_CALL);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
			}
			try {
				mediaPlayer.setDataSource(filePath);
				mediaPlayer.prepare();
				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						// TODO Auto-generated method stub
						mediaPlayer.release();
						mediaPlayer = null;
						stopPlayVoice(); // stop animation
						currentPlayListener = null;
					}

				});
				VoicePlayClickListener.isPlaying = true;
				mediaPlayer.start();
				showAnimation();

				// 如果是接收的消息
				if (message.direct == EMMessage.Direct.RECEIVE) {
					try {
						if (!message.isAcked) {
							message.isAcked = true;
							// 告知对方已读这条消息
							if (chatType != ChatType.GroupChat)
								EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
						}
					} catch (Exception e) {
						message.isAcked = false;
					}
					if (!message.isListened() && iv_read_status != null && iv_read_status.getVisibility() == View.VISIBLE) {
						// 隐藏自己未播放这条语音消息的标志
						iv_read_status.setVisibility(View.INVISIBLE);
						EMChatManager.getInstance().setMessageListened(message);
					}

				}

			} catch (Exception e) {
			}
		}

		// show the voice playing animation
		private void showAnimation() {
			// play voice, and start animation
			if (message.direct == EMMessage.Direct.RECEIVE) {
				voiceIconView.setImageResource(R.anim.voice_from_icon);
			} else {
				voiceIconView.setImageResource(R.anim.voice_to_icon);
			}
			voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
			voiceAnimation.start();
		}

		@Override
		public void onClick(View v) {
			String st = activity.getResources().getString(R.string.Is_download_voice_click_later);
			if (VoicePlayClickListener.isPlaying && currentPlayListener != null) {
				if (playMsgId != null && playMsgId.equals(message.getMsgId())) {
					currentPlayListener.stopPlayVoice();
					return;
				} else {
					currentPlayListener.stopPlayVoice();
				}
			}

			if (message.direct == EMMessage.Direct.SEND) {
				// for sent msg, we will try to play the voice file directly
				playVoice(voiceBody.getLocalUrl());
			} else {
				if (message.status == EMMessage.Status.SUCCESS) {
					File file = new File(voiceBody.getLocalUrl());
					if (file.exists() && file.isFile())
						playVoice(voiceBody.getLocalUrl());
					else
						EMLog.e(TAG, "file not exist");

				} else if (message.status == EMMessage.Status.INPROGRESS) {
					String s=new String();
					
					Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
				} else if (message.status == EMMessage.Status.FAIL) {
					Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							EMChatManager.getInstance().asyncFetchMessage(message);
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							super.onPostExecute(result);
							adapter.notifyDataSetChanged();
						}

					}.execute();

				}

			}
		}
	}

}

