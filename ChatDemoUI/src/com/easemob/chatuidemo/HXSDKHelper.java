package com.easemob.chatuidemo;

import java.util.Iterator;
import java.util.List;

import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.OnNotificationClickListener;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * The developer can directly use this HuanXin SDK helper or derive from this class to talk with HuanXin SDK
 * All the Huan Xin related initialization and global listener are implemented in this class which will 
 * help developer to speed up the SDK integration
 * 
 * @author easemob
 *
 */
public class HXSDKHelper {
    /**
     * application context
     */
    protected Context appContext = null;
    
    /**
     * HuanXin mode helper, which will manage the user data and user preferences
     */
    protected HXSDKMode hxModel = null;
    
    /**
     * MyConnectionListener
     */
    protected EMConnectionListener connectionListener = null;
    
    /**
     * init flag: if the sdk has been inited before, we don't need to init again
     */
    private boolean sdkInited = false;

    /**
     * this function will initialize the HuanXin SDK
     * 
     * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
     * 
     * for example:
     * 
     * public class DemoHXSDKHelper extends HXSDKHelper
     * 
     * HXHelper = new DemoHXSDKHelper();
     * if(HXHelper.onInit(context)){
     *     // do HuanXin related work
     * }
     */
    public synchronized boolean onInit(Context context, HXSDKMode model){
        if(sdkInited){
            return false;
        }

        appContext = context;
        hxModel = model;
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        
        // 如果使用到百度地图或者类似启动remote service的第三方库，这个if判断不能少
        if (processAppName == null || processAppName.equals("")) {
            // workaround for baidu location sdk
            // 百度定位sdk，定位服务运行在一个单独的进程，每次定位服务启动的时候，都会调用application::onCreate
            // 创建新的进程。
            // 但环信的sdk只需要在主进程中初始化一次。 这个特殊处理是，如果从pid 找不到对应的processInfo
            // processName，
            // 则此application::onCreate 是被service 调用的，直接返回
            return false;
        }
        
        // 初始化环信SDK,一定要先调用init()
        EMChat.getInstance().init(context);
        EMChat.getInstance().setDebugMode(true);
        Log.d("EMChat Demo", "initialize EMChat SDK");
        
        initHXOptions();
        initListener();
        sdkInited = true;
        return true;
    }
    
    /**
     * please make sure you have to get EMChatOptions by following method and set related options
     *      EMChatOptions options = EMChatManager.getInstance().getChatOptions();
     */
    protected void initHXOptions(){
        // 获取到EMChatOptions对象
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        // 默认环信是不维护好友关系列表的，如果app依赖环信的好友关系，把这个属性设置为true
        options.setUseRoster(false);
        // 设置收到消息是否有新消息通知(声音和震动提示)，默认为true
        options.setNotifyBySoundAndVibrate(hxModel.getSettingMsgNotification());
        // 设置收到消息是否有声音提示，默认为true
        options.setNoticeBySound(hxModel.getSettingMsgSound());
        // 设置收到消息是否震动 默认为true
        options.setNoticedByVibrate(hxModel.getSettingMsgVibrate());
        // 设置语音消息播放是否设置为扬声器播放 默认为true
        options.setUseSpeaker(hxModel.getSettingMsgSpeaker());
        // 设置notification消息点击时，跳转的intent为自定义的intent
        
        options.setOnNotificationClickListener(getNotificationClickListener());
        options.setNotifyText(getMessageNotifyListener());
    }
    
    /**
     * get the message notify listener
     * @return
     */
    protected OnMessageNotifyListener getMessageNotifyListener(){
        return null;
    }
    
    /**
     *get notification click listener
     */
    protected OnNotificationClickListener getNotificationClickListener(){
        return null;
    }

    /**
     * init HuanXin listeners
     */
    protected void initListener(){
        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                if (error == EMError.CONNECTION_CONFLICT) {
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
    }
    
    /**
     * the developer can override this function to handle connection conflict error
     */
    protected void onConnectionConflict(){
    }

    /**
     * handle the connection connected
     */
    protected void onConnectionConnected(){
        
    }
    
    /**
     * handle the connection disconnect
     * @param error see {@link EMError}
     */
    protected void onConnectionDisconnected(int error){
        
    }

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
}
