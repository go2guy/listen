package com.interact.listen.util;

public final class ComparisonUtil
{
    private ComparisonUtil()
    {
        throw new AssertionError("Cannot instantiate utility class ComparisonUtil");
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE", justification = "object1 won't be null where FindBugs says it will, see the JUnit tests")
    public static boolean isEqual(Object object1, Object object2)
    {
        if(object1 == null && object2 == null)
        {
            return true;
        }

        if(object1 == null && object2 != null)
        {
            return false;
        }

        if(object1 != null && object2 == null)
        {
            return false;
        }

        return object1.equals(object2);
    }
}
