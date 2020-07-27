package com.tycho.mss.layout;

import easytasks.ITask;
import easytasks.TaskAdapter;
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

    private final MultipartTask multipartTask = new MultipartTask();

    private final Map<MultipartTask.Task, TaskView> views = new HashMap<>();

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

    public void addTask(final MultipartTask.Task task){
        task.addTaskListener(new TaskAdapter(){
            @Override
            public void onTaskStarted(ITask itask) {
                views.get(task).setProgress(0.01f);
            }

            @Override
            public void onTaskStopped(ITask itask) {
                views.get(task).setProgress(task.getProgress());
            }
        });
        multipartTask.add(task);
        final TaskView taskView = new TaskView(task.getDescription());
        taskView.setProgress(0);
        this.views.put(task, taskView);
        getChildren().add(taskView);
    }

    public void start(final Runnable onFinished){
        new Thread(() -> {
            multipartTask.start();
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

    public static class MultipartTask extends easytasks.Task{

        private final List<Task> tasks = new ArrayList<>();

        @Override
        protected void run() throws Exception {
            Object object = null;
            for (Task task : tasks){
                task.setInput(object);
                task.start();
                object = task.getOutput();
            }
        }

        public void add(final Task task){
            task.addTaskListener(new TaskAdapter(){
                @Override
                public void onTaskFailed(ITask task, Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
            this.tasks.add(task);
        }

        public static abstract class Task extends easytasks.Task{

            private Object input = null;

            private Object output = null;

            private final String description;

            public Task(final String description){
                this.description = description;
            }

            public void setInput(Object input) {
                this.input = input;
            }

            protected Object getInput() {
                return input;
            }

            protected void setOutput(final Object output){
                this.output = output;
            }

            public Object getOutput() {
                return output;
            }

            public String getDescription() {
                return description;
            }
        }
    }
}
