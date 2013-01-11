package controllers;

import models.UserAccount;
import play.*;
import play.mvc.*;
import play.data.Form;

import views.html.*;

public class Application extends Controller {
	static Form<UserAccount> userAccountForm = form(UserAccount.class);

	public static Result index() throws Exception {
		// redirect to the "group Result
		return redirect(routes.Application.userAccounts());
	}

	public static Result userAccounts() {
		return ok(views.html.users.render(UserAccount.all(), userAccountForm));
	}

	public static Result newUserAccount() {
		Form<UserAccount> filledForm = userAccountForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(views.html.users.render(UserAccount.all(), filledForm));
		} else {
			UserAccount.create(filledForm.get());
			return redirect(routes.Application.userAccounts());
		}
	}
	
	public static Result deleteUserAccount(String id) {
		UserAccount.delete(id);
		return redirect(routes.Application.userAccounts());
	}

}