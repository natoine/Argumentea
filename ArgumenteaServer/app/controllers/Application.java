package controllers;

import models.Article;
import models.UserAccount;
import play.*;
import play.mvc.*;
import play.data.Form;

import views.html.*;

public class Application extends Controller {
	static Form<UserAccount> userAccountForm = form(UserAccount.class);
	static Form<Article> articleForm = form(Article.class);
	
	public static Result index() throws Exception {
		// redirect to the "group Result
		return redirect(routes.Application.userAccounts());
	}

	//UserAccount
	
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
	
	//Article
	
	public static Result articles() {
		return ok(views.html.articles.render(Article.all(), articleForm));
	}

	public static Result newArticle() {
		Form<Article> filledForm = articleForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(views.html.articles.render(Article.all(), filledForm));
		} else {
			Article.create(filledForm.get());
			return redirect(routes.Application.articles());
		}
	}
	
	public static Result deleteArticle(String id) {
		Article.delete(id);
		return redirect(routes.Application.articles());
	}
	
	public static Result article(String id)
	{
		Article article = Article.findById(id);
		if(article == null) return redirect(routes.Application.index());
		else return ok(views.html.article.render(article));
	}
}