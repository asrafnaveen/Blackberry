package bundle.android.views;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bundle.android.PublicStuffApplication;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;


public class PrivacyActivity extends PsActivity {
    private PublicStuffApplication app;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.policy);
        app = (PublicStuffApplication)getApplicationContext();
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        
        FlurryAgent.logEvent("Privacy Policy");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Privacy Policy");
        initElements();
    }
    private void initElements(){
        RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
        headerLayoutView.setBackgroundColor(app.getNavColor());

        ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
        if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
            AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
            imageLoader.execute(new Void[0]);
        }
        else{
            cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
        }
        TextView textView = (TextView) this.findViewById(R.id.largeText);
        textView.setText(Html.fromHtml(privacyPolicy));
        TextView title = (TextView) this.findViewById(R.id.listViewTitle);
        title.setText(PrivacyActivity.this.getString(R.string.privacyPolicy));
    }

    public void back(View v){
        finish();
    }

    private final String privacyPolicy = "" +
            " <div><p>PublicStuff has a focus on both privacy and security. We recognize that the information being held by PublicStuff is sensitive and it is treated as such. With user privacy and security in mind we created the following principles for PublicStuff:</p>" +
            " <ul>" +
            " <li>We will never sell or rent your personally identifiable information to third parties for marketing purposes</li> " +
            " <li>We will never share your contact information with another user without consent</li> " +
            " <li>All personally identifiable information you provide will be secured using industry standard protocols and technology</li> " +
            " <li>Users have the ability to change what is public but the default setting should err on the side of less public disclosure rather than more</li> " +
            " </ul>" +
            " " +
            " <h4>Personal Information Collected</h4>" +
            " <p>The primary method of collecting personal information is through forms and the registration process. PublicStuff requires a large amount of personal information during the registration process in order to ensure the users information is accurate and allowing PublicStuff to verify the information when necessary. By default, this information is not displayed, however users above the age of 18 can elect to make some of their personal information public.</p>" +
            " " +
            " <h4>Cookies</h4>" +
            " <p>Like most web sites, PublicStuff uses cookies and web log files to track site usage. A cookie is a tiny data file which resides on your computer which allows PublicStuffy to recognize you as a user when you return to our site using the same computer and web browser. Unfortunately, if your browser settings do not allow cookies, you will not be able to use our website. Like the information you enter at registration or in your Profile, cookie and log file data is used to customize your experience on the web site.</p>" +
            " <p>We use cookies to improve the quality of our service by storing user preferences and tracking user trends. In the course of serving advertisements or optimizing services to our users, we may allow authorized third parties to place or recognize a unique cookie on your browser. Any information provided to third parties through such cookies will not be personally identifiable but may provide general segment information, e.g. your industry or geographic location, for greater customization of your user experience, Most browsers are initially set up to accept cookies, but you can reset your browser to refuse all cookies or to indicate when a cookie is being sent. Unfortunately, if your browser settings do not allow cookies or you opt to refuse all cookies, you may not be able to use our website or services. PublicStuff does not store personally identifiable information in the cookies.</p>" +
            " " +
            " <h4>Log files, IP addresses and information about your computer</h4>" +
            " <p>Due to the communications standards on the Internet, when you visit the PublicStuff web site we automatically receive the URL of the site from which you came and the site to which you are going when you leave PublicStuff. We also receive the Internet protocol (IP) address of your computer (or the proxy server you use to access the World Wide Web), your computer operating system and type of web browser you are using, email patterns, as well as the name of your ISP. This information is used to analyze overall trends to help us improve the PublicStuff service. The linkage between your IP address and your personally identifiable information is never shared with third-parties without your permission or except when required by law.</p>" +
            " " +
            " <h4>Legal Disclaimer</h4>" +
            " <p>It is possible that we may need to disclose personal information when required by law. We will disclose such information wherein we have a good-faith belief that it is necessary to comply with a court order, ongoing judicial proceeding, or other legal process served on our company or to exercise our legal rights or defend against legal claims.</p>" +
            " " +
            " <h4>Disclosures to others:</h4>" +
            " <p>We may also disclose your personal and other information you provide, to another third party as part of a reorganization or a sale of the assets of a PublicStuff corporation division or company and only after taking steps to ensure that your privacy rights continue to be protected. Any third party to which we transfer or sell PublicStuff's assets will have the right to continue to use the personal and other information that you provide to us.</p>" +
            " " +
            " <h4>Important Information</h4>" +
            " <h5>1. Changes to this Privacy Policy</h5>" +
            " <p>PublicStuff may update this privacy policy. In the event there are significant changes in the way we treat your personally identifiable information, we will display a notice on this site. Unless stated otherwise, our current Privacy Policy applies to all information that we have about you and your account. If you continue to use the PublicStuff service after notice of changes have been sent to you or published on our site, you hereby provide your consent to the changed practices.</p>" +
            " <h5>2. Security</h5>" +
            " <p>In order to secure your personal information, access to your data on PublicStuff is password-protected, and sensitive data (such as credit card information) is protected by SSL encryption when it is exchanged between your web browser and our web site. To protect any data you store on our servers we also regularly audit our system for possible vulnerabilities and attacks and we use a tier-one secured-access data center. It is your responsibility to protect the security of your login information.</p>" +
            " <h5>3. How to contact us</h5>" +
            " <p>If you have questions or comments about this privacy policy, please email us at: mailto:ps@publicstuff.org>ps@publicstuff.org</a></p></div>\"";
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}
