package org.hesperides.core.infrastructure.security.groups;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class LdapSearchMetrics {

    private double totalCallsCounter = 0;
    private double failedCallsCounter = 0;
    private double unexpectedExceptionCounter = 0;
    private double retriesExhaustedExceptionCounter = 0;

    public LdapSearchMetrics(MeterRegistry meterRegistry) {
        Gauge.builder("totalCallsCounter", this,
                LdapSearchMetrics::getTotalCallsCounterAndReset)
                .register(meterRegistry);
        Gauge.builder("failedCallsCounter", this,
                LdapSearchMetrics::getFailedCallsCounterAndReset)
                .register(meterRegistry);
        Gauge.builder("unexpectedExceptionCounter", this,
                LdapSearchMetrics::getUnexpectedExceptionCounterAndReset)
                .register(meterRegistry);
        Gauge.builder("retriesExhaustedExceptionCounter", this,
                LdapSearchMetrics::getRetriesExhaustedExceptionCounterAndReset)
                .register(meterRegistry);
    }

    void incrTotalCallsCounter() {
        this.totalCallsCounter++;
    }

    void incrFailedCallsCounter() {
        this.failedCallsCounter++;
    }

    void incrUnexpectedExceptionCounter() {
        this.unexpectedExceptionCounter++;
    }

    void incrRetriesExhaustedExceptionCounter() {
        this.retriesExhaustedExceptionCounter++;
    }

    private double getTotalCallsCounterAndReset() {
        double result = totalCallsCounter;
        totalCallsCounter = 0;
        return result;
    }

    private double getFailedCallsCounterAndReset() {
        double result = failedCallsCounter;
        failedCallsCounter = 0;
        return result;
    }

    private double getUnexpectedExceptionCounterAndReset() {
        double result = unexpectedExceptionCounter;
        unexpectedExceptionCounter = 0;
        return result;
    }

    private double getRetriesExhaustedExceptionCounterAndReset() {
        double result = retriesExhaustedExceptionCounter;
        retriesExhaustedExceptionCounter = 0;
        return result;
    }
}
