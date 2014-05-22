package bundle.android.model.vo;


public class ApiStatusVO {
    private String type;
    private String message;
    private int code;
    private String codeMessage;


    public void setMessage(String message)
    {
        this.message = message;
    }
    public String getMessage()
    {
        return message;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public String getType()
    {
        return type;
    }
    public void setCode(int c)
    {
        this.code = c;
    }
    public int getCode()
    {
        return code;
    }
    public void setCodeMessage(String cm)
    {
        this.codeMessage = cm;
    }
    public String getCodeMessage()
    {
        return codeMessage;
    }


}
