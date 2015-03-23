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
package com.easemob.chatuidemo;

import java.util.Map;

import u.aly.aa;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.applib.controller.HXSDKHelper;
import com.easemob.applib.model.HXNotifier;
import com.easemob.applib.model.HXSDKModel;
import com.easemob.applib.model.HXNotifier.NotificationListener;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.OnNotificationClickListener;
import com.easemob.chatuidemo.activity.ChatActivity;
import com.easemob.chatuidemo.activity.MainActivity;
import com.easemob.chatuidemo.domain.User;
import com.easemob.chatuidemo.receiver.CallReceiver;
import com.easemob.chatuidemo.utils.CommonUtils;
import com.easemob.util.EMLog;
import com.easemob.util.EasyUtils;

/**
 * Demo UI HX SDK helper class which subclass HXSDKHelper
 * @author easemob
 *
 */
public class DemoHXSDKHelper extends HXSDKHelper{

    private static final String TAG = "DemoHXSDKHelper";
    /**
     * contact list in cache
     */
    private Map<String, User> contactList;
    private CallReceiver callReceiver;
    
    @Override
    protected void initHXOptions(){
        super.initHXOptions();
        // you can also get EMChatOptions to set related SDK options
        // EMChatOptions options = EMChatManager.getInstance().getChatOptions();
    }
    

    @Override
    protected void initListener(){
        super.initListener();
        IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction());
        if(callReceiver == null)
            callReceiver = new CallReceiver();
        //注册通话广播接收者
        appContext.registerReceiver(callReceiver, callFilter);    
        
    }
    
    @Override
    protected EMEventListener getEventListener() {
        return new EMEventListener() {
            
            @Override
            public void onEvent(EMNotifierEvent event) {
                EMMessage message = (EMMessage)event.getData();
                EMLog.d(TAG, "收到消息, messge type : " + event.getType() + ",id : " + message.getMsgId());
                switch (event.getType()) {
                case TypeNormalMessage:
                    //应用在后台，不需要刷新UI,通知栏提示新消息
                    if(!EasyUtils.isAppRunningForeground(appContext)){
                        HXNotifier.getInstance(appContext).notifyChatMsg(message);
                    }else{
                        if(ChatActivity.activityInstance != null){ //聊天页面在
                            if(isCurrentConversationMessage(message)){
                                //当前会话过来的消息
                                ChatActivity.activityInstance.onEvent(event);
                            }else{
                                HXNotifier.getInstance(appContext).notifyChatMsg(message);
                            }
                        }else{
                            //主页面在并且为top activity
                            if(MainActivity.activityInstance != null && 
                                    EasyUtils.getTopActivityName(appContext).equals(MainActivity.class.getName())){
                                //调用mian的onEvent();
                                MainActivity.activityInstance.onEvent(event);
                            }else{
                                HXNotifier.getInstance(appContext).notifyChatMsg(message);
                            }
                        }
                        //把事件向后传递
//                        HXNotifier.getInstance(appContext).notifyAllEventListeners(event);
                    }
                    break;
                case TypeCMD:
                    EMLog.d(TAG, "收到透传消息");
                    //获取消息body
                    CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
                    String action = cmdMsgBody.action;//获取自定义action
                    
                    //获取扩展属性 此处省略
//                  message.getStringAttribute("");
                    EMLog.d(TAG, String.format("透传消息：action:%s,message:%s", action,message.toString()));
                    String str = appContext.getString(R.string.receive_the_passthrough);
                    Toast.makeText(appContext, str+action, Toast.LENGTH_SHORT).show();
                    break;
                case TypeDeliveryAck:
                    message.setDelivered(true);
                    if(ChatActivity.activityInstance != null)
                        ChatActivity.activityInstance.onEvent(event);
//                    HXNotifier.getInstance(appContext).notifyAllEventListeners(event);
                    break;
                case TypeReadAck:
                    message.setAcked(true);
                    if(ChatActivity.activityInstance != null)
                        ChatActivity.activityInstance.onEvent(event);
//                    HXNotifier.getInstance(appContext).notifyAllEventListeners(event);
                    break;

                default:
                    break;
                }
            }
        };
    }

    /**
     * 自定义通知栏提示内容
     * @return
     */
    @Override
    protected NotificationListener getNotificationListener() {
        //可以覆盖默认的设置
        return new NotificationListener() {
            
            @Override
            public String setNotificationTitle(EMMessage message) {
              //修改标题,这里使用默认
                return null;
            }
            
            @Override
            public int setNotificationSmallIcon(EMMessage message) {
              //设置小图标，这里为默认
                return 0;
            }
            
            @Override
            public String setNotificationNotifyText(EMMessage message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                String ticker = CommonUtils.getMessageDigest(message, appContext);
                if(message.getType() == Type.TXT)
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                return message.getFrom() + ": " + ticker;
            }
            
            @Override
            public String setNotificationLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                return null;
                // return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
            }
            
            @Override
            public Intent setNotificationClickLaunchIntent(EMMessage message) {
                //设置点击通知栏跳转事件
                Intent intent = new Intent(appContext, ChatActivity.class);
                ChatType chatType = message.getChatType();
                if (chatType == ChatType.Chat) { // 单聊信息
                    intent.putExtra("userId", message.getFrom());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                } else { // 群聊信息
                    // message.getTo()为群聊id
                    intent.putExtra("groupId", message.getTo());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                }
                return intent;
            }
        };
    }
    
    
    
    @Override
    protected void onConnectionConflict(){
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("conflict", true);
        appContext.startActivity(intent);
    }
    
    @Override
    protected void onCurrentAccountRemoved(){
    	Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        appContext.startActivity(intent);
    }
    

    @Override
    protected HXSDKModel createModel() {
        return new DemoHXSDKModel(appContext);
    }
    
    /**
     * get demo HX SDK Model
     */
    public DemoHXSDKModel getModel(){
        return (DemoHXSDKModel) hxModel;
    }
    
    /**
     * 获取内存中好友user list
     *
     * @return
     */
    public Map<String, User> getContactList() {
        if (getHXId() != null && contactList == null) {
            contactList = ((DemoHXSDKModel) getModel()).getContactList();
        }
        
        return contactList;
    }

    /**
     * 设置好友user list到内存中
     *
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        this.contactList = contactList;
    }
    
    @Override
    public void logout(final EMCallBack callback){
        endCall();
        super.logout(new EMCallBack(){

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                setContactList(null);
                getModel().closeDB();
                if(callback != null){
                    callback.onSuccess();
                }
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
     * 是否为当前会话过来的消息
     * @param message
     * @return
     */
    private boolean isCurrentConversationMessage(EMMessage message){
        if (message.getChatType() == ChatType.GroupChat) { //群组消息
            if (message.getTo().equals(ChatActivity.activityInstance.getToChatUsername())){
                return true;
            }
        } else { //单聊消息
            if (message.getFrom().equals(ChatActivity.activityInstance.getToChatUsername()))
                return true;
        }
        return false;
    }
    
    
    void endCall(){
        try {
            EMChatManager.getInstance().endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
