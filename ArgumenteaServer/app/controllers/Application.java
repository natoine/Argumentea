package controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import models.Annotation;
import models.Login;
import models.RegistrationForm;
import models.UserAccount;
import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.mongodb.MongoException.DuplicateKey;

public class Application extends Controller 
{
	/**
	 * Modèle du formulaire d'inscription
	 */
	static Form<RegistrationForm> registrationForm 	= form(RegistrationForm.class);
	
	/**
	 * Modèle du formulaire de connexion
	 */
	static Form<UserAccount> userAccountForm 		= form(UserAccount.class);
	
	
	/**
	 * Affiche le formulaire de création de compte utilisateur
	 * @return
	 */
	public static Result register()
	{
		if(!Application.isConnected())
			return ok(views.html.register.render(registrationForm, flash("status"), flash("statusStyleCSS")));
		else
			return redirect(routes.Application.index());
	}

	/**
	 * Appelé lors de la soumissions du formulaire de création d'un nouveau compte utilisateur. Crée un nouvel utilisateur dans la BDD s'il n'y a pas d'erreurs
	 * @return
	 */
	public static Result newUserAccount() 
	{
		Form<RegistrationForm> filledForm = registrationForm.bindFromRequest();
		Map<String, List<ValidationError>> errors = filledForm.errors();
		
		if(filledForm.hasErrors()) 
		{
			flash("status", "Il y a eu une erreur dans le formulaire : <br />" + Application.getHTMLReadableErrors(errors));
			flash("statusStyleCSS", "status_error");
			return redirect(routes.Application.register());
		}
		else 
		{
			try
			{
				UserAccount user = new UserAccount();
				user.setEmail(filledForm.field("email").value());
				user.setNickname(filledForm.field("nickname").value());
				user.setFirstname(filledForm.field("firstname").value());
				user.setHashedPassword(Secured.hash(filledForm.field("password").value()));
				user.setLastname(filledForm.field("lastname").value());
				user.setCreationDate(new Date());
				
				UserAccount.create(user);
				
				flash("status", "Le compte a été correctement créé !");
				flash("statusStyleCSS", "status_success");
				
				return redirect(routes.Application.login());	
			}
			catch(DuplicateKey exception)
			{
				return badRequest();
			}
		}
	}
	
	/**
	 * Déconnexion d'un utilisateur
	 * @return
	 */
	public static Result logout()
	{
		if(Application.isConnected())
		{
			session().clear();
		}
		return redirect(routes.Application.index());
	}
	
	/**
	 * Affiche le formulaire de connexion
	 * @return
	 */
	public static Result login()
	{
		if(!Application.isConnected())
		{
			Form<Login> lform = form(Login.class);	
			return ok(views.html.login.render(lform, flash("status"), flash("statusStyleCSS")));
		}
		else
		{
			return redirect(routes.UserProfile.index());
		}
	}
	
	/**
	 * Appelé lors de la connexion d'un utilisateur (vérification des informations de connexion)
	 * @return
	 */
	public static Result checkLoginInfos()
	{
		Form<Login> form = form(Login.class).bindFromRequest();
		Map<String, List<ValidationError>> errors = form.errors();
		
		if(form.hasErrors()) 
		{
			return badRequest(views.html.login.render(
					form, 
					"Il y a des erreurs dans le formulaire :<br />" + Application.getHTMLReadableErrors(errors), 
					"status_error")
			);
		}
		else 
		{
			session("nickname", form.field("nickname").value());
			
			flash("status", "Vous êtes maintenant connecté.");
			flash("statusStyleCSS", "status_success");
			
			Logger.info("Connection of " + form.field("nickname").value());
			return redirect(routes.UserProfile.index());
		}
	}
	
	/**
	 * Renvoi vrai si l'utilisateur est connecté, faux sinon
	 * @return Renvoi vrai si l'utilisateur est connecté, faux sinon
	 */
	public static boolean isConnected()
	{
		if(session("nickname") != null)
			return true;
		else 
			return false;
	}
	
	/**
	 * Renvoi une string HTML affichant les erreurs passées en paramètres
	 * @param errors liste des erreurs
	 * @return description HTML des erreurs
	 */
	public static String getHTMLReadableErrors(Map<String, List<ValidationError>> errors)
	{
		String strError = "";
		
		for(String key : errors.keySet())
		{
			for(ValidationError error : errors.get(key))
			{
				if(key != null && !key.isEmpty())
					strError += key + " : ";
				
				if(error.message() != null)
					strError += error.message() + "<br />";
			}
		}
		
		return strError;
	}
	
	public static Result passwordRecoveryForm()
	{
		return ok();
		//return ok(views.html.passwordRecovery.render(flash("status"), flash("statusStyleCSS")));
	}
	
	public static Result passwordRecovery(String email)
	{
		//UserAccount account = UserAccount.findByMail(email);
		
		//MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
		
		return ok();
	}
	
	public static Result index() throws Exception 
	{
		if(session("nickname") != null)
		{
			return redirect(routes.UserProfile.index());
		}
		else
			return redirect(routes.Application.login());
	}

	//UserAccount
	public static Result userAccounts() 
	{
		return ok(views.html.users.render(UserAccount.all(), registrationForm));
	}
	
	public static Result seeUserAccount(String nickname)
	{
		UserAccount userAccount = UserAccount.findByNickname(nickname);
		if(userAccount == null)
			return redirect(routes.Application.index());
		else 
		{
			if(session("nickname") != null)
			{
				if(session("nickname").equals(nickname))
				{
					return redirect(routes.UserProfile.index());
				}
			}
			return ok(views.html.user.render(userAccount));
		}
	}
	
	public static Result showXPointer()
	{
		List<Annotation> annotations = Annotation.allAnnotation();
		
		return ok(views.html.showXPointer.render(annotations));
	}
}