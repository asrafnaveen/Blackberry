package bundle.android.model.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;

public class CustomFieldVO implements Serializable, Parcelable {
    private String name;
    private String description;
    private int id;
    private String type;
    private boolean isRequired = false;
    private boolean isPublic = false;
    private String value;
    private ArrayList<CustomFieldOptionsVO> options;

    public CustomFieldVO(){}

    public CustomFieldVO(int id, String name, String type, boolean isRequired, boolean isPublic, ArrayList<CustomFieldOptionsVO> options){
       setId(id);
        setName(name);
        setType(type);
        setRequired(isRequired);
        setPublic(isPublic);
        setOptions(options);
    }
    
    public void setOptions(ArrayList<CustomFieldOptionsVO> s){
    	options = s;
    }
    
    public ArrayList<CustomFieldOptionsVO> getOptions(){
    	return options;
    }

    public void setName(String s){
        name = s;
    }
    public String getName(){
        return name;
    }
    public void setDescription(String s){
        description = s;
    }
    public String getDescription(){
        return description;
    }

    public void setId(int i){
        id = i;
    }
    public int getId(){
        return id;
    }

    public void setType(String s){
        type = s;
    }
    public String getType(){
        return type;
    }
    public void setRequired(boolean b){
       isRequired = b;
    }
    public boolean getRequired(){
        return isRequired;
    }
    public void setPublic(boolean b){
        isPublic = b;
    }
    public boolean getPublic(){
        return isPublic;
    }

    public void setValue(String s){
        value = s;
    }
    public String getValue(){
        return value;
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
    public static final Parcelable.Creator<CustomFieldVO> CREATOR
            = new Parcelable.Creator<CustomFieldVO>() {
        public CustomFieldVO createFromParcel(Parcel in) {
            int description=in.readInt();
            Serializable s=in.readSerializable();
            switch(description)
            {
                case 0:
                    return (CustomFieldVO )s;
                default:
                    return null;
            }
        }

        public CustomFieldVO[] newArray(int size) {
            return new CustomFieldVO[size];
        }
    };
}
