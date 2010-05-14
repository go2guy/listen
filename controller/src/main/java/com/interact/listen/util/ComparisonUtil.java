package com.interact.listen.util;

/**
 * Provides utility methods for comparing objects.
 */
public final class ComparisonUtil
{
    private ComparisonUtil()
    {
        throw new AssertionError("Cannot instantiate utility class ComparisonUtil");
    }

    /**
     * Compares two objects for equality, taking {@code null} values into consideration. Offered as a convenience for
     * developers to avoid having to always check for {@code null} before performing {@code .equals()} comparisons.
     * 
     * @param object1 object to compare
     * @param object2 object to compare
     * @return {@code true} if objects are equal (or both {@code null}), {@code false} otherwise
     */
    public static boolean isEqual(Object object1, Object object2)
    {
        if(object1 == null && object2 == null)
        {
            return true;
        }

        if(object1 != null && object1.equals(object2))
        {
            return true;
        }

        return false;
    }
}
