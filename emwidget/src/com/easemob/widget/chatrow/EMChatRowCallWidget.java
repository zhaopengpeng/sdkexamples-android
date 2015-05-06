package com.easemob.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.uidemo.Constant;
import com.easemob.uidemo.R;

public class EMChatRowCallWidget extends EMChatRowWidget {

	public EMChatRowCallWidget(Context context, EMMessage message, int position, ViewGroup parent) {
		super(context);
		setupView(message, position, parent);
	}

	@Override
	public void setupView(EMMessage message, int position, ViewGroup parent) {
		if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false))
			convertView = inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
					R.layout.em_row_received_voice_call : R.layout.em_row_sent_voice_call, this);
		// 视频通话
		else if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
			convertView = inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
					R.layout.em_row_received_video_call : R.layout.em_row_sent_video_call, this);
			
		holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
		holder.staus_iv = (ImageView) convertView
				.findViewById(R.id.msg_status);
		holder.iv_avatar = (ImageView) convertView
				.findViewById(R.id.iv_userhead);
		// 这里是文字内容
		holder.tv = (TextView) convertView
				.findViewById(R.id.tv_chatcontent);
		holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
		
		holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
		holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
	}
	
	public void updateView(final EMMessage message, final int position, ViewGroup parent) {
		setAvatar(message, position, convertView, holder);
		updateAckDelivered(message, position, convertView, holder);
		setResendListener(message, position, convertView, holder);
		setOnBlackList(message, position, convertView, holder);

		handleCallMessage(message, position, convertView, holder);
	}

	/**
	 * 音视频通话记录
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	private void handleCallMessage(EMMessage message, final int position, final View convertView, ViewHolder holder) {
		TextMessageBody txtBody = (TextMessageBody) message.getBody();
		holder.tv.setText(txtBody.getMessage());
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

}
