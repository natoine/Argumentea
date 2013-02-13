package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import controllers.MorphiaObject;


@Entity("Resources")
public class Article extends Resource 
{
	
	public static List<Article> allArticle() 
	{
		List<Article> articles = new ArrayList<Article>() ;
		if (MorphiaObject.datastore != null) 
		{
			List<Resource> ressources = MorphiaObject.datastore.find(Resource.class).asList();
			for(Resource r : ressources) 
			{
				if(r.getClass().equals(Article.class)) articles.add((Article)r);
			}
			return articles ;
		} else {
			return articles ;
		}
	}
	
	public static List<Article> findByAuthor(UserAccount author)
	{
		List<Article> articles = new ArrayList<Article>();
		
		if (MorphiaObject.datastore != null) {
			List<Resource> ressources = MorphiaObject.datastore.find(Resource.class).asList();
			for(Resource r : ressources) 
			{
				if(r.getClass().equals(Article.class) && r.getAuthor().isSameUser(author))
				{
					articles.add((Article)r);
				}
			}
			return articles ;
		} else {
			return articles ;
		}
	}
	
	public static void create(Article article) 
	{
		article.setCreationDate(new Date());
		MorphiaObject.morphia.map(Resource.class);
		MorphiaObject.datastore.save(article);
	}

	public static void delete(String idToDelete) 
	{
		Article toDelete = MorphiaObject.datastore.find(Article.class).field("_id").equal(new ObjectId(idToDelete)).get();
		if (toDelete != null) {
			//Logger.info("toDelete: " + toDelete);
			MorphiaObject.datastore.delete(toDelete);
		} else {
			//Logger.debug("ID No Found: " + idToDelete);
		}
	}
		
	public static Article findById(String id)
	{
		return MorphiaObject.datastore.find(Article.class).field("_id").equal(new ObjectId(id)).get();
	}
}
