package pl.mateuszwinnicki.spring.executor;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class ContextAwareThreadPoolTaskExecutorTest {

    @Test
    public void submit_normalThreadPoolTaskExecutor_requestAttributesNotPresent() throws ExecutionException, InterruptedException, TimeoutException {
        final RequestAttributes requestAttributes = Mockito.mock(RequestAttributes.class);
        when(requestAttributes.getAttribute("wantToBeHere", 3)).thenReturn("HEY");

        RequestContextHolder.setRequestAttributes(requestAttributes);
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();

        final Future<?> future = executor.submit(() -> {
            final RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            assertNull(ra);
        });
        future.get(1, TimeUnit.SECONDS); //to be sure that task under executor will finish and call assertion
    }

    @Test
    public void submit_contextAwareRunnable_requestAttributesArePropagated() throws ExecutionException, InterruptedException, TimeoutException {
        final RequestAttributes requestAttributes = Mockito.mock(RequestAttributes.class);
        final String mockResponse = "OK";
        when(requestAttributes.getAttribute("test", 1)).thenReturn(mockResponse);

        RequestContextHolder.setRequestAttributes(requestAttributes);
        final ThreadPoolTaskExecutor executor = new ContextAwareThreadPoolTaskExecutor();
        executor.initialize();

        final Future<?> future = executor.submit(() -> {
            final RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            assertEquals(mockResponse, ra.getAttribute("test", 1));
        });
        future.get(1, TimeUnit.SECONDS); //to be sure that task under executor will finish and call assertion
    }

    @Test
    public void submit_contextAwareCallable_requestAttributesArePropagated() throws ExecutionException, InterruptedException, TimeoutException {
        final RequestAttributes requestAttributes = Mockito.mock(RequestAttributes.class);
        final String mockResponse = "GOOD";
        when(requestAttributes.getAttribute("secondTest", 2)).thenReturn(mockResponse);

        RequestContextHolder.setRequestAttributes(requestAttributes);
        final ThreadPoolTaskExecutor executor = new ContextAwareThreadPoolTaskExecutor();
        executor.initialize();

        final Future<?> future = executor.submit(() -> {
            final RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            assertEquals(mockResponse, ra.getAttribute("secondTest", 2));

        });
        future.get(1, TimeUnit.SECONDS); //to be sure that task under executor will finish and call assertion
    }

    /**
     * This test is checking if thread which will use CallerRunsPolicy (wait for main thread to use executor when new
     * threads are not available) will not reset context after execution. Normally we want to reset as we don't want to
     * store previous context in executor idle threads. So after extorting second thread to use main thread, context
     * should be not reset
     */
    @Test
    public void submit_callersRunPolicy_requestAttributesShouldNotBeResetAfterUsingMainThread() throws InterruptedException, ExecutionException, TimeoutException {
        final RequestAttributes requestAttributes = Mockito.mock(RequestAttributes.class);
        final String mockResponse = "Still here";
        when(requestAttributes.getAttribute("callersRun", 4)).thenReturn(mockResponse);

        RequestContextHolder.setRequestAttributes(requestAttributes);
        final ThreadPoolTaskExecutor executor = new ContextAwareThreadPoolTaskExecutor();
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.initialize();

        final Future<?> future1 = executor.submit(() -> {
            try {
                Thread.sleep(50); /*
                TODO: I don't know how to create such situation without timeouting first thread
                so second will use main thread. As I don't like unit test which are strictly depending on sleeping
                inside, it will be nice to find another solution.
                */
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }); //executor thread
        final Future<?> future2 = executor.submit(() -> {}); //main thread as executor thread is occupied
        future1.get(1, TimeUnit.SECONDS);
        future2.get(1, TimeUnit.SECONDS);

        final RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        assertEquals(mockResponse, ra.getAttribute("callersRun", 4));
    }

}