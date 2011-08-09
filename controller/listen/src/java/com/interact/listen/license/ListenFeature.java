package com.interact.listen.license;

import com.interact.license.client.Feature;

/**
 * List of features available for licensing within the Listen application.
 */
public enum ListenFeature implements Feature
{
    ACD                 ("Listen ACD",          "ACD", false),
    ACTIVE_DIRECTORY    ("Listen AD",           "Active Directory", false),
    AFTERHOURS          ("Listen After Hours",  "After Hours", true),
    ATTENDANT           ("Listen Attendant",    "Attendant", true),
    BROADCAST           ("Listen Broadcast",    "PBX Broadcast", true),
    CONFERENCING        ("Listen Conferencing", "Conferencing", true),
    CUSTOM_APPLICATIONS ("Listen Custom App",   "Custom SPOT Applications", true),
    FAX                 ("Listen Fax",          "Fax", true),
    FINDME              ("Listen Find Me",      "Find Me / Follow Me", true),
    TRANSCRIPTION       ("Listen Transcription","Transcription", true),
    VOICEMAIL           ("Listen Voice Mail",   "Voicemail", true),
    IPPBX               ("IP PBX",              "IP PBX", true);

    /** How this feature is represented in the license file */
    private String licenseFileFeatureName;

    /** How this feature is displayed on screens. */
    private String displayName;

    /** Whether or not this feature can be enabled/disabled for organizations */
    private boolean isPerOrganization = true;

    /** Constructs a new {@code ListenFeature} */
    private ListenFeature(String licenseFileFeatureName, String displayName, boolean isPerOrganization)
    {
        this.licenseFileFeatureName = licenseFileFeatureName;
        this.displayName = displayName;
        this.isPerOrganization = isPerOrganization;
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

    public boolean getIsPerOrganization()
    {
        return isPerOrganization;
    }
}
