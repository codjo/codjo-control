/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import junit.framework.TestCase;
import org.easymock.MockControl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TransfertDataTest extends TestCase {
    private static final String Q_TO_USER =
          "INSERT INTO USER ( QUARANTINE_ID, TOTO_COL, TiTi_COL ) "
          + "SELECT QUARANTINE.QUARANTINE_ID, QUARANTINE.TOTO_COL, QUARANTINE.TiTi_COL "
          + "FROM QUARANTINE WHERE QUARANTINE.ERROR_TYPE > 0";
    private static final String Q_TO_USER_DELETE =
          "DELETE QUARANTINE FROM QUARANTINE INNER JOIN USER "
          + "ON QUARANTINE.QUARANTINE_ID = USER.QUARANTINE_ID";
    private static final String USER_TO_Q =
          "INSERT INTO QUARANTINE ( TOTO_COL, TiTi_COL ) "
          + "SELECT USER.TOTO_COL, USER.TiTi_COL FROM USER WHERE USER.ERROR_TYPE <= 0";
    private static final String USER_TO_Q_DELETE =
          "DELETE USER WHERE ERROR_TYPE <= 0";
    private TransfertData transfert;
    private MockControl metaDataControl;
    private DatabaseMetaData metaDataMock;
    private MockControl rsControl;
    private ResultSet rsMock;
    private Connection connection;
    private PreparedStatement statement;


    public void test_buildQuery_replaceUserData_singleColumn() throws Exception {
        transfert = new TransfertData("QUARANTINE", "USER");
        transfert.addMatchingCol("TOTO_COL");
        transfert.setReplaceUserData(true);

        // QUARANTINE TO USER
        transfert.getQuarantineToUserQuery(connection).execute();

        verify(connection, times(1)).getMetaData();
        verify(connection).prepareStatement("delete USER from USER inner join QUARANTINE"
                                            + " on convert(varchar,USER.TOTO_COL) = convert(varchar,QUARANTINE.TOTO_COL)"
                                            + " where QUARANTINE.ERROR_TYPE > 0 ");
        verify(connection).prepareStatement(Q_TO_USER);
        verify(connection).prepareStatement(Q_TO_USER_DELETE);
        verify(statement, times(3)).execute();
        verifyMockCalls();

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

        verify(connection, times(1)).getMetaData();
        verify(connection).prepareStatement("delete USER from USER inner join QUARANTINE"
                                            + " on convert(varchar,USER.TOTO_COL) +'µ²'+ convert(varchar,USER.TiTi_COL) = "
                                            + "convert(varchar,QUARANTINE.TOTO_COL) +'µ²'+ convert(varchar,QUARANTINE.TiTi_COL)"
                                            + " where QUARANTINE.ERROR_TYPE > 0 ");
        verify(connection).prepareStatement(Q_TO_USER);
        verify(connection).prepareStatement(Q_TO_USER_DELETE);
        verify(statement, times(3)).execute();
        verifyMockCalls();

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
        verify(connection, times(1)).getMetaData();
        verify(connection).prepareStatement(USER_TO_Q);
        verify(connection).prepareStatement(USER_TO_Q_DELETE);
        verify(statement, times(2)).execute();
        verifyMockCalls();

        // QUARANTINE TO USER
        setupNewMockedConnection();

        transfert.getQuarantineToUserQuery(connection).execute();
        verify(connection, times(0)).getMetaData();
        verify(connection).prepareStatement(Q_TO_USER);
        verify(connection).prepareStatement(Q_TO_USER_DELETE);
        verify(statement, times(2)).execute();
    }


    private void verifyMockCalls() {
        // Check Mock
        metaDataControl.verify();
        rsControl.verify();
    }


    private void mockMetadataCall() throws SQLException {
        // Mock : md.getColumns(null, null, "QUARANTINE", null);
        metaDataMock.getColumns(null, null, "QUARANTINE", null);
        metaDataControl.setReturnValue(rsMock, 1);
        // Mock : La recuperation des Cols de "QUARANTINE" (rs.getString(4))
        mockColumns();

        // Activate control
        metaDataControl.replay();
        rsControl.replay();
    }


    private void mockColumns() throws SQLException {
        rsMock.next();
        rsControl.setReturnValue(true, 1);
        rsMock.getString(4);
        rsControl.setReturnValue("QUARANTINE_ID", 1);
        rsMock.next();
        rsControl.setReturnValue(true, 1);
        rsMock.getString(4);
        rsControl.setReturnValue("TOTO_COL", 1);
        rsMock.next();
        rsControl.setReturnValue(true, 1);
        rsMock.getString(4);
        rsControl.setReturnValue("TiTi_COL", 1);
        rsMock.next();
        rsControl.setReturnValue(false, 1);
        rsMock.close();
        rsControl.setVoidCallable(1);
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
        metaDataControl = MockControl.createControl(DatabaseMetaData.class);
        metaDataMock = (DatabaseMetaData)metaDataControl.getMock();

        rsControl = MockControl.createControl(ResultSet.class);
        rsMock = (ResultSet)rsControl.getMock();

        mockMetadataCall();
        setupNewMockedConnection();
    }


    private void setupNewMockedConnection() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(connection.getMetaData()).thenReturn(metaDataMock);
    }
}
