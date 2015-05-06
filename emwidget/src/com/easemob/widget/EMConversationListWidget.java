package com.easemob.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.widget.factory.EMConversationWidgetFactory;

public class EMConversationListWidget extends ListView implements AdapterView.OnItemClickListener {
	
	public interface EMConversationListWidgetUser {
		public void onClick(EMConversation conversation);
	}

	private final int MSG_REFRESH_ADAPTER_DATA = 0;
	
	private Context context;
	private ConverastionListAdapater adapter;
	private List<EMConversation> allConversations = new ArrayList<EMConversation>();
	private EMConversationListWidgetUser user;
	private List<EMConversation> copyConversationList;
	
	public EMConversationListWidget(Context context) {
		super(context);
		init(context);
	}

	public EMConversationListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public EMConversationListWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public EMConversationListWidget(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	private void init(Context context) {
		this.context = context;
		adapter = new ConverastionListAdapater(context, 0, allConversations);
		copyConversationList = new ArrayList<EMConversation>();

		this.setAdapter(adapter);

		this.setOnItemClickListener(this);

		refresh();
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MSG_REFRESH_ADAPTER_DATA:
				if (adapter != null) {
					adapter.conversationList.clear();
					adapter.conversationList.addAll(allConversations);
					adapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}
	};
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (EMConversationWidgetFactory.getInstance(context).onItemClick(adapter.getItem(position), position)) {
			return;
		}
		
		// default onItemClick behavior
		EMConversation conversation = (EMConversation) adapter.getItem(position);
		String username = conversation.getUserName();
		/*
	    // 进入聊天页面
//	    Intent intent = new Intent(EMConversationWidgetFactory.DEFAULT_ACTION);
	    Intent intent = new Intent(context, ChatActivity.class);
	    // 想改成下面的
//	    intent.putExtra(EMConversationWidgetFactory.DEFAULT_ACTION_PARAM_USERNAME, username);
//	    intent.putExtra(EMConversationWidgetFactory.DEFAULT_ACTION_PARAM_CHATTYPE,
//	    		conversation.isGroup() ? EMConversationWidgetFactory.CHATTYPE_CHAT : EMConversationWidgetFactory.CHATTYPE_GROUPCHAT);
	    
	    intent.putExtra("chatType", 1);
	    intent.putExtra("userId", username);
	    context.startActivity(intent);
	    */
		user.onClick(conversation);
	}

	/**
	 * 获取所有会话
	 * 
	 * @param context
	 * @return
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        +	 */
	private List<EMConversation> loadConversationsWithRecentChat() {
		// 获取所有会话，包括陌生人
		Map<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
		// 过滤掉messages size为0的conversation
		/**
		 * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
		 * 影响排序过程，Collection.sort会产生异常
		 * 保证Conversation在Sort过程中最后一条消息的时间不变 
		 * 避免并发问题
		 */
		List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
		synchronized (conversations) {
			for (EMConversation conversation : conversations.values()) {
				if (conversation.getAllMessages().size() != 0) {
					sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
				}
			}
		}
		try {
			// Internal is TimSort algorithm, has bug
			sortConversationByLastChatTime(sortList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<EMConversation> list = new ArrayList<EMConversation>();
		for (Pair<Long, EMConversation> sortItem : sortList) {
			list.add(sortItem.second);
		}
		return list;
	}

	/**
	 * 根据最后一条消息的时间排序
	 * 
	 * @param usernames
	 */
	private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
		Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
			@Override
			public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

				if (con1.first == con2.first) {
					return 0;
				} else if (con2.first > con1.first) {
					return 1;
				} else {
					return -1;
				}
			}

		});
	}
	
	public EMConversation getItem(int position) {
		return (EMConversation)adapter.getItem(position);
	}
	
	public void refresh() {
		allConversations = loadConversationsWithRecentChat();

		EMConversationWidgetFactory.getInstance(context).onPreUpdateData(allConversations);
		
		handler.sendEmptyMessage(MSG_REFRESH_ADAPTER_DATA);
	}
	
	public void filter(CharSequence str) {
		adapter.getFilter().filter(str);
	}
	
	class ConverastionListAdapater extends ArrayAdapter<EMConversation> {
		List<EMConversation> conversationList;
		private ConversationFilter conversationFilter;

		public ConverastionListAdapater(Context context, int resource,
				List<EMConversation> objects) {
			super(context, resource, objects);
			conversationList = objects;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stubEMApplication
			return conversationList.size();
		}

		@Override
		public EMConversation getItem(int arg0) {
			if (arg0 < conversationList.size()) {
				return conversationList.get(arg0);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return EMConversationWidgetFactory.getInstance(context).generateView(position, getItem(position), convertView, parent);
		}
		
		@Override
		public Filter getFilter() {
			if (conversationFilter == null) {
				conversationFilter = new ConversationFilter();
			}
			return conversationFilter;
		}
		
		class ConversationFilter extends Filter {

			@Override
			protected FilterResults performFiltering(CharSequence filter) {
				FilterResults results = new FilterResults();

				if (filter == null || filter.length() == 0) {
					results.values = allConversations;
					results.count = allConversations.size();
				} else {
					String prefixString = filter.toString();
					final int count = allConversations.size();
					final List<EMConversation> newValues = new ArrayList<EMConversation>();
					Map<String, EMConversation> container = new HashMap<String, EMConversation>();
					
					// prepare container data, if data match filter
					// data will be added to newValues, and removed from container 
					for (int i = 0; i < count; i++) {
						final EMConversation conversation = allConversations.get(i);
						String username = conversation.getUserName();

						EMGroup group = EMGroupManager.getInstance().getGroup(
								username);
						if (group != null) {
							username = group.getGroupName();
						}
						container.put(username, conversation);
					}
					
					// startWith
					List<String> toRemove = new ArrayList<String>();
					for (String username : container.keySet()) {
						if (username.startsWith(prefixString)) {
							newValues.add(container.get(username));
							toRemove.add(username);
						}
					}
					for (String username : toRemove) {
						container.remove(username);
					}
					
					// contains
					for (String username : container.keySet()) {
						if (username.contains(prefixString)) {
							newValues.add(container.get(username));
							toRemove.add(username);
						}
					}
					for (String username : toRemove) {
						container.remove(username);
					}

					results.values = newValues;
					results.count = newValues.size();
				}
				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				conversationList.clear();
				conversationList.addAll((List<EMConversation>) results.values);
				if (results.count > 0) {
//				    notiyfyByFilter = true;
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		}
	}
	
	public void setUser(EMConversationListWidgetUser user) {
		this.user= user;
	}
}
