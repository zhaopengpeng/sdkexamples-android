package com.easemob.widget.factory;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.ui.utils.UserUtils;
import com.easemob.uidata.User;
import com.easemob.uidemo.Constant;
import com.easemob.uidemo.R;

public class EMContactWidgetFactoryImpl extends EMContactWidgetFactory {

	public EMContactWidgetFactoryImpl(Context context) {
		super(context);
	}

	public View generateView(Object obj, View convertView, ViewGroup parentView, boolean isSectionHeader) {
		return getView(obj, convertView, parentView, isSectionHeader);
	}
	
	@Override
	public View generateView(Object obj, View convertView, ViewGroup parentView) {
//		return inflater.inflate(R.layout.contact_item, null);
		return null;
	}
	
	private static class ViewHolder {
	    ImageView avatar;
	    TextView unreadMsgView;
	    TextView nameTextview;
	    TextView tvHeader;
    }
    
	private View getView(Object object, View convertView, ViewGroup parent, boolean isSectionHeader) {
	    ViewHolder holder;
 		if(convertView == null){
 		    holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.em_row_contact, null);
			holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			holder.unreadMsgView = (TextView) convertView.findViewById(R.id.unread_msg_number);
			holder.nameTextview = (TextView) convertView.findViewById(R.id.name);
			holder.tvHeader = (TextView) convertView.findViewById(R.id.header);
			convertView.setTag(holder);
		}else{
		    holder = (ViewHolder) convertView.getTag();
		}
		
		User user = (User)object;
		//设置nick，demo里不涉及到完整user，用username代替nick显示
		String username = user.getUsername();
		String header = user.getHeader();
		if (isSectionHeader) {
			if ("".equals(header)) {
			    holder.tvHeader.setVisibility(View.GONE);
			} else {
			    holder.tvHeader.setVisibility(View.VISIBLE);
			    holder.tvHeader.setText(header);
			}
		} else {
		    holder.tvHeader.setVisibility(View.GONE);
		}
		//显示申请与通知item
		if(username.equals(Constant.NEW_FRIENDS_USERNAME)){
		    holder.nameTextview.setText(user.getNick());
		    holder.avatar.setImageResource(R.drawable.new_friends_icon);
			if(user.getUnreadMsgCount() > 0){
			    holder.unreadMsgView.setVisibility(View.VISIBLE);
			    holder.unreadMsgView.setText(user.getUnreadMsgCount()+"");
			}else{
			    holder.unreadMsgView.setVisibility(View.INVISIBLE);
			}
		}else if(username.equals(Constant.GROUP_USERNAME)){
			//群聊item
		    holder.nameTextview.setText(user.getNick());
		    holder.avatar.setImageResource(R.drawable.groups_icon);
		}else{
		    holder.nameTextview.setText(username);
		    //设置用户头像
			UserUtils.setUserAvatar(context, username, holder.avatar);
			if(holder.unreadMsgView != null)
			    holder.unreadMsgView.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}

	/**
	 *  加入"申请与通知"和"群聊"
	 *  把"申请与通知"添加到首位
	 */
	@Override
	public void onPreUpdateData(List<?> datas) {
		@SuppressWarnings("unchecked")
		List<User>users = (List<User>)datas;
		boolean alreadyUpdatedList = false;
		if (users.size() >= 2) {
			User user0 = users.get(0);
			User user1 = users.get(0);
			if (user0.getUsername().equals(Constant.NEW_FRIENDS_USERNAME) &&
					user1.getUsername().equals(Constant.GROUP_USERNAME)) {
				alreadyUpdatedList = true;
			}
		}
		if (!alreadyUpdatedList) {
			User user = new User(Constant.NEW_FRIENDS_USERNAME);
			String strChat = context.getResources().getString(R.string.Application_and_notify);
			user.setNick(strChat);
			user.setHeader("#");
			users.add(0, user);
			
			user = new User(Constant.GROUP_USERNAME);
			String strGroup = context.getResources().getString(R.string.group_chat);
			user.setNick(strGroup);
			user.setHeader("#");
			users.add(1, user);
		}
	}
}
