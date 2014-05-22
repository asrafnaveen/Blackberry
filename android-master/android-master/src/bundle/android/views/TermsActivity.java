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


public class TermsActivity extends PsActivity {
    private PublicStuffApplication app;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.policy);
        app = (PublicStuffApplication)getApplicationContext();
        DataStore appStore = new DataStore(app);
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();
        
        FlurryAgent.logEvent("Terms and Conditions");
        appStore.saveToPrefs("currentEvent", "Terms and Conditions");
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
        textView.setText(Html.fromHtml(terms));
        TextView title = (TextView) this.findViewById(R.id.listViewTitle);
        title.setText(TermsActivity.this.getString(R.string.ts));
    }

    public void back(View v){
        finish();
    }
    private final String terms = "" +
            "<div><p>READ THE FOLLOWING TERMS AND CONDITIONS CAREFULLY BEFORE USING THIS SITE. BY USING OR ACCESSING THIS SITE, YOU ACKNOWLEDGE THAT YOU HAVE READ THESE TERMS OF USE (\"AGREEMENT\"), AND THAT YOU ACCEPT AND WILL BE LEGALLY BOUND BY THE AGREEMENT.</p>" +
            " " +
            " <h5>Copyright and Trademark Notice</h5>" +
            " <p>The contents of this site are Copyright 2005 - 2012 PublicStuff Inc. All Rights Reserved. \"PublicStuff\", \"Powered by PublicStuff\", and the PublicStuff logo, are trademarks and/or service marks of PublicStuff Inc. Unless otherwise noted on the Site, all other trademarks, service marks, and logos used in this Site are the trademarks, service marks or logos of their respective owners.</p>" +
            " <h3>License Grant & Ownership by PublicStuff</h3>" +
            " <h4>License.</h4>" +
            " <p>Subject to the terms and conditions of this Agreement, and until termination of the Agreement, PublicStuff grants you a non-exclusive, non-transferable, limited license to view or print the Content in this Site without alterations, for personal, non-commercial use only. This limited license does not apply to any media or platform other than that of the current Site.</p>" +
            " <h4>Ownership.</h4>" +
            " <p>All Content on the Site is (and shall continue to be) owned exclusively by PublicStuff or others, and is protected under applicable copyrights, patents, trademarks, trade dress, and/or other proprietary rights, and the copying, redistribution, use or publication by you of any such Content or any part of the Site is prohibited. Under no circumstances will you acquire any ownership rights or other interest in any Content by or through your Site Use.</p>" +
            " <h4>Restrictions on Use</h4>" +
            " <p>Prohibited Acts. Concerning your Site Use or any Content, you agree not to knowingly: (i) use any device, software or technique to interfere with or attempt to interfere with the proper working of the Site; (ii) post or transmit to the Site any unlawful, fraudulent, harassing, libelous, or obscene Information of any kind; (iii) post or send to the Site any Information that contains a virus, bug, or other harmful item; (iv) publish, perform, distribute, prepare derivative works, copy, reverse engineer, or use the Content (other than as expressly permitted herein); (v) post or transmit into or on the Site any Information in violation of another party's copyright or intellectual property rights; (vi) take any action which imposes an unreasonable or disproportionately large load on PublicStuff's infrastructure; (vii) redeliver any of the Content using \"framing\", hyperlinks, or other technology without PublicStuff's express written permission; (viii) use any device or technology to provide repeated automated attempts to access password-protected portions of the Site; (ix) transmit or post any Content which discloses private or personal matters concerning any person; (x) interfere, in any way, with Others using this Site; (xi) transmit or post charity requests, petitions for signatures, chain letters or letters relating to pyramid schemes; or (xii) transmit or post off topic Content. </p>" +
            " <h4>Right to Regulate & Legal Compliance.</h4>" +
            " <p>You acknowledge that PublicStuff has the right, but not the obligation, to monitor the Site and to disclose any Information necessary to operate the Site, to protect PublicStuff, Others, and PublicStuff's customers, and to comply with legal obligations or governmental requests. PublicStuff reserves the right to refuse to post or to remove any Information on the Site, in whole or in part, for any reason. </p>" +
            " " +
            " <p>Law Compliance. You agree to comply with all governmental laws, statutes, ordinances, and regulations (including unfair competition, anti-discrimination or false advertising) regarding your Site Use.</p>" +
            " " +
            " <h4>Your Site Use Activities</h4>" +
            " <p>Password-Protected Areas. If you are allowed access to password-protected areas of the Site, you agree to keep your password confidential and to send Notice to PublicStuff within 24 hours if your password is compromised. You acknowledge that PublicStuff neither endorses nor is affiliated with any Linked-Site and is not responsible for any information that appears on the Linked-Site. You acknowledge that (i) the Internet is a network of computers worldwide, and that any Information submitted by you to PublicStuff necessarily is routed via third party computers to PublicStuff, (ii) PublicStuff is not responsible for lapses in on-line security and does not assume liability for improper use of your Information by a third party.</p>" +
            " " +
            " <h4>Submissions of Information by You</h4>" +
            " <p>Grant of License to PublicStuff. If you submit Information to the Site, you grant PublicStuff a non-exclusive, worldwide, royalty-free license (in any media now known or not currently known or invented) to link to, utilize, use, and prepare derivative works of the submitted Information. No Information you submit shall be deemed confidential. However, PublicStuff agrees to use your Information in accordance with PublicStuff's Privacy Policy applicable to personally identifiable user data. When you post Information to the Site, you warrant that you have the necessary rights to post this Information and grant other the right to use this Information under applicable copyright, patent, trademark and trade secret laws. You agree to indemnify, defend and hold PublicStuff and Others harmless from and against any claim that you did not have these rights. NOTWITHSTANDING THE FOREGOING, YOU RETAIN OWNERSHIP OF ANY COPYRIGHTS OR OTHER INTELLECTUAL PROPERTY RIGHTS APPLICABLE TO ANY INFORMATION YOU SUBMIT TO PUBLICSTUFF.</p>" +
            " " +
            " <h4>Applicability & Cooperation</h4>" +
            " <p>PublicStuff reserves the right to limit the provision of any product or service to any person, geographic area or jurisdiction as it so desires, or as required by law. PublicStuff in its sole discretion may add, delete or change the Content at any time, without notice to you.</p>" +
            " " +
            " <h4>Limited Warranty</h4>" +
            " <p>DISCLAIMER OF WARRANTY. PUBLICSTUFF AND ALL CONTENT PROVIDERS MAKE NO REPRESENTATION ABOUT THE SUITABILITY OF THE CONTENT ON THIS SITE. THIS SITE, INCLUDING ANY ACTIVITIES, SERVICES AND PRODUCTS OFFERED THROUGH THIS SITE), AND ACCESS TO ANY LINKED-SITE, IS PROVIDED TO YOU BY ALL CONTENT PROVIDERS \"AS IS\" AND \"AS AVAILABLE\", WITH NO REPRESENTATIONS OR WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AVAILABILITY OF THE APPLICATION, RELIABILITY, ACCURACY, TITLE AND NON-INFRINGEMENT. YOU HEREBY DISCLAIM ALL WARRANTIES BY PUBLICSTUFF RELATING TO YOUR SITE USE. YOU ACKNOWLEDGE THAT YOUR ACCESS TO THE SITE WILL NOT BE FREE OF INTERRUPTIONS, THAT THE INFORMATION HEREIN MAY CONTAIN BUGS, ERRORS, TECHNICAL INACCURACIES, PROBLEMS OR OTHER LIMITATIONS, AND THAT THE SITE MAY BE UNAVAILABLE FROM TIME TO TIME. YOU ASSUME TOTAL RESPONSIBILITY AND RISK FOR YOUR SITE USE AND SITE-RELATED SERVICES. </p>" +
            " " +
            " <h4>LIMITATION OF LIABILITY</h4>" +
            " <p>UNDER NO CIRCUMSTANCES WILL PUBLICSTUFF OR ANY OF THE CONTENT PROVIDERS BE LIABLE OR RESPONSIBLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, CONSEQUENTIAL (INCLUDING DAMAGES FROM LOSS OF BUSINESS, LOST PROFITS, LITIGATION, OR THE LIKE), SPECIAL, EXEMPLARY, PUNITIVE OR OTHER DAMAGES, UNDER ANY LEGAL THEORY, ARISING OUT OF OR IN ANY WAY RELATING TO THE SITE (OR ANY ACTIVITIES, SERVICES OR PRODUCTS OFFERED THROUGH THIS SITE), YOUR SITE USE, OR THE CONTENT, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. YOUR SOLE REMEDY FOR DISSATISFACTION WITH THE SITE AND/OR CONTENT IS TO CEASE ALL OF YOUR SITE USE. </p>" +
            " " +
            " <p>You may have additional rights under certain laws (including consumer laws) which do not allow the exclusion of implied warranties, or the exclusion or limitation of certain damages. If these laws apply to you, the exclusions or limitations in this Agreement that directly conflict with such laws may not apply to you.</p>" +
            " " +
            " <h4>Contacting PublicStuff</h4>" +
            " <p>We're always interested in your opinions and feedback. We hope you'll contact us at team@PublicStuff.com if you have any questions or ideas about our company or these Terms and Conditions of Use.</p>" +
            " " +
            " <h4>Miscellaneous</h4>" +
            " <p>Location & Interpretation. The language in this Agreement shall be interpreted as to its fair meaning and not strictly for or against any party. </p>" +
            " " +
            " <h4>Equitable Relief</h4>" +
            " <p>You acknowledge that any breach by you of the provisions of the Agreement will cause irreparable damage to PublicStuff or Others and that a remedy at law will be inadequate. Therefore, in addition to any and all other legal or equitable remedies, PublicStuff and Others will be entitled to injunctive relief for any breach of this Agreement.</p>" +
            " " +
            " <h4>Severability</h4>" +
            " <p>In the event that any of the provisions of this Agreement shall be held by a court or other tribunal of competent jurisdiction to be unenforceable, such provisions shall be limited or eliminated to the minimum extent necessary so that this Agreement shall otherwise remain in full force and effect and enforceable. </p>" +
            " " +
            " <h4>Complete Integration</h4>" +
            " <p>This Agreement constitutes the entire agreement between you and PublicStuff pertaining to the subject matter hereof. You agree to review this Agreement prior to any Site Use, and each Site Use by you shall constitute and be deemed your unconditional acceptance of this Agreement. This Agreement may be modified from time to time by PublicStuff, by posting a revised Agreement on the Site. </p>" +
            " " +
            " <h4>Termination</h4>" +
            " <p>The Agreement may be terminated by either Party, in its sole and absolute discretion, at any time and for any reason with or without notice. If the Agreement is terminated, you agree to cease all Site Use and, upon request by PublicStuff, to return all Information in your possession relating to the Site, and all copies thereof. </p>" +
            " " +
            " <h4>Survival of Certain Provisions</h4>" +
            " <p>Any and all provisions or obligations contained in this Agreement which by their nature or effect are required or intended to be observed, kept or performed after termination of this Agreement will survive the termination of this Agreement and remain binding upon and for the benefit of the parties, their successors and permitted assignees.</p>" +
            " " +
            " <h4>Waiver</h4>" +
            " <p>No delay or omission to exercise any right or remedy accruing to PublicStuff upon any breach or default by you shall constitute a waiver by PublicStuff of any breach or default. </p>" +
            " " +
            " <h4>Headings</h4>" +
            " <p>All article or section headings, or exhibit names, are for reference and convenience only and shall not be considered in the interpretation of the Agreement. </p>" +
            " " +
            " <h4>No Agency</h4>" +
            " <p>You and PublicStuff are independent contractors, and no agency, partnership, joint venture, employee-employer or franchiser-franchisee relationship is intended or created by this Agreement. </p>" +
            " " +
            " <h4>Conflicts</h4>" +
            " <p>If this Agreement conflicts with a provision of any other contract between you and PublicStuff relating to the Site, the provision in such other Agreement shall govern.</p>" +
            " " +
            " <h4>Glossary</h4>" +
            " <p>The following terms, when used in this Agreement, shall have the following meanings. </p>" +
            " <ul>" +
            " <li>\"Content\" means all Information, data, or other material, in any form or media, contained in, obtained from, or relating to the Site, including all results obtained from the Site. </li>" +
            " " +
            " <li>\"Content Providers\" means both PublicStuff and Others. </li>" +
            " " +
            " <li>\"Information\" includes all data, information, documents, files, personally-identifying information, and software disclosed by one party to the other in connection with the Site or your Site Use. </li>" +
            " " +
            " <li>\"Linked-Site\" means any Internet site (including all information, data, and content thereon) that is linked to the Site, but not owned by PublicStuff. </li>" +
            " " +
            " <li>\"Notice\" refers to the sending of Information by you to PublicStuff via certified mail, return receipt requested, to PublicStuff at the address noted in the section above entitled \"Contacting PublicStuff\". </li>" +
            " " +
            " <li>\"Others\" means PublicStuff's direct or indirect licensors, PublicStuff's affiliates, or other contributors to the Site (other than PublicStuff). </p>" +
            " " +
            " <li>\"Privacy Policy\" refers to PublicStuff's official published privacy policy, if any, describing PublicStuff's intended uses of your personally identifiable Information. </li>" +
            " " +
            " <li>\"Site\" means any PublicStuff Internet site, page (and all sub-pages), uniform resource locator (\"URL\"), domain location, and all Information and Content thereon. </li>" +
            " " +
            " <li>\"Site Use\" means your use of or access to the Site.</li>" +
            " </ul>" +
            " " +
            " <p>PublicStuff Inc.'s Privacy Policy" +
            " PublicStuff Inc. is committed to protecting the privacy of its Users. PublicStuff has created this privacy policy to tell you what information we gather, how we use it, and how to change it.</p>" +
            " " +
            " <h4>Use of Information About You</h4>" +
            " <p>If you are a User of our service, we will collect certain information about you and your use of the Site. We need this information so that we may appropriately define your role with the site, your level of access to information and to facilitate your communication and collaboration with other users. </p>" +
            " " +
            " <p>We use cookies to identify you once you have logged into our service as a User and to customize your visit. The use of cookies helps us to determine what information you are allowed to view and is used to ensure that you have the best experience possible at our site. If you would like to disallow the use of cookies, please change your browser preferences as outlined in your browser help section. Please understand though, that if you change your browser preferences, you will not be able to fully utilize the site. </p>" +
            " " +
            " <p>Cookies are small pieces of information that are stored by your browser on your computer's hard drive. A cookie cannot read data off your hard disk or read cookies created by other sites. PublicStuff also uses cookies to collect aggregated, broad information about Users' traffic patterns and related site usage information, and may disclose it to reputable third parties such as our merchants and strategic partners. In addition, PublicStuff may collect, capture and analyze information related to your usage of the site, may use such information for internal purposes, and on an aggregated basis may disclose, publish or sell that information to third parties on an aggregated or anonymous basis. This data will not include personally identifying information. Our purpose in collecting this information is to provide Users with the best possible product selection and service. </p>" +
            " " +
            " <p>PublicStuff will disclose your personally identifiable information only (1) with your permission, (2) when we believe in good faith that any applicable law, regulation, or legal process requires it, or (3) when we believe disclosure is necessary to protect or enforce our rights or the rights of another User or client.</p>" +
            " " +
            " <h4>Sites We Link To</h4>" +
            " <p>For your use, we may link to other sites that provide relevant content and services. PublicStuff is not responsible for the privacy practices, security, or the content of other Web sites, nor does it endorse the content or suitability of sites it links to.</p>" +
            " " +
            " <h4>E-mail</h4>" +
            " <p>We may occasionally send e-mails to Users to notify them of scheduled maintenance or other down periods, changes to our Terms and Conditions of Use, changes to this privacy policy, as well as other important messages.</p>" +
            " " +
            " <h4>Security</h4>" +
            " <p>This site has security procedures in place to prevent the loss, misuse, or alteration of data. All PublicStuff employees are briefed on measures to protect customer data, and access to customer data is limited physically and through electronic systems. PublicStuff has also taken a variety of steps to protect its systems, and encrypts the transmission of User registrations and account information.</p>" +
            " " +
            " <h4>Changes to this Policy, and Your Consent</h4>" +
            " <p>By accessing our Web site, you consent to the collection and use of this information by PublicStuff. We reserve the right to change this policy at any time. If we change our privacy policy, we will post those changes on this page so that you are always aware of what information we collect and how we use it, and we will e-mail notices of changes to User-related data policies to our Users.</p></div>";
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
    
}