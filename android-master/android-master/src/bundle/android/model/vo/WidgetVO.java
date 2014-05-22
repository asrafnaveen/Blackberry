package bundle.android.model.vo;

import java.io.Serializable;

public class WidgetVO implements Serializable
{
	private int image;
	private String title;
    private int id;
    private String url;
    private String description;

	public WidgetVO()
	{
	}

	public WidgetVO(int imageResourceId, String title, int id)
	{
		setImage(imageResourceId);
		setTitle(title);
        setId(id);
	}

    public int getImage() {
        return image;
    }

    public void setImage(int i) {
        this.image = i;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getUrl(){
        return url;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String desc){
        description = desc;
    }
}
