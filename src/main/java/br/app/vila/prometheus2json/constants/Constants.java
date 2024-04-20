package br.app.vila.prometheus2json.constants;

/**
 * Defines the default constants.
 *
 * @author fvilarin
 */
public abstract class Constants {
    // Default polling interval (in seconds).
    public static final int DEFAULT_POLLING_INTERVAL = 30;

    // Default user agent used to execute the requests (source and destination).
    public static final String DEFAULT_USER_AGENT = "prometheus2json/1.0.0";

    // Default content type used to execute the requests (source and destination).
    public static final String DEFAULT_CONTENT_TYPE = "application/json";

    // Default method used to execute the requests (source and destination).
    public static final String DEFAULT_METHOD = "GET";

    // Default settings filename.
    public static final String DEFAULT_SETTINGS_FILENAME = "settings.json";
}
