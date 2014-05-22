package bundle.android.model.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class RequestTypeListVO  implements Parcelable, Serializable {
    private ArrayList<RequestTypeVO> requestTypeList = new ArrayList<RequestTypeVO>();
    public RequestTypeListVO(){

    }
    public RequestTypeListVO(ArrayList<RequestTypeVO>list){
         requestTypeList = list;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.describeContents());
        parcel.writeSerializable(this.requestTypeList);
    }
    public static final Parcelable.Creator<RequestTypeListVO> CREATOR
            = new Parcelable.Creator<RequestTypeListVO>() {
        public RequestTypeListVO createFromParcel(Parcel in) {
            int description=in.readInt();
            Serializable s=in.readSerializable();
            switch(description)
            {
                case 0:
                    return (RequestTypeListVO )s;
                default:
                    return null;
            }
        }

        public RequestTypeListVO[] newArray(int size) {
            return new RequestTypeListVO[size];
        }
    };
}
