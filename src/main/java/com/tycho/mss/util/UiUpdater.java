package com.tycho.mss.util;

import easytasks.Task;

public abstract class UiUpdater extends Task {

    private final long interval;

    public UiUpdater(final long interval) {
        this.interval = interval;
    }

    @Override
    protected void run() {
        while (isRunning()){
            onUiUpdate();

            try {
                Thread.sleep(this.interval);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    protected abstract void onUiUpdate();
}
