package de.betaapps.andlytics;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import de.betaapps.andlyticsredux.R;

import org.acra.annotation.ReportsCrashes;

// dFoxNXhxUmVqemdib1UxbXNfSWZpUnc6MQ
@ReportsCrashes(
		formKey = "dGFYLTFsUzlCMXR4Wi13LURBb0hianc6MQ", 
		sharedPreferencesMode=Context.MODE_PRIVATE, 
		sharedPreferencesName=Preferences.PREF) 
public class AndlyticsApp extends Application {

	private static final String CONTENT_URI = "content://de.betaapps.andlytics.pro.ProContentProvider/pro";
		
	public static boolean proVersion = true;
	
	private String authToken;
	
	private String xsrfToken;
	
	private ContentAdapter db;
	
	private boolean skipMainReload;
	
	private String feedbackMessage;

	@Override
	public void onCreate() {
		super.onCreate();
		Preferences.disableCrashReports(this);
		setDbAdapter(new ContentAdapter(this)); 
	}

	public static boolean isProVersion(Context context) {

		if (!proVersion) {

			Uri allTitles = Uri.parse(CONTENT_URI);
			
			Cursor c = context.getContentResolver().query(allTitles, null, null, null, "");
	        
			if(c != null) {
			    proVersion = true;
			} else {
                proVersion = false;
			}
			return proVersion;
			
		}

		return proVersion;

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public void setDbAdapter(ContentAdapter db) {
		this.db = db;
	}

	public ContentAdapter getDbAdapter() {
		return db;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}


    public void setSkipMainReload(boolean skipMainReload) {
        this.skipMainReload = skipMainReload;
    }

    public boolean isSkipMainReload() {
        return skipMainReload;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }



}
