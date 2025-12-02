package com.example.cpustress;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cpu.consumer")
public class CpuConsumerProperties {
    /** Number of consumer threads to submit each cycle (approximate CPU targets) */
    private int consumers = 5;

    /** Busy duration in milliseconds per cycle */
    private long busyMs = 500;

    /** Full cycle period in milliseconds (busy + idle). Default 1000 => 500ms idle) */
    private long periodMs = 1000;

    public int getConsumers() {
        return consumers;
    }

    public void setConsumers(int consumers) {
        this.consumers = consumers;
    }

    public long getBusyMs() {
        return busyMs;
    }

    public void setBusyMs(long busyMs) {
        this.busyMs = busyMs;
    }

    public long getPeriodMs() {
        return periodMs;
    }

    public void setPeriodMs(long periodMs) {
        this.periodMs = periodMs;
    }
}
