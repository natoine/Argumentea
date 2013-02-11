package controllers;

import models.UserAccount;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class Admin extends Controller 
{

	public static Result index()
	{
		return ok();
	}
	
	public static Result deleteUserAccount(String id) 
	{
		UserAccount.delete(id);
		return redirect(routes.Application.userAccounts());
	}
	
}
