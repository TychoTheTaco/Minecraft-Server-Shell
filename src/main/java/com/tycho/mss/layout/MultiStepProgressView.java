package com.tycho.mss.layout;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiStepProgressView extends VBox {

    private final List<Task> tasks = new ArrayList<>();

    private final Map<Task, TaskView> views = new HashMap<>();

    public MultiStepProgressView(){
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/progress_view.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addTask(final Task task){
        this.tasks.add(task);
        final TaskView taskView = new TaskView(task.getDescription());
        taskView.setProgress(0);
        this.views.put(task, taskView);
        getChildren().add(taskView);
    }

    public void start(final Runnable onFinished){
        new Thread(() -> {
            Object next = null;
            for (Task task : tasks){
                task.setProgress(0.01f);
                views.get(task).setProgress(task.getProgress());
                task.setObject(next);
                task.run();
                next = task.getObject();
                task.setFinished(true);
                views.get(task).setProgress(task.getProgress());
            }
            onFinished.run();
        }).start();
    }

    private static class TaskView extends HBox{

        @FXML
        private ProgressIndicator progress_indicator;

        @FXML
        private Label label;

        @FXML
        private ImageView icon;

        public TaskView(final String description) {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/task_view.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            try {
                loader.load();
            }catch (IOException e) {
                throw new RuntimeException(e);
            }

            label.setText(description);
        }

        public void setProgress(final float progress){
            System.out.println("UPDATE: " + this + " " + progress);
            if (progress >= 1){
                progress_indicator.setVisible(false);
                progress_indicator.setManaged(false);
                icon.setVisible(true);
                icon.setManaged(true);
                icon.setEffect(new Blend(
                        BlendMode.SRC_ATOP,
                        new ColorAdjust(0, 0, 0, 0),
                        new ColorInput(
                                0,
                                0,
                                icon.getImage().getWidth(),
                                icon.getImage().getHeight(),
                                Color.GREEN
                        )
                ));
            }else if (progress == 0){
                icon.setEffect(new Blend(
                        BlendMode.SRC_ATOP,
                        new ColorAdjust(0, 0, 0, 0),
                        new ColorInput(
                                0,
                                0,
                                icon.getImage().getWidth(),
                                icon.getImage().getHeight(),
                                Color.WHITE
                        )
                ));
                progress_indicator.setVisible(false);
                progress_indicator.setManaged(false);
                icon.setVisible(true);
                icon.setManaged(true);
            }else{
                progress_indicator.setVisible(true);
                progress_indicator.setManaged(true);
                icon.setVisible(false);
                icon.setManaged(false);
            }
        }
    }

    public static abstract class Task implements Runnable{

        private boolean isFinished = false;

        private String description;

        private Object object;

        private float progress = 0;

        public void setProgress(float progress) {
            this.progress = progress;
        }

        public float getProgress() {
            return progress;
        }

        public Task(String description) {
            this.description = description;
        }

        public void setFinished(boolean finished) {
            isFinished = finished;
            if (isFinished){
                progress = 1;
            }else{
                progress = 0;
            }
        }

        public boolean isFinished() {
            return isFinished;
        }

        public String getDescription() {
            return description;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return object;
        }
    }
}
