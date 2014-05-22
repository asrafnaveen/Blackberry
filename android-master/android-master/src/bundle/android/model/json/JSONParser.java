package bundle.android.model.json;

import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.vo.ApiStatusVO;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser
{
    private JSONObject jsonObject;

    public JSONParser(String theResponse)
            throws JSONException
    {
        jsonObject = new JSONObject(theResponse);
    }


    //public abstract void parse() throws JSONException;


    public void setJsonObject(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject()
    {
        return jsonObject;
    }

    public ApiStatusVO getStatus()
            throws JSONException
    {
        JSONObject response = getResponse();
        JSONObject status =response.getJSONObject(ApiResponseHelper.STATUS);
        String statusType = status.getString(ApiResponseHelper.STATUS_TYPE);
        String statusMessage = status.getString(ApiResponseHelper.STATUS_MESSAGE);
        int code = status.getInt(ApiResponseHelper.STATUS_CODE);
        String codeMessage = status.getString(ApiResponseHelper.STATUS_CODE_MESSAGE);

        ApiStatusVO vo = new ApiStatusVO();
        vo.setType(statusType);
        vo.setMessage(statusMessage);
        vo.setCode(code);
        vo.setCodeMessage(codeMessage);

        return vo;
    }

    public JSONObject getResponse()
            throws JSONException
    {
        return getJsonObject().getJSONObject(ApiResponseHelper.RESPONSE);
    }

}