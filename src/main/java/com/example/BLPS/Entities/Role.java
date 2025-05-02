package com.example.BLPS.Entities;

import java.util.Set;

public enum Role {
    USER(Set.of(
            Privilege.VIEW_APPS,
            Privilege.INSTALL_APP,
            Privilege.RATE_APP
    )),

    DEVELOPER(Set.of(
            Privilege.VIEW_APPS,
            Privilege.UPLOAD_APP,
            Privilege.DELETE_OWN_APP,
            Privilege.VIEW_STATS,
            Privilege.RESPOND_TO_REVIEWS
    )),

    ADMIN(Set.of(
            Privilege.VIEW_APPS,
            Privilege.DELETE_ANY_APP,
            Privilege.BAN_USER,
            Privilege.APPROVE_APP,
            Privilege.VIEW_ALL_STATS
    ));

    private final Set<Privilege> privileges;

    Role(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }
}

