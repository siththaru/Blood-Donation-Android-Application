package com.dripblood.myapplication.viewmodels;

import java.io.Serializable;


public class CustomUserData implements Serializable {
   private String Latitude, Longitude, Contact;
   private String Name, BloodGroup;
   private String Time, Date;


   public CustomUserData() {

    }

    public CustomUserData(String latitude, String longitude, String contact, String name, String bloodGroup, String time, String date) {
        Latitude = latitude;
        Longitude = longitude;
        Contact = contact;
        Name = name;
        BloodGroup = bloodGroup;
        Time = time;
        Date = date;
    }

    public String getLatitude() { return Latitude; }

    public void setLatitude(String latitude) { Latitude = latitude; }

    public String getLongitude() { return Longitude; }

    public void setLongitude(String longitude) { Longitude = longitude; }

    public String getContact() {
        return Contact;
    }

    public void setContact(String contact) {
        this.Contact = contact;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
       this.Name = name;
    }

    public String getBloodGroup() {
        return BloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.BloodGroup = bloodGroup;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        this.Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        this.Date = date;
    }
}
