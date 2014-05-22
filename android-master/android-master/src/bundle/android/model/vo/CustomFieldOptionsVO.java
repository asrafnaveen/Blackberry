package bundle.android.model.vo;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomFieldOptionsVO implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5593240788075538030L;
	int superid;
	int id;
	String name;
	String description;
	boolean checked;
	
	public CustomFieldOptionsVO(int superid, int id, String name, String description, boolean checked){
		setSuperid(superid);
		setId(id);
		setName(name);
		setDescription(description);
		setChecked(checked);
	}
	
	/**
	 * @return the superid
	 */
	public int getSuperid() {
		return superid;
	}

	/**
	 * @param superid the superid to set
	 */
	public void setSuperid(int superid) {
		this.superid = superid;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public void setId(int i)
	{
		this.id = i;
	}
	
	public void setName(String n)
	{
		this.name = n;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
