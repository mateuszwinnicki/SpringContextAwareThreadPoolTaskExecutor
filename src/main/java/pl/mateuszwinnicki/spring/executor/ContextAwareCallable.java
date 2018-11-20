package pl.mateuszwinnicki.spring.executor;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;
import java.util.concurrent.Callable;

class ContextAwareCallable<T> implements Callable<T> {

    private final Callable<T> task;
    private final Long originalThreadId;
    private final Optional<RequestAttributes> context;

    ContextAwareCallable(final Callable<T> task, final Long originalThreadId, final RequestAttributes context) {
        this.task = task;
        this.originalThreadId = originalThreadId;
        this.context = Optional.ofNullable(context);
    }

    @Override
    public T call() throws Exception {
        context.ifPresent(RequestContextHolder::setRequestAttributes);
        try {
            return task.call();
        } finally {
            if(Thread.currentThread().getId() != originalThreadId) {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    }

}
