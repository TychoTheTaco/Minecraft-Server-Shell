package com.tycho.mss;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MenuPage {

    public enum Status{
        OK,
        WARNING,
        ERROR
    }

    private Status status = Status.OK;

    private ServerShell serverShell;

    public ServerShell getServerShell() {
        return serverShell;
    }

    public void setServerShell(ServerShell serverShell) {
        this.serverShell = serverShell;
    }

    public void onPageHidden(){

    }

    public void onPageSelected(){

    }

    public interface OnStatusChangedListener{
        void onStatusChanged(Status previous, Status status);
    }

    private final CopyOnWriteArrayList<OnStatusChangedListener> statusChangedListeners = new CopyOnWriteArrayList<>();

    public void addStatusChangedListener(final OnStatusChangedListener listener){
        this.statusChangedListeners.add(listener);
    }

    protected void setStatus(final Status status){
        if (status != this.status){
            final Status previous = this.status;
            this.status = status;
            for (OnStatusChangedListener listener : this.statusChangedListeners){
                listener.onStatusChanged(previous, status);
            }
        }
    }

    public final Status getStatus() {
        return status;
    }
}
