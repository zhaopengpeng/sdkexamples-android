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
package com.easemob.chatuidemo.activity;

import java.util.List;

import android.R.anim;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easemob.applib.controller.HXSDKHelper;
import com.easemob.applib.utils.HXPreferenceUtils;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.GroupAdapter;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;

public class GroupsActivity extends BaseActivity {
	public static final String TAG = "GroupsActivity";
	private ListView groupListView;
	protected List<EMGroup> grouplist;
	private GroupAdapter groupAdapter;
	private InputMethodManager inputMethodManager;
	public static GroupsActivity instance;
	private SyncListener syncListener;
	private ProgressDialog pd;

	private SwipeRefreshLayout swipeRefreshLayout;

	class SyncListener implements HXSDKHelper.SyncListener {
		@Override
		public void onSyncSucess(final boolean success) {
			EMLog.d(TAG, "onSyncGroupsFinish success:" + success);
			runOnUiThread(new Runnable() {
				public void run() {
					if (pd != null) {
						pd.hide();
					}
					swipeRefreshLayout.setRefreshing(false);
					if (success) {
						groupListView.setVisibility(View.VISIBLE);
					} else {
						if (!GroupsActivity.this.isFinishing()) {
							String s1 = getResources().getString(R.string.Failed_to_get_group_chat_information);
							Toast.makeText(GroupsActivity.this, s1, 1).show();
						}
						return;
					}
					if (groupListView != null && groupAdapter != null) {
						grouplist = EMGroupManager.getInstance().getAllGroups();
						groupAdapter = new GroupAdapter(GroupsActivity.this, 1, grouplist);
						groupListView.setAdapter(groupAdapter);
						groupAdapter.notifyDataSetChanged();
					}
				}
			});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_groups);

		instance = this;
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		grouplist = EMGroupManager.getInstance().getAllGroups();
		groupListView = (ListView) findViewById(R.id.list);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);

		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub

				initProgressDialog();
				if (pd != null) {
					pd.show();
				}
				new Thread() {
					@Override
					public void run() {
						try {
							HXSDKHelper.getInstance().getGroupsFromServer();
							HXPreferenceUtils.getInstance().setSettingSyncGroupsFinished(true);
						} catch (EaseMobException e) {
							e.printStackTrace();
						}
					}
				}.start();

			}
		});

		groupAdapter = new GroupAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);
		groupListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == groupAdapter.getCount() - 1) {
					// 新建群聊
					startActivityForResult(new Intent(GroupsActivity.this, NewGroupActivity.class), 0);
				} else {

					// 进入群聊
					Intent intent = new Intent(GroupsActivity.this, ChatActivity.class);
					// it is group chat
					intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
					intent.putExtra("groupId", groupAdapter.getItem(position - 1).getGroupId());
					startActivityForResult(intent, 0);
				}
			}

		});
		groupListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});

		syncListener = new SyncListener();
		HXSDKHelper.getInstance().addSyncGroupListener(syncListener);

		EMLog.d(TAG, "isSyncingGroupsFromServer:" + HXSDKHelper.getInstance().isSyncingGroupsFromServer());

		if (!HXSDKHelper.getInstance().isSyncingGroupsFromServer()
				&& !HXPreferenceUtils.getInstance().getSettingSyncGroupsFinished()) {
			groupListView.setVisibility(View.GONE);
		}
	}

	private void initProgressDialog() {
		if (pd != null) {
			return;
		}
		pd = new ProgressDialog(this);
		pd.setMessage(getString(R.string.Sync_Groups_From_Server));
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
			}
		});
	}

	/**
	 * 进入公开群聊列表
	 */
	public void onPublicGroups(View view) {
		startActivity(new Intent(this, PublicGroupsActivity.class));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		grouplist = EMGroupManager.getInstance().getAllGroups();
		groupAdapter = new GroupAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);
		groupAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		if (syncListener != null) {
			HXSDKHelper.getInstance().removeSyncGroupListener(syncListener);
			syncListener = null;
		}
		super.onDestroy();
		instance = null;
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}
}
