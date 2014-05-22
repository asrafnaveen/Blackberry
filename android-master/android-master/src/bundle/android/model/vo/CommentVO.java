package bundle.android.model.vo;

import java.io.Serializable;

public class CommentVO implements Serializable
{
    private String image;
    private String user;
    private String date;
    private String text;

    public CommentVO(){

    }

    public CommentVO(String image, String user, String date, String text){
        setImage(image);
        setUser(user);
        setDate(date);
        setText(text);
    }
    public void setImage(String img)
    {
        this.image = img;
    }
    public String getImage()
    {
        return image;
    }
    public void setUser(String name)
    {
        this.user = name;
    }
    public String getUser()
    {
        return user;
    }
    public void setDate(String d)
    {
        this.date = d;
    }
    public String getDate()
    {
        return date;
    }
    public void setText(String t){
        this.text = t;
    }
    public String getText(){
        return text;
    }

}
