package com.amo.app.worker;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An object that executes submitted tasks.
 * This provides a way of decoupling task submission from the
 * mechanics of how each task will be run, including details of thread
 * use, scheduling, it's normally used instead of explicitly creating threads.
 * For example, rather than invoking new Thread(new RunnableTask()).start()
 * for each of a set of tasks.
 */
public class ExecutorHelper {
    private static final int TYPE_BASE  = 50;
    public static final int TYPE_MAIN  = TYPE_BASE + 1;
    public static final int TYPE_LIGHT = TYPE_BASE + 2;
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor mMainExecutor;
    private static ThreadPoolExecutor mLightExecutor = null;

    public ExecutorHelper() {
        if (mMainExecutor == null) {
            mMainExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES*2, NUMBER_OF_CORES*2, 60L, TimeUnit.SECONDS
                    , new LinkedBlockingDeque<Runnable>(), new HelperThreadFactory());
        }
        if (mLightExecutor == null) {
            mLightExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES*2, NUMBER_OF_CORES*2, 60L, TimeUnit.SECONDS
                    , new LinkedBlockingDeque<Runnable>(), new HelperThreadFactory());
        }


    }

    public static ExecutorHelper getInstance() { return HelperInstanceHolder.INSTANCE; }

    public void execute(Runnable task, int type) {
        //Logger.d(">>>");
        switch (type) {
            case TYPE_MAIN:
                mMainExecutor.execute(task);
                break;
            case TYPE_LIGHT:
                mLightExecutor.execute(task);
                break;
            default:
                break;
        }
    }

    public Future submit(Callable<? extends Integer> callable, int type) {
        Future future = null;
        switch (type) {
            case TYPE_MAIN:
                future = mMainExecutor.submit(callable);
                break;
            case TYPE_LIGHT:
                future = mLightExecutor.submit(callable);
                break;
            default:
                break;
        }
        return future;
    }

    public String getResultOfSubmit(Future<? extends Integer> future, int time_limit_sec) {
        try {
            return String.valueOf(future.get(time_limit_sec, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return e.toString();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return e.toString();
        } catch (TimeoutException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public void shutdown(List<Runnable> tasks_awaiting, int type) {
        if (tasks_awaiting == null) {
            switch (type) {
                case TYPE_MAIN:
                    mMainExecutor.shutdown();
                    break;
                case TYPE_LIGHT:
                    mLightExecutor.shutdown();
                    break;
                default:
                    break;
            }
        } else {
            switch (type) {
                case TYPE_MAIN:
                    tasks_awaiting = mMainExecutor.shutdownNow();
                    break;
                case TYPE_LIGHT:
                    tasks_awaiting = mLightExecutor.shutdownNow();
                    break;
                default:
                    break;
            }
        }
    }

    private static class HelperInstanceHolder {
        private static final ExecutorHelper INSTANCE = new ExecutorHelper();
    }

    private class HelperThreadFactory implements ThreadFactory {
        private final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;

        HelperThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool_" + poolNumber.getAndIncrement() + "_thread_";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
