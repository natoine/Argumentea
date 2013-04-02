package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Annotation;
import models.AnnotationJudgment;
import models.Login;
import models.PasswordForm;
import models.PwdRecoveryHolder;
import models.RecoveryForm;
import models.RegistrationForm;
import models.UserAccount;
import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.mongodb.MongoException.DuplicateKey;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

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
	
	/**
	 * Affichage du formulaire de saisie du mail pour le changement de mot de passe
	 * @return
	 */
	public static Result passwordRecoveryForm()
	{
		return ok(views.html.passwordRecovery.render(flash("status"), flash("statusStyleCSS")));
	}
	
	/**
	 * Vérification après la soumission du mail pour la récupération de mot de passe
	 * @return
	 */
	public static Result passwordRecovery()
	{
		Form<RecoveryForm> form = form(RecoveryForm.class).bindFromRequest();
		Map<String, List<ValidationError>> errors = form.errors();
		
		if(form.hasErrors())
		{
			flash("status", Messages.get("errorform") + " :<br />" + Application.getHTMLReadableErrors(errors));
			flash("statusStyleCSS", "status_error");
			return redirect(routes.Application.passwordRecoveryForm());
		}
		else
		{
			String email = form.field("email").value();
			UserAccount account = UserAccount.findByMail(email);
			
			String randomHash = Secured.hash(UUID.randomUUID().toString());
			
			MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
			mail.setSubject(Messages.get("recovery.mail.header"));
			mail.addRecipient(email);
			mail.addFrom("test@naturalpad.org");
			try {
				String link = "http://" + Play.application().configuration().getString("application.baseURL") + ":" + Play.application().configuration().getString("application.http.port") + "/recover/" + URLEncoder.encode(randomHash, "UTF-8");
				mail.send(Messages.get("recovery.mail.content", account.getNickname(), link));
				PwdRecoveryHolder holder = new PwdRecoveryHolder();
				holder.setRandomHash(randomHash);
				holder.setExpireDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1 heure de temps d'expiration
				holder.setUser(account);
				
				PwdRecoveryHolder.create(holder);
				
				flash("status", Messages.get("recovery.status.success"));
				flash("statusStyleCSS", "status_info");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			return redirect(routes.Application.login());
		}
	}
	
	/**
	 * Affichage du formulaire de changement de mot de passe pour la recovery
	 * @param hash
	 * @return
	 */
	public static Result recoveryForm(String hash)
	{
		PwdRecoveryHolder holder = PwdRecoveryHolder.findByHash(hash);
		
		if(holder != null && hash != null && !hash.isEmpty())
		{
			if(holder.getExpireDate().after(new Date()))
			{
				return ok(views.html.recoveryCheck.render(hash));
			}
			else
			{
				flash("status", Messages.get("recovery.status.expire"));
				flash("statusStyleCSS", "status_error");
				return redirect(routes.Application.login());
			}
		}
		else
		{
			flash("status", Messages.get("recovery.status.usernotfound"));
			flash("statusStyleCSS", "status_error");
			return redirect(routes.Application.login());
		}
	}
	
	/**
	 * Vérification du nouveau mot de passe
	 * @param hash
	 * @param userId
	 * @return
	 */
	public static Result recoveryCheck()
	{
		Form<PasswordForm> form = form(PasswordForm.class).bindFromRequest();
		Map<String, List<ValidationError>> errors = form.errors();
		
		String hash = form.field("hash").value();
		
		if(form.hasErrors()) 
		{
			flash("status", Messages.get("errorform") + " :<br />" + Application.getHTMLReadableErrors(errors));
			flash("statusStyleCSS", "status_error");
			return redirect(routes.Application.recoveryForm(hash));
		}
		else
		{
			String password = form.field("password").value();
			
			PwdRecoveryHolder holder = PwdRecoveryHolder.findByHash(hash);
			
			UserAccount account = UserAccount.findById(holder.user.getId());
			account.setHashedPassword(Secured.hash(password));
			MorphiaObject.datastore.save(account);
			
			PwdRecoveryHolder.delete(holder.getId());
			
			flash("status", Messages.get("recovery.status.changepassword"));
			flash("statusStyleCSS", "status_success");
			
			return redirect(routes.Application.login());
		}
	}
}