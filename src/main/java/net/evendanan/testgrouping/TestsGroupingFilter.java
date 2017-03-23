package net.evendanan.testgrouping;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;

import java.util.Locale;
import java.util.Properties;

/**
 * Will filter out tests that are not in the split group.
 */
public class TestsGroupingFilter extends Filter {
    public static final String TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY = "TestsGroupingFilter_TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY";
    public static final String TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY = "TestsGroupingFilter_TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY";

    public static void addTestsGroupingFilterWithSystemPropertiesData(Filterable testRunner, boolean failIfDataMissing) {
        Properties systemProperties = System.getProperties();
        if (systemProperties.containsKey(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY) && systemProperties.containsKey(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY)) {
            int groupCount = Integer.parseInt(systemProperties.getProperty(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY));
            int groupToExecute = Integer.parseInt(systemProperties.getProperty(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY));
            addTestsGroupingFilterToRunner(testRunner, groupCount, groupToExecute);
        } else if (failIfDataMissing) {
            throw new IllegalStateException(String.format(Locale.US, "Could not find '%s' and '%s' in System.properties!", TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY, TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY));
        }
    }

    public static void addTestsGroupingFilterToRunner(Filterable testRunner, int groupCount, int groupToExecute) {
        try {
            testRunner.filter(new TestsGroupingFilter(groupCount, groupToExecute));
        } catch (NoTestsRemainException e) {
            //swallow.
            //I know what I'm doing
        }
    }

    private final int mGroupToExecute;
    private final int mGroupCount;

    public TestsGroupingFilter(int groupCount, int groupToExecute) {
        if (groupCount <= 0)
            throw new IllegalArgumentException("groupCount should be greater than zero.");
        if (groupToExecute < 0)
            throw new IllegalArgumentException("groupToExecute should be a non-negative number.");
        if (groupToExecute >= groupCount)
            throw new IllegalArgumentException("groupToExecute should less than groupCount.");

        mGroupToExecute = groupToExecute;
        mGroupCount = groupCount;
    }

    @Override
    public boolean shouldRun(Description description) {
        return getGroupNumberFor(description, mGroupCount) == mGroupToExecute;
    }

    private int getGroupNumberFor(Description description, int groupCount) {
        return Math.abs(getHashcodeForDescription(description)) % groupCount;
    }

    /**
     * Calculate a hashcode for the specified {@link Description}.
     */
    protected int getHashcodeForDescription(Description description) {
        return stableStringHashcode(description.getClassName());
    }

    /**
     * This is a stable (known) implementation of calculating a hash-code for the specified
     * {@link String}. This is here to ensure that you get the same hashcode for a String no
     * matter which JDK version or OS you are using.
     * <p>
     * Note: This hash function is in no way cryptographically impressive. But we can assume that over a large
     * number of tests, this should have normal distribution.
     */
    protected static int stableStringHashcode(String string) {
        int hash = 0;
        for (char c : string.toCharArray()) hash += c;

        return hash;
    }

    @Override
    public String describe() {
        return String.format(Locale.US, "Execute tests from group %d (out of %d)", mGroupToExecute, mGroupCount);
    }
}
