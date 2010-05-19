package com.interact.listen.license;

import com.interact.license.client.Feature;
import com.interact.listen.resource.*;

import java.util.HashSet;
import java.util.Set;

/**
 * List of features available for licensing within the Listen application.
 */
public enum ListenFeature implements Feature
{
    CONFERENCING("Listen Controller Conferencing", Conference.class, ConferenceHistory.class, Participant.class, Pin.class),
    FINDME("Listen Controller FindMe"),
    VOICEMAIL("Listen Controller Voicemail", Voicemail.class);

    /** How this feature is represented in the license file */
    private String licenseFileFeatureName;

    /** Collection of {@link Resource} classes associated with this {@code ListenFeature} */
    private Set<Class<? extends Resource>> resourceClasses = new HashSet<Class<? extends Resource>>();

    /** Constructs a new {@code ListenFeature} */
    private ListenFeature(String licenseFileFeatureName, Class<? extends Resource>... resourceClasses)
    {
        this.licenseFileFeatureName = licenseFileFeatureName;
        for(Class<? extends Resource> apiClass : resourceClasses)
        {
            this.resourceClasses.add(apiClass);
        }
    }

    public Set<Class<? extends Resource>> getResourceClasses()
    {
        return resourceClasses;
    }

    @Override
    public String asLicenseFileFeatureName()
    {
        return licenseFileFeatureName;
    }

    /**
     * Retrieves a {@code Set} of {@code ListenFeature}s that are associated with the provided {@link Resource} class.
     * 
     * @param resourceClass {@code Resource} class to find {@code ListenFeature}s for
     * @return {@code Set} of {@code ListenFeature}s (empty if none found)
     */
    public static Set<ListenFeature> getFeaturesWithResourceClass(Class<? extends Resource> resourceClass)
    {
        Set<ListenFeature> matchingFeatures = new HashSet<ListenFeature>();
        for(ListenFeature feature : values())
        {
            if(feature.resourceClasses.contains(resourceClass))
            {
                matchingFeatures.add(feature);
            }
        }
        return matchingFeatures;
    }
}
