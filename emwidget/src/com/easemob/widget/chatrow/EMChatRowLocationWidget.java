package com.easemob.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.chat.LocationMessageBody;
import com.easemob.uidemo.R;
import com.easemob.util.LatLng;

public class EMChatRowLocationWidget extends EMChatRowWidget {

	public EMChatRowLocationWidget(Context context, EMMessage message, int position,
			ViewGroup parent) {
		super(context);
		setupView(message, position, parent);
	}

	@Override
	public void setupView(EMMessage message, int position, ViewGroup parent) {
		convertView = inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
				R.layout.em_row_received_location : R.layout.em_row_sent_location, this);
		
		holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
		holder.tv = (TextView) convertView.findViewById(R.id.tv_location);
		holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
		holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
		holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
	}
	
	public void updateView(final EMMessage message, final int position, ViewGroup parent) {
		setAvatar(message, position, convertView, holder);
		updateAckDelivered(message, position, convertView, holder);
		setResendListener(message, position, convertView, holder);
		setOnBlackList(message, position, convertView, holder);
		handleLocationMessage(message, position, convertView, holder);
	}

	/**
	 * 处理位置消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleLocationMessage(final EMMessage message, final int position, final View convertView, final ViewHolder holder) {
		TextView locationView = ((TextView) convertView.findViewById(R.id.tv_location));
		LocationMessageBody locBody = (LocationMessageBody) message.getBody();
		locationView.setText(locBody.getAddress());
		LatLng loc = new LatLng(locBody.getLatitude(), locBody.getLongitude());
		locationView.setOnClickListener(new MapClickListener(loc, locBody.getAddress()));
		locationView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				chatWidget.getActivity().startActivityForResult(
						(new Intent(context, ContextMenu.class)).putExtra("position", position).putExtra("type",
								EMMessage.Type.LOCATION.ordinal()), REQUEST_CODE_CONTEXT_MENU);
				return false;
			}
		});

		if (message.direct == EMMessage.Direct.RECEIVE) {
			return;
		}
		// deal with send message
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
			break;
		default:
			sendMsgInBackground(message, holder);
		}
	}

	/*
	 * 点击地图消息listener
	 */
	class MapClickListener implements View.OnClickListener {

		LatLng location;
		String address;

		public MapClickListener(LatLng loc, String address) {
			location = loc;
			this.address = address;

		}

		@Override
		public void onClick(View v) {
			
			// TODO, EMWidget
//			Intent intent;
//			intent = new Intent(context, BaiduMapActivity.class);
//			intent.putExtra("latitude", location.latitude);
//			intent.putExtra("longitude", location.longitude);
//			intent.putExtra("address", address);
//			activity.startActivity(intent);
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
