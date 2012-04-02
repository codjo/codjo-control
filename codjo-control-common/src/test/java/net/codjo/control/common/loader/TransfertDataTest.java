/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import junit.framework.TestCase;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TransfertDataTest extends TestCase {
    private static final String Q_TO_USER =
          "insert into USER ( QUARANTINE_ID, TOTO_COL, TiTi_COL ) "
          + "select QUARANTINE.QUARANTINE_ID, QUARANTINE.TOTO_COL, QUARANTINE.TiTi_COL "
          + "from QUARANTINE where QUARANTINE.ERROR_TYPE > 0";
    private static final String Q_TO_USER_DELETE =
          "delete QUARANTINE"
          + " where QUARANTINE_ID in ("
          + "   select USER.QUARANTINE_ID"
          + "   from QUARANTINE INNER JOIN USER"
          + "   on QUARANTINE.QUARANTINE_ID = USER.QUARANTINE_ID)";
    private static final String USER_TO_Q =
          "insert into QUARANTINE ( TOTO_COL, TiTi_COL ) "
          + "select USER.TOTO_COL, USER.TiTi_COL from USER where USER.ERROR_TYPE <= 0";
    private static final String USER_TO_Q_DELETE =
          "delete USER where ERROR_TYPE <= 0";
    private TransfertData transfert;
    private Connection connection;
    private PreparedStatement statement;
    ResultSet resultSet4Metadata;


    public void test_buildQuery_replaceUserData_singleColumn() throws Exception {
        transfert = new TransfertData("QUARANTINE", "USER");
        transfert.addMatchingCol("TOTO_COL");
        transfert.setReplaceUserData(true);

        // QUARANTINE TO USER
        transfert.getQuarantineToUserQuery(connection).execute();

        verify(resultSet4Metadata, times(1)).getMetaData();
        verify(connection).prepareStatement("delete USER from USER inner join QUARANTINE"
                                            + " on convert(varchar,USER.TOTO_COL) = convert(varchar,QUARANTINE.TOTO_COL)"
                                            + " where QUARANTINE.ERROR_TYPE > 0 ");
        verify(connection).prepareStatement(Q_TO_USER);
        verify(connection).prepareStatement(Q_TO_USER_DELETE);
        verify(statement, times(3)).execute();

        // QUARANTINE TO USER - Mecanisme de cache
        setupNewMockedConnection();

        transfert.getQuarantineToUserQuery(connection).execute();

        verify(connection, times(0)).getMetaData();
        verify(connection).prepareStatement("delete USER from USER inner join QUARANTINE"
                                            + " on convert(varchar,USER.TOTO_COL) = convert(varchar,QUARANTINE.TOTO_COL)"
                                            + " where QUARANTINE.ERROR_TYPE > 0 ");
        verify(connection).prepareStatement(Q_TO_USER);
        verify(connection).prepareStatement(Q_TO_USER_DELETE);
        verify(statement, times(3)).execute();

        // USER TO QUARANTINE
        setupNewMockedConnection();

        transfert.getUserToQuarantineQuery(connection).execute();

        verify(connection, times(0)).getMetaData();
        verify(connection).prepareStatement(USER_TO_Q);
        verify(connection).prepareStatement(USER_TO_Q_DELETE);
        verify(statement, times(2)).execute();
    }


    public void test_buildQuery_replaceUserData() throws Exception {
        transfert = new TransfertData("QUARANTINE", "USER");
        transfert.addMatchingCol("TOTO_COL");
        transfert.addMatchingCol("TiTi_COL");
        transfert.setReplaceUserData(true);

        assertTrue(transfert.isReplaceUserData());
        assertEquals("[TOTO_COL, TiTi_COL]", transfert.getMatchingCols().toString());
        assertUnmodifiableList(transfert.getMatchingCols());

        // QUARANTINE TO USER
        transfert.getQuarantineToUserQuery(connection).execute();

        verify(resultSet4Metadata, times(1)).getMetaData();
        verify(connection).prepareStatement("delete USER from USER inner join QUARANTINE"
                                            + " on convert(varchar,USER.TOTO_COL) +'µ²'+ convert(varchar,USER.TiTi_COL) = "
                                            + "convert(varchar,QUARANTINE.TOTO_COL) +'µ²'+ convert(varchar,QUARANTINE.TiTi_COL)"
                                            + " where QUARANTINE.ERROR_TYPE > 0 ");
        verify(connection).prepareStatement(Q_TO_USER);
        verify(connection).prepareStatement(Q_TO_USER_DELETE);
        verify(statement, times(3)).execute();

        // USER TO QUARANTINE
        setupNewMockedConnection();

        transfert.getUserToQuarantineQuery(connection).execute();

        verify(connection, times(0)).getMetaData();
        verify(connection).prepareStatement(USER_TO_Q);
        verify(connection).prepareStatement(USER_TO_Q_DELETE);
        verify(statement, times(2)).execute();
    }


    public void test_buildQuery() throws Exception {
        transfert = new TransfertData("QUARANTINE", "USER");

        // USER TO QUARANTINE
        transfert.getUserToQuarantineQuery(connection).execute();
        verify(resultSet4Metadata, times(1)).getMetaData();
        verify(connection).prepareStatement(USER_TO_Q);
        verify(connection).prepareStatement(USER_TO_Q_DELETE);
        verify(statement, times(2)).execute();

        // QUARANTINE TO USER
        setupNewMockedConnection();

        transfert.getQuarantineToUserQuery(connection).execute();
        verify(connection, times(0)).getMetaData();
        verify(connection).prepareStatement(Q_TO_USER);
        verify(connection).prepareStatement(Q_TO_USER_DELETE);
        verify(statement, times(2)).execute();
    }


    private void assertUnmodifiableList(List matchingCols) {
        try {
            matchingCols.remove(0);
            fail("Liste non modifiable");
        }
        catch (Exception ex) {
            // Ok
        }
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupNewMockedConnection();
        mockMetadataStuff();
    }


    private void setupNewMockedConnection() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }


    private void mockMetadataStuff() throws SQLException {
        Statement statement4Metadadta = mock(Statement.class);
        resultSet4Metadata = mock(ResultSet.class);
        ResultSetMetaData resultSetMetadata = mock(ResultSetMetaData.class);

        when(connection.createStatement()).thenReturn(statement4Metadadta);

        String metadataQuery = "select * from QUARANTINE where 1 = 0";
        when(statement4Metadadta.executeQuery(metadataQuery)).thenReturn(resultSet4Metadata);
        when(statement4Metadadta.executeQuery(not(eq(metadataQuery))))
              .thenThrow(new SQLException("Unexpected query has been detected -> != " + metadataQuery));

        when(resultSet4Metadata.getMetaData()).thenReturn(resultSetMetadata);
        when(resultSetMetadata.getColumnCount()).thenReturn(3);
        when(resultSetMetadata.getColumnLabel(1)).thenReturn("QUARANTINE_ID");
        when(resultSetMetadata.getColumnLabel(2)).thenReturn("TOTO_COL");
        when(resultSetMetadata.getColumnLabel(3)).thenReturn("TiTi_COL");
    }
}
