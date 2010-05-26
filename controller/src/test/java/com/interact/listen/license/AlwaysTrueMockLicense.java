package com.interact.listen.license;

import com.interact.license.client.Feature;

public class AlwaysTrueMockLicense implements com.interact.license.client.License
{
    @Override
    public int getLicensedFeatureCount(Feature feature)
    {
        return 1;
    }

    @Override
    public boolean isFeatureLicensed(Feature feature)
    {
        return true;
    }
}
