package models;


import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

@Entity
public class UserAccount 
{
	@Id
	public ObjectId id;
	@Required
	public String nickname;

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
	
}
