package com.mycompany.broadcast.model;

import java.io.Serializable;

public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String phoneNumber;
    private String status;       // "Pending" | "Sending" | "Sent" | "Failed"
    private boolean selected;    // Untuk checkbox selection di dataTable

    // Constructor default
    public Customer() {
        this.status = "Pending";
        this.selected = false;
    }

    // Constructor lengkap
    public Customer(Long id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = "Pending";
        this.selected = false;
    }

    // Getter & Setter untuk SEMUA field
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
