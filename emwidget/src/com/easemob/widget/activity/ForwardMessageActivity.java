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

import android.os.Bundle;

import com.easemob.uidata.User;

public class ForwardMessageActivity extends PickContactNoCheckboxActivity {
	private User selectUser;
	private String forward_msg_id;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		forward_msg_id = getIntent().getStringExtra("forward_msg_id");
	}

	@Override
	protected void onListItemClick(int position) {
		// TODO: EMWidget
//		selectUser = contactAdapter.getItem(position);
//		Intent intent = new Intent(this, ChatActivity.class);
//		if (selectUser == null)
//			return;
//		// it is single chat
//		intent.putExtra("userId", selectUser.getUsername());
//		intent.putExtra("forward_msg_id", forward_msg_id);
//		startActivity(intent);
//		finish();
	}

}
