package pl.mateuszwinnicki.spring.executor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ContextAwareThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return super.submit(new ContextAwareCallable<T>(
            task,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes()
        ));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(final Callable<T> task) {
        return super.submitListenable(new ContextAwareCallable<T>(
            task,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes()
        ));
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return super.submit(new ContextAwareRunnable(
            task,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes()
        ));
    }

    @Override
    public ListenableFuture<?> submitListenable(final Runnable task) {
        return super.submitListenable(new ContextAwareRunnable(
            task,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes()
        ));
    }

    @Override
    public void execute(final Runnable task) {
        super.execute(new ContextAwareRunnable(
            task,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes())
        );
    }

    @Override
    public void execute(final Runnable task, final long startTimeout) {
        super.execute(new ContextAwareRunnable(
            task,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes()
        ), startTimeout);
    }

    @Override
    public Thread createThread(final Runnable runnable) {
        return super.createThread(new ContextAwareRunnable(
            runnable,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes()
        ));
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        return super.newThread(new ContextAwareRunnable(
            runnable,
            Thread.currentThread().getId(),
            RequestContextHolder.currentRequestAttributes()
        ));
    }

}
