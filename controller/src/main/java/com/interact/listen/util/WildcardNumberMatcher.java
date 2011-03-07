package com.interact.listen.util;

import java.util.Map;

/**
 * Interface representing a way to check if a given number matches any records, any number of which may
 * contain a character representing a wildcard.
 */
public interface WildcardNumberMatcher
{
    /**
     * Checks if the given String(representing a number) matches any configured, possibly 
     * wildcarded Strings(numbers) 
     * 
     * @param number number to check against configured numbers
     * @param mappings parameters to be sent in request body
     */
    public String findMatch(String number, Map<String, String> mappings);
}
