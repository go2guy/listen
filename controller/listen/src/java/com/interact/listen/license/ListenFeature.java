package com.interact.listen.license;

import com.interact.license.client.Feature;

/**
 * List of features available for licensing within the Listen application.
 */
public enum ListenFeature implements Feature
{
    ACD("Listen ACD", "ACD"),
    AFTERHOURS("Listen After Hours", "After Hours"),
    ATTENDANT("Listen Attendant", "Attendant"),
    BROADCAST("Listen Broadcast", "PBX Broadcast"),
    CONFERENCING("Listen Conferencing", "Conferencing"),
    CUSTOM_APPLICATIONS("Listen Custom App", "Custom SPOT Applications"),
    FINDME("Listen Find Me", "Find Me / Follow Me"),
    VOICEMAIL("Listen Voice Mail", "Voicemail"),
    IPPBX("IP PBX", "IP PBX");

    /** How this feature is represented in the license file */
    private String licenseFileFeatureName;

    /** How this feature is displayed on screens. */
    private String displayName;

    /** Constructs a new {@code ListenFeature} */
    private ListenFeature(String licenseFileFeatureName, String displayName)
    {
        this.licenseFileFeatureName = licenseFileFeatureName;
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public String asLicenseFileFeatureName()
    {
        return licenseFileFeatureName;
    }
}
