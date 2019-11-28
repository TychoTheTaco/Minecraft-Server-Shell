package com.tycho.mss.util;

import easytasks.Task;
import javafx.application.Platform;

public abstract class UiUpdater extends Task {

    private final long interval;

    public UiUpdater(final long interval) {
        this.interval = interval;
    }

    @Override
    protected void run() {
        while (isRunning()){
            Platform.runLater(this::onUiUpdate);

            try {
                Thread.sleep(this.interval);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    protected abstract void onUiUpdate();
}
