package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;

import com.mongodb.MongoException.DuplicateKey;

import models.Annotation;
import models.Article;
import models.Resource;
import models.Selection;
import models.UserAccount;
import play.*;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.Session;
import play.data.Form;

import views.html.*;

public class Application extends Controller {
	static Form<UserAccount> userAccountForm = form(UserAccount.class);
	static Form<Article> articleForm = form(Article.class);
	static Form<Annotation> annotationForm = form(Annotation.class);
	
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
			try{
				UserAccount.create(filledForm.get());
				return redirect(routes.Application.userAccounts());
			}
			catch(DuplicateKey exception)
			{
				return badRequest(views.html.users.render(UserAccount.all(), filledForm));
			}
			
		}
	}
	
	public static Result deleteUserAccount(String id) {
		UserAccount.delete(id);
		return redirect(routes.Application.userAccounts());
	}
	
	public static Result seeUserAccount(String nickname)
	{
		UserAccount userAccount = UserAccount.findByNickname(nickname);
		if(userAccount == null) return redirect(routes.Application.index());
		else return ok(views.html.user.render(userAccount));
	}
	
	//Article
	
	public static Result articles() {
		return ok(views.html.articles.render(Article.allArticle(), articleForm));
	}

	public static Result newArticle() {
		Form<Article> filledForm = articleForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(views.html.articles.render(Article.allArticle(), filledForm));
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
		else
		{
			List<Selection> selections = getSelectionFromPanier(session());
			//List<Annotation> annotations = Annotation.findByResourceId(id);
			List<Annotation> annotations = new ArrayList<Annotation>();
			return ok(views.html.article.render(article, annotations, annotationForm, selections));
		}
	}
	
	//Annotations
	public static Result annotations() {
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
		if(filledForm.hasErrors()) {
			return badRequest(views.html.annotations.render(Annotation.allAnnotation(), filledForm));
		} else {
			Annotation annotation = filledForm.get();
		//	annotation.setAnnotated(annotated);
			Annotation.create(annotation);
			return redirect(routes.Application.annotations());
		}
	}
	
	public static Result deleteAnnotation(String id) {
		Annotation.delete(id);
		return redirect(routes.Application.annotations());
	}
	
	public static Result annotation(String id)
	{
		Annotation annotation = Annotation.findById(id);
		if(annotation == null) return redirect(routes.Application.index());
		else 
		{
			List<Selection> selections = getSelectionFromPanier(session());
			return ok(views.html.annotation.render(annotation, selections));
		}
	}	
	
	//Selections
	@BodyParser.Of(Json.class)
	public static Result addSelectionPanier()
	{
		JsonNode json = request().body().asJson();
		String selectedContent = json.findPath("selectedContent").getTextValue();
		String originId = json.findPath("originId").getTextValue();
		String pointerBegin = json.findPath("pointerBegin").getTextValue();
		String pointerEnd = json.findPath("pointerEnd").getTextValue();
		if(selectedContent == null || originId == null || pointerBegin == null || pointerEnd == null
				|| selectedContent.equals("") || originId.equals("") || pointerBegin.equals("") || pointerEnd.equals("")
			) // if ! allright send badRequest
		{
		    return badRequest("Missing parameter [name]");
		} 
		else 
		{
			//récupération du panier existant
			List<Selection> selections = getSelectionFromPanier(session());
			//Fabrication de la sélection reçue
			Selection selection = new Selection();
			selection.setOrigin(Resource.findById(originId));
			selection.setPointerBegin(pointerBegin);
			selection.setPointerEnd(pointerEnd);
			selection.setSelectedContent(selectedContent);
			//Ajout au panier de la sélection reçue
			selections.add(selection);
			//Add in session jsonserialized List<selection> (get puis deserialize puis add)
			session("panier", Json.toJson(selections).asText());
			return ok("selection ajoutée au panier");
		}
	}
	
	private static List<Selection> getSelectionFromPanier(Session session)
	{
		List<Selection> selections = new ArrayList<Selection>();
		String jsonSelections = session.get("panier");
		if(jsonSelections != null && !jsonSelections.equals("")) 
		{
			JsonNode selectionsJsonArray = Json.parse(jsonSelections);
			int nbSelections = selectionsJsonArray.size();
			for(int cptSelection = 0 ; cptSelection < nbSelections ; cptSelection++)
			{
				selections.add(Json.fromJson(selectionsJsonArray.get(cptSelection) , Selection.class));
			}
		}
		return selections ;
	}
}