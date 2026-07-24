package com.pradip.tradingbot.dto;

public class LoginResult {

    private AccessTokenData session;
    private int instrumentCount;

    public AccessTokenData getSession() {
        return session;
    }

    public void setSession(AccessTokenData session) {
        this.session = session;
    }

    public int getInstrumentCount() {
        return instrumentCount;
    }

    public void setInstrumentCount(int instrumentCount) {
        this.instrumentCount = instrumentCount;
    }
}
