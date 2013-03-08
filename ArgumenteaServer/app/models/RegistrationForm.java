package models;

/**
 * Classe servant à mapper les champs du formulaire de création d'utilisateur
 * @author NaturalPad
 *
 */
public class RegistrationForm 
{

	public String nickname;
	public String password;
	public String passwordRepeat;
	public String firstname;
	public String lastname;
	public String email;
	
	/**
	 * Méthode de vérification des infos sur formulaire. Mettre ici toutes les contraintes sur l'inscription (unicité, passwords équivalents, etc)
	 * @return message d'erreur s'il y en a un sinon null
	 */
	public String validate()
	{
		UserAccount testNickname = UserAccount.findByNickname(this.nickname);
		UserAccount testMail = UserAccount.findByMail(this.email);
		
		if(this.password.length() < 4)
			return "Le mot de passe doit contenir au moins 4 caractères.";
		
		if(!this.password.equals(this.passwordRepeat))
			return "Les deux mots de passe ne sont pas identiques.";
		
		if(testNickname != null)
			return "Ce nom d'utilisateur existe déjà.";
		
		if(testMail != null)
			return "Cet email est déjà enregistré dans notre base de données.";
		
		return null;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
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

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
