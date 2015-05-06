package com.easemob.widget.chatrow;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.NormalFileMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.uidemo.R;
import com.easemob.util.EMLog;
import com.easemob.util.FileUtils;
import com.easemob.util.TextFormater;

public class EMChatRowFileWidget extends EMChatRowWidget {
	private static final String TAG = "EMChatRowRecvFileWidget";
	private Timer timer = new Timer();

	public EMChatRowFileWidget(Context context, EMMessage message, int position, ViewGroup parent) {
		super(context);
		setupView(message, position, parent);
	}

	@Override
	public void setupView(EMMessage message, int position, ViewGroup parent) {
		convertView = inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
				R.layout.em_row_received_file : R.layout.em_row_sent_file, this);
		holder = new ViewHolder();
		convertView.setTag(holder);
		
		holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
		holder.tv_file_name = (TextView) convertView.findViewById(R.id.tv_file_name);
		holder.tv_file_size = (TextView) convertView.findViewById(R.id.tv_file_size);
		holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
		holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
		holder.tv_file_download_state = (TextView) convertView.findViewById(R.id.tv_file_state);
		holder.ll_container = (LinearLayout) convertView.findViewById(R.id.ll_file_container);
		// 这里是进度值
		holder.tv = (TextView) convertView.findViewById(R.id.percentage);
		holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
	}
	
	public void updateView(final EMMessage message, final int position, ViewGroup parent) {
		setAvatar(message, position, convertView, holder);
		updateAckDelivered(message, position, convertView, holder);
		setResendListener(message, position, convertView, holder);
		setOnBlackList(message, position, convertView, holder);
		
		handleFileMessage(message, position, convertView, holder);
	}
	
	/**
	 * 文件消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleFileMessage(final EMMessage message, final int position, View convertView, final ViewHolder holder) {
		final NormalFileMessageBody fileMessageBody = (NormalFileMessageBody) message.getBody();
		final String filePath = fileMessageBody.getLocalUrl();
		holder.tv_file_name.setText(fileMessageBody.getFileName());
		holder.tv_file_size.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
		holder.ll_container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				File file = new File(filePath);
				if (file != null && file.exists()) {
					// 文件存在，直接打开
					FileUtils.openFile(file, (Activity) context);
				} else {
					// 下载
					// TODO, EMWidget
//					context.startActivity(new Intent(context, ShowNormalFileActivity.class).putExtra("msgbody", fileMessageBody));
				}
				if (message.direct == EMMessage.Direct.RECEIVE && !message.isAcked) {
					try {
						EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
						message.isAcked = true;
					} catch (EaseMobException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		String st1 = context.getResources().getString(R.string.Have_downloaded);
		String st2 = context.getResources().getString(R.string.Did_not_download);
		if (message.direct == EMMessage.Direct.RECEIVE) { // 接收的消息
			EMLog.d(TAG, "it is receive msg");
			File file = new File(filePath);
			if (file != null && file.exists()) {
				holder.tv_file_download_state.setText(st1);
			} else {
				holder.tv_file_download_state.setText(st2);
			}
			return;
		}

		// until here, deal with send voice msg
		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.INVISIBLE);
			holder.tv.setVisibility(View.INVISIBLE);
			holder.staus_iv.setVisibility(View.INVISIBLE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.INVISIBLE);
			holder.tv.setVisibility(View.INVISIBLE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			// set a timer
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					chatWidget.getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							holder.pb.setVisibility(View.VISIBLE);
							holder.tv.setVisibility(View.VISIBLE);
							holder.tv.setText(message.progress + "%");
							if (message.status == EMMessage.Status.SUCCESS) {
								holder.pb.setVisibility(View.INVISIBLE);
								holder.tv.setVisibility(View.INVISIBLE);
								timer.cancel();
							} else if (message.status == EMMessage.Status.FAIL) {
								holder.pb.setVisibility(View.INVISIBLE);
								holder.tv.setVisibility(View.INVISIBLE);
								holder.staus_iv.setVisibility(View.VISIBLE);
								Toast.makeText(context,
										context.getString(R.string.send_fail) + context.getString(R.string.connect_failuer_toast), 0)
										.show();
								timer.cancel();
							}

						}
					});

				}
			}, 0, 500);
			break;
		default:
			// 发送消息
			sendMsgInBackground(message, holder);
		}

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
