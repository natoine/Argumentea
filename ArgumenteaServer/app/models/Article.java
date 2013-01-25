package models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;

@Entity
public class Article {

	@Id
	private ObjectId id;
	@Required
	private String content ;
	private String title ;
	@Reference
	private UserAccount author ;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public UserAccount getAuthor() {
		return author;
	}

	public void setAuthor(UserAccount author) {
		this.author = author;
	}

	public static List<Article> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Article.class).asList();
		} else {
			return new ArrayList<Article>();
		}
	}

	public static void create(Article article) {
		MorphiaObject.datastore.save(article);
	}

	public static void delete(String idToDelete) {
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
