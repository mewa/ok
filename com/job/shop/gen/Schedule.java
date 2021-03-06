package com.job.shop.gen;

import java.io.PrintStream;
import java.util.*;

/**
 * Created by Mewa on 2015-12-30.
 */
public class Schedule {
    ArrayList<Job> up = new ArrayList<>();
    ArrayList<Job> down = new ArrayList<>();
    ArrayList<Job> toAdd = new ArrayList<>();

    public Schedule() {
    }

    public Schedule(Schedule schedule) {
        for (Job job : schedule.up) {
            this.up.add(new Job(job));
        }
        for (Job job : schedule.down) {
            this.down.add(new Job(job));
        }
        for (Job job : schedule.down) {
            this.toAdd.add(new Job(job));
        }
    }

    public Schedule crossing(Schedule schedule) {
        Schedule children = new Schedule();
        ArrayList<Task> first = getTasks();
        ArrayList<Task> second = schedule.getTasks();
        int division = new Random().nextInt(first.size() - 2);
        for (int i = first.size() - 1; i >= division; i--) {
            first.remove(i);
        }
        for (Task task : second) {
            boolean alreadyIn = false;
            for (int i = 0; i < first.size(); i++) {
                if (first.get(i).equals(task)) {
                    alreadyIn = true;
                    break;
                }
            }
            if (!alreadyIn) {
                first.add(task);
            }
        }
        for (Task task : first) {
            children.addTask(task);
        }
        children.addAllDown();
        return children;
    }

    public Schedule crossingReverse(Schedule schedule) {
        Schedule children = new Schedule();
        ArrayList<Task> first = getTasks();
        ArrayList<Task> second = schedule.getTasks();
        int division = new Random().nextInt(first.size() - 2);
        for (int i = division; i >= 0; i--) {
            first.remove(i);
        }
        for (Task task : second) {
            boolean alreadyIn = false;
            for (int i = 0; i < first.size(); i++) {
                if (first.get(i).equals(task)) {
                    alreadyIn = true;
                    break;
                }
            }
            if (!alreadyIn) {
                first.add(0, task);
            }
        }
        for (Task task : first) {
            children.addTask(task);
        }
        children.addAllDown();
        return children;
    }

    public void mutate() {
        int x = new Random().nextInt(up.size());
        int y = new Random().nextInt(up.size());
        int min;
        int max;
        if (y == x) {
            if (y > 0) {
                y--;
            } else {
                y++;
            }
        }
        min = x > y ? y : x;
        max = x > y ? x : y;
        x = max - min;
        Job job = up.get(0);
        ArrayList<Task> tasks = removeTo(min);
        Task temp = tasks.get(x);
        tasks.remove(x);
        tasks.add(x, tasks.get(0));
        tasks.remove(0);
        tasks.add(0, temp);
        for (Task task : tasks) {
            addTask(task);
        }
        addAllDown();
    }

    public void print(PrintStream printStream) {
        //printStream.print("up\t");
        for (Job job : up) {
            //System.out.print(job.getWhenStarts() + " " + job.getNumber() + " " + job.getTime() + " " + job.getWhenJobWillEnd() + "\t|\t");
            printStream.print(job.getNumber() + ":" + job.getTime() + ":" + job.getWhenStarts() + "\t");
        }
        printStream.println();
        //printStream.print("dn\t");
        for (Job job : down) {
            //System.out.print(job.getWhenStarts() + " " + job.getNumber() + " " + job.getTime() + " " + job.getWhenJobWillEnd() + "\t|\t");
            printStream.print(job.getNumber() + ":" + job.getTime() + ":" + job.getWhenStarts() + "\t");
        }
        printStream.println();
    }

    private ArrayList<Task> removeTo(int x) {

        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = up.size() - 1; i >= x; i--) {
            Job upJobToCopy, downJobToCopy = null;
            upJobToCopy = up.get(i);
            up.remove(i);
            for (int j = down.size() - 1; j >= 0; j--) {
                if (upJobToCopy.equals(down.get(j))) {
                    downJobToCopy = down.get(j);
                    down.remove(j);
                    break;
                }
            }
            tasks.add(new Task(upJobToCopy, downJobToCopy));
        }
        Collections.reverse(tasks);
        return tasks;
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = up.size() - 1; i >= 0; i--) {
            Job upJobToCopy, downJobToCopy = null;
            upJobToCopy = up.get(i);
            for (int j = down.size() - 1; j >= 0; j--) {
                if (upJobToCopy.equals(down.get(j))) {
                    downJobToCopy = down.get(j);
                    break;
                }
            }
            tasks.add(new Task(upJobToCopy, downJobToCopy));
        }
        Collections.reverse(tasks);
        return tasks;
    }

    public Schedule generuj() {
        ArrayList<Task> copyTasks = new ArrayList<>();
        for (Task task : Main.originalTasks) {
            copyTasks.add(new Task(task));
        }
        while (copyTasks.size() > 0) {
            int random = new Random().nextInt(copyTasks.size());
            addTask(copyTasks.get(random));
            copyTasks.remove(random);
        }
        addAllDown();
        return this;
    }

    public void addTask(Task task) {
        if (up.size() == 0) {
            addTaskUp(0, task, 1);
        } else {
            boolean added = false;
            for (int i = 0; i < up.size() - 1; i++) {
                if (up.get(i + 1).getWhenStarts() - up.get(i).getWhenJobWillEnd() - 1 >= task.getUp().getTime() &&
                        Main.nextBreakUp(up.get(i).getWhenJobWillEnd()) - up.get(i).getWhenJobWillEnd() - 1 >= task.getUp().getTime()) {
                    addTaskUp(i + 1, task, up.get(i).getWhenJobWillEnd() + 1);
                    added = true;
                    break;
                }
            }
            if (!added) {
                if (Main.nextBreakUp(up.get(up.size() - 1).getWhenJobWillEnd()) - up.get(up.size() - 1).getWhenJobWillEnd() - 1 >= task.getUp().getTime()) {
                    addTaskUp(up.size(), task, up.get(up.size() - 1).getWhenJobWillEnd() + 1);
                } else {
                    addTaskUp(up.size(), task, Main.timeOfNextBreakUpEnd(up.get(up.size() - 1).getWhenJobWillEnd() + 1));
                }
            }
        }
    }


    private void addTaskUp(int where, Task task, int beginning) {
        task.getUp().setWhenStarts(beginning);
        task.setDownPossibleStart();
        toAdd.add(task.getDown());
        up.add(where, task.getUp());
    }

    public void addAllDown() {
        while (toAdd.size() > 0) {
            boolean added = false;
            Job job = toAdd.get(0);
            for (int i = 0; i < down.size() - 1; i++) {
                if (down.get(i + 1).getWhenStarts() - down.get(i).getWhenJobWillEnd() - 1 >= job.getTime() &&
                        Main.nextBreakDown(down.get(i).getWhenJobWillEnd()) - down.get(i).getWhenJobWillEnd() - 1 >= job.getTime() &&
                        down.get(i + 1).getWhenStarts() - job.getWhenCouldStarts() - 1 >= job.getTime() &&
                        Main.nextBreakDown(job.getWhenCouldStarts()) - job.getWhenCouldStarts() - 1 >= job.getTime()) {
                    job.setWhenStarts(job.getWhenCouldStarts() > down.get(i).getWhenJobWillEnd() + 1 ?
                            job.getWhenCouldStarts() : down.get(i).getWhenJobWillEnd() + 1);
                    down.add(i + 1, job);
                    added = true;
                    break;
                }
            }
            if (down.size() == 0) {
                if (Main.nextBreakDown(job.getWhenCouldStarts()) - job.getWhenCouldStarts() - 1 > job.getTime()) {
                    job.setWhenStarts(job.getWhenCouldStarts());
                    added = true;
                    down.add(job);
                }
            }
            if (!added) {
                if (Main.nextBreakDown(down.get(down.size() - 1).getWhenJobWillEnd()) - down.get(down.size() - 1).getWhenJobWillEnd() - 1 >= job.getTime() &&
                        Main.nextBreakDown(down.get(down.size() - 1).getWhenJobWillEnd()) - job.getWhenCouldStarts() - 1 >= job.getTime()) {
                    job.setWhenStarts(job.getWhenCouldStarts() > down.get(down.size() - 1).getWhenJobWillEnd() + 1 ?
                            job.getWhenCouldStarts() : down.get(down.size() - 1).getWhenJobWillEnd() + 1);
                } else {
                    job.setWhenStarts(Main.nextBreakDown(down.get(down.size() - 1).getWhenJobWillEnd()) > job.getWhenCouldStarts() ?
                            Main.nextBreakDown(down.get(down.size() - 1).getWhenJobWillEnd()) : job.getWhenCouldStarts());
                }
                down.add(job);
            }

            toAdd.remove(0);
        }
    }

    public int getTime() {
        int upTime = up.get(up.size() - 1).getWhenJobWillEnd();
        int downTime = down.get(down.size() - 1).getWhenJobWillEnd();
        return upTime > downTime ? upTime : downTime;
    }

    @Override
    public String toString() {
        return Integer.toString(getTime());
    }
}
