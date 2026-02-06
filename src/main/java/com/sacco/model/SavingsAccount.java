package com.sacco.model;

import java.math.BigDecimal;

public class SavingsAccount {
    private final long id;
    private final long memberId;
    private final String memberName;
    private final String accountNo;
    private final BigDecimal balance;

    public SavingsAccount(long id, long memberId, String memberName, String accountNo, BigDecimal balance) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.accountNo = accountNo;
        this.balance = balance;
    }

    public long getId() {
        return id;
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
