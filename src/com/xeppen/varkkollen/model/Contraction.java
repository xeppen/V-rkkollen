package com.xeppen.varkkollen.model;

import java.lang.reflect.Array;

public class Contraction {
	Integer id = 0;
	String startTime = "";
	String stopTime = "";
	Integer duration_min = 0;
	Integer duration_sec = 0;
	Integer intencity = 0;
	String note = "";
	
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getStopTime() {
		return stopTime;
	}
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getDurationMin() {
		return duration_min;
	}
	public void setDurationMin(Integer duration) {
		this.duration_min = duration;
	}
	public Integer getDurationSec() {
		return duration_sec;
	}
	public void setDurationSec(Integer duration) {
		this.duration_sec = duration;
	}
	public Integer getIntencity() {
		return intencity;
	}
	public void setIntencity(Integer intencity) {
		this.intencity = intencity;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
}
