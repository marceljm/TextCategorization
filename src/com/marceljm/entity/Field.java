package com.marceljm.entity;

public class Field implements Comparable<Field> {

	private Float value = 0F;

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
		return value.compareTo(field.getValue());
	}

}
