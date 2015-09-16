package com.medicineprof.registration.model;

import java.io.Serializable;

/**
 * Created by neurons on 9/16/15.
 */
public class Contact implements Serializable {
    private String name;
    private String phone;
    private String jabberUsername;
    private boolean contactExists;
    private boolean contactAdded;

    public Contact(){}

    public Contact(Contact origin){
        name = origin.name;
        phone = origin.phone;
        jabberUsername = origin.jabberUsername;
        contactAdded = origin.contactAdded;
        contactExists = origin.contactExists;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getJabberUsername() {
        return jabberUsername;
    }

    public void setJabberUsername(String jabberUsername) {
        this.jabberUsername = jabberUsername;
    }

    public boolean isContactExists() {
        return contactExists;
    }

    public void setContactExists(boolean contactExists) {
        this.contactExists = contactExists;
    }

    public boolean isContactAdded() {
        return contactAdded;
    }

    public void setContactAdded(boolean contactAdded) {
        this.contactAdded = contactAdded;
    }
}
