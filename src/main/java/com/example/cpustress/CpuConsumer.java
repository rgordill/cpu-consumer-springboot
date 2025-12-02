package com.example.cpustress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class CpuConsumer {
    private static final Logger log = LoggerFactory.getLogger(CpuConsumer.class);

    private final CpuConsumerProperties props;

    private ScheduledExecutorService scheduler;
    private ExecutorService workerPool;

    private ScheduledFuture<?> scheduledFuture;
    private volatile boolean running = false;

    public CpuConsumer(CpuConsumerProperties props) {
        this.props = props;
        // initialize executors lazily when start() is called so properties can be adjusted before start
    }

    /**
     * Start the consumer using the values from properties.
     * Kept for compatibility; delegates to the parameterized start.
     */
    public synchronized void start() {
        start(null, null, null);
    }

    /**
     * Start the consumer. Any null parameter will fall back to the value from {@code props}.
     *
     * @param consumersOverride number of worker threads to create, or null to use props.getConsumers()
     * @param busyMsOverride how long each busy task should run (ms), or null to use props.getBusyMs()
     * @param periodMsOverride scheduling period (ms) between busy task submissions, or null to use props.getPeriodMs()
     */
    public synchronized void start(Integer consumersOverride, Long busyMsOverride, Long periodMsOverride) {
        if (running) {
            log.info("CpuConsumer already running");
            return;
        }

        final int consumers = (consumersOverride != null) ? consumersOverride : props.getConsumers();
        final long busyMs = (busyMsOverride != null) ? busyMsOverride : props.getBusyMs();
        final long periodMs = (periodMsOverride != null) ? periodMsOverride : props.getPeriodMs();

        log.info("Starting CpuConsumer: consumers={}, busyMs={}, periodMs={}", consumers, busyMs, periodMs);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cpu-consumer-scheduler");
            t.setDaemon(true);
            return t;
        });

        workerPool = Executors.newFixedThreadPool(consumers, new ThreadFactory() {
            private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
            private int idx = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = defaultFactory.newThread(r);
                t.setName("cpu-worker-" + (idx++));
                t.setDaemon(true);
                return t;
            }
        });

        final long finalBusyMs = busyMs;
        final int finalConsumers = consumers;
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> submitBusyTasks(finalBusyMs, finalConsumers), 0, periodMs, TimeUnit.MILLISECONDS);
        running = true;
    }

    private void submitBusyTasks(long busyMs, int consumers) {
        List<Future<?>> futures = new ArrayList<>(consumers);
        for (int i = 0; i < consumers; i++) {
            futures.add(workerPool.submit(() -> busyWait(busyMs)));
        }
        // wait for them to finish (they should finish within busyMs)
        for (Future<?> f : futures) {
            try {
                f.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                log.warn("Worker timed out waiting for busy task to finish");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ee) {
                log.error("Worker execution exception", ee);
            }
        }
    }

    private void busyWait(long millis) {
        final long start = System.nanoTime();
        final long end = start + TimeUnit.MILLISECONDS.toNanos(millis);
        // busy loop using math ops to avoid being optimized out
        while (System.nanoTime() < end) {
            double x = Math.random();
            Math.sqrt(x);
        }
    }

    public synchronized void stop() {
        if (!running) {
            log.info("CpuConsumer already stopped");
            return;
        }
        log.info("Stopping CpuConsumer");
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (workerPool != null) {
            workerPool.shutdownNow();
            workerPool = null;
        }
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    @PreDestroy
    public void shutdown() {
        stop();
    }
}
