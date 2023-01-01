package com.unipi.msc.javablockchainapi.Model.V3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class CustomRecursiveAction extends RecursiveAction {
    private Runnable runnable;
    private int[] nonceRange = new int[2];
    private static final int THRESHOLD = 5;

    public CustomRecursiveAction(int[] nonceRange, Runnable runnable) {
        this.nonceRange = nonceRange;
        this.runnable = runnable;
    }

    @Override
    protected void compute() {
        if (nonceRange[1]-nonceRange[0] > THRESHOLD){
            ForkJoinTask.invokeAll(createSubtasks());
        }else {
            processing(nonceRange);
        }
    }

    private void processing(int[] nonceRange) {
        for (int i = nonceRange[0];i<nonceRange[1];i++){
            runnable.run();
        }
    }

    private List<CustomRecursiveAction> createSubtasks() {
        List<CustomRecursiveAction> subtasks = new ArrayList<>();
        subtasks.add(new CustomRecursiveAction(new int[]{nonceRange[0], nonceRange[1] / 2},runnable));
        subtasks.add(new CustomRecursiveAction(new int[]{nonceRange[1] / 2, nonceRange[1]},runnable));
        return subtasks;
    }
}
