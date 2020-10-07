package com.dierauf.rachio.familygiftexchange.model;

import java.io.Serializable;

public class FamilyMember implements Serializable {

	private static final long serialVersionUID = 4032591556895521620L;

	private int id = -1;
	private String name;

	public FamilyMember(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.id;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		FamilyMember other = (FamilyMember) obj;
		if (this.id != other.id)
			return false;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		return true;
	}

}
