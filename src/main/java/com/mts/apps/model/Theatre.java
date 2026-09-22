package com.mts.apps.model;

/** Maps to the theatres table. */
public class Theatre {

    private int theatreId;
    private String name;
    private String city;
    private String address;
    private int totalSeats;

    public Theatre() {
    }

    public Theatre(int theatreId, String name, String city, String address, int totalSeats) {
        this.theatreId = theatreId;
        this.name = name;
        this.city = city;
        this.address = address;
        this.totalSeats = totalSeats;
    }

    public int getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(int theatreId) {
        this.theatreId = theatreId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    @Override
    public String toString() {
        return "Theatre{" + "theatreId=" + theatreId + ", " + "name=" + name + ", " + "city=" + city + ", " + "address=" + address + ", " + "totalSeats=" + totalSeats + "}";
    }
}
