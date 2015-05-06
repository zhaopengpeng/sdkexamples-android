package com.easemob.widget.chatrow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.TextMessageBody;
import com.easemob.ui.utils.UserUtils;
import com.easemob.uidemo.Constant;
import com.easemob.uidemo.R;
import com.easemob.widget.EMChatWidget;
import com.easemob.widget.activity.ForwardMessageActivity;

public abstract class EMChatRowWidget extends LinearLayout {
	
	protected Context context;
	protected LayoutInflater inflater;
	protected EMChatWidget chatWidget;
	protected ViewHolder holder = new ViewHolder();
	protected View convertView;

	protected static final int REQUEST_CODE_EMPTY_HISTORY = 2;
	protected static final int REQUEST_CODE_CONTEXT_MENU = 3;
	protected static final int REQUEST_CODE_MAP = 4;
	protected static final int REQUEST_CODE_TEXT = 5;
	protected static final int REQUEST_CODE_VOICE = 6;
	protected static final int REQUEST_CODE_PICTURE = 7;
	protected static final int REQUEST_CODE_LOCATION = 8;
	protected static final int REQUEST_CODE_NET_DISK = 9;
	protected static final int REQUEST_CODE_FILE = 10;
	protected static final int REQUEST_CODE_COPY_AND_PASTE = 11;
	protected static final int REQUEST_CODE_PICK_VIDEO = 12;
	protected static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
	protected static final int REQUEST_CODE_VIDEO = 14;
	protected static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
	protected static final int REQUEST_CODE_SELECT_USER_CARD = 16;
	protected static final int REQUEST_CODE_SEND_USER_CARD = 17;
	protected static final int REQUEST_CODE_CAMERA = 18;
	protected static final int REQUEST_CODE_LOCAL = 19;
	protected static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
	protected static final int REQUEST_CODE_GROUP_DETAIL = 21;
	protected static final int REQUEST_CODE_SELECT_VIDEO = 23;
	protected static final int REQUEST_CODE_SELECT_FILE = 24;
	protected static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;

	protected static final int RESULT_CODE_COPY = 1;
	protected static final int RESULT_CODE_DELETE = 2;
	protected static final int RESULT_CODE_FORWARD = 3;
	protected static final int RESULT_CODE_OPEN = 4;
	protected static final int RESULT_CODE_DWONLOAD = 5;
	protected static final int RESULT_CODE_TO_CLOUD = 6;
	protected static final int RESULT_CODE_EXIT_GROUP = 7;
	
	
	public static final String IMAGE_DIR = "chat/image/";
	public static final String VOICE_DIR = "chat/audio/";
	public static final String VIDEO_DIR = "chat/video";

	
	public static class ViewHolder {
		ImageView iv;
		TextView tv;
		ProgressBar pb;
		ImageView staus_iv;
		ImageView iv_avatar;
		TextView tv_usernick;
		ImageView playBtn;
		TextView timeLength;
		TextView size;
		LinearLayout container_status_btn;
		LinearLayout ll_container;
		ImageView iv_read_status;
		// 显示已读回执状态
		TextView tv_ack;
		// 显示送达回执状态
		TextView tv_delivered;

		TextView tv_file_name;
		TextView tv_file_size;
		TextView tv_file_download_state;
	}
	
	public EMChatRowWidget(Context context) {
		super(context);
		init(context);
	}

	public EMChatRowWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EMChatRowWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public EMChatRowWidget(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	protected void init(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		chatWidget = EMChatWidget.activityInstance;
		holder = new ViewHolder();
	}
	

	/**
	 * 发送消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
		holder.staus_iv.setVisibility(View.GONE);
		holder.pb.setVisibility(View.VISIBLE);

		EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
			@Override
			public void onSuccess() {
				updateSendedView(message, holder);
			}
			@Override
			public void onError(int code, String error) {
				updateSendedView(message, holder);
			}
			@Override
			public void onProgress(int progress, String status) {
			}
		});
	}
	
	/**
	 * general UI behavior
	 * 		Update ack & delivered 
	 * 		send back ack
	 * @param message
	 * @param convertView
	 * @param holder
	 */
	protected void updateAckDelivered(final EMMessage message, int position, View convertView, ViewHolder holder) {
		// 群聊时，显示接收的消息的发送人的名称
		if (message.getChatType() == ChatType.GroupChat && message.direct == EMMessage.Direct.RECEIVE){
		    //demo里使用username代码nick
			holder.tv_usernick.setText(message.getFrom());
		}
		// 如果是发送的消息并且不是群聊消息，显示已读textview
		if (message.direct == EMMessage.Direct.SEND && message.getChatType() != ChatType.GroupChat) {
			holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
			holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
			if (holder.tv_ack != null) {
				if (message.isAcked) {
					if (holder.tv_delivered != null) {
						holder.tv_delivered.setVisibility(View.INVISIBLE);
					}
					holder.tv_ack.setVisibility(View.VISIBLE);
				} else {
					holder.tv_ack.setVisibility(View.INVISIBLE);

					// check and display msg delivered ack status
					if (holder.tv_delivered != null) {
						if (message.isDelivered) {
							holder.tv_delivered.setVisibility(View.VISIBLE);
						} else {
							holder.tv_delivered.setVisibility(View.INVISIBLE);
						}
					}
				}
			}
		} else {
			// 如果是文本或者地图消息并且不是group messgae，显示的时候给对方发送已读回执
			if ((message.getType() == Type.TXT || message.getType() == Type.LOCATION) && !message.isAcked && message.getChatType() != ChatType.GroupChat) {
				// 不是语音通话记录
				if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
					try {
						EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
						// 发送已读回执
						message.isAcked = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * general UI behavior
	 *		set re-send event
	 * @param message
	 * @param position
	 * @param convertView
	 * @param holder
	 */
	protected void setResendListener(final EMMessage message, final int position, View convertView, ViewHolder holder) {
		if (message.direct == EMMessage.Direct.SEND) {
			View statusView = convertView.findViewById(R.id.msg_status);
			// 重发按钮点击事件			
			statusView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(context)
					.setTitle(R.string.resend)
					.setMessage(R.string.confirm_resend)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									message.status = EMMessage.Status.CREATE;
									MessageAdapter adapter = chatWidget.getAdapter();
									adapter.refreshSeekTo(position);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
				}
			});
		}
	}

	/**
	 * general UI behavior
	 *		set add to blacklist behavior
	 * @param message
	 * @param position
	 * @param convertView
	 * @param holder
	 */
	protected void setOnBlackList(EMMessage message, final int position, View convertView, ViewHolder holder) {
		if (message.direct == EMMessage.Direct.RECEIVE) {
			final String st = context.getResources().getString(R.string.Into_the_blacklist);
			// 长按头像，移入黑名单
			holder.iv_avatar.setOnLongClickListener(new OnLongClickListener() {
	
				@Override
				public boolean onLongClick(View v) {
					Intent intent = new Intent(context, AlertDialog.class);
					intent.putExtra("msg", st);
					intent.putExtra("cancel", true);
					intent.putExtra("position", position);
					chatWidget.getActivity().startActivityForResult(intent, REQUEST_CODE_ADD_TO_BLACKLIST);
					return true;
				}
			});
		}
	}
	
	//设置用户头像
	protected void setAvatar(EMMessage message, final int position, View convertView, ViewHolder holder) {
		setUserAvatar(message, holder.iv_avatar);
	}

	/**
	 * 显示用户头像
	 * @param message
	 * @param imageView
	 */
	private void setUserAvatar(EMMessage message, ImageView imageView){
	    if(message.direct == Direct.SEND){
	        //显示自己头像
	        UserUtils.setUserAvatar(context, EMChatManager.getInstance().getCurrentUser(), imageView);
	    }else{
	        UserUtils.setUserAvatar(context, message.getFrom(), imageView);
	    }
	}

	public abstract void setupView(final EMMessage message, final int position, ViewGroup parent);
	public abstract void updateView(final EMMessage message, final int position, ViewGroup parent);

	public abstract void updateSendedView(final EMMessage message, final ViewHolder holder);
	public abstract void onProgress(final EMMessage message, final ViewHolder holder, int progress, String status);
	
//	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
//		
//		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
//			
//			MessageAdapter adapter = chatWidget.getAdapter();
//			int position = data.getIntExtra("position", -1);
//			if (position == -1) {
//				return;
//			}
//			EMMessage message = (EMMessage)adapter.getItem(position);
//			
//			switch (resultCode) {
//			case RESULT_CODE_COPY: // 复制消息
//				@SuppressWarnings("deprecation")
//				ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//				clipboard.setText(((TextMessageBody) message.getBody()).getMessage());
//				break;
//			case RESULT_CODE_DELETE: // 删除消息
//				chatWidget.getConversation().removeMessage(message.getMsgId());
//				chatWidget.getAdapter().refreshSeekTo(position > 0 ? position - 1 : 0);
//				break;
//
//			case RESULT_CODE_FORWARD: // 转发消息
//				// TODO, EMWidget
//				EMMessage forwardMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", 0));
//				Intent intent = new Intent(context, ForwardMessageActivity.class);
//				intent.putExtra("forward_msg_id", forwardMsg.getMsgId());
//				chatWidget.getActivity().startActivity(intent);
//				break;
//			default:
//				break;
//			}
//		}		
//	}
	
}
