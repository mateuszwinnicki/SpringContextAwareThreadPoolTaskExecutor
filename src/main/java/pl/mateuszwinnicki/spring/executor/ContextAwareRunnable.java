package pl.mateuszwinnicki.spring.executor;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

class ContextAwareRunnable implements Runnable {

    private final Runnable task;
    private final Long originalThreadId;
    private final Optional<RequestAttributes> context;

    ContextAwareRunnable(final Runnable task, final Long originalThreadId, final RequestAttributes context) {
        this.task = task;
        this.originalThreadId = originalThreadId;
        this.context = Optional.ofNullable(context);
    }

    @Override
    public void run() {
        context.ifPresent(RequestContextHolder::setRequestAttributes);
        try {
            task.run();
        } finally {
            if(Thread.currentThread().getId() != originalThreadId) {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    }

}
