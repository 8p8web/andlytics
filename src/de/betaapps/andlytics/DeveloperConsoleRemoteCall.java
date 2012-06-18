package de.betaapps.andlytics;

import de.betaapps.andlyticsredux.R;


public abstract class DeveloperConsoleRemoteCall<Params, Result> {

    public DeveloperConsoleRemoteCall() {
       
    }

    public abstract DeveloperConsoleRemoteCall<Params, Result> execute(Params... params);
    
}
