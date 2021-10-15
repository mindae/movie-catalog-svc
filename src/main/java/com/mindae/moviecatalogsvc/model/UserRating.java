package com.mindae.moviecatalogsvc.model;

import java.util.List;

public class UserRating {
	private List<Rating> userRating;

	public List<Rating> getUserRating() {
		return userRating;
	}

	public void setUserRating(List<Rating> userRating) {
		this.userRating = userRating;
	}

	public UserRating(List<Rating> userRating) {
		this.userRating = userRating;
	}
	
	public UserRating() {}

	@Override
	public String toString() {
		return "UserRating [userRating=" + userRating + "]";
	}
	
	
}
