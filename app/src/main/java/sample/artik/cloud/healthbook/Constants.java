
package sample.artik.cloud.healthbook;

/**
 * Constants for the Sample.
 * 
 * @author Maneesh Sahu
 */
public class Constants {

    public static final String CLIENT_ID = "4ac0a3bd4f7c4ecd8542bd6905197acb";

    public static final String AUTHORIZATION_IMPLICIT_SERVER_URL = "https://accounts.artik.cloud/authorize";

    public static final String REDIRECT_URL = "artikcloud://localhost";

    public static final String DT_OPEN_WEATHER_MAP = "dt9ad7ecfd34324765a9b12ef98a51b29e";

    public static final String DT_PEDOMETER =  "dta8ad42083f33441b8677e5b36f049a4b";

    public static final String[] DEVICE_TYPE_NAMES = { "OPEN_WEATHER_MAP", "PEDOMETER" };

    public static final String[] DEVICE_TYPES = { DT_OPEN_WEATHER_MAP, DT_PEDOMETER };

    private Constants() {
    }
}
