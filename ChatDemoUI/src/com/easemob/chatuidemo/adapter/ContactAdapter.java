/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.chatuidemo.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.easemob.applib.utils.HXPreferenceUtils;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.domain.User;
import com.easemob.chatuidemo.widget.Sidebar;

/**
 * 简单的好友Adapter实现
 *
 */
public class ContactAdapter extends ArrayAdapter<User>  implements SectionIndexer{

	List<String> list;
	List<User> userList;
	List<User> copyUserList;
	private LayoutInflater layoutInflater;
	private EditText query;
	private ImageButton clearSearch;
	private SparseIntArray positionOfSection;
	private SparseIntArray sectionOfPosition;
	private Sidebar sidebar;
	private int res;
	String str;
	public MyFilter myFilter;

	public ContactAdapter(Context context, int resource, List<User> objects,Sidebar sidebar) {
		super(context, resource, objects);
		str = context.getResources().getString(R.string.search);
		this.res = resource;
		this.sidebar=sidebar;
		this.userList=objects;
		copyUserList = new ArrayList<User>();
		copyUserList.addAll(objects);
		layoutInflater = LayoutInflater.from(context);
	}
	
//	@Override
//	public int getViewTypeCount() {
//		return 2;
//	}

//	@Override
//	public int getItemViewType(int position) {
//		return position == 0 ? 0 : 1;
//		return 0;
//	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		if (position == 0) {//搜索框
//			if(convertView == null){
//				convertView = layoutInflater.inflate(R.layout.activity_null, null);
				/*
				 * 原先的代码      
				 * mender：      zhaopeng
				 */
//				query = (EditText) convertView.findViewById(R.id.query);
//				query.setHint(str);
//				clearSearch = (ImageButton) convertView.findViewById(R.id.search_clear);
//				query.addTextChangedListener(new TextWatcher() {
//					public void onTextChanged(CharSequence s, int start, int before, int count) {
//						ContactAdapter.this.getFilter().filter(s);
//						if (s.length() > 0) {
//							clearSearch.setVisibility(View.VISIBLE);
//							if (sidebar != null)
//								sidebar.setVisibility(View.GONE);
//						} else {
//							clearSearch.setVisibility(View.INVISIBLE);
//							if (sidebar != null)
//								sidebar.setVisibility(View.VISIBLE);
//						}
//					}
//	
//					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//					}
//	
//					public void afterTextChanged(Editable s) {
//					}
//				});
//				clearSearch.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//						if (((Activity) getContext()).getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
//							if (((Activity) getContext()).getCurrentFocus() != null)
//							manager.hideSoftInputFromWindow(((Activity) getContext()).getCurrentFocus().getWindowToken(),
//									InputMethodManager.HIDE_NOT_ALWAYS);
//						//清除搜索框文字
//						query.getText().clear();
//					}
//				});
//			}
//		}else{
			if(convertView == null){
				convertView = layoutInflater.inflate(res, null);
			}
			
			ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
			TextView unreadMsgView = (TextView) convertView.findViewById(R.id.unread_msg_number);
			TextView nameTextview = (TextView) convertView.findViewById(R.id.name);
			TextView tvHeader = (TextView) convertView.findViewById(R.id.header);
			User user = getItem(position);
			if(user == null)
				Log.d("ContactAdapter", position + "");
			//设置nick，demo里不涉及到完整user，用username代替nick显示
			String username = user.getUsername();
			String header = user.getHeader();
			if (position == 0 || header != null && !header.equals(getItem(position - 1).getHeader())) {
				if ("".equals(header)) {
					tvHeader.setVisibility(View.GONE);
				} else {
					tvHeader.setVisibility(View.VISIBLE);
					tvHeader.setText(header);
				}
			} else {
				tvHeader.setVisibility(View.GONE);
			}
			//显示申请与通知item
			if(username.equals(Constant.NEW_FRIENDS_USERNAME)){
				nameTextview.setText(user.getNick());
				avatar.setImageResource(R.drawable.new_friends_icon);
				if(user.getUnreadMsgCount() > 0){
					unreadMsgView.setVisibility(View.VISIBLE);
					unreadMsgView.setText(user.getUnreadMsgCount()+"");
				}else{
					unreadMsgView.setVisibility(View.INVISIBLE);
				}
			}else if(username.equals(Constant.GROUP_USERNAME)){
				//群聊item
				nameTextview.setText(user.getNick());
				avatar.setImageResource(R.drawable.groups_icon);
			}else{
				nameTextview.setText(username);
				if(unreadMsgView != null)
					unreadMsgView.setVisibility(View.INVISIBLE);
				avatar.setImageResource(R.drawable.default_avatar);
			}
//		}
		
		return convertView;
	}
	
	@Override
	public User getItem(int position) {
		User user = new User();
		user.setHeader(getContext().getString(R.string.search_header));
//		return position == 0 ? user : super.getItem(position - 1);
		return super.getItem(position);
	}
	
	@Override
	public int getCount() {
		//有搜索框，count+1
		return super.getCount();
	}

	public int getPositionForSection(int section) {
		return positionOfSection.get(section);
	}

	public int getSectionForPosition(int position) {
		return sectionOfPosition.get(position);
	}
	
	@Override
	public Object[] getSections() {
		positionOfSection = new SparseIntArray();
		sectionOfPosition = new SparseIntArray();
		int count = getCount();
		list = new ArrayList<String>();
		list.add(getContext().getString(R.string.search_header));
		positionOfSection.put(0, 0);
		sectionOfPosition.put(0, 0);
		for (int i = 1; i < count; i++) {

			String letter = getItem(i).getHeader();
			System.err.println("contactadapter getsection getHeader:" + letter + " name:" + getItem(i).getUsername());
			int section = list.size() - 1;
			if (list.get(section) != null && !list.get(section).equals(letter)) {
				list.add(letter);
				section++;
				positionOfSection.put(section, i);
			}
			sectionOfPosition.put(i, section);
		}
		return list.toArray(new String[list.size()]);
	}
	
	@Override
	public Filter getFilter() {
		if(myFilter==null){
			myFilter = new MyFilter(userList);
		}
		return myFilter;
	}
	
	private class  MyFilter extends Filter{
		List<User> mList=null;
		
		public MyFilter(List<User> myList) {
			super();
			this.mList = myList;
		}

		@Override
		protected synchronized FilterResults  performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			if(mList==null){
				mList = new ArrayList<User>();
			}
			if(prefix==null || prefix.length()==0){
				results.values = copyUserList;
				results.count = copyUserList.size();
			}else{
				String prefixString = prefix.toString();
				final int count = mList.size();
				final ArrayList<User> newValues = new ArrayList<User>();
				for(int i=0;i<count;i++){
					final User user = mList.get(i);
					String username = user.getUsername();
					
					EMConversation conversation = EMChatManager.getInstance().getConversation(username);
					if(conversation != null){
						username = conversation.getUserName();
					}
					
					if(username.startsWith(prefixString)){
						newValues.add(user);
					}
					else{
						 final String[] words = username.split(" ");
	                     final int wordCount = words.length;
	
	                     // Start at index 0, in case valueText starts with space(s)
	                     for (int k = 0; k < wordCount; k++) {
	                         if (words[k].startsWith(prefixString)) {
	                             newValues.add(user);
	                             break;
	                         }
	                     }
					}
				}
				results.values=newValues;
				results.count=newValues.size();
			}
			return results;
		}

		@Override
		protected synchronized void publishResults(CharSequence constraint,
				FilterResults results) {
			userList.clear();
			userList.addAll((List<User>)results.values);
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
	
	
	
	

}
