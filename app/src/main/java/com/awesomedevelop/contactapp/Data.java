package com.awesomedevelop.contactapp;

/**
 * Created by Taras on 01.02.2015.
 */
public class Data {
    String name;
    String phone;
    String delete;

    public Data(String name, String phone,String delete){
        this.name = name;
        this.phone = phone;
        this.delete = delete;
    }
    public String getName (){return name;}
    public String getPhone(){return phone;}
    public String getDelete (){return delete;}


}