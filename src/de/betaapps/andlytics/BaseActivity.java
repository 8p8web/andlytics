package de.betaapps.andlytics;

import de.betaapps.andlytics.admob.AdmobAccountRemovedException;
import de.betaapps.andlytics.admob.AdmobAskForPasswordException;
import de.betaapps.andlytics.admob.AdmobGenericException;
import de.betaapps.andlytics.admob.AdmobInvalidRequestException;
import de.betaapps.andlytics.admob.AdmobInvalidTokenException;
import de.betaapps.andlytics.admob.AdmobRateLimitExceededException;
import de.betaapps.andlytics.chart.Chart.ChartSet;
import de.betaapps.andlytics.dialog.CrashDialog;
import de.betaapps.andlytics.dialog.CrashDialog.CrashDialogBuilder;
import de.betaapps.andlytics.exception.AuthenticationException;
import de.betaapps.andlytics.exception.DeveloperConsoleException;
import de.betaapps.andlytics.exception.InvalidJSONResponseException;
import de.betaapps.andlytics.exception.MultiAccountAcception;
import de.betaapps.andlytics.exception.NetworkException;
import de.betaapps.andlytics.exception.NoCookieSetException;
import de.betaapps.andlytics.exception.SignupException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;

import org.acra.ACRA;
import org.acra.ErrorReporter;

public class BaseActivity extends Activity {

	private static final int REQUEST_AUTHENTICATE = 0;

    private static final String TAG = BaseActivity.class.getSimpleName();

	private View downloadsButton;
	private View admobButton;
	private View commentsButton;
	protected String packageName;
	protected String iconFilePath;
	protected String accountname;
	private View ratingsButton;

	public boolean isProVersion() {
		return AndlyticsApp.isProVersion(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			packageName = b.getString(Constants.PACKAGE_NAME_PARCEL);
			iconFilePath = b.getString(Constants.ICON_FILE_PARCEL);
			accountname = b.getString(Constants.AUTH_ACCOUNT_NAME);
			Preferences.saveAccountName(this,accountname);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (findViewById(R.id.tabbar_button_comments) != null) {
			setupTabbar();
		}

	}

	public void setupTabbar() {

		downloadsButton = findViewById(R.id.tabbar_button_downloads);
		ratingsButton = findViewById(R.id.tabbar_button_ratings);
		commentsButton = findViewById(R.id.tabbar_button_comments);
		admobButton = findViewById(R.id.tabbar_button_back);

		if (this instanceof ChartActivity) {
			ChartSet currentChartSet = ((ChartActivity) this).getCurrentChartSet();
			if (currentChartSet.equals(ChartSet.DOWNLOADS)) {
				downloadsButton.setSelected(true);
				commentsButton.setSelected(false);
				admobButton.setSelected(false);
				ratingsButton.setSelected(false);

			} else if (currentChartSet.equals(ChartSet.RATINGS)) {

				downloadsButton.setSelected(false);
				commentsButton.setSelected(false);
				ratingsButton.setSelected(true);
				admobButton.setSelected(false);
			}
		} else if (this instanceof CommentsActivity) {
			commentsButton.setSelected(true);
			admobButton.setSelected(false);
			downloadsButton.setSelected(false);
			ratingsButton.setSelected(false);
		} else if (this instanceof AdmobActivity) {
            commentsButton.setSelected(false);
            admobButton.setSelected(true);
            downloadsButton.setSelected(false);
            ratingsButton.setSelected(false);
		}

		downloadsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!(BaseActivity.this instanceof ChartActivity && ((ChartActivity) BaseActivity.this)
						.getCurrentChartSet().equals(ChartSet.DOWNLOADS))) {

					commentsButton.setSelected(false);
					admobButton.setSelected(false);
					ratingsButton.setSelected(false);
					downloadsButton.setSelected(true);

					startChartActivity(ChartSet.DOWNLOADS);
				}

			}
		});

		ratingsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!(BaseActivity.this instanceof ChartActivity && ((ChartActivity) BaseActivity.this)
						.getCurrentChartSet().equals(ChartSet.RATINGS))) {

					commentsButton.setSelected(false);
					admobButton.setSelected(false);
					ratingsButton.setSelected(true);
					downloadsButton.setSelected(false);
					startChartActivity(ChartSet.RATINGS);
				}

			}
		});

		commentsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!(BaseActivity.this instanceof CommentsActivity)) {

					downloadsButton.setSelected(false);
					admobButton.setSelected(false);
					ratingsButton.setSelected(false);
					commentsButton.setSelected(true);
					startActivity(CommentsActivity.class, true, false);
				}

			}
		});

		admobButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                if (!(BaseActivity.this instanceof AdmobActivity)) {

                    downloadsButton.setSelected(false);
                    admobButton.setSelected(true);
                    ratingsButton.setSelected(false);
                    commentsButton.setSelected(false);
                    startActivity(AdmobActivity.class, true, false);
                }
            }
		});
	}

	public void startActivity(Class<?> clazz, boolean disableAnimation, boolean skipDataReload) {
		Intent intent = new Intent(BaseActivity.this, clazz);
		intent.putExtra(Constants.PACKAGE_NAME_PARCEL, packageName);
		intent.putExtra(Constants.ICON_FILE_PARCEL, iconFilePath);
		intent.putExtra(Constants.AUTH_ACCOUNT_NAME, accountname);
		if (disableAnimation) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		}
		if(skipDataReload) {
		    getAndlyticsApplication().setSkipMainReload(true); 
		}

		startActivity(intent);
	}

	public void startChartActivity(ChartSet set) {
		Intent intent = new Intent(BaseActivity.this, ChartActivity.class);
		intent.putExtra(Constants.PACKAGE_NAME_PARCEL, packageName);
		intent.putExtra(Constants.ICON_FILE_PARCEL, iconFilePath);
		intent.putExtra(Constants.AUTH_ACCOUNT_NAME, accountname);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		intent.putExtra(Constants.CHART_SET, set.name());

		startActivity(intent);
	}

	public void handleUserVisibleException(Exception e) {
		if (e instanceof NetworkException) {
			Toast.makeText(BaseActivity.this, "A network error has occurred. Please try again later.",
					Toast.LENGTH_LONG).show();
		}  else if (e instanceof SignupException) {
			Toast.makeText(BaseActivity.this,
					accountname + " is not an android developer account, sign up at:\n\n" + e.getMessage(),
					Toast.LENGTH_LONG).show();
			Toast.makeText(BaseActivity.this,
					accountname + " is not an android developer account, sign up at:\n\n" + e.getMessage(),
					Toast.LENGTH_LONG).show();
		} else if (e instanceof AuthenticationException || e instanceof NoCookieSetException) {
			
			Toast.makeText(BaseActivity.this,
					"authentication failed for: " + accountname,
					Toast.LENGTH_LONG).show();

		} else if (e instanceof AdmobRateLimitExceededException) {
            
            Toast.makeText(BaseActivity.this,
                    "Can't load Admob data because Admob has restricted the number of requests from this app, try again later.",
                    Toast.LENGTH_LONG).show();

        } else if (e instanceof AdmobAskForPasswordException) {
            
            Log.w(TAG, "ask for admob credentials");
            getAndlyticsApplication().setSkipMainReload(true);
            
		} else if (e instanceof AdmobAccountRemovedException) {
            
            Toast.makeText(BaseActivity.this,
                    "AdMob account \"" + ((AdmobAccountRemovedException)e).getAccountName() + "\" is missing. If this happens repeatedly try moving Andlytics from sdcard to internal storage.",
                    Toast.LENGTH_LONG).show();
            Toast.makeText(BaseActivity.this,
                            "AdMob account \"" + ((AdmobAccountRemovedException)e).getAccountName() + "\" is missing. If this happens repeatedly try moving Andlytics from sdcard to internal storage.",
                            Toast.LENGTH_LONG).show();

        } else if (e instanceof AdmobInvalidRequestException) {
            
            Toast.makeText(BaseActivity.this, "Error while requesting AdMob API", Toast.LENGTH_LONG).show();

        } else if (e instanceof AdmobInvalidTokenException) {
            
            
            Toast.makeText(BaseActivity.this,
                    "Error while authenticating admob account. Please try again later.",
                    Toast.LENGTH_LONG).show();

		} else if (e instanceof AdmobGenericException) {
            
		    Log.w(TAG, e.getMessage(), e);
		    
            Toast.makeText(BaseActivity.this,
                    "Unabled to load Admob data, please try again later.",
                    Toast.LENGTH_LONG).show();
			
        } else if (e instanceof DeveloperConsoleException) {
            
            int appVersionCode = getAppVersionCode(this);
            if(Preferences.getLatestVersionCode(this) > appVersionCode) {
                showNewVersionDialog(e);
            } else {
                
                showCrashDialog(e);
            }

        
		} else if (e instanceof InvalidJSONResponseException) {
			
		    int appVersionCode = getAppVersionCode(this);
            if(Preferences.getLatestVersionCode(this) > appVersionCode) {
                showNewVersionDialog(e);
            } else {
                
                showGoogleErrorDialog(e);
            }

        } else if (e instanceof MultiAccountAcception) {
            
                showAspErrorDialog(e);
            

        }
	}

	private void showNewVersionDialog(Exception e) {
        if(!isFinishing()) {
            
            CrashDialog.CrashDialogBuilder builder = new CrashDialogBuilder(this);
            builder.setTitle("Sorry, update required.");
            builder.setMessage(R.string.newversion_desc);
            builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
    
                public void onClick(DialogInterface dialog, int which) {
                    
                    Intent goToMarket = null;
                    goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=de.betaapps.andlytics"));
                    startActivity(goToMarket);
                    
                    dialog.dismiss();
                }
    
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
    
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
    
            });
    
            builder.create().show();
        }
	            
    }

    protected void showCrashDialog(final Exception e) {
		
		if(!isFinishing()) {
			
			CrashDialog.CrashDialogBuilder builder = new CrashDialogBuilder(this);
			builder.setTitle("Sorry...");
			builder.setMessage(R.string.crash_desc);
			builder.setPositiveButton("send report", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which) {
				    
				    if(!isFinishing()) {
				        
				        Thread thread = new Thread(new Runnable() {
				            
				            @Override
				            public void run() {
				                sendAracReport(e, true);
				            }
				            
				        });
				        thread.run();
				        dialog.dismiss();
				    }
	
				}
	
			});
			builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
	
			});
	
			builder.create().show();
		}


	}

    private void sendAracReport(Exception e, boolean userTriggered) {
        ACRA.init(getApplication());
        if(!userTriggered) {
            ErrorReporter.getInstance().setUserComment("silent");
        }
        ErrorReporter.getInstance().handleSilentException(e);
        ErrorReporter.getInstance().disable();
    }

   protected void showProDialog() {
         
        if(!isFinishing()) {
            
            CrashDialog.CrashDialogBuilder builder = new CrashDialogBuilder(this);
            builder.setTitle("Andlytics Donate / Pro Key");
            builder.setMessage(R.string.pro_desc);
            builder.setPositiveButton("buy pro key", new DialogInterface.OnClickListener() {
    
                public void onClick(DialogInterface dialog, int which) {
                    
                    Intent goToMarket = null;
                    goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=de.betaapps.andlytics.pro"));
                    startActivity(goToMarket);
                    
                    dialog.dismiss();
                }
    
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
    
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
    
            });
    
            builder.create().show();
        }
    }

	
	protected void showGoogleErrorDialog(final Exception e) {
		
		if(!isFinishing()) {

			CrashDialog.CrashDialogBuilder builder = new CrashDialogBuilder(this);
			builder.setTitle("Sorry... ");
			builder.setMessage("Updates are currently not possible, probably because the remote interface changed.\n\nPlease help us to fix this by sending us error report data. Information about the error (stacktrace) and information about your device such as device manufacturer's name, device model number, operating system, etc. will be sent to help us identifying the problem.\n\nThank you!");
			builder.setPositiveButton("send report", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which) {
	
					Thread thread = new Thread(new Runnable() {
	
						@Override
						public void run() {
						    sendAracReport(e, true);
						}
					});
					thread.run();
					dialog.dismiss();
				}
	
			});
			builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
	
			});
	
			builder.create().show();
		
		}

	}
	
    protected void showAspErrorDialog(final Exception e) {
        
        if(!isFinishing()) {

            CrashDialog.CrashDialogBuilder builder = new CrashDialogBuilder(this);
            builder.setTitle("Multiple Developer Accounts");
            builder.setMessage("Your account is linked to multiple developer consoles. This feature is not supported in andlytics.\n\nYou can unlink additional accounts or ask Google for a public app stats API at: \n\nhttp://support.google.com/googleplay\n\nSorry :(");
            builder.setPositiveButton("logout", new DialogInterface.OnClickListener() {
    
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Preferences.removeAccountName(BaseActivity.this);
                    Preferences.saveSkipAutoLogin(BaseActivity.this, true);
                    Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);

                }
    
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
    
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Preferences.removeAccountName(BaseActivity.this);
                    Preferences.saveSkipAutoLogin(BaseActivity.this, true);
                    Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
                }
    
            });
    
            builder.create().show();
        
        }

    }	
	
	public ContentAdapter getDbAdapter() {
		return getAndlyticsApplication().getDbAdapter();
	}

	protected void authenticateAccountFromPreferences(boolean invalidateToken, AuthenticationCallback callback) {
		
		String accountName = Preferences.getAccountName(this);

		if (accountName != null) {
			AccountManager manager = AccountManager.get(this);
			Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE_GOOGLE);
			int size = accounts.length;
			for (int i = 0; i < size; i++) {
				Account account = accounts[i];
				if (accountName.equals(account.name)) {
					if (invalidateToken) {
						manager.invalidateAuthToken(Constants.ACCOUNT_TYPE_GOOGLE, getAndlyticsApplication().getAuthToken());
					}
					getAndlyticsApplication().setAuthToken(null);
					authenticateAccount(manager, account, callback);
				}
			}
		} else {
			getAndlyticsApplication().setAuthToken(null);
		}
	}

	private void authenticateAccount(final AccountManager manager, final Account account, final AuthenticationCallback callback) {

		Preferences.saveAccountName(this, account.name);
		
		AccountManager accountManager = AccountManager.get(this.getApplicationContext());

        final AccountManagerCallback<Bundle> myCallback = new AccountManagerCallback<Bundle>() {

            @SuppressWarnings("unchecked")
            public void run(final AccountManagerFuture<Bundle> arg0) {
                try {

                    String authToken = arg0.getResult().getString(
                            AccountManager.KEY_AUTHTOKEN);

                    if (authToken != null) {            
                                            //do something with the auth token    
                    //  got token form manager - set in application an exit
                        getAndlyticsApplication().setAuthToken(authToken);
                       
                        runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                callback.authenticationSuccess();
                            }
                        });

                    } else {
                        
                        Log.e(TAG, "auth token is null, authentication failed");
                                                   //not expected at all
                    }

                } catch (Exception e) {     
                    getAndlyticsApplication().setSkipMainReload(true);
                    Log.e(TAG, "error during authentication", e);
                                            //error
                }
                
            }

        };
        

        accountManager.getAuthToken(account,
                        Constants.AUTH_TOKEN_TYPE_ANDROID_DEVLOPER,null, BaseActivity.this, myCallback, null);
		
        /*

		Bundle bundle;
		try {
			bundle = manager.getAuthToken(account, Constants.AUTH_TOKEN_TYPE_ANDROID_DEVLOPER, true, null, null).getResult();

			if (bundle.containsKey(AccountManager.KEY_INTENT)) {
				
				// ask user for permission - launch account manager intent
				Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
				int flags = intent.getFlags();
				flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
				intent.setFlags(flags);
				getAndlyticsApplication().setAuthToken(null);
				getAndlyticsApplication().setRunningAuthenticationRequestIntent(true);
				startActivityForResult(intent, REQUEST_AUTHENTICATE);

			} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {

				//  got token form manager - set in application an exit
				String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
				getAndlyticsApplication().setAuthToken(authToken);
				
				//manager.invalidateAuthToken(Constants.ACCOUNT_TYPE_GOOGLE, authToken);


			}
		} catch (OperationCanceledException e1) {
			e1.printStackTrace();
		} catch (AuthenticatorException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/

	}

/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:

			getAndlyticsApplication().setRunningAuthenticationRequestIntent(false);

			if (resultCode == RESULT_OK) {
				
				AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						authenticateAccountFromPreferences(false);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						onPostAuthentication();
					}
					
					
				};
				t.execute();

			} else {
			    getAndlyticsApplication().setSkipMainReload(true);
				Log.e("Andlytics", "REQUEST_AUTHENTICATE, result is NOT ok");
			}
		}
	}*/
	
	protected void showLoadingIndecator(ViewSwitcher switcher) {
        Animation loadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);
        loadingAnim.setInterpolator(new LinearInterpolator());

        switcher.showNext();
        switcher.findViewById(R.id.loading).startAnimation(loadingAnim);
	}

	protected void hideLoadingIndecator(ViewSwitcher switcher) {
	    switcher.showPrevious();
	    switcher.findViewById(R.id.loading).clearAnimation();
        switcher.findViewById(R.id.loading).setAnimation(null);
    }

	public AndlyticsApp getAndlyticsApplication() {
		return (AndlyticsApp) getApplication();
	}
	
	protected void onPostAuthentication() {
	}
	
    @Override
    public void onBackPressed() {
        if(this instanceof ChartActivity ||
            this instanceof AdmobActivity || 
            this instanceof CommentsActivity) {
            commentsButton.setSelected(false);
            downloadsButton.setSelected(false);
            ratingsButton.setSelected(false);
            admobButton.setSelected(false);

            startActivity(Main.class, false, true);

            overridePendingTransition(R.anim.activity_prev_in, R.anim.activity_prev_out);
        } else {
            super.onBackPressed();
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pinfo.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(AndlyticsApp.class.getSimpleName(), "unable to read version code", e);
        }
        return 0;
    }

}
