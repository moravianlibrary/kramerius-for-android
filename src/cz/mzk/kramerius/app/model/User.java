package cz.mzk.kramerius.app.model;

import java.util.ArrayList;
import java.util.List;

public class User {

	private String firstName;
	private String surname;
	private String login;
	private String password;
	
	private List<String> roles;
	
	public User() {
		roles = new ArrayList<String>();
	}

	public String getFirstName() {
		return firstName;
	}

	public String getSurname() {
		return surname;
	}

	public String getLogin() {
		return login;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void addRole(String role) {
		this.roles.add(role);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	
}
