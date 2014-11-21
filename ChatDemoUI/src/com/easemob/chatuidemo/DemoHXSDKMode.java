package com.easemob.chatuidemo;

import com.easemob.chatuidemo.utils.PreferenceUtils;

import android.content.Context;

public class DemoHXSDKMode extends HXSDKMode{
    protected Context context = null;
    public DemoHXSDKMode(Context ctx){
        context = ctx;
    }
    
    @Override
    public void setSettingMsgNotification(boolean paramBoolean) {
        // TODO Auto-generated method stub
        PreferenceUtils.getInstance(context).setSettingMsgNotification(paramBoolean);
    }

    @Override
    public boolean getSettingMsgNotification() {
        // TODO Auto-generated method stub
        return PreferenceUtils.getInstance(context).getSettingMsgNotification();
    }

    @Override
    public void setSettingMsgSound(boolean paramBoolean) {
        // TODO Auto-generated method stub
        PreferenceUtils.getInstance(context).setSettingMsgSound(paramBoolean);
    }

    @Override
    public boolean getSettingMsgSound() {
        // TODO Auto-generated method stub
        return PreferenceUtils.getInstance(context).getSettingMsgSound();
    }

    @Override
    public void setSettingMsgVibrate(boolean paramBoolean) {
        // TODO Auto-generated method stub
        PreferenceUtils.getInstance(context).setSettingMsgVibrate(paramBoolean);
    }

    @Override
    public boolean getSettingMsgVibrate() {
        // TODO Auto-generated method stub
        return PreferenceUtils.getInstance(context).getSettingMsgVibrate();
    }

    @Override
    public void setSettingMsgSpeaker(boolean paramBoolean) {
        // TODO Auto-generated method stub
        PreferenceUtils.getInstance(context).setSettingMsgSpeaker(paramBoolean);
    }

    @Override
    public boolean getSettingMsgSpeaker() {
        // TODO Auto-generated method stub
        return PreferenceUtils.getInstance(context).getSettingMsgSpeaker();
    }

}
