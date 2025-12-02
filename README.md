# CPU Consumer Spring Boot sample

This small Spring Boot app demonstrates a CPU consumer that uses 5 threads to fully consume CPU for 500ms every second, then remains idle for the other 500ms.

How it works
- A scheduled task runs every 1000ms (1s).
- On each run it submits 5 tasks to a fixed thread pool. Each task busy-waits for 500ms.
- This produces an approximate pattern of 5 CPU cores busy for ~500ms and idle for ~500ms.

Run

From the `cpu-consumer-springboot` directory:

```bash
mvn spring-boot:run
```

Endpoints
- POST /api/cpu/start  -> start the consumer
- POST /api/cpu/stop   -> stop the consumer
- GET  /api/cpu/status -> check running status and config

Notes
- The busy-wait uses math operations to avoid JIT optimizations that could remove the loop.
- To observe CPU usage: run `top`, `htop` or `pidstat` while the consumer is running.
- Adjust `consumers`, `busyMs` and `periodMs` in `CpuConsumer.java` if you want different behavior.
 - The properties are configurable via `src/main/resources/application.properties` using the prefix `cpu.consumer`.
	 Example:

```properties
cpu.consumer.consumers=5
cpu.consumer.busyMs=500
cpu.consumer.periodMs=1000
```
