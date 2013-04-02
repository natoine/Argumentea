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
	
	public static void create(Article article) 
	{
		article.setCreationDate(new Date());
		MorphiaObject.morphia.map(Resource.class);
		MorphiaObject.datastore.save(article);
	}

	public static void delete(String idToDelete) 
	{
		Article.delete(new ObjectId(idToDelete));
	}
	
	public static void delete(ObjectId idToDelete) 
	{
		Article toDelete = MorphiaObject.datastore.find(Article.class).field("_id").equal(idToDelete).get();
		
		if (toDelete != null) 
		{
			List<Annotation> annotationsToDelete = Annotation.findByResourceId(idToDelete.toString());
			
			for(Annotation annotation : annotationsToDelete)
			{
				Annotation.delete(annotation.getId());
			}
			
			MorphiaObject.datastore.delete(toDelete);
		} 
	}
		
	public static Article findById(String id)
	{
		return MorphiaObject.datastore.find(Article.class).field("_id").equal(new ObjectId(id)).get();
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
	
	public static List<Article> findByAuthor(UserAccount author, int limit)
	{
		List<Article> articles = findByAuthor(author);
		if(articles.size() - 10 < 0)
		{
			return articles.subList(0, articles.size());
		}
		else
		{
			return articles.subList(articles.size() - 10, articles.size());
		}
	}
	
	public static List<Article> findByRange(int start, int end)
	{
		List<Article> articles = Article.allArticle();
		
		if(start < articles.size())
		{
			if(end < articles.size())
			{
				return articles.subList(start, end);
			}
			else
			{
				return articles.subList(start, articles.size());
			}
		}
		else
			return articles;
	}
	
	public static int getTotalArticles()
	{
		return Article.allArticle().size();
	}

	@Override
	public boolean isArticle() {
		return true;
	}

	@Override
	public boolean isAnnotation() {
		return false;
	}
}
