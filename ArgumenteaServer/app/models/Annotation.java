package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;

@Entity("Resources")
public class Annotation extends Resource{

	private Date creationDate ;
	@Reference
	private List<Selection> annotateds ;
	
	public List<Selection> getAnnotateds() {
		return annotateds;
	}

	public void setAnnotateds(List<Selection> annotateds) {
		this.annotateds = annotateds;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public static List<Annotation> allAnnotation() {
		List<Annotation> annotations = new ArrayList<Annotation>() ;
		if (MorphiaObject.datastore != null) {
			List<Resource> ressources = MorphiaObject.datastore.find(Resource.class).asList();
			for(Resource r : ressources) 
			{
				if(r.getClass().equals(Annotation.class)) annotations.add((Annotation)r);
			}
			return annotations ;
		} else {
			return annotations ;
		}
	}
	
	public static void create(Annotation annotation) {
		annotation.setCreationDate(new Date());
		MorphiaObject.morphia.map(Resource.class);
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
	
	/*public static List<Annotation> findByResourceId(String id)
	{
		Resource resource = Resource.findById(id);
		return MorphiaObject.datastore.find(Annotation.class).field("annotated").equal(resource).asList();
	}*/
	
}