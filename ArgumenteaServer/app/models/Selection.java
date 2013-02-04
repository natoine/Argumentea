package models;

import java.util.Date;

import org.bson.types.ObjectId;

import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;

@Entity("Selection")
public class Selection {

	@Id
	private ObjectId id;
	@Reference
	private UserAccount creator ; // the guy that made the selection
	@Required
	private String selectedContent ;
	private Date creationDate ;
	@Reference @Required
	private Resource origin ;
	private String pointerBegin ; //du coup pointer mériterait d'être une classe avec sa propre méthode equals et une hierarchie de sous types
	private String pointerEnd ;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public UserAccount getCreator() {
		return creator;
	}
	public void setCreator(UserAccount creator) {
		this.creator = creator;
	}
	public String getSelectedContent() {
		return selectedContent;
	}
	public void setSelectedContent(String selectedContent) {
		this.selectedContent = selectedContent;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Resource getOrigin() {
		return origin;
	}
	public void setOrigin(Resource origin) {
		this.origin = origin;
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
	
	public static void createSelection(Selection selection)
	{
		selection.setCreationDate(new Date());
		MorphiaObject.datastore.save(selection);
	}
	
	public static void delete(String idToDelete) {
		Selection toDelete = MorphiaObject.datastore.find(Selection.class).field("_id").equal(new ObjectId(idToDelete)).get();
		if (toDelete != null) {
			//Logger.info("toDelete: " + toDelete);
			MorphiaObject.datastore.delete(toDelete);
		} else {
			//Logger.debug("ID No Found: " + idToDelete);
		}
	}
	
	public static Selection findById(String id)
	{
		return MorphiaObject.datastore.find(Selection.class).field("_id").equal(new ObjectId(id)).get();
	}
}
