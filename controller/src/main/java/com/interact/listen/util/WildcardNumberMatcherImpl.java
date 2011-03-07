package com.interact.listen.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class WildcardNumberMatcherImpl implements WildcardNumberMatcher
{
    private static final Logger LOG = Logger.getLogger(WildcardNumberMatcherImpl.class);
    
    public String findMatch(String number, Map<String, String> mappings)
    {
        // if we have a specific mapping for the number, simply return it
        if(mappings.containsKey(number))
        {
            return mappings.get(number);
        }

        // TreeMap, ordered by the length of its entries, descending - this sorting will allow us to loop to find a
        // specific match later
        Map<String, String> wildcards = new TreeMap<String, String>(new Comparator<String>()
        {
            @Override
            public int compare(String a, String b)
            {
                return Integer.valueOf(b.length()).compareTo(a.length());
            }
        });

        // strip all of the specific mappings (we didn't find one at this point, so we're looking for a wildcarded one)
        // also take out any mappings that are longer than the number itself, they won't match
        for(Map.Entry<String, String> entry : mappings.entrySet())
        {
            if(entry.getKey().endsWith("*") && entry.getKey().length() < number.length() + 1)
            {
                wildcards.put(entry.getKey(), entry.getValue());
            }
        }
        
        for(Map.Entry<String, String> entry : wildcards.entrySet())
        {
            // length of 1 has to be a single '*'; if we got this far, we matched the '*'
            if(entry.getKey().length() == 1)
            {
                return entry.getValue();
            }

            // if the first M (m = key.length) digits of number.length equal the key, we found a match
            int keyLength = entry.getKey().length();
            if(entry.getKey().substring(0, keyLength - 1).equals(number.substring(0, keyLength - 1)))
            {
                return entry.getValue();
            }
        }

        // no match found
        return null;
    }
}
