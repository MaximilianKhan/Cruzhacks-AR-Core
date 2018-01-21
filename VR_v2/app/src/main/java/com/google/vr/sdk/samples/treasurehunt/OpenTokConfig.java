package com.google.vr.sdk.samples.treasurehunt;
import android.webkit.URLUtil;

public class OpenTokConfig {
    // *** Fill the following variables using your own Project info from the OpenTok dashboard  ***
    // ***                      https://dashboard.tokbox.com/projects                           ***

    // Replace with your OpenTok API key
    public static final String API_KEY = "46043442";
    // Replace with a generated Session ID
    public static final String SESSION_ID = "2_MX40NjA0MzQ0Mn5-MTUxNjUzMTU4NjU3NH55bDgrWi8vcGxMaHpZeldrMW8rYWJwQ0t-fg";
    // Replace with a generated token (from the dashboard or using an OpenTok server SDK)
    public static final String TOKEN = "T1==cGFydG5lcl9pZD00NjA0MzQ0MiZzaWc9YjFmOTA0MDJiY2JmNjVjZTFhMDMzMGM1NDE5ZTI0Y2YxNWIwNDE5NjpzZXNzaW9uX2lkPTJfTVg0ME5qQTBNelEwTW41LU1UVXhOalV6TVRVNE5qVTNOSDU1YkRncldpOHZjR3hNYUhwWmVsZHJNVzhyWVdKd1EwdC1mZyZjcmVhdGVfdGltZT0xNTE2NTMxNjA1Jm5vbmNlPTAuMDQzMTI5NzEzMDY1MTkzNTMmcm9sZT1tb2RlcmF0b3ImZXhwaXJlX3RpbWU9MTUxNzEzNjQwNCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";

    /*                           ***** OPTIONAL *****
     If you have set up one of the OpenTok server side samples to provide session information
     replace the null value in CHAT_SERVER_URL with it.

     For example (if using a heroku subdomain), enter : "https://yoursubdomain.herokuapp.com"
    */
    public static final String CHAT_SERVER_URL = null;
    public static final String SESSION_INFO_ENDPOINT = CHAT_SERVER_URL + "/session";


    // *** The code below is to validate this configuration file. You do not need to modify it  ***

    public static String webServerConfigErrorMessage;
    public static String hardCodedConfigErrorMessage;

    public static boolean areHardCodedConfigsValid() {

        if (OpenTokConfig.API_KEY != null && !OpenTokConfig.API_KEY.isEmpty()
                && OpenTokConfig.SESSION_ID != null && !OpenTokConfig.SESSION_ID.isEmpty()
                && OpenTokConfig.TOKEN != null && !OpenTokConfig.TOKEN.isEmpty()) {
            return true;
        }
        else {
            hardCodedConfigErrorMessage = "API KEY, SESSION ID, and TOKEN in OpenTokConfig.java cannot be null or empty.";
            return false;
        }
    }

    public static boolean isWebServerConfigUrlValid(){

        if (OpenTokConfig.CHAT_SERVER_URL == null || OpenTokConfig.CHAT_SERVER_URL.isEmpty()) {
            webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java must not be null or empty";
            return false;
        } else if ( !( URLUtil.isHttpsUrl(OpenTokConfig.CHAT_SERVER_URL) || URLUtil.isHttpUrl(OpenTokConfig.CHAT_SERVER_URL)) ) {
            webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java must be specified as either http or https";
            return false;
        } else if ( !URLUtil.isValidUrl(OpenTokConfig.CHAT_SERVER_URL) ) {
            webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java is not a valid URL";
            return false;
        } else {
            return true;
        }
    }
}
