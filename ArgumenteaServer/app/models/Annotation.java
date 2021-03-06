package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;

@Entity("Resources")
public class Annotation extends Resource
{
	@Reference
	private Resource annotated ;
	
	private String annotatedContent ;
	
	private String pointerBegin ; //du coup pointer mériterait d'être une classe avec sa propre méthode equals et une hierarchie de sous types
	
	private String pointerEnd ;
	
	
	public Resource getAnnotated() {
		return annotated;
	}
	
	public void setAnnotated(Resource annotated) {
		this.annotated = annotated;
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
	
	public static void create(Annotation annotation) {
		annotation.setCreationDate(new Date());
		MorphiaObject.morphia.map(Resource.class);
		MorphiaObject.datastore.save(annotation);
	}
	
	/**
	 * Renvoi la liste des annotations qui pointent vers l'annotation id
	 * @param id
	 * @return
	 */
	public static List<Annotation> findByResourceId(String id, int start, int end, boolean reverse)
	{
		List<Annotation> annotations = Annotation.findByResourceId(id);
		if(reverse)
		{
			Collections.reverse(annotations);
		}
		
		if(start < annotations.size())
		{
			if(end < annotations.size())
			{
				return annotations.subList(start, end);
			}
			else
			{
				return annotations.subList(start, annotations.size());
			}
		}
		else
			return annotations;
	}
	
	/**
	 * Renvoi la liste des annotations qui pointent vers l'annotation id
	 * @param id
	 * @return
	 */
	public static List<Annotation> findByResourceId(String id)
	{
		Resource resource = Resource.findById(id);
		return MorphiaObject.datastore.find(Annotation.class).field("annotated").equal(resource).asList();
	}
	
	public static void delete(Object idToDelete)
	{
		Annotation toDelete = MorphiaObject.datastore.find(Annotation.class).field("_id").equal(idToDelete).get();
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

	public static void delete(String idToDelete) 
	{
		Annotation.delete(new ObjectId(idToDelete));
	}
	
	public static Annotation findById(String id)
	{
		return MorphiaObject.datastore.find(Annotation.class).field("_id").equal(new ObjectId(id)).get();
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
	
	public static List<Annotation> findByRange(int start, int end)
	{
		List<Annotation> annotations = Annotation.allAnnotation();
		
		if(start < annotations.size())
		{
			if(end < annotations.size())
			{
				return annotations.subList(start, end);
			}
			else
			{
				return annotations.subList(start, annotations.size());
			}
		}
		else
			return annotations;
	}
	
	public static int getTotalAnnotations()
	{
		return Annotation.allAnnotation().size();
	}
	
	public static List<Annotation> findByAuthor(UserAccount author)
	{
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		if (MorphiaObject.datastore != null) {
			List<Resource> ressources = MorphiaObject.datastore.find(Resource.class).asList();
			for(Resource r : ressources) 
			{
				if(r instanceof Annotation && r.getAuthor().isSameUser(author))
				{
					annotations.add((Annotation)r);
				}
			}
			return annotations ;
		} else {
			return annotations ;
		}
	}
	
	public static List<Annotation> findByAuthor(UserAccount author, int limit)
	{
		List<Annotation> annotation = findByAuthor(author);
		if(annotation.size() - 10 < 0)
		{
			return annotation.subList(0, annotation.size());
		}
		else
		{
			return annotation.subList(annotation.size() - 10, annotation.size());
		}
	}
	
	@Override
	public boolean isArticle() {
		return false;
	}

	@Override
	public boolean isAnnotation() {
		return true;
	}
}