package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Annotation;
import models.Article;
import models.Login;
import models.RegistrationForm;
import models.Resource;
import models.UserAccount;
import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.MongoException.DuplicateKey;

public class Application extends Controller 
{
	static Form<RegistrationForm> registrationForm = form(RegistrationForm.class);
	static Form<UserAccount> userAccountForm = form(UserAccount.class);
//	static Form<Article> articleForm = form(Article.class);
//	static Form<Annotation> annotationForm = form(Annotation.class);
	
	public static Result index() throws Exception 
	{
		if(session("nickname") != null)
		{
			return ok(views.html.index.render());
		}
		else
			return redirect(routes.Application.login());
	}

	//UserAccount
	public static Result userAccounts() 
	{
		return ok(views.html.users.render(UserAccount.all(), registrationForm));
	}
	
	public static Result register()
	{
		return ok();
	}
 
	public static Result newUserAccount() 
	{
		
		Form<RegistrationForm> filledForm = registrationForm.bindFromRequest();
		
		//Form<UserAccount> filledForm = userAccountForm.bindFromRequest();
		
		Map<String, List<ValidationError>> errors = filledForm.errors();
		
		for(String key : errors.keySet())
		{
			for(ValidationError error : errors.get(key))
			{
				System.out.println("error (" + key + ") : " + error.message());
			}
		}
		
		if(filledForm.hasErrors()) 
		{
			Logger.error("There was an error in the registration form.");
			return badRequest(views.html.users.render(UserAccount.all(), filledForm));
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
				
				UserAccount.create(user);
				//UserAccount.create(filledForm.get());
				return redirect(routes.Application.userAccounts());	
			}
			catch(DuplicateKey exception)
			{
				return badRequest(views.html.users.render(UserAccount.all(), filledForm));
			}
		}
	}
	
	public static Result login()
	{
		Form<Login> lform = form(Login.class);	
		return ok(views.html.login.render(lform));
	}
	
	public static Result checkLoginInfos()
	{
		Form<Login> form = form(Login.class).bindFromRequest();
		if(form.hasErrors()) 
		{
            return badRequest(views.html.login.render(form));
        }
		else 
		{
            session("nickname", form.field("nickname").value());
            Logger.info("Connection of " + form.field("nickname").value());
        }
		return ok();
	}
	
	public static Result seeUserAccount(String nickname)
	{
		UserAccount userAccount = UserAccount.findByNickname(nickname);
		if(userAccount == null) return redirect(routes.Application.index());
		else return ok(views.html.user.render(userAccount));
	}
	
//	public static Result deleteUserAccount(String id) 
//	{
//		UserAccount.delete(id);
//		return redirect(routes.Application.userAccounts());
//	}
	
	//Article
	
//	public static Result articles() 
//	{
//		return ok(views.html.articles.render(Article.allArticle(), articleForm));
//	}
//
//	public static Result newArticle() 
//	{
//		Form<Article> filledForm = articleForm.bindFromRequest();
//		if(filledForm.hasErrors()) 
//		{
//			return badRequest(views.html.articles.render(Article.allArticle(), filledForm));
//		} 
//		else 
//		{
//			Article.create(filledForm.get());
//			return redirect(routes.Application.articles());
//		}
//	}
//	
//	public static Result deleteArticle(String id) 
//	{
//		Article.delete(id);
//		return redirect(routes.Application.articles());
//	}
//	
//	public static Result article(String id)
//	{
//		Article article = Article.findById(id);
//		if(article == null) return redirect(routes.Application.index());
//		else
//		{	List<Annotation> annotations = Annotation.findByResourceId(id);
//			return ok(views.html.article.render(article, annotations, annotationForm));
//		}
//	}
//	
//	//Annotations
//	public static Result annotations() 
//	{
//		return ok(views.html.annotations.render(Annotation.allAnnotation(), annotationForm));
//	}
//
//	public static Result getAnnotationsOnArticle(String id)
//	{
//		List<Annotation> annotations = Annotation.findByResourceId(id);		
//		return ok(views.html.annotations.render(annotations, annotationForm));
//	}
//	
//	public static Result newAnnotation() 
//	{
//		Map<String, String[]> requestData = request().body().asFormUrlEncoded() ;
//		Map<String,String> anyData = new HashMap();
//		anyData.put("title", requestData.get("title")[0]);
//		anyData.put("content", requestData.get("content")[0]);
//		anyData.put("annotatedContent", requestData.get("annotatedContent")[0]);
//		anyData.put("author.id", requestData.get("author.id")[0]);
//		anyData.put("pointerBegin", requestData.get("pointerBegin")[0]);
//		anyData.put("pointerEnd", requestData.get("pointerEnd")[0]);
//
//		String annotatedId = requestData.get("annotated.id")[0] ;
//		Resource annotated = Resource.findById(annotatedId);
//		
//		Form<Annotation> filledForm = annotationForm.bind(anyData);
//		if(filledForm.hasErrors()) 
//		{
//			return badRequest(views.html.annotations.render(Annotation.allAnnotation(), filledForm));
//		} 
//		else 
//		{
//			Annotation annotation = filledForm.get();
//			annotation.setAnnotated(annotated);
//			Annotation.create(annotation);
//			return redirect(routes.Application.annotations());
//		}
//	}
//	
//	public static Result deleteAnnotation(String id) 
//	{
//		Annotation.delete(id);
//		return redirect(routes.Application.annotations());
//	}
//	
//	public static Result annotation(String id)
//	{
//		Annotation annotation = Annotation.findById(id);
//		if(annotation == null) return redirect(routes.Application.index());
//		else return ok(views.html.annotation.render(annotation));
//	}	
	
}