package com.sacco.model;

import java.math.BigDecimal;

public class SavingsAccount {
    private final long id;
    private final String externalId;
    private final long memberId;
    private final String memberName;
    private final String accountNo;
    private final BigDecimal balance;

    public SavingsAccount(long id, String externalId, long memberId, String memberName, String accountNo, BigDecimal balance) {
        this.id = id;
        this.externalId = externalId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.accountNo = accountNo;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public long getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
