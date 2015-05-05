package com.easemob.chatuidemo.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.uidata.User;
import com.easemob.widget.EMContactListWidget;

public class ContactListWidget extends EMContactListWidget {

	public ContactListWidget(Context context) {
		super(context);
	}

	public ContactListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ContactListWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ContactListWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	protected List<User> getContactList() {
		List<User> contactList = new ArrayList<User>();
		for (com.easemob.chatuidemo.domain.User user : DemoApplication.getInstance().getContactList().values()) {
			User temp = new User(user.getUsername());
			contactList.add(temp);
		}
		return contactList;
	}
}
