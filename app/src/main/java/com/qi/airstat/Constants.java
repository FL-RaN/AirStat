package com.qi.airstat;

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

    final static public int AIR_LABEL_INDEX_PM25 = 0;
    final static public int AIR_LABEL_INDEX_TEMPERATURE = 1;
    final static public int AIR_LABEL_INDEX_CO = 2;
    final static public int AIR_LABEL_INDEX_SO2 = 3;
    final static public int AIR_LABEL_INDEX_NO2 = 4;
    final static public int AIR_LABEL_INDEX_O3 = 5;

    // Intent request codes
    final static public int REQUEST_CONNECT_DEVICE = 1;
    final static public int REQUEST_ENABLE_BT = 2;

    // Message types sent from the DeviceConnector Handler
    final static public int MESSAGE_STATE_CHANGE = 1;
    final static public int MESSAGE_READ = 2;
    final static public int MESSAGE_WRITE = 3;
    final static public int MESSAGE_DEVICE_NAME = 4;
    final static public int MESSAGE_TOAST = 5;

    final static public int STATE_NONE = 0;       // we're doing nothing
    final static public int STATE_CONNECTING = 1; // now initiating an outgoing connection
    final static public int STATE_CONNECTED = 2;  // now connected to a remote device

    final static public int BLUETOOTH_SCANNED_TEXT_MAC_OFFSET = 17;

    final static public int SENSOR_REALTIME_RECORD_LIMIT = 60;

    final static public String BLUETOOTH_MESSAGE_STATE_CHANGE = "BLUETOOTH_MESSAGE_STATE_CHANGE";
    final static public String BLUETOOTH_MESSAGE_STATE_READ = "MESSAGE_READ";
    final static public String BLUETOOTH_MESSAGE_MESSAGE_WRITE = "BLUETOOTH_MESSAGE_MESSAGE_WRITE";
    final static public String BLUETOOTH_MESSAGE_MESSAGE_DEVICE_NAME = "BLUETOOTH_MESSAGE_MESSAGE_DEVICE_NAME";
    final static public String BLUETOOTH_MESSAGE_MESSAGE_TOAST = "BLUETOOTH_MESSAGE_MESSAGE_TOAST";

    final static public String BLUETOOTH_UNKNOWN_DEVICE = "UNKNOWN DEVICE";
    final static public String BLC_SCAN_RESULT_MAC = "BLC_SCAN_RESULT_MAC";
    final static public String BLC_SCAN_RESULT_DEV = "BLC_SCAN_RESULT_DEV";

    final static public String AIR_GRAPH_DESCRIPTION_PM25 = "PM2.5";
    final static public String AIR_GRAPH_DESCRIPTION_TEMPERATURE = "TEMPERATURE";
    final static public String AIR_GRAPH_DESCRIPTION_CO = "CO";
    final static public String AIR_GRAPH_DESCRIPTION_SO2 = "SO2";
    final static public String AIR_GRAPH_DESCRIPTION_NO2 = "NO2";
    final static public String AIR_GRAPH_DESCRIPTION_O3 = "O3";

    final static public String AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX = "AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX";

    final static public String DATABASE_NAME = "AirStat.db";
    final static public String DATABASE_AIR_TABLE = "AIR";
    final static public String DATABASE_HEART_RATE_TABLE = "HEART_RATE";
    final static public String DATABASE_COMMON_COLUMN_ID = "ID";
    final static public String DATABASE_COMMON_COLUMN_TIME_STAMP = "TIME_STAMP";
    final static public String DATABASE_HEART_RATE_COLUMN_HEART_RATE = "SIGNAL";
    final static public String DATABASE_AIR_COLUMN_CO = "CO";
    final static public String DATABASE_AIR_COLUMN_TEMPERATURE = "TEMPERATURE";
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
                    + DATABASE_AIR_COLUMN_TEMPERATURE + " REAL, "
                    + DATABASE_AIR_COLUMN_SO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_NO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_O3 + " REAL, "
                    + DATABASE_AIR_COLUMN_PM25 + " REAL" +
                    ")";

    final static public String DATABASE_QUERY_CREATE_HEART_RATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_HEART_RATE_TABLE + " ("
                    + DATABASE_COMMON_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DATABASE_COMMON_COLUMN_TIME_STAMP +" CHAR(12), "
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

    final static public String HTTP_REQUEST_LOGIN = "1";
    final static public String HTTP_DATA_LOGIN_EMAIL = "email";
    final static public String HTTP_DATA_LOGIN_PASSWORD = "pwd";

    final static public String HTTP_REQUEST_FORGOT_PASSWORD = "2";
    final static public String HTTP_DATA_FORGOT_PASSWORD_EMAIL = "email";

    final static public String HTTP_REQUEST_CREATE_NEW_ACCOUNT = "3";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_FIRST_NAME = "fname";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_LAST_NAME = "lname";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_EMAIL = "email";
    final static public String HTTP_DATA_CREATE_NEW_ACCOUNT_PASSWORD = "password";

    /*
    GoogleMap
     */
    final static public String AQI_LEVEL_DEFAULT = "#6d6d6d";
    final static public String AQI_LEVEL_GOOD = "#009865";
    final static public String AQI_LEVEL_MODERATE = "#fede33";
    final static public String AQI_LEVEL_SENSITIVE = "#ff9934";
    final static public String AQI_LEVEL_UNHEALTHY = "#cc0033";
    final static public String AQI_LEVEL_VERY_UNHEALTHY = "#670099";
    final static public String AQI_LEVEL_HAZARDOUS = "#7e0123";

    private Constants() { /* DO NOTHING */ }

    public static Constants getInstance() {
        return instance;
    }
}