package com.qi.airstat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Constants {
    private static Constants instance = new Constants();

    final static public int AIR_DATA_VIEW_PAGER_MAX_PAGES = 6;
    final static public int FAKE_DATA_SERVICE_UPDATE_INTERVAL = 1000; // Unit: millisecond

    final static public int CLIENT_REGISTER = 100;
    final static public int CLIENT_UNREGISTER = 101;
    final static public int QUEUED_AIR_DATA = 200;
    final static public int QUEUED_HEART_RATE_DATA = 300;
    final static public int SERVICE_AIR_DATA_UPDATED = 400;
    final static public int SERVICE_HEART_RATE_DATA_UPDATED = 401;
    final static public int SERVICE_BLUETOOTH_NOT_SUPPORTED = 500;

    final static public int DATABASE_VERSION = 1000;

    final static public int BLUETOOTH_PERMISSION_REQUEST = 100;
    final static public int BLUETOOTH_SCAN_REQEUST = 101;

    final static public int LOCATION_PERMISSION_REQUEST = 200;

    final static public String BLUETOOTH_SCAN_ACTIVITY_EXTRA_MAC = "BLUETOOTH_SCAN_ACTIVITY_EXTRA_MAC";

    final static public String AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX = "AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX";

    final static public String DATABASE_NAME = "AirStat.db";
    final static public String DATABASE_AIR_TABLE = "AIR";
    final static public String DATABASE_HEART_RATE_TABLE = "HEART_RATE";
    final static public String DATABASE_COMMON_COLUMN_ID = "ID";
    final static public String DATABASE_COMMON_COLUMN_TIME_STAMP = "TIME_STAMP";
    final static public String DATABASE_HEART_RATE_COLUMN_HEART_RATE = "SIGNAL";
    final static public String DATABASE_AIR_COLUMN_CO = "CO";
    final static public String DATABASE_AIR_COLUMN_CO2 = "CO2";
    final static public String DATABASE_AIR_COLUMN_SO2 = "SO2";
    final static public String DATABASE_AIR_COLUMN_NO2 = "NO2";
    final static public String DATABASE_AIR_COLUMN_O3 = "O3";
    final static public String DATABASE_AIR_COLUMN_PM25 = "PM25";
    final static public String DATABASE_AIR_COLUMN_LAT = "LAT";
    final static public String DATABASE_AIR_COLUMN_LON = "LON";
    final static public String DATABASE_QUERY_CREATE_AIR_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_AIR_TABLE + " ("
                    + DATABASE_COMMON_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DATABASE_COMMON_COLUMN_TIME_STAMP + " CHAR(12), "
                    + DATABASE_AIR_COLUMN_LAT + " REAL, "
                    + DATABASE_AIR_COLUMN_LON + " REAL, "
                    + DATABASE_AIR_COLUMN_CO + " REAL, "
                    + DATABASE_AIR_COLUMN_CO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_SO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_NO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_O3 + " REAL, "
                    + DATABASE_AIR_COLUMN_PM25 + " REAL" +
                    ")";

    final static public String DATABASE_QUERY_CREATE_HEART_RATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_HEART_RATE_TABLE + " ("
                    + DATABASE_COMMON_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DATABASE_COMMON_COLUMN_TIME_STAMP + " CHAR(12), "
                    + DATABASE_HEART_RATE_COLUMN_HEART_RATE + " INT" +
                    ")";

    final static public String DATABASE_QUERY_DROP_TABLE_AIR = "DROP TABLE IF EXISTS " + DATABASE_AIR_TABLE;
    final static public String DATABASE_QUERY_DROP_TABLE_HEART_RATE = "DROP TABLE IF EXISTS " + DATABASE_HEART_RATE_TABLE;

    final static public int HTTP_CONNECT_TIMEOUT = 10000;
    final static public int HTTP_READ_TIMEOUT = 15000;

    /*
    Don't know exactly...;;
     */
    final static public String HTTP_STR_URL_LOGIN = "http://teamc-iot.calit2.net/IOT/public/Login";
    //    final static public String HTTP_STR_URL_LOGIN = "http://teama-iot.calit2.net/slim/recieveData.php/rcvJSON";
    final static public String HTTP_STR_URL_CREATE_NEW_ACCOUNT = "http://teamc-iot.calit2.net/IOT/public/reg";
    final static public String HTTP_STR_URL_FORGOT_PASSWORD = "http://teamc-iot.calit2.net/IOT/public/ResetPwd";

    final static public String HTTP_STR_URL_TEST = "http://teamc-iot.calit2.net/IOT/public/rcv_json_data";

    final static public String HTTP_MSG_ID = "msgid";

    final static public int HTTP_REQUEST_LOGIN = 1;
    final static public String HTTP_DATA_LOGIN_EMAIL = "email";
    final static public String HTTP_DATA_LOGIN_PASSWORD = "pwd";

    final static public int HTTP_REQUEST_FORGOT_PASSWORD = 2;
    final static public String HTTP_DATA_FORGOT_PASSWORD_EMAIL = "email";

    final static public int HTTP_REQUEST_CREATE_NEW_ACCOUNT = 3;
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_FIRST_NAME = "fname";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_LAST_NAME = "lname";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_EMAIL = "email";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_PASSWORD = "password";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_CONFIRM_PASSWORD = "cpassword";

    public static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^[a-zA-Z0-9]{6,16}$");

    final static public String HTTP_RESPONSE_RESULT = "result";
    final static public int HTTP_RESPONSE_RESULT_OK = 0;
    final static public int HTTP_RESPONSE_RESULT_FAIL = 1;

    static public int UID = -1;

    /*
    GoogleMap
     */
    final static public String AQI_LEVEL_DEFAULT = "#dd6d6d6d";
    final static public String AQI_LEVEL_GOOD = "#dd009865";
    final static public String AQI_LEVEL_MODERATE = "#ddfede33";
    final static public String AQI_LEVEL_SENSITIVE = "#ddff9934";
    final static public String AQI_LEVEL_UNHEALTHY = "#ddcc0033";
    final static public String AQI_LEVEL_VERY_UNHEALTHY = "#dd670099";
    final static public String AQI_LEVEL_HAZARDOUS = "#dd7e0123";

    final static public int HTTP_DATA_MAP_GET_ONGOING_SESSION_TIME_QUALTUM = 1000;

    final static public String HTTP_STR_URL_ONGOING_SESSION = "http://teamc-iot.calit2.net/IOT/public/realtime_json";

    final static public int HTTP_REQUEST_ONGOING_SESSION = 3000;
    final static public String HTTP_DATA_MAP_RANGE_MIN_LAT = "minlat";
    final static public String HTTP_DATA_MAP_RANGE_MAX_LAT = "maxlat";
    final static public String HTTP_DATA_MAP_RANGE_MIN_LNG = "minlng";
    final static public String HTTP_DATA_MAP_RANGE_MAX_LNG = "maxlng";

    final static public String HTTP_DATA_MAP_ONGOING_SESSION_CID = "connectionID";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_TIME_STAMP = "timeStamp";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_AIR = "AIR";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_CO = "CO";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_NO2 = "NO2";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_O3 = "O3";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_PM = "PM";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_SO2 = "SO2";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_TEMP = "temperature";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_LAT = "latitude";
    final static public String HTTP_DATA_MAP_ONGOING_SESSION_LNG = "longitude";

    public static long getSystemTime() {
        return Long.parseLong(new SimpleDateFormat("yymmddhhmmss").format(new Date()));
    }

    private Constants() { /* DO NOTHING */ }

    public static Constants getInstance() {
        return instance;
    }
}