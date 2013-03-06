package models;

import play.data.validation.Constraints.Required;

/**
 * Classe servant à mapper les champs du formulaire de login
 * @author NaturalPad
 *
 */
public class Login 
{
	@Required
	public String nickname;
	
	@Required
	public String password;
	
	/**
	 * Méthode de vérification des infos du formulaire de connexion. Utilisée pour vérifier si l'utilisateur existe bien et que le mot de passe est bon
	 * @return
	 */
	public String validate()
	{
		if(UserAccount.authenticate(nickname, password) == null)
		{
			return "Problème lors de l'authentification : le nom d'utilisateur n'existe pas ou le mot de passe n'est pas correct.";
		}
		return null;
	}
}
