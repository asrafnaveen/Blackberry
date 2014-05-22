package bundle.android.model.vo;

import com.wassabi.psmobile.R;

import java.io.Serializable;

public class RequestListVO implements Serializable {
    private int id;
    private String image = "";
    private String title;
    private String address;
    private String status;
    private int statusImage;
    private int followersCount;
    private String distance;
    private int commentsCount;
    private boolean userFollowing = false;
    private boolean userComment = false;
    private boolean userRequest = false;
    private double latitude;
    private double longitude;

    public RequestListVO(){

    }

    public void setId(int i){
        this.id = i;
    }
    public int getId(){
        return this.id;
    }
    public void setImage(String i){
        this.image = i;
    }
    public String getImage(){
        return this.image;
    }

    public void setTitle(String t){
        this.title = t;
    }
    public String getTitle(){
        return this.title;
    }

    public void setAddress(String a){
        this.address = a;
    }
    public String getAddress(){
        return this.address;
    }

    public void setStatus(String s){
        if(s.equals("submitted")){
            this.status = "Submitted";
            setStatusImage(R.drawable.ic_status_small_1);
        }
        else if (s.equals("received")){
            this.status = "Acknowledged";
            setStatusImage(R.drawable.ic_status_small_2);
        }
        else if(s.equals("in progress")){
            this.status = "In Progress";
            setStatusImage(R.drawable.ic_status_small_3);
        }
        else if(s.equals("completed")){
            this.status = "Completed";
            setStatusImage(R.drawable.ic_status_small_4);
        }
        else{
            this.status = "Other";
            setStatusImage(R.drawable.ic_status_small_5);
        }
    }
    public String getStatus(){
        return this.status;
    }

    private void setStatusImage(int r){
        this.statusImage = r;
    }
    public int getStatusImage(){
        return this.statusImage;
    }

    public void setFollowersCount(int c){
        this.followersCount = c;
    }
    public int getFollowersCount(){
        return this.followersCount;
    }

    public void setDistance(String d){
        this.distance = d;
    }
    public String getDistance(){
        return this.distance;
    }

    public void setCommentsCount(int c){
        this.commentsCount = c;
    }
    public int getCommentsCount(){
        return this.commentsCount;
    }

    public void setUserFollowing(boolean b){
        this.userFollowing = b;
    }
    public boolean getUserFollowing(){
        return this.userFollowing;
    }

    public void setUserRequest(boolean b){
        this.userRequest = b;
    }
    public boolean getUserRequest(){
        return this.userRequest;
    }

    public void setUserComment(boolean b){
        this.userComment = b;
    }
    public boolean getUserComment(){
        return this.userComment;
    }

    public void setLatitude(double l){
        this.latitude = l;
    }
    public double getLatitude(){
        return this.latitude;
    }

    public void setLongitude(double l){
        this.longitude = l;
    }
    public double getLongitude(){
        return this.longitude;
    }


}
