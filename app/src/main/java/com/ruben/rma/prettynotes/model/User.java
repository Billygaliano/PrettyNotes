package com.ruben.rma.prettynotes.model;

import java.math.BigDecimal;

/**
 * Created by inftel12 on 29/3/16.
 */
public class User {
    private BigDecimal userID;
    private String facebookUserID;

    public BigDecimal getUserID() {
        return userID;
    }

    public void setUserID(BigDecimal userID) {
        this.userID = userID;
    }

    public String getFacebookUserID() {
        return facebookUserID;
    }

    public void setFacebookUserID(String facebookUserID) {
        this.facebookUserID = facebookUserID;
    }
}
