package com.wisesoft.project.models;

import jakarta.persistence.*;

@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addressId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    private String address;
    private String phoneNumber;
    private String otherPhoneNumber;
    private String postCode;
    private String city;
    private String district;
    // Constructors

    public Address() {
    }

    public Address(
            User user,
            String address,
            String phoneNumber,
            String otherPhoneNumber,
            String postCode,
            String city,
            String district)
    {
        this.user = user;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.otherPhoneNumber = otherPhoneNumber;
        this.postCode = postCode;
        this.city = city;
        this.district = district;
    }

    // Getters and setters

    public Integer getAddressId() {
        return addressId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOtherPhoneNumber() {
        return otherPhoneNumber;
    }

    public void setOtherPhoneNumber(String otherPhoneNumber) {
        this.otherPhoneNumber = otherPhoneNumber;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
