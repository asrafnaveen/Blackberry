package bundle.android.model.vo;


public class ImageDialogVO
{
    private int image;
    private String title;
    private String subTitle;
    private boolean selected;

    public ImageDialogVO(){

    }

    public ImageDialogVO(int image, String title, String subTitle, boolean selected){
        setImageView(image);
        setTitle(title);
        setSubTitle(subTitle);
        setSelected(selected);
    }
    public void setImageView(int id)
    {
        this.image = id;
    }
    public int getImage()
    {
        return image;
    }
    public void setTitle(String name)
    {
        this.title = name;
    }
    public String getTitle()
    {
        return title;
    }
    public void setSubTitle(String name)
    {
        this.subTitle = name;
    }
    public String getSubTitle()
    {
        return subTitle;
    }
    public void setSelected(boolean selected){
        this.selected = selected;
    }
    public boolean getSelected(){
        return selected;
    }

}