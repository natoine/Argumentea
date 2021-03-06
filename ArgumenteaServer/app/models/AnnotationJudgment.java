package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

@Entity("Resources")
public class AnnotationJudgment extends Annotation
{
	private String reformulation ;
	
	private String judgment ;

	public String getReformulation() {
		return reformulation;
	}

	public void setReformulation(String reformulation) {
		this.reformulation = reformulation;
	}

	public String getJudgment() {
		return judgment;
	}

	public void setJudgment(String judgment) 
	{
		this.judgment = judgment;
	}
}