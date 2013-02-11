package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Annotation;
import models.Article;
import models.Resource;
import models.UserAccount;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class UserProfile extends Controller
{
	protected static Form<Article> articleForm = form(Article.class);
	protected static Form<Annotation> annotationForm = form(Annotation.class);
	
	public static Result articles() 
	{
		return ok(views.html.articles.render(Article.allArticle(), articleForm));
	}

	public static Result newArticle() 
	{
		Form<Article> filledForm = articleForm.bindFromRequest();
		if(filledForm.hasErrors()) 
		{
			return badRequest(views.html.articles.render(Article.allArticle(), filledForm));
		} 
		else 
		{
			Article.create(filledForm.get());
			return redirect(routes.UserProfile.articles());
		}
	}
	
	public static Result deleteArticle(String id) 
	{
		// vérifier que l'article appartient bien a la personne connectée
		
		Article.delete(id);
		return redirect(routes.UserProfile.articles());
	}
	
	public static Result article(String id)
	{
		Article article = Article.findById(id);
		if(article == null) return redirect(routes.Application.index());
		else
		{	List<Annotation> annotations = Annotation.findByResourceId(id);
			return ok(views.html.article.render(article, annotations, annotationForm));
		}
	}
	
	//Annotations
	public static Result annotations() 
	{
		return ok(views.html.annotations.render(Annotation.allAnnotation(), annotationForm));
	}

	public static Result getAnnotationsOnArticle(String id)
	{
		List<Annotation> annotations = Annotation.findByResourceId(id);		
		return ok(views.html.annotations.render(annotations, annotationForm));
	}
	
	public static Result newAnnotation() 
	{
		Map<String, String[]> requestData = request().body().asFormUrlEncoded() ;
		Map<String,String> anyData = new HashMap();
		anyData.put("title", requestData.get("title")[0]);
		anyData.put("content", requestData.get("content")[0]);
		anyData.put("annotatedContent", requestData.get("annotatedContent")[0]);
		anyData.put("author.id", requestData.get("author.id")[0]);
		anyData.put("pointerBegin", requestData.get("pointerBegin")[0]);
		anyData.put("pointerEnd", requestData.get("pointerEnd")[0]);

		String annotatedId = requestData.get("annotated.id")[0] ;
		Resource annotated = Resource.findById(annotatedId);
		
		Form<Annotation> filledForm = annotationForm.bind(anyData);
		if(filledForm.hasErrors()) 
		{
			return badRequest(views.html.annotations.render(Annotation.allAnnotation(), filledForm));
		} 
		else 
		{
			Annotation annotation = filledForm.get();
			annotation.setAnnotated(annotated);
			Annotation.create(annotation);
			return redirect(routes.UserProfile.annotations());
		}
	}
	
	public static Result deleteAnnotation(String id) 
	{
		// vérifier que l'annotation appartient bien a la personne connectée
		Annotation.delete(id);
		return redirect(routes.UserProfile.annotations());
	}
	
	public static Result annotation(String id)
	{
		Annotation annotation = Annotation.findById(id);
		if(annotation == null) return redirect(routes.Application.index());
		else return ok(views.html.annotation.render(annotation));
	}	
}
