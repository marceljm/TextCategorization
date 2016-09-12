package com.marceljm.entity;

public class Field implements Comparable<Field> {

	public Field(String name, Float value) {
		this.name = name;
		this.value = value;
	}

	public Field() {
	}

	private Float value;

	private String name;

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Field field) {
		return this.value.compareTo(field.getValue());
	}

}
