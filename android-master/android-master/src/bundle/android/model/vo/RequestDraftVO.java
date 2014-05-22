package bundle.android.model.vo;

import java.io.Serializable;

public class RequestDraftVO implements Serializable {
    private String title;
    private String description;
    private RequestTypeVO requestType;
    private String address;
    private String city;
    private String state;
    private String zipcode;
    private double latitude;
    private double longitude;
    private boolean isPrivate;
    private String pathToImage;
    private int spaceId;
    private int clientId;
    private String locationString;
    private boolean titleChanged;
    private CustomFieldArrayVO customFields;
    private int uniqueID;
    public RequestDraftVO(){
        setUniqueID();
    }
    public void setUniqueID(){
        uniqueID = (int) ((Math.random()*1000000)+1);
    }
    public int getUniqueID(){
        return uniqueID;
    }
    public void setTitle(String t){
        title = t;
    }
    public String getTitle(){
        return title;
    }
    public void setDescription(String d){
        description =d;
    }
    public String getDescription(){
        return description;
    }
    public void setRequestType(RequestTypeVO rt){
        requestType = rt;
    }
    public RequestTypeVO getRequestType(){
        return requestType;
    }
    public void setAddress(String add){
        address = add;
    }
    public String getAddress(){
        return address;
    }
    public void setCity(String c){
        city = c;
    }
    public String getCity(){
        return city;
    }
    public void setState(String st){
        state =st;
    }
    public String getState(){
        return state;
    }
    public void setZipcode(String zip){
        zipcode = zip;
    }
    public String getZipcode(){
        return zipcode;
    }
    public void setLatitude(double lat){
        latitude = lat;
    }
    public double getLatitude(){
        return latitude;
    }
    public void setLongitude(double lon){
        longitude = lon;
    }
    public double getLongitude(){
        return longitude;
    }
    public void setPrivate(boolean p){
        isPrivate = p;
    }
    public boolean getPrivate(){
        return isPrivate;
    }
    public void setPathToImage(String s){
        pathToImage = s;
    }
    public String getPathToImage(){
        return pathToImage;
    }
    public void setSpaceId(int id){
        spaceId = id;
    }
    public int getSpaceId(){
        return spaceId;
    }
    public void setClientId(int id){
        clientId = id;
    }
    public int getClientId(){
        return clientId;
    }
    public void setLocationString(String ls){
        locationString = ls;
    }
    public String getLocationString(){
        return locationString;
    }
    public void setTitleChanged(boolean b){
        titleChanged = b;
    }
    public boolean getTitleChanged(){
        return titleChanged;
    }
    public void setCustomFields(CustomFieldArrayVO customFieldVOs){
        customFields = customFieldVOs;
    }
    public CustomFieldArrayVO getCustomFields(){
        return customFields;
    }

}
