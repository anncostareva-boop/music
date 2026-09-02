package com.music.app.dao.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionManager {
    Connection getConnection() throws SQLException;
    Connection getConnection(boolean autoCommit) throws SQLException;
    Connection getConnection(boolean autoCommit, int transactionIsolation) throws SQLException;
}
