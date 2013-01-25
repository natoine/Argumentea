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
public class Annotation {

	@Id
	private ObjectId id ;
	@Reference
	private Article annotated ;
	@Reference
	private UserAccount author ;
	@Required
	private String content ;
	private String annotatedContent ;
	private String pointerBegin ; //du coup pointer mériterait d'être une classe avec sa propre méthode equals et une hierarchie de sous types
	private String pointerEnd ;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public Article getAnnotated() {
		return annotated;
	}
	public void setAnnotated(Article annotated) {
		this.annotated = annotated;
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
	
	public String getAnnotatedContent() {
		return annotatedContent;
	}
	public void setAnnotatedContent(String annotatedContent) {
		this.annotatedContent = annotatedContent;
	}
	public String getPointerBegin() {
		return pointerBegin;
	}
	public void setPointerBegin(String pointerBegin) {
		this.pointerBegin = pointerBegin;
	}
	public String getPointerEnd() {
		return pointerEnd;
	}
	public void setPointerEnd(String pointerEnd) {
		this.pointerEnd = pointerEnd;
	}
	public static List<Annotation> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Annotation.class).asList();
		} else {
			return new ArrayList<Annotation>();
		}
	}
	
	public static void create(Annotation annotation) {
		MorphiaObject.datastore.save(annotation);
	}

	public static void delete(String idToDelete) {
		Annotation toDelete = MorphiaObject.datastore.find(Annotation.class).field("_id").equal(new ObjectId(idToDelete)).get();
		if (toDelete != null) {
			//Logger.info("toDelete: " + toDelete);
			MorphiaObject.datastore.delete(toDelete);
		} else {
			//Logger.debug("ID No Found: " + idToDelete);
		}
	}
	
	public static Annotation findById(String id)
	{
		return MorphiaObject.datastore.find(Annotation.class).field("_id").equal(new ObjectId(id)).get();
	}
	
	public static List<Annotation> findByArticleId(String id)
	{
		Article article = Article.findById(id);
		return MorphiaObject.datastore.find(Annotation.class).field("annotated").equal(article).asList();
	}
	
}