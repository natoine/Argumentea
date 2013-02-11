package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class Secured extends Security.Authenticator 
{
	@Override
	public String getUsername(Context ctx) {
	    return ctx.session().get("email");
	}
	
	@Override
	public Result onUnauthorized(Context ctx) {
	    return redirect(routes.Application.login());
	}
	
	/**
	 * Renvoi le hash (SHA1 de MD5) d'une chaine
	 * @param toHash chaine a hasher
	 * @return la chaine hashe
	 */
	public static String hash(String toHash)
	{
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			
			return new String(sha1.digest(md5.digest(toHash.getBytes())));
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
