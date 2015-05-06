package com.easemob.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.GroupReomveListener;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.NormalFileMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VideoMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.ui.utils.CommonUtils;
import com.easemob.ui.utils.SmileUtils;
import com.easemob.uidemo.R;
import com.easemob.util.EMLog;
import com.easemob.util.ImageUtils;
import com.easemob.util.PathUtil;
import com.easemob.widget.chatrow.EMChatRowVoiceWidget;
import com.easemob.widget.chatrow.MessageAdapter;

public class EMChatWidget extends LinearLayout implements OnClickListener, EMEventListener{
	
	public interface EMChatWidgetUser {
		public void onGroupDetail(String groupId);
	}
	
	private static final String TAG = "ChatActivity";
	private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
	public static final int REQUEST_CODE_CONTEXT_MENU = 3;
	private static final int REQUEST_CODE_MAP = 4;
	public static final int REQUEST_CODE_TEXT = 5;
	public static final int REQUEST_CODE_VOICE = 6;
	public static final int REQUEST_CODE_PICTURE = 7;
	public static final int REQUEST_CODE_LOCATION = 8;
	public static final int REQUEST_CODE_NET_DISK = 9;
	public static final int REQUEST_CODE_FILE = 10;
	public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
	public static final int REQUEST_CODE_PICK_VIDEO = 12;
	public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
	public static final int REQUEST_CODE_VIDEO = 14;
	public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
	public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
	public static final int REQUEST_CODE_SEND_USER_CARD = 17;
	public static final int REQUEST_CODE_CAMERA = 18;
	public static final int REQUEST_CODE_LOCAL = 19;
	public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
	public static final int REQUEST_CODE_GROUP_DETAIL = 21;
	public static final int REQUEST_CODE_SELECT_VIDEO = 23;
	public static final int REQUEST_CODE_SELECT_FILE = 24;
	public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;

	public static final int RESULT_CODE_COPY = 1;
	public static final int RESULT_CODE_DELETE = 2;
	public static final int RESULT_CODE_FORWARD = 3;
	public static final int RESULT_CODE_OPEN = 4;
	public static final int RESULT_CODE_DWONLOAD = 5;
	public static final int RESULT_CODE_TO_CLOUD = 6;
	public static final int RESULT_CODE_EXIT_GROUP = 7;

	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;

	public static final String COPY_IMAGE = "EASEMOBIMG";
	
	EMVoiceRecordWidget voiceRecordWidget;
	
	private Context context;
	private LayoutInflater inflater;
	private ListView listView;
	private PasteEditText mEditTextContent;
	private View buttonSetModeKeyboard;
	private View buttonSetModeVoice;
	private View buttonSend;
	private View buttonPressToSpeak;
	private LinearLayout btnContainer;
	private View more;
	private ClipboardManager clipboard;
//	private ViewPager expressionViewpager;
	private EMExpressionWidget expressionWidget;
	private InputMethodManager manager;
	private int chatType;
	private EMConversation conversation;
	public static EMChatWidget activityInstance = null;
	public Activity activity;
	// 给谁发送消息
	private String toChatUsername;	
	private MessageAdapter adapter;
	private File cameraFile;
	static int resendPos;

	private GroupListener groupListener;

	private ImageView iv_emoticons_normal;
	private ImageView iv_emoticons_checked;
	private RelativeLayout edittext_layout;
	private ProgressBar loadmorePB;
	private boolean isloading;
	private final int pagesize = 20;
	private boolean haveMoreData = true;
	private Button btnMore;
	public String playMsgId;

	
	private EMGroup group;

	private String forwardMsgId;
	
	private EMChatWidgetUser user;
	
	public EMChatWidget(Context context) {
		super(context);
		init(context);
	}

	public EMChatWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public EMChatWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public EMChatWidget(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	public void setUser(EMChatWidgetUser user) {
		this.user = user;
	}
	
	public void init(Context context) {
		this.context = context;
		this.activity = (Activity)context;
		LayoutInflater.from(context).inflate(R.layout.em_chat_widget, this);
		activityInstance = this;
		initView();
	}

	/**
	 * initView
	 */
	protected void initView() {
		voiceRecordWidget = (EMVoiceRecordWidget) findViewById(R.id.voice_recorder);
		
		listView = (ListView) findViewById(R.id.list);
		mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
		buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
		edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
		buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
		buttonSend = findViewById(R.id.btn_send);
		buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
//		expressionViewpager = (ViewPager) findViewById(R.id.vPager);
//		emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
		expressionWidget = (EMExpressionWidget) findViewById(R.id.expressioin_widget);
		btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
		iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
		iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);
		loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
		btnMore = (Button) findViewById(R.id.btn_more);
		iv_emoticons_normal.setVisibility(View.VISIBLE);
		iv_emoticons_checked.setVisibility(View.INVISIBLE);
		more = findViewById(R.id.more);
		edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
		
		int[] ids = {R.id.et_sendmessage, R.id.btn_set_mode_keyboard, R.id.btn_set_mode_voice, R.id.btn_send, R.id.btn_press_to_speak, R.id.btn_more,
				R.id.container_remove, R.id.container_to_group, R.id.btn_picture, R.id.btn_take_picture, R.id.btn_location, R.id.iv_emoticons_normal,
				R.id.iv_emoticons_checked, R.id.btn_video, R.id.btn_file, R.id.btn_voice_call, R.id.btn_video_call, R.id.title_bar_back};
		
		for (int id : ids) {
			findViewById(id).setOnClickListener(this);
		}
		
//		// 表情list
//		reslist = getExpressionRes(35);
//		// 初始化表情viewpager
//		List<View> views = new ArrayList<View>();
//		View gv1 = getGridChildView(1);
//		View gv2 = getGridChildView(2);
//		views.add(gv1);
//		views.add(gv2);
//		expressionViewpager.setAdapter(new ExpressionPagerAdapter(views));
		
		edittext_layout.requestFocus();
//		voiceRecorder = new VoiceRecorder(micImageHandler);
		buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
		mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
				} else {
					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
				}

			}
		});
		mEditTextContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
				more.setVisibility(View.GONE);
				iv_emoticons_normal.setVisibility(View.VISIBLE);
				iv_emoticons_checked.setVisibility(View.INVISIBLE);
				expressionWidget.setVisibility(View.GONE);
				btnContainer.setVisibility(View.GONE);
			}
		});
		// 监听文字框
		mEditTextContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(s)) {
					btnMore.setVisibility(View.GONE);
					buttonSend.setVisibility(View.VISIBLE);
				} else {
					btnMore.setVisibility(View.VISIBLE);
					buttonSend.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		expressionWidget.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String filename = expressionWidget.getExpression(parent, position);
				try {
					// 文字输入框可见时，才可输入表情
					// 按住说话可见，不让输入表情
					if (buttonSetModeKeyboard.getVisibility() != View.VISIBLE) {

						if (filename != "delete_expression") { // 不是删除键，显示表情
							// 这里用的反射，所以混淆的时候不要混淆SmileUtils这个类
							Class<?> clz = Class.forName("com.easemob.ui.utils.SmileUtils");
							Field field = clz.getField(filename);
							mEditTextContent.append(SmileUtils.getSmiledText(context,
									(String) field.get(null)));
						} else { // 删除文字或者表情
							if (!TextUtils.isEmpty(mEditTextContent.getText())) {

								int selectionStart = mEditTextContent.getSelectionStart();// 获取光标的位置
								if (selectionStart > 0) {
									String body = mEditTextContent.getText().toString();
									String tempStr = body.substring(0, selectionStart);
									int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
									if (i != -1) {
										CharSequence cs = tempStr.substring(i, selectionStart);
										if (SmileUtils.containsKey(cs.toString()))
											mEditTextContent.getEditableText().delete(i, selectionStart);
										else
											mEditTextContent.getEditableText().delete(selectionStart - 1,
													selectionStart);
									} else {
										mEditTextContent.getEditableText().delete(selectionStart - 1, selectionStart);
									}
								}
							}

						}
					}
				} catch (Exception e) {
				}

			}
		});
		
	}
	

	public void setForwardMsgId(String forwardUserId) {
		this.forwardMsgId = forwardUserId;
		// show forward message if the message is not null
		if (forwardMsgId != null) {
			// 显示发送要转发的消息
			forwardMessage(forwardMsgId);
		}
	}

	public void setUpView(int chatType, String toChatUsername) {
		this.chatType = chatType;
		this.toChatUsername = toChatUsername;
		
		iv_emoticons_normal.setOnClickListener(this);
		iv_emoticons_checked.setOnClickListener(this);
		// position = getIntent().getIntExtra("position", -1);
		clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		this.activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// 判断单聊还是群聊

		if (chatType == CHATTYPE_SINGLE) { // 单聊
			((TextView) findViewById(R.id.name)).setText(toChatUsername);
			// conversation =
			// EMChatManager.getInstance().getConversation(toChatUsername,false);
		} else {
			// 群聊
			findViewById(R.id.container_to_group).setVisibility(View.VISIBLE);
			findViewById(R.id.container_remove).setVisibility(View.GONE);
			findViewById(R.id.btn_voice_call).setVisibility(View.GONE);
			findViewById(R.id.btn_video_call).setVisibility(View.GONE);
			group = EMGroupManager.getInstance().getGroup(toChatUsername);
			if (group != null)
				((TextView) findViewById(R.id.name)).setText(group.getGroupName());
			else
				((TextView) findViewById(R.id.name)).setText(toChatUsername);
			// conversation =
			// EMChatManager.getInstance().getConversation(toChatUsername,true);
		}
		conversation = EMChatManager.getInstance().getConversation(toChatUsername);
		// 把此会话的未读数置为0
		conversation.resetUnreadMsgCount();

		// 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
		// 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
		final List<EMMessage> msgs = conversation.getAllMessages();
		int msgCount = msgs != null ? msgs.size() : 0;
		if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
			String msgId = null;
			if (msgs != null && msgs.size() > 0) {
				msgId = msgs.get(0).getMsgId();
			}
			if (chatType == CHATTYPE_SINGLE) {
				conversation.loadMoreMsgFromDB(msgId, pagesize);
			} else {
//				conversation.loadMoreGroupMsgFromDB(msgId, pagesize);
			}
		}
		adapter = new MessageAdapter(context, this, toChatUsername, chatType);
		// 显示消息
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new ListScrollListener());
		adapter.refreshSelectLast();

		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				more.setVisibility(View.GONE);
				iv_emoticons_normal.setVisibility(View.VISIBLE);
				iv_emoticons_checked.setVisibility(View.INVISIBLE);
				expressionWidget.setVisibility(View.GONE);
				btnContainer.setVisibility(View.GONE);
				return false;
			}
		});

		// TODO: EMWidget
//		// 监听当前会话的群聊解散被T事件
		groupListener = new GroupListener();
		EMGroupManager.getInstance().addGroupChangeListener(groupListener);
	}

	/**
	 * onActivityResult
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_CODE_EXIT_GROUP) {
			activity.setResult(Activity.RESULT_OK);
			activity.finish();
			return;
		}
		
		if (adapter.onActivityResult(requestCode, resultCode, data)) {
			return;
		}

		if (resultCode == Activity.RESULT_OK) { // 清空消息
			if (requestCode == REQUEST_CODE_EMPTY_HISTORY) {
				// 清空会话
				EMChatManager.getInstance().clearConversation(toChatUsername);
				adapter.refresh();
			} else if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
				if (cameraFile != null && cameraFile.exists())
					sendPicture(cameraFile.getAbsolutePath());
			} else if (requestCode == REQUEST_CODE_SELECT_VIDEO) { // 发送本地选择的视频

				int duration = data.getIntExtra("dur", 0);
//				String videoPath = data.getStringExtra("path");
				String videoPath = getVideoPathByUri(data.getData());
				File file = new File(PathUtil.getInstance().getImagePath(), "" + System.currentTimeMillis());
				Bitmap bitmap = null;
				FileOutputStream fos = null;
				try {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, 3);
					if (bitmap == null) {
						EMLog.d("chatactivity", "problem load video thumbnail bitmap,use default icon");
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_panel_video_icon);
					}
					fos = new FileOutputStream(file);

					bitmap.compress(CompressFormat.JPEG, 100, fos);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						fos = null;
					}
					if (bitmap != null) {
						bitmap.recycle();
						bitmap = null;
					}

				}
				sendVideo(videoPath, file.getAbsolutePath(), duration / 1000);

			} else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
				if (data != null) {
					Uri selectedImage = data.getData();
					if (selectedImage != null) {
						sendPicByUri(selectedImage);
					}
				}
			} else if (requestCode == REQUEST_CODE_SELECT_FILE) { // 发送选择的文件
				if (data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						sendFile(uri);
					}
				}

			} else if (requestCode == REQUEST_CODE_MAP) { // 地图
				double latitude = data.getDoubleExtra("latitude", 0);
				double longitude = data.getDoubleExtra("longitude", 0);
				String locationAddress = data.getStringExtra("address");
				if (locationAddress != null && !locationAddress.equals("")) {
					more(more);
					sendLocationMsg(latitude, longitude, "", locationAddress);
				} else {
					String st = getResources().getString(R.string.unable_to_get_loaction);
					Toast.makeText(context, st, Toast.LENGTH_SHORT).show();
				}
				// 重发消息
			} else if (requestCode == REQUEST_CODE_TEXT || requestCode == REQUEST_CODE_VOICE
					|| requestCode == REQUEST_CODE_PICTURE || requestCode == REQUEST_CODE_LOCATION
					|| requestCode == REQUEST_CODE_VIDEO || requestCode == REQUEST_CODE_FILE) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_COPY_AND_PASTE) {
				// 粘贴
				if (clipboard.hasPrimaryClip()) {
					String pasteText = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
					if (pasteText.startsWith(COPY_IMAGE)) {
						// 把图片前缀去掉，还原成正常的path
						sendPicture(pasteText.replace(COPY_IMAGE, ""));
					}

				}
			} else if (requestCode == REQUEST_CODE_ADD_TO_BLACKLIST) { // 移入黑名单
				EMMessage deleteMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", -1));
				addUserToBlacklist(deleteMsg.getFrom());
			} else if (conversation.getMsgCount() > 0) {
				adapter.refresh();
				activity.setResult(Activity.RESULT_OK);
			} else if (requestCode == REQUEST_CODE_GROUP_DETAIL) {
				adapter.refresh();
			}
		}
	}

	/**
	 * 消息图标点击事件
	 * 
	 * @param view
	 */
	@Override
	public void onClick(View view) {
		@SuppressWarnings("unused")
		String st1 = getResources().getString(R.string.not_connect_to_server);
		int id = view.getId();
		if (id == R.id.container_to_group) {
			toGroupDetails(view);
		} else if (id == R.id.btn_set_mode_keyboard) {
			setModeKeyboard(view);
		} else if (id == R.id.container_remove) {
			emptyHistory(view);
		} else if (id == R.id.btn_set_mode_voice) {
			setModeVoice(view);
//		} else if (id == R.id.et_sendmessage) {
//			editClick(view);
		} else if (id == R.id.btn_more) {
			more(view);
		} else if (id == R.id.btn_send) {// 点击发送按钮(发文字和表情)
			sendText(mEditTextContent.getText().toString());
		} else if (id == R.id.btn_take_picture) {
			selectPicFromCamera();// 点击照相图标
		} else if (id == R.id.btn_picture) {
			selectPicFromLocal(); // 点击图片图标
		} else if (id == R.id.btn_location) { // 位置
			// TODO EMWidget
//			startActivityForResult(new Intent(this, BaiduMapActivity.class), REQUEST_CODE_MAP);
		} else if (id == R.id.iv_emoticons_normal) { // 点击显示表情框
			more.setVisibility(View.VISIBLE);
			iv_emoticons_normal.setVisibility(View.INVISIBLE);
			iv_emoticons_checked.setVisibility(View.VISIBLE);
			btnContainer.setVisibility(View.GONE);
			expressionWidget.setVisibility(View.VISIBLE);
			hideKeyboard();
		} else if (id == R.id.iv_emoticons_checked) { // 点击隐藏表情框
			iv_emoticons_normal.setVisibility(View.VISIBLE);
			iv_emoticons_checked.setVisibility(View.INVISIBLE);
			btnContainer.setVisibility(View.VISIBLE);
			expressionWidget.setVisibility(View.GONE);
			more.setVisibility(View.GONE);
		} else if (id == R.id.btn_video) {
			selectVideoFromLocal();
		} else if (id == R.id.btn_file) { // 点击文件图标
			selectFileFromLocal();
		} else if (id == R.id.btn_voice_call) { // 点击语音电话图标
			// TODO, EMWidget
//			if (!EMChatManager.getInstance().isConnected())
//				Toast.makeText(this, st1, 0).show();
//			else
//				startActivity(new Intent(ChatActivity.this, VoiceCallActivity.class).putExtra("username",
//						toChatUsername).putExtra("isComingCall", false));
		} else if (id == R.id.btn_video_call) { // 视频通话
			// TODO, EMWidget
//			if (!EMChatManager.getInstance().isConnected())
//				Toast.makeText(this, st1, 0).show();
//			else
//				startActivity(new Intent(this, VideoCallActivity.class).putExtra("username", toChatUsername).putExtra(
//						"isComingCall", false));
		} else if (id == R.id.title_bar_back) {
			activity.finish();
		}
	}

	/**
	 * 事件监听
	 * 
	 * see {@link EMNotifierEvent}
     */
    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
        case EventNewMessage:
        {
            //获取到message
            EMMessage message = (EMMessage) event.getData();
            
            String username = null;
            //群组消息
            if(message.getChatType() == ChatType.GroupChat){
                username = message.getTo();
            }
            else{
                //单聊消息
                username = message.getFrom();
            }

            //如果是当前会话的消息，刷新聊天页面
            if(username.equals(getToChatUsername())){
                refreshUIWithNewMessage();
    			// TODO, EMWidget
                //声音和震动提示有新消息
//                HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(message);
            }else{
    			// TODO, EMWidget
//                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
            	//如果消息不是和当前聊天ID的消息
            }

            break;
        }
        case EventDeliveryAck:
        {
            //获取到message
            refreshUI();
            break;
        }
        case EventReadAck:
        {
            //获取到message
            refreshUI();
            break;
        }
        case EventOfflineMessage:
        {
            refreshUI();
            break;
        }
        default:
            break;
        }
        
    }
	
	
	private void refreshUIWithNewMessage(){
        adapter.refreshSelectLast();
	}

	private void refreshUI() {
		adapter.refresh();
	}

	/**
	 * 照相获取图片
	 */
	public void selectPicFromCamera() {
		if (!CommonUtils.isExistSdcard()) {
			String st = getResources().getString(R.string.sd_card_does_not_exist);
			Toast.makeText(context, st, Toast.LENGTH_SHORT).show();
			return;
		}

		// TODO, EMWidget
//		cameraFile = new File(PathUtil.getInstance().getImagePath(), DemoApplication.getInstance().getUserName()
		cameraFile = new File(PathUtil.getInstance().getImagePath(), "gm"
				+ System.currentTimeMillis() + ".jpg");
		cameraFile.getParentFile().mkdirs();
		activity.startActivityForResult(
				new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
				REQUEST_CODE_CAMERA);
	}

	/**
	 * 选择文件
	 */
	private void selectFileFromLocal() {
		Intent intent = null;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		activity.startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
	}

	/**
	 * 从图库获取图片
	 */
	public void selectPicFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		activity.startActivityForResult(intent, REQUEST_CODE_LOCAL);
	}
	
	/**
	 * 从图库获取视频
	 */
	public void selectVideoFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("video/*");

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
		}
		activity.startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
	}

	/**
	 * 发送文本消息
	 * 
	 * @param content
	 *            message content
	 * @param isResend
	 *            boolean resend
	 */
	private void sendText(String content) {

		if (content.length() > 0) {
			EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
			// 如果是群聊，设置chattype,默认是单聊
			if (chatType == CHATTYPE_GROUP)
				message.setChatType(ChatType.GroupChat);
			TextMessageBody txtBody = new TextMessageBody(content);
			// 设置消息body
			message.addBody(txtBody);
			// 设置要发给谁,用户username或者群聊groupid
			message.setReceipt(toChatUsername);
			// 把messgage加到conversation中
			conversation.addMessage(message);
			// 通知adapter有消息变动，adapter会根据加入的这条message显示消息和调用sdk的发送方法
			adapter.refreshSelectLast();
			mEditTextContent.setText("");

			activity.setResult(Activity.RESULT_OK);

		}
	}

	/**
	 * 发送语音
	 * 
	 * @param filePath
	 * @param fileName
	 * @param length
	 * @param isResend
	 */
	private void sendVoice(String filePath, String fileName, String length, boolean isResend) {
		if (!(new File(filePath).exists())) {
			return;
		}
		try {
			final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VOICE);
			// 如果是群聊，设置chattype,默认是单聊
			if (chatType == CHATTYPE_GROUP)
				message.setChatType(ChatType.GroupChat);
			message.setReceipt(toChatUsername);
			int len = Integer.parseInt(length);
			VoiceMessageBody body = new VoiceMessageBody(new File(filePath), len);
			message.addBody(body);

			conversation.addMessage(message);
			adapter.refreshSelectLast();
			activity.setResult(Activity.RESULT_OK);
			// send file
			// sendVoiceSub(filePath, fileName, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送图片
	 * 
	 * @param filePath
	 */
	private void sendPicture(final String filePath) {
		String to = toChatUsername;
		// create and add image message in view
		final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP)
			message.setChatType(ChatType.GroupChat);

		message.setReceipt(to);
		ImageMessageBody body = new ImageMessageBody(new File(filePath));
		// 默认超过100k的图片会压缩后发给对方，可以设置成发送原图
		// body.setSendOriginalImage(true);
		message.addBody(body);
		conversation.addMessage(message);

//		listView.setAdapter(adapter);
		adapter.refreshSelectLast();
		activity.setResult(Activity.RESULT_OK);
		// more(more);
	}

	/**
	 * 发送视频消息
	 */
	private void sendVideo(final String filePath, final String thumbPath, final int length) {
		final File videoFile = new File(filePath);
		if (!videoFile.exists()) {
			return;
		}
		try {
			EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VIDEO);
			// 如果是群聊，设置chattype,默认是单聊
			if (chatType == CHATTYPE_GROUP)
				message.setChatType(ChatType.GroupChat);
			String to = toChatUsername;
			message.setReceipt(to);
			VideoMessageBody body = new VideoMessageBody(videoFile, thumbPath, length, videoFile.length());
			message.addBody(body);
			conversation.addMessage(message);
			listView.setAdapter(adapter);
			adapter.refreshSelectLast();
			activity.setResult(Activity.RESULT_OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据图库图片uri发送图片
	 * 
	 * @param selectedImage
	 */
	private void sendPicByUri(Uri selectedImage) {
		// String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(selectedImage, null, null, null, null);
		String st8 = getResources().getString(R.string.cant_find_pictures);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex("_data");
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			cursor = null;

			if (picturePath == null || picturePath.equals("null")) {
				Toast toast = Toast.makeText(context, st8, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
			sendPicture(picturePath);
		} else {
			File file = new File(selectedImage.getPath());
			if (!file.exists()) {
				Toast toast = Toast.makeText(context, st8, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;

			}
			sendPicture(file.getAbsolutePath());
		}
	}
	
	/**
	 * 根据uri发送视频
	 * 
	 * @param selectedVideo
	 */
	private String getVideoPathByUri(Uri selectedVideo) {
		// String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(selectedVideo, null, null, null, null);
		String st8 = getResources().getString(R.string.cant_find_pictures);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex("_data");
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			cursor = null;

			if (picturePath == null || picturePath.equals("null")) {
				Toast toast = Toast.makeText(context, st8, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return null;
			}
			return picturePath;
		} else {
			File file = new File(selectedVideo.getPath());
			if (!file.exists()) {
				Toast toast = Toast.makeText(context, st8, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return null;

			}
			return file.getAbsolutePath();
		}
	}

	/**
	 * 发送位置信息
	 * 
	 * @param latitude
	 * @param longitude
	 * @param imagePath
	 * @param locationAddress
	 */
	private void sendLocationMsg(double latitude, double longitude, String imagePath, String locationAddress) {
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.LOCATION);
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP)
			message.setChatType(ChatType.GroupChat);
		LocationMessageBody locBody = new LocationMessageBody(locationAddress, latitude, longitude);
		message.addBody(locBody);
		message.setReceipt(toChatUsername);
		conversation.addMessage(message);
		listView.setAdapter(adapter);
		adapter.refreshSelectLast();
		activity.setResult(Activity.RESULT_OK);

	}

	/**
	 * 发送文件
	 * 
	 * @param uri
	 */
	private void sendFile(Uri uri) {
		String filePath = null;
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					filePath = cursor.getString(column_index);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			filePath = uri.getPath();
		}
		File file = new File(filePath);
		if (file == null || !file.exists()) {
			String st7 = getResources().getString(R.string.File_does_not_exist);
			Toast.makeText(context, st7, Toast.LENGTH_SHORT).show();
			return;
		}
		if (file.length() > 10 * 1024 * 1024) {
			String st6 = getResources().getString(R.string.The_file_is_not_greater_than_10_m);
			Toast.makeText(context, st6, Toast.LENGTH_SHORT).show();
			return;
		}

		// 创建一个文件消息
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.FILE);
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP)
			message.setChatType(ChatType.GroupChat);

		message.setReceipt(toChatUsername);
		// add message body
		NormalFileMessageBody body = new NormalFileMessageBody(new File(filePath));
		message.addBody(body);
		conversation.addMessage(message);
		listView.setAdapter(adapter);
		adapter.refreshSelectLast();
		activity.setResult(Activity.RESULT_OK);
	}

	/**
	 * 重发消息
	 */
	private void resendMessage() {
		EMMessage msg = null;
		msg = conversation.getMessage(resendPos);
		// msg.setBackSend(true);
		msg.status = EMMessage.Status.CREATE;

		adapter.refreshSeekTo(resendPos);
	}

	/**
	 * 显示语音图标按钮
	 * 
	 * @param view
	 */
	public void setModeVoice(View view) {
		hideKeyboard();
		edittext_layout.setVisibility(View.GONE);
		more.setVisibility(View.GONE);
		view.setVisibility(View.GONE);
		buttonSetModeKeyboard.setVisibility(View.VISIBLE);
		buttonSend.setVisibility(View.GONE);
		btnMore.setVisibility(View.VISIBLE);
		buttonPressToSpeak.setVisibility(View.VISIBLE);
		iv_emoticons_normal.setVisibility(View.VISIBLE);
		iv_emoticons_checked.setVisibility(View.INVISIBLE);
		btnContainer.setVisibility(View.VISIBLE);
		expressionWidget.setVisibility(View.GONE);

	}

	/**
	 * 显示键盘图标
	 * 
	 * @param view
	 */
	public void setModeKeyboard(View view) {
		// mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener()
		// {
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// if(hasFocus){
		// getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		// }
		// }
		// });
		edittext_layout.setVisibility(View.VISIBLE);
		more.setVisibility(View.GONE);
		view.setVisibility(View.GONE);
		buttonSetModeVoice.setVisibility(View.VISIBLE);
		// mEditTextContent.setVisibility(View.VISIBLE);
		mEditTextContent.requestFocus();
		// buttonSend.setVisibility(View.VISIBLE);
		buttonPressToSpeak.setVisibility(View.GONE);
		if (TextUtils.isEmpty(mEditTextContent.getText())) {
			btnMore.setVisibility(View.VISIBLE);
			buttonSend.setVisibility(View.GONE);
		} else {
			btnMore.setVisibility(View.GONE);
			buttonSend.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 点击清空聊天记录
	 * 
	 * @param view
	 */
	public void emptyHistory(View view) {
//		String st5 = getResources().getString(R.string.Whether_to_empty_all_chats);
//		activity.startActivityForResult(new Intent(context, AlertDialog.class).putExtra("titleIsCancel", true).putExtra("msg", st5)
//				.putExtra("cancel", true), REQUEST_CODE_EMPTY_HISTORY);
		
		new AlertDialog.Builder(context)
		.setTitle(R.string.Whether_to_empty_all_chats)
//		.setMessage(R.string.confirm_resend)
		.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						EMChatManager.getInstance().clearConversation(toChatUsername);
						adapter.refresh();
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

	/**
	 * 点击进入群组详情
	 * 
	 * @param view
	 */
	public void toGroupDetails(View view) {
		if (group == null) {
			Toast.makeText(context, R.string.gorup_not_found, Toast.LENGTH_SHORT).show();
			return;
		}
		if (this.user != null) {
			this.user.onGroupDetail(toChatUsername);
		}
	}

	/**
	 * 显示或隐藏图标按钮页
	 * 
	 * @param view
	 */
	public void more(View view) {
		if (more.getVisibility() == View.GONE) {
			EMLog.d(TAG, "more gone");
			hideKeyboard();
			more.setVisibility(View.VISIBLE);
			btnContainer.setVisibility(View.VISIBLE);
			expressionWidget.setVisibility(View.GONE);
		} else {
			if (expressionWidget.getVisibility() == View.VISIBLE) {
				expressionWidget.setVisibility(View.GONE);
				btnContainer.setVisibility(View.VISIBLE);
				iv_emoticons_normal.setVisibility(View.VISIBLE);
				iv_emoticons_checked.setVisibility(View.INVISIBLE);
			} else {
				more.setVisibility(View.GONE);
			}

		}

	}

	/**
	 * 点击文字输入框
	 * 
	 * @param v
	 */
	public void editClick(View v) {
		listView.setSelection(listView.getCount() - 1);
		if (more.getVisibility() == View.VISIBLE) {
			more.setVisibility(View.GONE);
			iv_emoticons_normal.setVisibility(View.VISIBLE);
			iv_emoticons_checked.setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * 按住说话listener
	 * 
	 */
	class PressToSpeakListen implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				try {
					if (EMChatRowVoiceWidget.VoicePlayClickListener.isPlaying)
						EMChatRowVoiceWidget.VoicePlayClickListener.currentPlayListener.stopPlayVoice();
					v.setPressed(true);
					voiceRecordWidget.startRecording(null, toChatUsername);
				} catch (Exception e) {
					v.setPressed(false);
				}
				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					voiceRecordWidget.showHint1();
				} else {
					voiceRecordWidget.showHint2();
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (event.getY() < 0) {
					// discard the recorded audio.
					voiceRecordWidget.discardRecording();
				} else {
					// stop recording and send voice file
					String st1 = getResources().getString(R.string.Recording_without_permission);
					String st2 = getResources().getString(R.string.The_recording_time_is_too_short);
					String st3 = getResources().getString(R.string.send_failure_please);
					try {
						int length = voiceRecordWidget.stopRecoding();
						if (length > 0) {
							sendVoice(voiceRecordWidget.getVoiceFilePath(), voiceRecordWidget.getVoiceFileName(toChatUsername),
									Integer.toString(length), false);
						} else if (length == EMError.INVALID_FILE) {
							Toast.makeText(context, st1, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(context, st2, Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(context, st3, Toast.LENGTH_SHORT).show();
					}
				}
				return true;
			default:
				if (voiceRecordWidget != null) {
					voiceRecordWidget.discardRecording();
				}
				return false;
			}
		}
	}
	
	@Override
	public void onAttachedToWindow() {
		if (group != null)
			((TextView) findViewById(R.id.name)).setText(group.getGroupName());
		adapter.refresh();

		// TODO, EMWidget
//		DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
//		sdkHelper.pushActivity(this);
		// register the event listener when enter the foreground
		EMChatManager.getInstance().registerEventListener(
				this,
				new EMNotifierEvent.Event[] { EMNotifierEvent.Event.EventNewMessage,
						EMNotifierEvent.Event.EventDeliveryAck, EMNotifierEvent.Event.EventReadAck });
	}
	
	@Override
	public void onDetachedFromWindow() {
		activityInstance = null;
		EMGroupManager.getInstance().removeGroupChangeListener(groupListener);

		// unregister this event listener when this activity enters the
		// background
		EMChatManager.getInstance().unregisterEventListener(this);
	}
	
	public void onPause() {
		voiceRecordWidget.discardRecording();
		if (EMChatRowVoiceWidget.VoicePlayClickListener.isPlaying && EMChatRowVoiceWidget.VoicePlayClickListener.currentPlayListener != null) {
			// 停止语音播放
			EMChatRowVoiceWidget.VoicePlayClickListener.currentPlayListener.stopPlayVoice();
		}
	}

	/**
	 * 隐藏软键盘
	 */
	private void hideKeyboard() {
		if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (activity.getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 加入到黑名单
	 * 
	 * @param username
	 */
	private void addUserToBlacklist(final String username) {
		final ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage(context.getString(R.string.Is_moved_into_blacklist));
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					EMContactManager.getInstance().addUserToBlackList(username, false);
					activity.runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(context, R.string.Move_into_blacklist_success, Toast.LENGTH_SHORT).show();
						}
					});
				} catch (EaseMobException e) {
					e.printStackTrace();
					activity.runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(context, R.string.Move_into_blacklist_failure, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}

	/**
	 * 覆盖手机返回键
	 */
	public boolean onBackPressed() {
		if (more.getVisibility() == View.VISIBLE) {
			more.setVisibility(View.GONE);
			iv_emoticons_normal.setVisibility(View.VISIBLE);
			iv_emoticons_checked.setVisibility(View.INVISIBLE);
			return true;
		}
		return false;
	}

	/**
	 * listview滑动监听listener
	 * 
	 */
	private class ListScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (view.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
					isloading = true;
					loadmorePB.setVisibility(View.VISIBLE);
					// sdk初始化加载的聊天记录为20条，到顶时去db里获取更多					
					List<EMMessage> messages;
					EMMessage firstMsg = conversation.getAllMessages().get(0);
					try {
						// 获取更多messges，调用此方法的时候从db获取的messages
						// sdk会自动存入到此conversation中
						if (chatType == CHATTYPE_SINGLE)
							messages = conversation.loadMoreMsgFromDB(firstMsg.getMsgId(), pagesize);
						else
							messages = conversation.loadMoreGroupMsgFromDB(firstMsg.getMsgId(), pagesize);
					} catch (Exception e1) {
						loadmorePB.setVisibility(View.GONE);
						return;
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
					if (messages.size() != 0) {
						// 刷新ui
						if (messages.size() > 0) {
							adapter.refreshSeekTo(messages.size() - 1);
						}
						
						if (messages.size() != pagesize)
							haveMoreData = false;
					} else {
						haveMoreData = false;
					}
					loadmorePB.setVisibility(View.GONE);
					isloading = false;

				}
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		}
	}


	/**
	 * 转发消息
	 * 
	 * @param forward_msg_id
	 */
	protected void forwardMessage(String forward_msg_id) {
		EMMessage forward_msg = EMChatManager.getInstance().getMessage(forward_msg_id);
		EMMessage.Type type = forward_msg.getType();
		switch (type) {
		case TXT:
			// 获取消息内容，发送消息
			String content = ((TextMessageBody) forward_msg.getBody()).getMessage();
			sendText(content);
			break;
		case IMAGE:
			// 发送图片
			String filePath = ((ImageMessageBody) forward_msg.getBody()).getLocalUrl();
			if (filePath != null) {
				File file = new File(filePath);
				if (!file.exists()) {
					// 不存在大图发送缩略图
					filePath = CommonUtils.getThumbnailImagePath(filePath);
				}
				sendPicture(filePath);
			}
			break;
		default:
			break;
		}
	}

	// TODO: Group集成以后处理
	/**
	 * 监测群组解散或者被T事件
	 * 
	 */
	class GroupListener extends GroupReomveListener {

		@Override
		public void onUserRemoved(final String groupId, String groupName) {
			activity.runOnUiThread(new Runnable() {

				public void run() {
					// TODO, EMWidget
//					String st13 = getResources().getString(R.string.you_are_group);
//					if (toChatUsername.equals(groupId)) {
//						Toast.makeText(ChatActivity.this, st13, 1).show();
//						if (GroupDetailsActivity.instance != null)
//							GroupDetailsActivity.instance.finish();
//						finish();
//					}
				}
			});
		}

		@Override
		public void onGroupDestroy(final String groupId, String groupName) {
			// 群组解散正好在此页面，提示群组被解散，并finish此页面
			activity.runOnUiThread(new Runnable() {

				public void run() {
					// TODO, EMWidget
//					String st14 = getResources().getString(R.string.the_current_group);
//					if (toChatUsername.equals(groupId)) {
//						Toast.makeText(ChatActivity.this, st14, 1).show();
//						if (GroupDetailsActivity.instance != null)
//							GroupDetailsActivity.instance.finish();
//						finish();
//					}
				}
			});
		}

	}

	public String getToChatUsername() {
		return toChatUsername;
	}

	public ListView getListView() {
		return listView;
	}

	public MessageAdapter getAdapter() {
		return adapter;
	}
	
	public EMConversation getConversation() {
		return conversation;
	}

	static class ToolBoxWidget extends LinearLayout {

		public ToolBoxWidget(Context context) {
			super(context);
		}

		public ToolBoxWidget(Context context, AttributeSet attrs) {
			super(context, attrs);
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.ToolBoxBtn);

			int innerBackGround = a.getResourceId(
					R.styleable.ToolBoxBtn_innerBackGround, -1);
			int innerText = a.getResourceId(R.styleable.ToolBoxBtn_innerText,
					-1);

			LayoutInflater inflater = LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.em_toolbox_btn, this);
			ImageView image = (ImageView) view.findViewById(R.id.inner_image);
			TextView text = (TextView) view.findViewById(R.id.inner_text);
			image.setBackgroundResource(innerBackGround);
			text.setText(innerText);
		}

		public ToolBoxWidget(Context context, AttributeSet attrs,
				int defStyleAttr) {
			super(context, attrs, defStyleAttr);
		}

		public ToolBoxWidget(Context context, AttributeSet attrs,
				int defStyleAttr, int defStyleRes) {
			super(context, attrs, defStyleAttr, defStyleRes);
		}
	}
	
	public Activity getActivity() {
		return this.activity;
	}
}
