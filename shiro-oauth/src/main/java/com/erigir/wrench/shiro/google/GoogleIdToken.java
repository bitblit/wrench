package com.erigir.wrench.shiro.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Created by chrweiss on 9/22/14.
 */
public class GoogleIdToken {
    @JsonProperty("issuer")
    private String issuerIdentifier;
    @JsonProperty("audience")
    private String audienceId;
    @JsonProperty("issued_at")
    private Long tokenIssued;
    @JsonProperty("email")
    private String email;
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    @JsonProperty("expires_in")
    private Long tokenExpires;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("issued_to")
    private String clientId;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("verified_email")
    private String verifiedEmail;
    @JsonProperty("access_type")
    private String accessType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIssuerIdentifier() {
        return issuerIdentifier;
    }

    public void setIssuerIdentifier(String issuerIdentifier) {
        this.issuerIdentifier = issuerIdentifier;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAudienceId() {
        return audienceId;
    }

    public void setAudienceId(String audienceId) {
        this.audienceId = audienceId;
    }

    public Long getTokenIssued() {
        return tokenIssued;
    }

    public void setTokenIssued(Long tokenIssued) {
        this.tokenIssued = tokenIssued;
    }

    public Long getTokenExpires() {
        return tokenExpires;
    }

    public void setTokenExpires(Long tokenExpires) {
        this.tokenExpires = tokenExpires;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getVerifiedEmail() {
        return verifiedEmail;
    }

    public void setVerifiedEmail(String verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

