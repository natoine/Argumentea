package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;

@Polymorphic
@Entity("Resources")
public class Article extends Resource{
	
	public static List<Article> allArticle() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Article.class).asList();
		} else {
			return new ArrayList<Article>();
		}
	}

	public static void create(Article article) {
		MorphiaObject.morphia.map(Resource.class);
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
	
	/*public static Map<String,String> options() {
		List<Article> as = allArticle();
		LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
		for(Article a: as) {
			options.put(a.getId().toString(), a.getTitle());
		}
		return options;
	}*/
	
	public static Article findById(String id)
	{
		return MorphiaObject.datastore.find(Article.class).field("_id").equal(new ObjectId(id)).get();
	}
}
