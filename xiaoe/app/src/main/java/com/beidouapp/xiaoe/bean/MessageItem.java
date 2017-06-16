package com.beidouapp.xiaoe.bean;

/**
 * Created by hHui on 2017/6/12.
 * <p>
 * 消息实体
 */

public class MessageItem {
    MessageOwner owner = MessageOwner.me;
    /**
     * 消息内容
     */
    String msg = "";
    /**
     * 是否问录音文件
     */
    boolean isSound = false;
    /**
     * 录音地址
     */
    String soundPath = "";

    /**
     * @param owner 消息所有者  someone：消息显示在左边   me：消息显示在右边
     * @param msg   消息内容
     */
    public MessageItem(MessageOwner owner, String msg) {
        this.owner = owner;
        this.msg = msg;
    }

    /**
     * @param isSound   是否是录音文件
     * @param soundPath
     */
    public MessageItem(boolean isSound, String soundPath) {
        this.isSound = isSound;
        this.soundPath = soundPath;
    }

    public String getSoundPath() {
        return soundPath;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
    }

    public boolean isSound() {
        return isSound;
    }

    public void setSound(boolean sound) {
        isSound = sound;
    }

    public MessageOwner getOwner() {
        return owner;
    }

    public void setOwner(MessageOwner owner) {
        this.owner = owner;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
