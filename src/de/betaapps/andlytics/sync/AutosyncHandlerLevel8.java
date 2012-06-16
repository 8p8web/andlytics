package de.betaapps.andlytics.sync;

import de.betaapps.andlytics.Constants;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.PeriodicSync;
import android.os.Bundle;

import java.util.List;


public class AutosyncHandlerLevel8 implements AutosyncHandler {

    @Override
    public boolean isAutosyncEnabled(String accountname) {
        Account account = new Account(accountname, Constants.ACCOUNT_TYPE_GOOGLE);
        return ContentResolver.getSyncAutomatically(account, Constants.ACCOUNT_AUTHORITY);
    }
    
    @Override
    public int getAutosyncPeriod(String accountname) {

        int result = 0;
        
        Account account = new Account(accountname, Constants.ACCOUNT_TYPE_GOOGLE);
        if (ContentResolver.getSyncAutomatically(account, Constants.ACCOUNT_AUTHORITY)) {
            List<PeriodicSync> periodicSyncs = ContentResolver.getPeriodicSyncs(account, Constants.ACCOUNT_AUTHORITY);
            for (PeriodicSync periodicSync : periodicSyncs) {
                result = (int) periodicSync.period;
                break;
            }
        }
        return result;
        
    }

    @Override
    public void setAutosyncPeriod(String accountName, Integer period) {
        
        Bundle extras = new Bundle();
        Account account = new Account(accountName, Constants.ACCOUNT_TYPE_GOOGLE);
        
        if(period == 0) {
            ContentResolver.setSyncAutomatically(account, Constants.ACCOUNT_AUTHORITY, false);
        } else {
            ContentResolver.setSyncAutomatically(account, Constants.ACCOUNT_AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, Constants.ACCOUNT_AUTHORITY, extras, period); 
        }
        
    }
    

}
