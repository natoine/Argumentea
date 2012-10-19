package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class UserAccount extends Model
{
	@Id
	public Long id;
	
	@Required
	public String nickName;
	
	@Email @Required
	public String mail;
}
