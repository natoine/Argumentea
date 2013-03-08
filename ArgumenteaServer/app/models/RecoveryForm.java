package models;

import play.i18n.Messages;

/**
 * Wrapper pour le formulaire de soumission de mail pour la récupération du mot de passe
 * @author NaturalPad
 *
 */
public class RecoveryForm 
{
	public String email;

	public String getEmail() 
	{
		return email;
	}

	public void setEmail(String email) 
	{
		this.email = email;
	}
	
	public String validate()
	{
		UserAccount account = UserAccount.findByMail(this.email);
		
		if(account == null)
			return Messages.get("recovery.mailnotfound");
		
		return null;
	}

}
