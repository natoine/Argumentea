package models;

import play.data.validation.Constraints.Required;


public class Login 
{
	@Required
	public String nickname;
	
	@Required
	public String password;
	
	public String validate()
	{
		if(UserAccount.authenticate(nickname, password) == null)
		{
			return "Problème lors de l'authentification.";
		}
		return null;
	}
}
