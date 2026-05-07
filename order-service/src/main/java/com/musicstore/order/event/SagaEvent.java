package com.musicstore.order.event;

import java.time.Instant;

/**
 * Base interface for all saga event types.
 * Provides a common contract for timestamp tracking across distributed transactions.
 */
public interface SagaEvent {

    /**
     * Returns the timestamp when the event occurred.
     *
     * @return the event timestamp
     */
    Instant timestamp();
}
