package com.sacco.model;

public class Member {
    private final long id;
    private final String fullName;
    private final String nationalId;
    private final String phone;
    private final String email;

    public Member(long id, String fullName, String nationalId, String phone, String email) {
        this.id = id;
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.phone = phone;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
