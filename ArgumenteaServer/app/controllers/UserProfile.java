package controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.htmlparser.util.ParserException;

import models.Annotation;
import models.AnnotationJudgment;
import models.Article;
import models.Resource;
import models.UserAccount;
import models.annotationHtml.AnnotatedHtml;
import models.annotationHtml.SplitedXpointer;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

//@Security.Authenticated(Secured.class)
public class UserProfile extends Controller
{
	protected static Form<Article> articleForm = form(Article.class);
	protected static Form<Annotation> annotationForm = form(Annotation.class);
	protected static Form<AnnotationJudgment> annotationJgtForm = form(AnnotationJudgment.class);
	
	public static Result index()
	{
		UserAccount currentUser = UserAccount.findByNickname(session("nickname"));
		
		List<Article> articles = Article.findByAuthor(currentUser, 10);
		List<Annotation> annotations = Annotation.findByAuthor(currentUser, 10);
		
		Collections.reverse(articles);
		Collections.reverse(annotations);
		
		return ok(views.html.userprofile.render(articles, annotations, flash("status"), flash("statusStyleCSS")));
	}
	
	public static Result articles() 
	{
		List<Article> articles = Article.allArticle();
		Collections.reverse(articles);
		return ok(views.html.articles.render(articles));
	}
	
	public static Result myArticles()
	{
		UserAccount currentUser = UserAccount.findByNickname(session("nickname"));
		List<Article> articles = Article.findByAuthor(currentUser);
		Collections.reverse(articles);
		return ok(views.html.myArticles.render(articles));
	}
	
	public static Result newArticleForm()
	{
		return ok(views.html.newArticleForm.render(articleForm));
	}

	public static Result newArticle() 
	{
		Form<Article> filledForm = articleForm.bindFromRequest();
		if(filledForm.hasErrors()) 
		{
			return badRequest(views.html.newArticleForm.render(filledForm));
		} 
		else 
		{
			Article article = filledForm.get();
						
			UserAccount author = UserAccount.findByNickname(session("nickname"));
			
			article.setAuthor(author);
			Article.create(article);
			return redirect(routes.UserProfile.myArticles());
		}
	}
	
	public static Result deleteArticle(String id) 
	{
		// vérifier que l'article appartient bien a la personne connectée
		Article.delete(id);
		return redirect(routes.UserProfile.articles());
	}
	
	public static Result article(String id, Integer page)
	{
		Article article = Article.findById(id);
		if(article == null) return redirect(routes.Application.index());
		else
		{	
			int totalAnnotations = Annotation.findByResourceId(id).size();
			
			int nbPages = (int)Math.ceil(totalAnnotations / 10.0f);
			
			List<Annotation> annotations = Annotation.findByResourceId(id, page * 10, page * 10 + 10, true);
			
			return ok(views.html.article.render(article, annotations, annotationForm, nbPages, page));
		}
	}
	
	public static Result getArticleContentAnnotated()
	{
		JsonNode json = request().body().asJson();
		
		String articleId = json.get("articleId").asText();
		
		if( ! articleId.equals(""))
		{
			
			Article article = Article.findById(articleId);
			String htmlContent = article.getContent();
			//System.out.println(htmlContent);
			List<String> annotationsId = json.get("annotationsId").findValuesAsText("wut");
			
			//System.out.println("MY JSON = " + json);
			
			//System.out.println("nb annotations : " + annotationsId.size());
			AnnotatedHtml annotatedHtml = new AnnotatedHtml(htmlContent);
			SplitedXpointer splitedXpointerStart = new SplitedXpointer();
			SplitedXpointer splitedXpointerEnd = new SplitedXpointer();
			for(String annotationId : annotationsId)
			{
				//System.out.println("Trying to find annotation with id : " + annotationId);
				
				Annotation annotation = Annotation.findById(annotationId);
				splitedXpointerStart = SplitedXpointer.createXpointer(annotation.getPointerBegin(), splitedXpointerStart);
				splitedXpointerEnd = SplitedXpointer.createXpointer(annotation.getPointerEnd(), splitedXpointerEnd);
				String onHover = "";
				if(annotation.getContent().length() > 140)
				{
					onHover = annotation.getContent().substring(0, 140) + " ...";
				}
				else onHover = annotation.getContent();
				try {
					if(annotation instanceof AnnotationJudgment)
					{
						if(((AnnotationJudgment)annotation).getJudgment().equalsIgnoreCase("ok"))
							annotatedHtml.highLight(splitedXpointerStart, splitedXpointerEnd, "green", annotation.getId().toString(), onHover);
						else annotatedHtml.highLight(splitedXpointerStart, splitedXpointerEnd, "red", annotation.getId().toString(), onHover);
					}
					else annotatedHtml.highLight(splitedXpointerStart, splitedXpointerEnd, "yellow", annotation.getId().toString(), onHover);
				} catch (ParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//System.out.println("not able to parse HTML !");
				}
			}
			return ok(annotatedHtml.getHtmlContent());
		}
		else return badRequest();
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
	
	public static Result newAnnotationJgtJson()
	{
		JsonNode json = request().body().asJson();
		Map<String, String> anyData = new HashMap<String, String>();
		anyData.put("pointerBegin", json.get("pointerBegin").asText()) ;
		anyData.put("pointerEnd", json.get("pointerEnd").asText());
		anyData.put("title", json.get("title").asText());
		anyData.put("content", json.get("content").asText());
		anyData.put("reformulation", json.get("reformulation").asText());
		anyData.put("judgment", json.get("jugement").asText());
		anyData.put("annotatedContent", json.get("annotatedContent").asText());
		UserAccount author = UserAccount.findByNickname(session("nickname"));
		anyData.put("author.id", author.getId().toString());
		String annotatedId = json.get("annotatedId").asText();
		System.out.println("annotatedId : " + annotatedId);
		Resource annotated = Resource.findById(annotatedId);
		Form<AnnotationJudgment> filledForm = annotationJgtForm.bind(anyData);
		if(filledForm.hasErrors()) 
		{
			return badRequest();
		}
		else
		{
			AnnotationJudgment annotation = filledForm.get();
			annotation.setAnnotated(annotated);
			AnnotationJudgment.create(annotation);
			return ok();
		}
	}
	
	public static Result newAnnotationJson()
	{
		JsonNode json = request().body().asJson();
		Map<String, String> anyData = new HashMap<String, String>();
		anyData.put("pointerBegin", json.get("pointerBegin").asText()) ;
		anyData.put("pointerEnd", json.get("pointerEnd").asText());
		anyData.put("title", json.get("title").asText());
		anyData.put("content", json.get("content").asText());
		anyData.put("annotatedContent", json.get("annotatedContent").asText());
		UserAccount author = UserAccount.findByNickname(session("nickname"));
		anyData.put("author.id", author.getId().toString());
		String annotatedId = json.get("annotatedId").asText();
		System.out.println("annotatedId : " + annotatedId);
		Resource annotated = Resource.findById(annotatedId);
		Form<Annotation> filledForm = annotationForm.bind(anyData);
		if(filledForm.hasErrors()) 
		{
			return badRequest();
		}
		else
		{
			Annotation annotation = filledForm.get();
			annotation.setAnnotated(annotated);
			Annotation.create(annotation);
			return ok();
		}
	}
	
	public static Result newAnnotation() 
	{
		Map<String, String[]> requestData = request().body().asFormUrlEncoded() ;
		Map<String, String> anyData = new HashMap<String, String>();
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
	
	public static Result annotation(String id, Integer page)
	{
		Annotation annotation = Annotation.findById(id);
		if(annotation == null) return redirect(routes.Application.index());
		else
		{
			int totalAnnotations = Annotation.findByResourceId(id).size();
			
			int nbPages = (int)Math.ceil(totalAnnotations / 10.0f);
			
			List<Annotation> annotations = Annotation.findByResourceId(id, page * 10, page * 10 + 10, true);
			
			return ok(views.html.annotation.render(annotation, annotations, annotationForm, nbPages, page));
		}
	}
	
	public static Result myAnnotations()
	{
		UserAccount currentUser = UserAccount.findByNickname(session("nickname"));
		List<Annotation> annotations = Annotation.findByAuthor(currentUser);
		Collections.reverse(annotations);
		return ok(views.html.myAnnotations.render(annotations));
	}
}
