package com.interact.listen.util;

public class ComparisonUtil
{
    private ComparisonUtil()
    {
        throw new AssertionError("Cannot instantiate utility class ComparisonUtil");
    }

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
