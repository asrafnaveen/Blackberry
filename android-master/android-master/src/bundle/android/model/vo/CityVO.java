package bundle.android.model.vo;

public class CityVO
{
	private boolean isClient;
	private String cityName;
	private String cityState;
    private boolean selected;
    private int id;

	public CityVO()
	{
	}

	public CityVO(String cityName, String cityState, int id, boolean isClient, boolean isSelected)
	{
		this.isClient = isClient;
		this.cityName = cityName;
		this.cityState= cityState;
        this.id = id;
        this.selected = isSelected;
	}

    public boolean getIsClient()
	{
		return isClient;
	}

	public void setIsClient(boolean client)
	{
		isClient = client;
	}

	public String getCityName()
	{
		return cityName;
	}

	public void setCityName(String cityName)
	{
		this.cityName = cityName;
	}

	public String getCityStateAbbr()
	{
		return cityState;
	}

	public void setCityStateAbbr(String cityState)
	{
		this.cityState = cityState;
	}

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
    public boolean getSelected()
    {
        return selected;
    }

    public void setSelected(boolean s)
    {
        selected = s;
    }

}
