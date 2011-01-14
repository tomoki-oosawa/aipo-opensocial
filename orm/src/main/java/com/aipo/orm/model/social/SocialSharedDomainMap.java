package com.aipo.orm.model.social;

import com.aipo.orm.model.social.auto._SocialSharedDomainMap;

public class SocialSharedDomainMap extends _SocialSharedDomainMap {

    private static SocialSharedDomainMap instance;

    private SocialSharedDomainMap() {}

    public static SocialSharedDomainMap getInstance() {
        if(instance == null) {
            instance = new SocialSharedDomainMap();
        }

        return instance;
    }
}
