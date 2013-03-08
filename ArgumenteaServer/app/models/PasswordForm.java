package models;

import play.i18n.Messages;

public class PasswordForm 
{
	public String password;
	public String passwordRepeat;
	
	public String validate()
	{
		if(this.password.length() < 4)
			return Messages.get("password.tooshort");
		
		if(!this.password.equals(this.passwordRepeat))
			return Messages.get("password.notsame");
		
		return null;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordRepeat() {
		return passwordRepeat;
	}

	public void setPasswordRepeat(String passwordRepeat) {
		this.passwordRepeat = passwordRepeat;
	}
	
	
}
