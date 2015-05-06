package com.easemob.widget.factory;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class EMConversationWidgetFactory extends EMWidgetFactory {

	private static EMConversationWidgetFactory instance = null;
	
	public static final String DEFAULT_ACTION = "com.easemob.intent.EM_ACTION_CHAT"; 
	public static final String DEFAULT_ACTION_PARAM_USERNAME = "username"; 
	public static final String DEFAULT_ACTION_PARAM_CHATTYPE = "chattype"; 

	public static final String CHATTYPE_CHAT = "chat"; 
	public static final String CHATTYPE_GROUPCHAT = "groupchat"; 
	
	public static EMConversationWidgetFactory getInstance(Context context) {
		if (instance == null) {
			instance = new EMConversationWidgetFactoryImp(context);
		}
		return instance;
	}
	
	public static void setFactory(EMConversationWidgetFactory factory) {
		instance = factory;
	}
	
	public EMConversationWidgetFactory(Context context) {
		super(context);
	}

	public View generateView(int position, Object obj, View convertView, ViewGroup parent) {
		return null;
	}
	
	public View generateView(Object obj, View convertView, ViewGroup parentView) {
		return null;
	}
	
	/**
	 * 
	 * @param obj: it supposed to be EMConversation type, 
	 * 				but user can add customized data in onPreUpdateData, so the obj's class type may varied from it. 
	 * @param position: item's list position
	 *  
	 * @return
	 * 		false: not handled in current EMConversationWidgetFactory
	 * 		true: handled in current EMConversationWidgetFactory, 
	 * 			  will no longer execute EMConversationListWidget's onItemClick behavior
	 */
	public boolean onItemClick(Object obj, int position) {
		return false;
	}
}
