package bundle.android.model.vo;

public class UserDetailVO {
    private int count;
    private int image;
    private String title;

    public UserDetailVO(){}
    public UserDetailVO(String title, int image, int count){
        setTitle(title);
        setImage(image);
        setCount(count);
    }

    public void setTitle(String t){
        title = t;
    }
    public String getTitle(){
        return title;
    }
    public void setCount(int c){
        count = c;
    }
    public int getCount(){
        return count;
    }
    public void setImage(int i){
        image = i;
    }
    public int getImage(){
        return image;
    }

}
