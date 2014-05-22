package bundle.android.model.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class CustomFieldArrayVO implements Serializable, Parcelable {
    private ArrayList<CustomFieldVO> custFields;
    public CustomFieldArrayVO(){}

    public CustomFieldArrayVO(ArrayList<CustomFieldVO> customFields){
        setCustFields(customFields);
    }
    public void setCustFields(ArrayList<CustomFieldVO> customFieldVOs){
        custFields = customFieldVOs;
    }
    public ArrayList<CustomFieldVO> getCustFields(){
        return custFields;
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
    public static final Parcelable.Creator<CustomFieldArrayVO> CREATOR
            = new Parcelable.Creator<CustomFieldArrayVO>() {
        public CustomFieldArrayVO createFromParcel(Parcel in) {
            int description=in.readInt();
            Serializable s=in.readSerializable();
            switch(description)
            {
                case 0:
                    return (CustomFieldArrayVO )s;
                default:
                    return null;
            }
        }

        public CustomFieldArrayVO[] newArray(int size) {
            return new CustomFieldArrayVO[size];
        }
    };
}
