package com.qi.airstat;

public class Constants {
    private static Constants instance = new Constants();

    final static public int AIR_DATA_VIEW_PAGER_MAX_PAGES = 6;
    final static public int SENSOR_DATA_UPDATE_SERVICE_INTERVAL = 1000; // Unit: millisecond

    final static public int CLIENT_REGISTER = 100;
    final static public int CLIENT_UNREGISTER = 101;
    final static public int QUEUED_AIR_DATA = 200;
    final static public int QUEUED_HEART_RATE_DATA = 300;
    final static public int SERVICE_AIR_DATA_UPDATED = 400;
    final static public int SERVICE_HEART_RATE_DATA_UPDATED = 401;

    final static public int DATABASE_VERSION = 1000;

    final static public String AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX = "AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX";

    final static public String DATABASE_NAME = "AirStat.db";
    final static public String DATABASE_AIR_TABLE = "AIR";
    final static public String DATABASE_HEART_RATE_TABLE = "HEART_RATE";
    final static public String DATABASE_COMMON_COLUMN_TIME_STAMP = "TIME_STAMP";
    final static public String DATABASE_HEART_RATE_COLUMN_HEART_RATE = "HEART_RATE";
    final static public String DATABASE_AIR_COLUMN_CO = "CO";
    final static public String DATABASE_AIR_COLUMN_CO2 = "CO2";
    final static public String DATABASE_AIR_COLUMN_SO2 = "SO2";
    final static public String DATABASE_AIR_COLUMN_NO2 = "NO2";
    final static public String DATABASE_AIR_COLUMN_O3 = "O3";
    final static public String DATABASE_AIR_COLUMN_PM25 = "PM25";
    final static public String DATABASE_QUERY_CREATE_AIR_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_AIR_TABLE + " ("
                    + DATABASE_COMMON_COLUMN_TIME_STAMP + " CHAR(12) PRIMARY KEY, "
                    + DATABASE_AIR_COLUMN_CO + " REAL, "
                    + DATABASE_AIR_COLUMN_CO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_SO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_NO2 + " REAL, "
                    + DATABASE_AIR_COLUMN_O3 + " REAL, "
                    + DATABASE_AIR_COLUMN_PM25 + " REAL" +
            ")";

    final static public String DATABASE_QUERY_CREATE_HEART_RATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_HEART_RATE_TABLE + " ("
                    + DATABASE_COMMON_COLUMN_TIME_STAMP +" CHAR(12) PRIMARY KEY, "
                    + DATABASE_HEART_RATE_COLUMN_HEART_RATE + " REAL" +
            ")";

    final static public String DATABASE_QUERY_DROP_TABLE_AIR = "DROP TABLE IF EXISTS air";
    final static public String DATABASE_QUERY_DROP_TABLE_HEART_RATE = "DROP TABLE IF EXISTS heart_rate";


    final static public int COMMUNICATION_CONNECT_TIMEOUT = 10000;
    final static public int COMMUNICATION_READ_TIMEOUT = 15000;

    /*
    Don't know exactly...;;
     */
    final static public String COMMUNICATION_MSG_ID = "msgid";
    final static public String COMMUNICATION_USER_EMAIL = "email";
    final static public String COMMUNICATION_USER_PASSWORD = "password";

    final static public String COMMUNICATION_LOGIN_REQUEST = "1";

    private Constants() { /* DO NOTHING */ }

    public static Constants getInstance() {
        return instance;
    }


}
