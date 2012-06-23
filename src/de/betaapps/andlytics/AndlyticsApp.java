package de.betaapps.andlytics;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import de.betaapps.andlytics.dialog.CrashDialog;
import de.betaapps.andlytics.dialog.CrashDialog.CrashDialogBuilder;
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

    public static void showDonateDialog(final Context context) {
        final String PREF_SHOWN_DIALOG = "andlytics.PREF_SHOWN_DIALOG";
        
        int versionCode = 0;
        try { versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode; }
        catch (NameNotFoundException e) { e.printStackTrace(); }
        int shownDialogCode = PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_SHOWN_DIALOG, 0);
        
        if (shownDialogCode < versionCode) {
            // Donate dialog
            CrashDialog.CrashDialogBuilder builder = new CrashDialogBuilder(context);
            builder.setTitle("Please support ;)");
            builder.setMessage(R.string.donate_desc);
            builder.setPositiveButton("donate", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://m.8p8web.it/a/donate.php")));
                    dialog.dismiss();
                }
    
            });
            builder.setNegativeButton("not now", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
            });
            //builder.create().show();
            
            //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            //editor.putInt(PREF_SHOWN_DIALOG, versionCode); editor.commit();
        }
    }

}
