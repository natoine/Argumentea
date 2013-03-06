package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

import controllers.MorphiaObject;
import controllers.Secured;

@Entity
public class UserAccount 
{
	@Id
	private ObjectId id;
	
	@Indexed(unique = true) @Required 
	private String nickname ;
	
	private String firstname ;
	
	private String lastname ;
	
	@Email @Required @Indexed(unique = true) 
	private String email ;
	
	@Required
	private String hashedPassword;
	
	private Date creationDate;

	
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

	public static Map<String,String> options() {
		List<UserAccount> uas = all();
		LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
		for(UserAccount ua: uas) {
			options.put(ua.id.toString(), ua.nickname);
		}
		return options;
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
	
	public static UserAccount findByNickname(String nickname)
	{
		return MorphiaObject.datastore.find(UserAccount.class).field("nickname").equal(nickname).get();
	}
	
	public static UserAccount findByMail(String mail)
	{
		return MorphiaObject.datastore.find(UserAccount.class).field("email").equal(mail).get();
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	
	public static UserAccount authenticate(String nickname, String password)
	{
		UserAccount user = MorphiaObject.datastore.find(UserAccount.class).field("nickname").equal(nickname).get();
		String hashPassword = Secured.hash(password);
		
		if(user != null && hashPassword != null)
		{
			if(user.getHashedPassword().equals(hashPassword))
			{
				return user;
			}
			else
				return null;
		}
		else
			return null;
	}
	
	public String validate()
	{
		Logger.info("Validate in UserAccont");
		return "ok";
	}
	
	public boolean isSameUser(UserAccount user)
	{
		return user.getId().equals(this.getId());
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}