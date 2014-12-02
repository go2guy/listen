package com.interact.listen.utils

import grails.util.Holders;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: knovak
 * Date: 12/2/14
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListenPromptUtil
{
    public static String ACD_LOCATION = "acd";

    public static String buildFilepath(String storageLocation, long organizationId)
    {
        String baseDir = Holders.config.com.interact.listen.artifactsDirectory;

        return baseDir + File.separatorChar + organizationId + File.separatorChar + storageLocation;
    }

    public static String buildFileName(String file, String storageLocation, long organizationId)
    {
        String baseDir = buildFilepath(storageLocation, organizationId);

        return baseDir + File.separatorChar + file;
    }
}
