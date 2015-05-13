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
package com.easemob.applib.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Message;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.applib.model.DefaultHXSDKModel;
import com.easemob.applib.model.HXNotifier;
import com.easemob.applib.model.HXNotifier.HXNotificationInfoProvider;
import com.easemob.applib.model.HXSDKModel;
import com.easemob.applib.utils.HXPreferenceUtils;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatConfig.EMEnvMode;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.GroupChangeListener;
import com.easemob.exceptions.EaseMobException;

/**
 * The developer can derive from this class to talk with HuanXin SDK
 * All the Huan Xin related initialization and global listener are implemented in this class which will 
 * help developer to speed up the SDK integration。
 * this is a global instance class which can be obtained in any codes through getInstance()
 * 
 * 开发人员可以选择继承这个环信SDK帮助类去加快初始化集成速度。此类会初始化环信SDK，并设置初始化参数和初始化相应的监听器
 * 不过继承类需要根据要求求提供相应的函数，尤其是提供一个{@link HXSDKModel}. 所以请实现abstract protected HXSDKModel createModel();
 * 全局仅有一个此类的实例存在，所以可以在任意地方通过getInstance()函数获取此全局实例
 * 
 * @author easemob
 *
 */
public abstract class HXSDKHelper {

	/**
	 * 群组更新完成
	 */
	public interface SyncListener {
		public void onSyncSucess(boolean success);
	}
	
    private static final String TAG = "HXSDKHelper";
    
    /**
     * application context
     */
    protected Context appContext = null;
    
    /**
     * HuanXin mode helper, which will manage the user data and user preferences
     */
    protected HXSDKModel hxModel = null;
    
    /**
     * MyConnectionListener
     */
    protected EMConnectionListener connectionListener = null;
    
    /**
     * HuanXin ID in cache
     */
    protected String hxId = null;
    
    /**
     * password in cache
     */
    protected String password = null;
    
    /**
     * init flag: test if the sdk has been inited before, we don't need to init again
     */
    private boolean sdkInited = false;

    /**
     * the global HXSDKHelper instance
     */
    private static HXSDKHelper me = null;
    
    /**
     * the notifier
     */
    protected HXNotifier notifier = null;

	/**
	 * HuanXin sync groups status listener
	 */
	private List<SyncListener> syncGroupsListeners;

	/**
	 * HuanXin sync contacts status listener
	 */
	private List<SyncListener> syncContactsListeners;

	/**
	 * HuanXin sync blacklist status listener
	 */
	private List<SyncListener> syncBlackListListeners;

	private boolean isSyncingGroupsFromServer = false;

	private boolean isSyncingContactsFromServer = false;

	private boolean isSyncingBlackListFromServer = false;
	
	private boolean syncGroupsFromServerFailed = false;

	private boolean syncContactsFromServerFailed = false;

	private boolean syncBlackListFromServerFailed = false;

//	private boolean syncGroupsFromServerFailed = false;
//
//	private boolean syncContactsFromServerFailed = false;
//
//	private boolean syncBlackListFromServerFailed = false;

	private List<SyncPendingMessage> messages = null;

    protected HXSDKHelper(){
        me = this;
    }
    
    /**
     * this function will initialize the HuanXin SDK
     * 
     * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
     * 
     * 环信初始化SDK帮助函数
     * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
     * 
     * for example:
     * 例子：
     * 
     * public class DemoHXSDKHelper extends HXSDKHelper
     * 
     * HXHelper = new DemoHXSDKHelper();
     * if(HXHelper.onInit(context)){
     *     // do HuanXin related work
     * }
     */
    public synchronized boolean onInit(Context context){
        if(sdkInited){
            return true;
        }

        appContext = context;

		syncGroupsListeners = new ArrayList<SyncListener>();
		syncContactsListeners = new ArrayList<SyncListener>();
		syncBlackListListeners = new ArrayList<SyncListener>();

        // create HX SDK model
        hxModel = createModel();
        
        // create a defalut HX SDK model in case subclass did not provide the model
        if(hxModel == null){
            hxModel = new DefaultHXSDKModel(appContext);
        }
        
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        
        Log.d(TAG, "process app name : " + processAppName);
        
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName == null || !processAppName.equalsIgnoreCase(hxModel.getAppProcessName())) {
            Log.e(TAG, "enter the service process!");
            
            // 则此application::onCreate 是被service 调用的，直接返回
            return false;
        }

        // 初始化环信SDK,一定要先调用init()
        EMChat.getInstance().init(context);
        
        // 设置sandbox测试环境
        // 建议开发者开发时设置此模式
        if(hxModel.isSandboxMode()){
            EMChat.getInstance().setEnv(EMEnvMode.EMSandboxMode);
        }
        
        if(hxModel.isDebugMode()){
            // set debug mode in development process
            EMChat.getInstance().setDebugMode(true);    
        }

        Log.d(TAG, "initialize EMChat SDK");
                
        initHXOptions();
        initListener();
        sdkInited = true;
        return true;
    }
    
    /**
     * get global instance
     * @return
     */
    public static HXSDKHelper getInstance(){
        return me;
    }
    
    public HXSDKModel getModel(){
        return hxModel;
    }
    
    public String getHXId(){
        if(hxId == null){
            hxId = hxModel.getHXId();
        }
        return hxId;
    }
    
    public String getPassword(){
        if(password == null){
            password = hxModel.getPwd();
        }
        return password;    
    }
    
    public void setHXId(String hxId){
        if (hxId != null) {
            if(hxModel.saveHXId(hxId)){
                this.hxId = hxId;
            }
        }
    }
    
    public void setPassword(String password){
        if(hxModel.savePassword(password)){
            this.password = password;
        }
    }
    
    /**
     * the subclass must override this class to provide its own model or directly use {@link DefaultHXSDKModel}
     * @return
     */
    abstract protected HXSDKModel createModel();
    
    /**
     * please make sure you have to get EMChatOptions by following method and set related options
     *      EMChatOptions options = EMChatManager.getInstance().getChatOptions();
     */
    protected void initHXOptions(){
        Log.d(TAG, "init HuanXin Options");
        
        // 获取到EMChatOptions对象
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(hxModel.getAcceptInvitationAlways());
        // 默认环信是不维护好友关系列表的，如果app依赖环信的好友关系，把这个属性设置为true
        options.setUseRoster(hxModel.getUseHXRoster());
        // 设置是否需要已读回执
        options.setRequireAck(hxModel.getRequireReadAck());
        // 设置是否需要已送达回执
        options.setRequireDeliveryAck(hxModel.getRequireDeliveryAck());
        // 设置从db初始化加载时, 每个conversation需要加载msg的个数
        options.setNumberOfMessagesLoaded(1);
        
        notifier = createNotifier();
        notifier.init(appContext);
        
        notifier.setNotificationInfoProvider(getNotificationListener());
    }
    
    /**
     * subclass can override this api to return the customer notifier
     * 
     * @return
     */
    protected HXNotifier createNotifier(){
        return new HXNotifier();
    }
    
    public HXNotifier getNotifier(){
        return notifier;
    }
    
    /**
     * logout HuanXin SDK
     */
    public void logout(final EMCallBack callback){
        setPassword(null);
        EMChatManager.getInstance().logout(new EMCallBack(){

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                if(callback != null){
                    callback.onSuccess();
                }
				HXPreferenceUtils.getInstance().setSettingSyncGroupsFinished(false);
				HXPreferenceUtils.getInstance().setSettingSyncContactsFinished(false);
       		 	HXPreferenceUtils.getInstance().setSettingSyncBlackListFinished(false);
            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub
                if(callback != null){
                    callback.onProgress(progress, status);
                }
            }
            
        });
    }
    
    /**
     * 检查是否已经登录过
     * @return
     */
    public boolean isLogined(){
       return EMChat.getInstance().isLoggedIn();
    }
    
    protected HXNotificationInfoProvider getNotificationListener(){
        return null;
    }

    /**
     * init HuanXin listeners
     */
    protected void initListener(){
        Log.d(TAG, "init listener");
        
        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
            	if (error == EMError.USER_REMOVED) {
            		onCurrentAccountRemoved();
            	}else if (error == EMError.CONNECTION_CONFLICT) {
                    onConnectionConflict();
                }else{
                    onConnectionDisconnected(error);
                }
            }

            @Override
            public void onConnected() {
                onConnectionConnected();
            }
        };
        
        //注册连接监听
        EMChatManager.getInstance().addConnectionListener(connectionListener);       
    }

    /**
     * the developer can override this function to handle connection conflict error
     */
    protected void onConnectionConflict(){}

    
    /**
     * the developer can override this function to handle user is removed error
     */
    protected void onCurrentAccountRemoved(){}
    
    
    /**
     * handle the connection connected
     */
    protected void onConnectionConnected(){}
    
    /**
     * handle the connection disconnect
     * @param error see {@link EMError}
     */
    protected void onConnectionDisconnected(int error){}

    /**
     * check the application process name if process name is not qualified, then we think it is a service process and we will not init SDK
     * @param pID
     * @return
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
    
        
    public void addSyncGroupListener(SyncListener listener) {
	    if (listener == null) {
		    return;
	    }
	    if (!syncGroupsListeners.contains(listener)) {
		    syncGroupsListeners.add(listener);
	    }
    }

    public void removeSyncGroupListener(SyncListener listener) {
	    if (listener == null) {
		    return;
	    }
	    if (syncGroupsListeners.contains(listener)) {
		    syncGroupsListeners.remove(listener);
	    }
    }

    public void addSyncContactListener(SyncListener listener) {
	    if (listener == null) {
		    return;
	    }
	    if (!syncContactsListeners.contains(listener)) {
		    syncContactsListeners.add(listener);
	    }
    }

    public void removeSyncContactListener(SyncListener listener) {
	    if (listener == null) {
		    return;
	    }
	    if (syncContactsListeners.contains(listener)) {
		    syncContactsListeners.remove(listener);
	    }
    }

    public void addSyncBlackListListener(SyncListener listener) {
	    if (listener == null) {
		    return;
	    }
	    if (!syncBlackListListeners.contains(listener)) {
		    syncBlackListListeners.add(listener);
	    }
    }

    public void removeSyncBlackListListener(SyncListener listener) {
	    if (listener == null) {
		    return;
	    }
	    if (syncBlackListListeners.contains(listener)) {
		    syncBlackListListeners.remove(listener);
	    }
    }

    /**
     * 同步操作，从服务器获取群组列表
     * 该方法会记录更新状态，可以通过isSyncingGroupsFromServer获取是否正在更新
     * 和HXPreferenceUtils.getInstance().getSettingSyncGroupsFinished()获取是否更新已经完成
     * @throws EaseMobException
     */
    public synchronized void getGroupsFromServer() throws EaseMobException {
	    try {
	    	isSyncingGroupsFromServer = true;
		    EMGroupManager.getInstance().getGroupsFromServer();
		    HXPreferenceUtils.getInstance().setSettingSyncGroupsFinished(true);		 	
	    	isSyncingGroupsFromServer = false;
		    syncGroupsFromServerFailed = false;
		    for (SyncListener listener : syncGroupsListeners) {
			    listener.onSyncSucess(true);
		    }
	    } catch (EaseMobException e) {
	    	isSyncingGroupsFromServer = false;
		    syncGroupsFromServerFailed = true;
		    for (SyncListener listener : syncGroupsListeners) {
			    listener.onSyncSucess(false);
		    }
		    e.printStackTrace();
		    throw e;
	    }
    }

    public synchronized List<String> getContactsFromServer() throws EaseMobException {
	    List<String> usernames = null;
	    try {
	    	isSyncingContactsFromServer = true;
		    usernames = EMContactManager.getInstance().getContactUserNames();
		    HXPreferenceUtils.getInstance().setSettingSyncContactsFinished(true);
	    	isSyncingContactsFromServer = false;
		    syncContactsFromServerFailed = false;
		    for (SyncListener listener : syncContactsListeners) {
			    listener.onSyncSucess(true);
		    }
	    } catch (EaseMobException e) {
	    	isSyncingContactsFromServer = false;
		    syncContactsFromServerFailed = true;
		    for (SyncListener listener : syncContactsListeners) {
			    listener.onSyncSucess(false);
		    }
		    e.printStackTrace();
		    throw e;
	    }
	    return usernames;
    }

    public synchronized List<String> getBlackListFromServer() throws EaseMobException {
	    List<String> usernames = null; 
	    try {
	    	isSyncingBlackListFromServer = true;
		    usernames = EMContactManager.getInstance().getBlackListUsernamesFromServer();
		    HXPreferenceUtils.getInstance().setSettingSyncBlackListFinished(true);
	    	isSyncingBlackListFromServer = false;
		    syncBlackListFromServerFailed = false;
		    for (SyncListener listener : syncBlackListListeners) {
			    listener.onSyncSucess(true);
		    }
	    } catch (EaseMobException e) {
	    	isSyncingBlackListFromServer = false;
		    syncBlackListFromServerFailed = true;
		    for (SyncListener listener : syncBlackListListeners) {
			    listener.onSyncSucess(false);
		    }
		    e.printStackTrace();
		    throw e;
	    }
	    return usernames;
    }

    public boolean isSyncingGroupsFromServer() {
	    return isSyncingGroupsFromServer;
    }

    public boolean isSyncingContactsFromServer() {
	    return isSyncingContactsFromServer;
    }

    public boolean isSyncingBlackListFromServer() {
	    return isSyncingBlackListFromServer;
    }
    
    public boolean syncGroupsFromServerFailed() {
	    return syncGroupsFromServerFailed;
    }

    public boolean syncContactsFromServerFailed() {
	    return syncContactsFromServerFailed;
    }

    public boolean syncBlackListFromServerFailed() {
	    return syncBlackListFromServerFailed;
    }

    public enum EMSyncPendingMessage {
	    EContactOnAdded,
	    EContactOnDeleted,
	    EContactOnInvited,
	    EContactOnAgreed,
	    EContactOnRefused,
	    EGroupOnInvitationReceived,
	    EGroupOnInvitationAccepted,
	    EGroupOnInvitationDeclined,
	    EGroupOnUserRemoved,
	    EGroupOnGroupDestroy,
	    EGroupOnApplicationReceived,
	    EGroupOnApplicationAccept,
	    EGroupOnApplicationDeclined
    }

    /**
     * 异步登录时，同步联系人和群组时如果收到服务器消息，需要先缓存到本地消息队列中，等待
     * 联系人和群组同步成功后处理 
     */
    static class SyncPendingMessage {
	    private EMSyncPendingMessage type;
	    private Message msg;

	    SyncPendingMessage(EMSyncPendingMessage type, Message msg) {
		    this.type = type;
		    this.msg = msg;
	    }

	    public EMSyncPendingMessage getType() {
		    return this.type;
	    }

	    public Message getMessage() {
		    return this.msg;
	    }
    }

    public void addMessage(EMSyncPendingMessage type, Message msg) {
	    if (messages == null) {
		    messages = new ArrayList<SyncPendingMessage>();
	    }
	    messages.add(new SyncPendingMessage(type, msg));
    }

    /**
     * 异步执行，处理在同步联系人和群组过程中收到的服务器通知    
     */
    @SuppressWarnings("unchecked")
	   	public void handlePendingMessages() {
		    EMContactListener contactListener = EMContactManager.getInstance().getContactListener();
		    List<GroupChangeListener> groupListeners = EMGroupManager.getInstance().getGroupChangeListener();

		    if (contactListener == null || messages == null || groupListeners == null) {
			    return;
		    }
		    while (messages.size() > 0) {
			    Object _msg = messages.get(0);
			    messages.remove(0);
			    if (_msg == null && !(_msg instanceof SyncPendingMessage)) {
				    continue;
			    }
			    SyncPendingMessage msg = (SyncPendingMessage)_msg;
			    Message rawMsg = msg.getMessage();
			    switch (msg.getType()) {
				    // EContact____
				    case EContactOnAdded:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof List<?>) { 
						    contactListener.onContactAdded((List<String>)rawMsg.obj);
					    }
					    break;
				    case EContactOnDeleted:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof List<?>) { 
						    contactListener.onContactDeleted((List<String>)rawMsg.obj);
					    }
					    break;
				    case EContactOnInvited:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof Map<?, ?>) {
						    Map<String, String> params = (Map<String, String>) rawMsg.obj;
						    if (params.containsKey("username") && params.containsKey("reason")) {
							    String username = params.get("username");
							    String reason = params.get("reason");
							    contactListener.onContactInvited(username, reason);
						    }
					    }
					    break;
				    case EContactOnAgreed:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof String) {
						    contactListener.onContactAgreed((String)rawMsg.obj);
					    }
					    break;
				    case EContactOnRefused:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof String) {
						    contactListener.onContactRefused((String)rawMsg.obj);
					    }
					    break;
					    // EGroup____
				    case EGroupOnInvitationReceived:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof Map<?, ?>) {
						    Map<String, String> params = (Map<String, String>) rawMsg.obj;
						    if (params.containsKey("groupId") && params.containsKey("groupName") &&
								    params.containsKey("inviter") && params.containsKey("reason")) {
							    String groupId = params.get("groupId");
							    String groupName = params.get("groupName");
							    String inviter = params.get("inviter");
							    String reason = params.get("reason");
							    for (GroupChangeListener listener : groupListeners) {
								    listener.onInvitationReceived(groupId, groupName, inviter, reason);
							    }
						    }
					    }
					    break;
				    case EGroupOnInvitationAccepted:
					    break;
				    case EGroupOnInvitationDeclined:
					    break;
				    case EGroupOnUserRemoved:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof Map<?, ?>) {
						    Map<String, String> params = (Map<String, String>) rawMsg.obj;
						    if (params.containsKey("groupId") && params.containsKey("groupName")) {
							    String groupId = params.get("groupId");
							    String groupName = params.get("groupName");
							    for (GroupChangeListener listener : groupListeners) {
								    listener.onUserRemoved(groupId, groupName);
							    }
						    }
					    }
					    break;
				    case EGroupOnGroupDestroy:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof Map<?, ?>) {
						    Map<String, String> params = (Map<String, String>) rawMsg.obj;
						    if (params.containsKey("groupId") && params.containsKey("groupName")) {
							    String groupId = params.get("groupId");
							    String groupName = params.get("groupName");
							    for (GroupChangeListener listener : groupListeners) {
								    listener.onGroupDestroy(groupId, groupName);
							    }
						    }
					    }
					    break;
				    case EGroupOnApplicationReceived:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof Map<?, ?>) {
						    Map<String, String> params = (Map<String, String>) rawMsg.obj;
						    if (params.containsKey("groupId") && params.containsKey("groupName") &&
								    params.containsKey("inviter") && params.containsKey("reason")) {
							    String groupId = params.get("groupId");
							    String groupName = params.get("groupName");
							    String inviter = params.get("inviter");
							    String reason = params.get("reason");
							    for (GroupChangeListener listener : groupListeners) {
								    listener.onApplicationReceived(groupId, groupName, inviter, reason);
							    }
						    }
					    }
					    break;
				    case EGroupOnApplicationAccept:
					    if (rawMsg != null && rawMsg.obj != null && rawMsg.obj instanceof Map<?, ?>) {
						    Map<String, String> params = (Map<String, String>) rawMsg.obj;
						    if (params.containsKey("groupId") && params.containsKey("groupName")) {
							    String groupId = params.get("groupId");
							    String groupName = params.get("groupName");
							    String applyer = params.get("applyer");
							    for (GroupChangeListener listener : groupListeners) {
								    listener.onApplicationAccept(groupId, groupName, applyer);
							    }
						    }
					    }
					    break;
				    case EGroupOnApplicationDeclined:
					    break;
				    default:
					    break;
			    }
		    }

		    // release message queue
		    messages.clear();
		    messages = null;
	    }

    public boolean isSyncReady() {
	    return HXPreferenceUtils.getInstance().getSettingSyncGroupsFinished() && 
		    HXPreferenceUtils.getInstance().getSettingSyncContactsFinished() &&
		    HXPreferenceUtils.getInstance().getSettingSyncBlackListFinished();
    }
}
