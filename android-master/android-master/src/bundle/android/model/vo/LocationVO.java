package bundle.android.model.vo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationVO {
    private String cityName;
    private String state;
    private String stateAbbrev;
    private String address;
    private String zipcode;
    private double latitude;
    private double longitude;
    
    protected List<Address> addresses;
    protected Geocoder gcd;

    public LocationVO(){

    }

    /**
     * LocationVO object from geolocation
     * @param location
     * @param context
     */
    public LocationVO(Location location, Context context){
        if(location!=null){
            Geocoder gcd = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = new ArrayList<Address>();
            try
            {
                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if (addresses.size() > 0){
                Address parseAddress= addresses.get(0);
                ///set state
                if(parseAddress.getAdminArea()!=null){
                   this.setState(parseAddress.getAdminArea());
                   this.setStateAbbrev(parseAddress.getAdminArea());
                }
                //set city
                if(parseAddress.getLocality()!=null)
                    this.setCityName(parseAddress.getLocality());
                else if(parseAddress.getSubLocality()!=null)
                    this.setCityName(parseAddress.getSubLocality());
                ///set zipcode
                if(parseAddress.getPostalCode()!=null)
                    this.setZipcode(parseAddress.getPostalCode());
                ///address
                if(parseAddress.getAddressLine(0)!=null)
                    this.setAddress(parseAddress.getAddressLine(0));
                ///set latitude and longitude
                this.setLatitude(parseAddress.getLatitude());
                this.setLongitude(parseAddress.getLongitude());
            }
        }
    }

    /**
     * LocationVO object from latitude, longitude
     * @param latitude
     * @param longitude
     * @param context
     */
    public LocationVO(final double latitude, final double longitude, Context context){
            gcd = new Geocoder(context, Locale.getDefault());
            addresses = new ArrayList<Address>();
            
            new Thread()
            {
              public void run()
              {
            	  try
                  {
                      addresses = gcd.getFromLocation(latitude, longitude, 1);
                      
                      if (addresses.size() > 0){
                          Address parseAddress= addresses.get(0);
                          ///set state
                          if(parseAddress.getAdminArea()!=null){
                              LocationVO.this.setState(parseAddress.getAdminArea());
                              LocationVO.this.setStateAbbrev(parseAddress.getAdminArea());
                          }
                          //set city
                          if(parseAddress.getLocality()!=null)
                        	  LocationVO.this.setCityName(parseAddress.getLocality());
                          else if(parseAddress.getSubLocality()!=null)
                        	  LocationVO.this.setCityName(parseAddress.getSubLocality());
                          ///set zipcode
                          if(parseAddress.getPostalCode()!=null)
                        	  LocationVO.this.setZipcode(parseAddress.getPostalCode());
                          ///address
                          if(parseAddress.getAddressLine(0)!=null)
                        	  LocationVO.this.setAddress(parseAddress.getAddressLine(0));
                          ///set latitude and longitude
                          LocationVO.this.setLatitude(parseAddress.getLatitude());
                          LocationVO.this.setLongitude(parseAddress.getLongitude());
                      }
                  }
                  catch (IOException e)
                  {
                      e.printStackTrace();
                  }
              }
            }.run();
           
            
    }

    /**
     * LocationVO object from address
     * @param addressString
     * @param context
     */
    public LocationVO(String addressString, Context context){
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = new ArrayList<Address>();
        try
        {
            addresses = gcd.getFromLocationName(addressString, 1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (addresses.size() > 0){
            Address parseAddress= addresses.get(0);
            ///set state
            if(parseAddress.getAdminArea()!=null){
                this.setState(parseAddress.getAdminArea());
                this.setStateAbbrev(parseAddress.getAdminArea());
            }
            //set city
            if(parseAddress.getLocality()!=null)
                this.setCityName(parseAddress.getLocality());
            else if(parseAddress.getSubLocality()!=null)
                this.setCityName(parseAddress.getSubLocality());
            ///set zipcode
            if(parseAddress.getPostalCode()!=null)
                this.setZipcode(parseAddress.getPostalCode());
            ///address
            if(parseAddress.getAddressLine(0)!=null)
                this.setAddress(parseAddress.getAddressLine(0));
            ///set latitude and longitude
            this.setLatitude(parseAddress.getLatitude());
            this.setLongitude(parseAddress.getLongitude());
        }
    }
    /**
     * LocationVO object from address
     * @param addressString
     * @param context
     */
    public LocationVO(String addressString, double botLat, double botLon, double topLat, double topLon, Context context){
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = new ArrayList<Address>();
        try
        {
            addresses = gcd.getFromLocationName(addressString, 1, botLat, botLon, topLat, topLon);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (addresses.size() > 0){
            Address parseAddress= addresses.get(0);
            ///set state
            if(parseAddress.getAdminArea()!=null){
                this.setState(parseAddress.getAdminArea());
                this.setStateAbbrev(parseAddress.getAdminArea());
            }
            //set city
            if(parseAddress.getLocality()!=null)
                this.setCityName(parseAddress.getLocality());
            else if(parseAddress.getSubLocality()!=null)
                this.setCityName(parseAddress.getSubLocality());
            ///set zipcode
            if(parseAddress.getPostalCode()!=null)
                this.setZipcode(parseAddress.getPostalCode());
            ///address
            if(parseAddress.getAddressLine(0)!=null)
                this.setAddress(parseAddress.getAddressLine(0));
            ///set latitude and longitude
            this.setLatitude(parseAddress.getLatitude());
            this.setLongitude(parseAddress.getLongitude());
        }
    }

    public void setCityName(String name)
    {
        this.cityName = name;
    }
    public String getCityName()
    {
        return cityName;
    }
    public void setState(String name)
    {
        this.state = name;
    }
    public String getState()
    {
        return state;
    }
    public void setStateAbbrev(String name)
    {
        this.stateAbbrev = abbreviatedState(name);
    }
    public String getStateAbbrev()
    {
        return stateAbbrev;
    }
    public void setAddress(String name)
    {
        this.address = name;
    }
    public String getAddress()
    {
        return address;
    }
    public void setZipcode(String name)
    {
        this.zipcode = name;
    }
    public String getZipcode()
    {
        return zipcode;
    }
    public void setLatitude(double coord)
    {
        this.latitude = coord;
    }
    public double getLatitude()
    {
        return latitude;
    }
    public void setLongitude(double coord)
    {
        this.longitude = coord;
    }
    public double getLongitude()
    {
        return longitude;
    }

    /**
     * Mapping states/provinces to PublicStuff abbreviations.
     * @param state
     * @return
     */
    public String abbreviatedState(String state){
       String [] map = new String[]{"Alabama", "AL", "Alaska", "AK", "Arizona", "AZ","Arkansas","AR","American Samoa","AS","California","CA","Colorado","CO","Connecticut","CT","Delaware","DE","District Of Columbia","DC","Federated States of Micronesia","FM","Florida","FL","Georgia","GA","Guam","GU","Hawaii","HI","Idaho","ID","Illinois","IL","Indiana","IN","Iowa","IA","Kansas","KS","Kentucky","KY","Louisiana","LA","Maine","ME","Maryland","MD","Massachusetts","MA","Michigan","MI","Minnesota","MN","Mississippi","MS","Missouri","MO","Marshall Islands","MH","Northern Mariana Islands","MP","Montana","MT","Nebraska","NE","Nevada","NV","New Hampshire","NH","New Jersey","NJ","New Mexico","NM","New York","NY","North Carolina","NC","North Dakota","ND","Ohio","OH","Oklahoma","OK","Oregon","OR","Palau","PW","Pennsylvania","PA","Puerto Rico","PR","Rhode Island","RI","South Carolina","SC","South Dakota","SD","Tennessee","TN","Texas","TX","Utah","UT","Vermont","VT","Virginia","VA","Virgin Islands","VI","Washington","WA","West Virginia","WV","Wisconsin","WI","Wyoming","WY", "Ontario", "ON"};
        for (int i = 0; i <map.length; i+=2) {
            if (state.equals(map[i])) {
                return map[i+1];
            }
        }
        return state;
    }
}
