package net.evendanan.testgrouping;

import org.junit.runner.Description;

/**
 * Defines the interface for a strategy used to calculate the test's execution group.
 */
public interface HashingStrategy {
    /**
     * Should return an integer (non-negative) that used to describes the execution group.
     * @param description the test method {@link Description} instance.
     */
    int calculateHashFromDescription(Description description);
}
