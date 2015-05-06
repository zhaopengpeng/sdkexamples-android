package com.easemob.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;

import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.uidata.User;
import com.easemob.uidemo.R;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.easemob.widget.factory.EMContactWidgetFactory;

public class EMContactListWidget extends LinearLayout {
	
	public interface EMContactListWidgetUser {
		public void onItemClick(int position, Object obj);
	}
	
	Context context;
	ListView listView;
	ContactAdapter adapter;
	List<User> contactList = new ArrayList<User>();
	private Sidebar sidebar;
	private EMContactListWidgetUser user;
	
	public static final int MSG_UPDATE_LIST = 0;
	
	Handler handler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_LIST:
				if(adapter != null)
				    adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	public EMContactListWidget(Context context) {
		super(context);
		init(context);
	}

	public EMContactListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public EMContactListWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public EMContactListWidget(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	private void init(Context context) {
		this.context = context;
		View view = LayoutInflater.from(context).inflate(R.layout.em_contact_widget, this);
		listView = (ListView)view.findViewById(R.id.list);
		adapter = new ContactAdapter(context, 0, contactList);
		listView.setAdapter(adapter);
		sidebar = (Sidebar) view.findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (user != null) {
					user.onItemClick(position, getItem(position));
				}
			}
		});
		
		refresh();
	}
	
	private void setUserHeader(String username, User user) {
		String headerName = null;
		if (!TextUtils.isEmpty(user.getNick())) {
			headerName = user.getNick();
		} else {
			headerName = user.getUsername();
		}
		if (Character.isDigit(headerName.charAt(0))) {
			user.setHeader("#");
		} else {
			user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
					.toUpperCase());
			char header = user.getHeader().toLowerCase().charAt(0);
			if (header < 'a' || header > 'z') {
				user.setHeader("#");
			}
		}
	}
	
	/**
	 * 用户可以派生自己的EMContactListWidget
	 * 并重写getContactList，来提供自定义的联系人列表
	 * @return
	 */
	protected List<User> getContactList() {
		List<String> contacts = null;
		List<User> results = new ArrayList<User>();
		try {
			contacts = EMChatManager.getInstance().getContactUserNames();
		} catch (EaseMobException e) {
			e.printStackTrace();
		}
		if (contacts == null) {
			return results;
		}
		for (String username : contacts) {
			User user = new User(username);
			results.add(user);
			setUserHeader("", user);
		}
		return results;
	}
	
	public void refresh() {
		contactList.clear();
		contactList.addAll(getContactList());
		Collections.sort(contactList, new Comparator<User>() {
			@Override
			public int compare(final User user1, final User user2) {
				return user1.getUsername().compareTo(user2.getUsername());
			}
		});
		
		EMContactWidgetFactory.getInstance(context).onPreUpdateData(contactList);

		adapter.updateSections();
		
		Message msg = handler.obtainMessage(MSG_UPDATE_LIST);
		handler.sendMessage(msg);
	}
	
	public void filter(CharSequence str) {
		adapter.getFilter().filter(str);
	}
	
	class ContactAdapter extends ArrayAdapter<User>  implements SectionIndexer{
		private static final String TAG = "ContactAdapter";
		List<String> list;
		List<User> userList;
		List<User> copyUserList;
		private SparseIntArray positionForSection;
		private SparseIntArray sectionForPosition;
		private MyFilter myFilter;
	    private boolean notiyfyByFilter;

		public ContactAdapter(Context context, int resource, List<User> objects) {
			super(context, resource, objects);
			this.userList = objects;
			copyUserList = new ArrayList<User>();
			copyUserList.addAll(objects);
		}
		
		public void updateSections() {
			getSections();
		}

		public int getPositionForSection(int section) {
			return positionForSection.get(section);
		}

		public int getSectionForPosition(int position) {
			return sectionForPosition.get(position);
		}
		
		@Override
		public Object[] getSections() {
			positionForSection = new SparseIntArray();
			sectionForPosition = new SparseIntArray();
			int count = getCount();
			list = new ArrayList<String>();
			int section = 0;
			String prev = "";
			for (int i = 0; i < count; i++) {
				String header = getItem(i).getHeader();
				if (getItem(i).getHeader() != null && !prev.equals(header)) {
					list.add(header);
					section++;
					positionForSection.put(section, i);
					prev = header;
				}
				sectionForPosition.put(i, section);
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
	        List<User> mOriginalList = null;
			
			public MyFilter(List<User> myList) {
				this.mOriginalList = myList;
			}

			@Override
			protected synchronized FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();
				if(mOriginalList==null){
				    mOriginalList = new ArrayList<User>();
				}
				EMLog.d(TAG, "contacts original size: " + mOriginalList.size());
				EMLog.d(TAG, "contacts copy size: " + copyUserList.size());
				
				if(prefix==null || prefix.length()==0){
					results.values = copyUserList;
					results.count = copyUserList.size();
				}else{
					String prefixString = prefix.toString();
					final int count = mOriginalList.size();
					final ArrayList<User> newValues = new ArrayList<User>();
					for(int i=0;i<count;i++){
						final User user = mOriginalList.get(i);
						String username = user.getUsername();
						
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
				EMLog.d(TAG, "contacts filter results size: " + results.count);
				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected synchronized void publishResults(CharSequence constraint,
					FilterResults results) {
				userList.clear();
				userList.addAll((List<User>)results.values);
				EMLog.d(TAG, "publish contacts filter results size: " + results.count);
				if (results.count > 0) {
				    notiyfyByFilter = true;
					notifyDataSetChanged();
					notiyfyByFilter = false;
				} else {
					notifyDataSetInvalidated();
				}
			}
		}
		
		
		@Override
		public void notifyDataSetChanged() {
		    super.notifyDataSetChanged();
		    if(!notiyfyByFilter){
		        copyUserList.clear();
		        copyUserList.addAll(userList);
		    }
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = EMContactWidgetFactory.getInstance(context).generateView(position, convertView, parent);
			if (view != null) {
				return view;
			}
			boolean isSectionIndex = false;
			if (positionForSection != null) {
				isSectionIndex = positionForSection.indexOfValue(position) >= 0;
			}
			User user = null;
			if (position < userList.size()) {
				user = userList.get(position);
			} else {
				return null;
			}
			return EMContactWidgetFactory.getInstance(context).generateView(user, convertView, parent, isSectionIndex);
		}
	}
	
	public ListView getListView() {
		return listView;
	}

	public Object getItem(int position) {
		return adapter.getItem(position);
	}
	
	public void setUser(EMContactListWidgetUser user) {
		this.user = user;
	}

}
