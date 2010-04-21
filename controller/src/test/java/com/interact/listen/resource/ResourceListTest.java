package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ResourceListTest
{
    @Test
    public void test_getFieldsForQuery_sortsFieldsAlphabetically()
    {
        Set<String> fields = new HashSet<String>();
        fields.add("banana");
        fields.add("apple");
        fields.add("xylophone");
        fields.add("bananabanana");
        fields.add("BANANA");
        fields.add("!exclamation");

        ResourceList resourceList = new ResourceList();
        resourceList.setFields(fields);

        final String expected = "!exclamation,BANANA,apple,banana,bananabanana,xylophone";
        assertEquals(expected, resourceList.getFieldsForQuery());
    }
}
