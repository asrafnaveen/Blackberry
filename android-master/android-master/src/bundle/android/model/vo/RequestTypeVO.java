package bundle.android.model.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class RequestTypeVO extends ArrayList<RequestTypeVO> implements Serializable, Parcelable  {
	private int id;
	private String name;
	private String description;
	private boolean hasCustomField;
	private boolean isSelected = false;
	private String category_id;
	private String disable_title;
	private String force_private;
	private String disable_description;
	private boolean isCategory;
	
	public RequestTypeVO(){}

	public RequestTypeVO(int id, String name, boolean custom_field/*, String category_id, boolean disable_title, boolean force_private, boolean disable_description*/){
		setId(id);
		setName(name);
		setHasCustomField(custom_field);
		/*setCategoryId(category_id);
		setDisableTitle(disable_title);
		setForcePrivate(force_private);
		setDisableDescription(disable_description);*/
	}
	
	public void setId(int i){
		id = i;
	}
	public int getId(){
		return id;
	}
	public void setName(String n){
		name = n;
	}
	public String getName(){
		return name;
	}
	public void setDescription(String d){
		description = d;
	}
	public String getDescription(){
		return description;
	}
	public void setHasCustomField(boolean cf){
		hasCustomField = cf;
	}
	public boolean getHasCustomField(){
		return hasCustomField;
	}
	public void setSelected(boolean b){
		isSelected = b;
	}
	public boolean getSelected(){
		return isSelected;
	}
	
	public void setCategoryId(String cat){
		category_id = cat;
	}
	
	public String getCategoryId(){
		return category_id;
	}
	
	public void setDisableTitle(String disableTitle){
		disable_title = disableTitle;
	}
	
	public String getDisableTitle(){
		return disable_title;
	}
	
	public void setForcePrivate(String forcePrivate){
		force_private = forcePrivate;
	}

	public String getForcePrivate(){
		return force_private;
	}
	
	public void setDisableDescription(String disableDescription){
		disable_description = disableDescription;
	}
	
	public String getDisableDescription(){
		return disable_description;
	}

	public void setIsCategory(boolean isCat){
		isCategory = isCat;
	}
	
	public boolean getIsCategory(){
		return isCategory;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((category_id == null) ? 0 : category_id.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime
				* result
				+ ((disable_description == null) ? 0 : disable_description
						.hashCode());
		result = prime * result
				+ ((disable_title == null) ? 0 : disable_title.hashCode());
		result = prime * result
				+ ((force_private == null) ? 0 : force_private.hashCode());
		result = prime * result + (hasCustomField ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result + (isCategory ? 1231 : 1237);
		result = prime * result + (isSelected ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestTypeVO other = (RequestTypeVO) obj;
		if (category_id == null) {
			if (other.category_id != null)
				return false;
		} else if (!category_id.equals(other.category_id))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (disable_description == null) {
			if (other.disable_description != null)
				return false;
		} else if (!disable_description.equals(other.disable_description))
			return false;
		if (disable_title == null) {
			if (other.disable_title != null)
				return false;
		} else if (!disable_title.equals(other.disable_title))
			return false;
		if (force_private == null) {
			if (other.force_private != null)
				return false;
		} else if (!force_private.equals(other.force_private))
			return false;
		if (hasCustomField != other.hasCustomField)
			return false;
		if (id != other.id)
			return false;
		if (isCategory != other.isCategory)
			return false;
		if (isSelected != other.isSelected)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(this.describeContents());
		parcel.writeSerializable(this);
	}
	public static final Parcelable.Creator<RequestTypeVO> CREATOR
	= new Parcelable.Creator<RequestTypeVO>() {
		public RequestTypeVO createFromParcel(Parcel in) {
			int description=in.readInt();
			Serializable s=in.readSerializable();
			switch(description)
			{
			case 0:
				return (RequestTypeVO )s;
			default:
				return null;
			}
		}

		public RequestTypeVO[] newArray(int size) {
			return new RequestTypeVO[size];
		}
	};
}
