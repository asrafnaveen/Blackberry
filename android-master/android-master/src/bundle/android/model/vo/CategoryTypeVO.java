package bundle.android.model.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class CategoryTypeVO implements Serializable, Parcelable {
	private int id;
	private int client;
	private String name;
	private String gov_creator;
	private String description;
	private String parent;
	private String date_created;
	public CategoryTypeVO(){}

	public CategoryTypeVO(int id, int client, String name, String gov_creator, String description, String parent, String date_created){
		setId(id);
		setClient(client);
		setName(name);
		setGovCreator(gov_creator);
		setDescription(description);
		setParent(parent);
		setDateCreated(date_created);
		
	}

	public void setId(int i){
		id = i;
	}
	public int getId(){
		return id;
	}
	
	public void setClient(int cl){
		client = cl;
	}
	
	public int getClient(){
		return client;
	}
	
	public void setName(String n){
		name = n;
	}
	
	public String getName(){
		return name;
	}
	
	public void setGovCreator(String gc){
		gov_creator = gc;
	}
	
	public String getGovCreator(){
		return gov_creator;
	}
	
	public void setDescription(String d){
		description = d;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setParent(String p){
		parent = p;
	}
	
	public String getParent(){
		return parent;
	}
	
	public void setDateCreated(String dc){
		date_created = dc;
	}
	
	public String getDateCreated(){
		return date_created;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(this.describeContents());
		parcel.writeSerializable(this);
	}
	public static final Parcelable.Creator<CategoryTypeVO> CREATOR
	= new Parcelable.Creator<CategoryTypeVO>() {
		public CategoryTypeVO createFromParcel(Parcel in) {
			int description=in.readInt();
			Serializable s=in.readSerializable();
			switch(description)
			{
			case 0:
				return (CategoryTypeVO )s;
			default:
				return null;
			}
		}

		public CategoryTypeVO[] newArray(int size) {
			return new CategoryTypeVO[size];
		}
	};
}
