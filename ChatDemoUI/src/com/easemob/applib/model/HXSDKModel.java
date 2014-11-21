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
package com.easemob.applib.model;

import java.util.List;
import java.util.Map;

import com.easemob.applib.controller.HXSDKHelper;
import com.easemob.chatuidemo.domain.User;

/**
 * HX SDK app model which will manage the user data and preferences
 * @author youni
 *
 */
public abstract class HXSDKModel {
    public abstract void setSettingMsgNotification(boolean paramBoolean);

    public abstract boolean getSettingMsgNotification();

    public abstract void setSettingMsgSound(boolean paramBoolean);

    public abstract boolean getSettingMsgSound();

    public abstract void setSettingMsgVibrate(boolean paramBoolean);

    public abstract boolean getSettingMsgVibrate();

    public abstract void setSettingMsgSpeaker(boolean paramBoolean);

    public abstract boolean getSettingMsgSpeaker();
    
    public boolean getUseHXRoster(){
        return false;
    }
    
    public abstract boolean saveUserName(String username);
    public abstract String getUserName();
    
    public abstract boolean savePassword(String pwd);
    public abstract String getPwd();
    
    public abstract boolean saveContactList(List<User> contactList);
    public abstract Map<String, User> getContactList();
    
    public abstract void closeDB();
}
