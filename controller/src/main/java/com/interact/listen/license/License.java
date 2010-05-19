package com.interact.listen.license;

import com.interact.license.client.Feature;
import com.interact.license.client.UnleashLicense;
import com.interact.license.client.validate.CryptoDsigSignatureValidator;
import com.interact.license.client.validate.LicenseFileSignatureValidator;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Provides the ability to determine whether or not specific {@link ListenFeature}s are licensed for this application.
 */
public final class License
{
    private static final Logger LOG = Logger.getLogger(License.class);

    /** Single instance */
    private static final License INSTANCE = new License("/interact/master/.iiXmlLicense");

    /** License */
    private com.interact.license.client.License license;

    /** Constructs a new License */
    private License(String file)
    {
        File licenseFile = new File(file);
        LicenseFileSignatureValidator signatureValidator = new CryptoDsigSignatureValidator();
        license = new UnleashLicense(licenseFile, signatureValidator);
    }

    /**
     * Returns {@code true} if the provided {@link Feature} is licensed, {@code false} otherwise.
     * 
     * @param feature {@code Feature} to check
     * @return {@code true} if {@code Feature} is licensed, {@code false} otherwise
     */
    public static boolean isLicensed(Feature feature)
    {
        boolean isActuallyLicensed = INSTANCE.license.isFeatureLicensed(feature);
        LOG.warn("Overriding feature license value of [" + isActuallyLicensed + "] with [true], temporarily");
        return true; // FIXME once the features are available in Unleash, return the actual license value
    }
}
