package com.example.estimoteclassassignment.Model;

public class Item {
    public int discount;
    public String name;
    public String photo;
    public Double price;
    public String region;

    public Item(int discount, String name, String photo, Double price, String region) {
        this.discount = discount;
        this.name = name;
        this.photo = photo;
        this.price = price;
        this.region = region;
    }
}
