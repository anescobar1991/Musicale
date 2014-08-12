package com.anescobar.musicale.interfaces;

/**
 * Created by Andres Escobar on 8/11/14.
 * Interface to be used as a callBack from Session HeartbeatCheck async task
 */
public interface OnSessionHeartbeatCheckCompleted {
    public void onHeartbeatCheckTaskCompleted(boolean sessionValid);
}
