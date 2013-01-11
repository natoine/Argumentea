package models;


import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

@Entity
public class UserAccount 
{
	@Id
	private ObjectId id;
	@Required
	private String nickname ;
	private String firstname ;
	private String lastname ;
	@Email @Required
	private String email ;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public static List<UserAccount> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(UserAccount.class).asList();
		} else {
			return new ArrayList<UserAccount>();
		}
	}

	public static void create(UserAccount userAccount) {
		MorphiaObject.datastore.save(userAccount);
	}

	public static void delete(String idToDelete) {
		UserAccount toDelete = MorphiaObject.datastore.find(UserAccount.class).field("_id").equal(new ObjectId(idToDelete)).get();
		if (toDelete != null) {
			//Logger.info("toDelete: " + toDelete);
			MorphiaObject.datastore.delete(toDelete);
		} else {
			//Logger.debug("ID No Found: " + idToDelete);
		}
	}
}