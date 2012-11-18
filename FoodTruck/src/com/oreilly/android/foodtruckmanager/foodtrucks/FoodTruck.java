package com.oreilly.android.foodtruckmanager.foodtrucks;

import java.io.Serializable;
import java.util.Date;

public class FoodTruck implements Serializable  
{
	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	private String name;
	private String description;
	private String phone;
	private String location;
	private String time;
	
}
