package com.wisewealth.entity;

public enum CategoryEnum {
    ACCOUNT_OPENING("Account Opening"),
    SIP("SIP"),
    LUMPSUM("Lumpsum"),
    GOAL_PLANNING("Goal Planning"),
    RETIREMENT("Retirement"),
    TAX("Tax"),
    PORTFOLIO_REVIEW("Portfolio Review"),
    OTHER("Other");

    private final String value;

    CategoryEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
