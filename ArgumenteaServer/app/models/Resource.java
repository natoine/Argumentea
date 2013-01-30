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
public abstract class Resource {

	@Id
	private ObjectId id;
	private String title ;
	@Reference
	private UserAccount author ;
	@Required
	private String content ;
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public static List<Resource> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Resource.class).asList();
		} else {
			return new ArrayList<Resource>();
		}
	}
	
	public static Resource findById(String id)
	{
		return MorphiaObject.datastore.find(Resource.class).field("_id").equal(new ObjectId(id)).get();
	}
	
	public static Map<String,String> options() {
		List<Resource> resources = all();
		LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
		for(Resource r: resources) {
			options.put(r.id.toString(), r.title);
		}
		return options;
	}
}
