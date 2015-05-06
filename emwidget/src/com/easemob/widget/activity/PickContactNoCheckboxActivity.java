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
package com.easemob.widget.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SectionIndexer;

import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.uidata.User;
import com.easemob.uidemo.R;
import com.easemob.util.EMLog;
import com.easemob.widget.Sidebar;
import com.easemob.widget.factory.EMContactWidgetFactory;

public class PickContactNoCheckboxActivity extends BaseActivity {

	private ListView listView;
	private Sidebar sidebar;
	protected ContactAdapter contactAdapter;
	private List<User> contactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_pick_contact_no_checkbox);
		listView = (ListView) findViewById(R.id.list);
		sidebar = (Sidebar) findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		contactList = new ArrayList<User>();
		// 获取设置contactlist
		getContactList();
		// 设置adapter
		contactAdapter = new ContactAdapter(this, R.layout.em_row_contact, contactList);
		listView.setAdapter(contactAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}
		});
	}

	protected void onListItemClick(int position) {
//		if (position != 0) {
			setResult(RESULT_OK, new Intent().putExtra("username", contactAdapter.getItem(position)
					.getUsername()));
			finish();
//		}
	}

	public void back(View view) {
		finish();
	}

	private void getContactList() {
		contactList.clear();
		List<String> contacts = null;
		try {
			contacts = EMChatManager.getInstance().getContactUserNames();
		} catch (EaseMobException e) {
			e.printStackTrace();
		}
		if (contacts == null) {
			return;
		}
		for (String username : contacts) {
			User user = new User(username);
			contactList.add(user);
		}
		// 排序
		Collections.sort(contactList, new Comparator<User>() {

			@Override
			public int compare(User lhs, User rhs) {
				return lhs.getUsername().compareTo(rhs.getUsername());
			}
		});
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
			View view = EMContactWidgetFactory.getInstance(PickContactNoCheckboxActivity.this).generateView(position, convertView, parent);
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
			return EMContactWidgetFactory.getInstance(PickContactNoCheckboxActivity.this).generateView(user, convertView, parent, isSectionIndex);
		}
	}
}
