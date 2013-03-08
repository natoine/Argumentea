package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;

@Entity
public class PwdRecoveryHolder 
{	
	@Id
	private ObjectId id;

	@Reference
	public UserAccount user;
	
	public Date expireDate;
	
	public String randomHash;
	
	
	public static PwdRecoveryHolder findByHash(String hash)
	{
		return MorphiaObject.datastore.find(PwdRecoveryHolder.class).field("randomHash").equal(hash).get();
	}
	
	public static boolean alreadyExists(UserAccount user)
	{
		PwdRecoveryHolder usr = MorphiaObject.datastore.find(PwdRecoveryHolder.class).field("user").equal(user).get();
		
		if(usr.getExpireDate().after(new Date()))
			return true;
		else
		{
			PwdRecoveryHolder.delete(usr.getId());
			return false;
		}
	}
	
	public static List<PwdRecoveryHolder> all() 
	{
		if (MorphiaObject.datastore != null) 
		{
			return MorphiaObject.datastore.find(PwdRecoveryHolder.class).asList();
		}
		else
		{
			return new ArrayList<PwdRecoveryHolder>();
		}
	}

	public static void create(PwdRecoveryHolder pwdRecovery) 
	{
		MorphiaObject.datastore.save(pwdRecovery);
	}
	
	public static void delete(ObjectId idToDelete) 
	{
		PwdRecoveryHolder toDelete = MorphiaObject.datastore.find(PwdRecoveryHolder.class).field("_id").equal(idToDelete).get();
		if (toDelete != null) 
		{
			MorphiaObject.datastore.delete(toDelete);
		}
	}

	public static void delete(String idToDelete) 
	{
		PwdRecoveryHolder toDelete = MorphiaObject.datastore.find(PwdRecoveryHolder.class).field("_id").equal(new ObjectId(idToDelete)).get();
		if (toDelete != null) 
		{
			MorphiaObject.datastore.delete(toDelete);
		}
	}

	public UserAccount getUser() {
		return user;
	}

	public void setUser(UserAccount user) {
		this.user = user;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getRandomHash() {
		return randomHash;
	}

	public void setRandomHash(String randomHash) {
		this.randomHash = randomHash;
	}
	
}
