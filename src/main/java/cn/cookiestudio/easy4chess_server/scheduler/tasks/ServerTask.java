package cn.cookiestudio.easy4chess_server.scheduler.tasks;

public abstract class ServerTask {

    protected Runnable task;
    protected boolean cancel = false;
    protected Runnable cancelTask;

    public ServerTask(Runnable task){
        this.task = task;
    }

    public abstract void tryInvokeTask();//try if need to invoke the task(ep: if the task has a delay, the task will not be called unless the delay expires)

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel() {
        this.cancel = true;
        if (this.cancelTask != null)
            this.cancelTask.run();
    }

    public Runnable getTask() {
        return task;
    }

    public void setCancelTask(Runnable runnable){
        this.cancelTask = runnable;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }

    public Runnable getCancelTask() {
        return cancelTask;
    }
}
