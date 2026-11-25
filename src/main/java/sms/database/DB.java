package sms.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Forwarding delegate to sms.DB to maintain backward compatibility.
 */
public class DB {
    public static Connection getConnection() throws SQLException {
        return sms.DB.getConnection();
    }

    public static boolean checkConnection() {
        return sms.DB.checkConnection();
    }

    public static void init() {
        sms.DB.init();
    }
}
