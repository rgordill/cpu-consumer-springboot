package com.example.cpustress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cpu")
public class CpuController {

    private final CpuConsumer consumer;
    private final CpuConsumerProperties props;

    @Autowired
    public CpuController(CpuConsumer consumer, CpuConsumerProperties props) {
        this.consumer = consumer;
        this.props = props;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(
            @RequestParam(name = "consumers", required = false) Integer consumers,
            @RequestParam(name = "busyMs", required = false) Long busyMs,
            @RequestParam(name = "periodMs", required = false) Long periodMs
    ) {
        consumer.start(consumers, busyMs, periodMs);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        consumer.stop();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> m = new HashMap<>();
        m.put("running", consumer.isRunning());
        // report configured defaults from properties
        m.put("consumers", props.getConsumers());
        m.put("busyMs", props.getBusyMs());
        m.put("periodMs", props.getPeriodMs());
        return ResponseEntity.ok(m);
    }
}
