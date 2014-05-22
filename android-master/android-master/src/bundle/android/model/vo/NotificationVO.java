package bundle.android.model.vo;

import java.io.Serializable;

public class NotificationVO implements Serializable{
    private int requestId;
    private boolean isComment = false;
    private boolean isCommentReply = false;
    private boolean isStatusUpdate = false;
    private boolean read = false;
    private long date;
    private String status;

    public NotificationVO(){}

    public NotificationVO(int requestId, long date){
       setRequestId(requestId);
       setDate(date);
    }

    public void setRequestId(int i){
        requestId = i;
    }
    public int getRequestId(){
        return requestId;
    }
    public void setIsComment(boolean b){
         isComment = b;
    }
    public boolean getIsComment(){
        return isComment;
    }
    public void setIsCommentReply(boolean b){
        isCommentReply = b;
    }
    public boolean getIsCommentReply(){
        return isCommentReply;
    }
    public void setIsStatusUpdate(boolean b){
        isStatusUpdate = b;
    }
    public boolean getIsStatusUpdate(){
        return isStatusUpdate;
    }
    public void setRead(boolean b){
        read =b;
    }
    public boolean getRead(){
        return read;
    }
    public void setDate(long d){
        date = d;
    }
    public long getDate(){
        return date;
    }
    public void setStatus(String s){
        status = s;
    }
    public String getStatus(){
        return status;
    }


}
