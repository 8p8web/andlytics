package de.betaapps.andlytics.sync;

import de.betaapps.andlytics.AppStatsDiff;
import de.betaapps.andlytics.ContentAdapter;
import de.betaapps.andlytics.DeveloperConsole;
import de.betaapps.andlytics.exception.AuthenticationException;
import de.betaapps.andlytics.exception.DeveloperConsoleException;
import de.betaapps.andlytics.exception.InvalidJSONResponseException;
import de.betaapps.andlytics.exception.MultiAccountAcception;
import de.betaapps.andlytics.exception.NetworkException;
import de.betaapps.andlytics.exception.NoCookieSetException;
import de.betaapps.andlytics.exception.SignupException;
import de.betaapps.andlytics.model.AppInfo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyncAdapterService extends Service {

	private static final String TAG = SyncAdapterService.class.getSimpleName();

	private static SyncAdapterImpl sSyncAdapter = null;

	private static ContentAdapter db;

	public SyncAdapterService() {
		super();
	}

	private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
		private Context mContext;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			try {
				SyncAdapterService.performSync(mContext, account, extras, authority, provider, syncResult);
			} catch (OperationCanceledException e) {
				Log.w(TAG, "operation canceled", e);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}

	private SyncAdapterImpl getSyncAdapter() {
		if (sSyncAdapter == null)
			sSyncAdapter = new SyncAdapterImpl(this);
		return sSyncAdapter;
	}

	private static void performSync(Context context, Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) throws OperationCanceledException {
		 
		Bundle bundle = null;
		String token = null;
		
		try {
			bundle = AccountManager.get(context).getAuthToken(account, "androiddeveloper", true, null, null).getResult();
			if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
				token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
				
				DeveloperConsole console = new DeveloperConsole(context);
				List<AppInfo> appDownloadInfos = console.getAppDownloadInfos(token, account.name);

				Log.d(TAG, "andlytics from sync adapter, size: " + appDownloadInfos.size());
				
				List<AppStatsDiff> diffs = new ArrayList<AppStatsDiff>();

				db = new ContentAdapter(context);
				for (AppInfo appDownloadInfo : appDownloadInfos) {
					// update in database
					diffs.add(db.insertOrUpdateStats(appDownloadInfo));
				}
				Log.d(TAG, "sucessfully synced andlytics");
				
				// check for notifications
				NotificationHandler.handleNotificaions(context, diffs, account.name);
				
			} else {
				Log.e(TAG, "error during sync auth, no token found");
			}
		} catch (AuthenticatorException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (IOException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (NetworkException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (DeveloperConsoleException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (InvalidJSONResponseException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (SignupException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (AuthenticationException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (NoCookieSetException e) {
			Log.e(TAG, "error during sync auth", e);
		} catch (MultiAccountAcception e) {
            Log.e(TAG, "error during sync auth", e);
        }

	}
}