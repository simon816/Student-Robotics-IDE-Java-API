package org.srobo.ide.api;

public enum ErrorConstants {

    E_MALFORMED_REQUEST(1),
    E_INTERNAL_ERROR(2),
    E_BAD_AUTH_TOKEN(3),
    E_PERM_DENIED(4),
    E_AUTH_FAILED(5),
    E_AUTH_DENIED(6),
    E_NOT_IMPL(7),
    E_PROJ_NONEXISTANT(8),
    E_LDAP_NOT_AUTHED(9),
    E_NO_EXPIRY_TIME(10),
    E_TOKEN_STRAT_CONFIG(11);

    public final int code;

    ErrorConstants(int code) {
        this.code = code;
    }
}
