package com.sacco.model;

public class Member {
    private final long id;
    private final String externalId;
    private final String fullName;
    private final String nationalId;
    private final String phone;
    private final String email;
    private final String photoPath;

    public Member(long id, String externalId, String fullName, String nationalId, String phone, String email, String photoPath) {
        this.id = id;
        this.externalId = externalId;
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.phone = phone;
        this.email = email;
        this.photoPath = photoPath;
    }

    public long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
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

    public String getPhotoPath() {
        return photoPath;
    }
}
