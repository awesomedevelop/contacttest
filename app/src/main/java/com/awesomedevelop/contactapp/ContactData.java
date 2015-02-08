package com.awesomedevelop.contactapp;

/**
 * Created by Taras on 29.01.2015.
 */
public class ContactData {
    String name;
    String phone;
    String photo;



    public ContactData(String name, String phone, String photo) {
        this.name = name;
        this.phone = phone;
        this.photo = photo;

    }


    public String getName() {return name;}
    public String getPhone(){return phone;}
    public String getPhoto() {return photo;}


}
