/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import junit.framework.TestCase;
import org.easymock.MockControl;

/**
 *
 */
public class TransfertDataTest extends TestCase {
    private static final String Q_TO_USER =
          "INSERT INTO USER ( QUARANTINE_ID, TOTO_COL, TiTi_COL ) "
          + "SELECT QUARANTINE.QUARANTINE_ID, QUARANTINE.TOTO_COL, QUARANTINE.TiTi_COL "
          + "FROM QUARANTINE WHERE QUARANTINE.ERROR_TYPE > 0 "
          + "DELETE QUARANTINE FROM QUARANTINE INNER JOIN USER "
          + "ON QUARANTINE.QUARANTINE_ID = USER.QUARANTINE_ID";
    private static final String USER_TO_Q =
          "INSERT INTO QUARANTINE ( TOTO_COL, TiTi_COL ) "
          + "SELECT USER.TOTO_COL, USER.TiTi_COL FROM USER WHERE USER.ERROR_TYPE <= 0 "
          + "DELETE USER WHERE ERROR_TYPE <= 0";
    private TransfertData transfert;
    private MockControl connectionControl;
    private Connection connectionMock;
    private MockControl metaDataControl;
    private DatabaseMetaData metaDataMock;
    private MockControl rsControl;
    private ResultSet rsMock;


    public void test_buildQuery_replaceUserData_singleColumn()
          throws Exception {
        mockMetadataCall();

        transfert = new TransfertData("QUARANTINE", "USER");
        transfert.addMatchingCol("TOTO_COL");
        transfert.setReplaceUserData(true);

        // QUARANTINE TO USER
        String q2user = transfert.getQuarantineToUserQuery(connectionMock);
        assertEquals("delete USER from USER inner join QUARANTINE"
                     + " on convert(varchar,USER.TOTO_COL) = convert(varchar,QUARANTINE.TOTO_COL)"
                     + " where QUARANTINE.ERROR_TYPE > 0 " + Q_TO_USER, q2user);
        assertEquals("2 appels declenche le même resultat :) Et sans de nouvelle requete Meta",
                     q2user, transfert.getQuarantineToUserQuery(connectionMock));

        // USER TO QUARANTINE
        String user2q = transfert.getUserToQuarantineQuery(connectionMock);
        assertEquals(USER_TO_Q, user2q);
        assertEquals("2 appels declenche le même resultat :) Et sans de nouvelle requete Meta",
                     user2q, transfert.getUserToQuarantineQuery(connectionMock));

        // Mock stuff
        verifyMetadataCall();
    }


    public void test_buildQuery_replaceUserData()
          throws Exception {
        mockMetadataCall();

        transfert = new TransfertData("QUARANTINE", "USER");
        transfert.addMatchingCol("TOTO_COL");
        transfert.addMatchingCol("TiTi_COL");
        transfert.setReplaceUserData(true);
        assertTrue(transfert.isReplaceUserData());
        assertEquals("[TOTO_COL, TiTi_COL]", transfert.getMatchingCols().toString());
        assertUnmodifiableList(transfert.getMatchingCols());

        // QUARANTINE TO USER
        String q2user = transfert.getQuarantineToUserQuery(connectionMock);
        assertEquals("delete USER from USER inner join QUARANTINE"
                     + " on convert(varchar,USER.TOTO_COL) +'µ²'+ convert(varchar,USER.TiTi_COL) = "
                     + "convert(varchar,QUARANTINE.TOTO_COL) +'µ²'+ convert(varchar,QUARANTINE.TiTi_COL)"
                     + " where QUARANTINE.ERROR_TYPE > 0 " + Q_TO_USER, q2user);
        assertEquals("2 appels declenche le même resultat :) Et sans de nouvelle requete Meta",
                     q2user, transfert.getQuarantineToUserQuery(connectionMock));

        // USER TO QUARANTINE
        String user2q = transfert.getUserToQuarantineQuery(connectionMock);
        assertEquals(USER_TO_Q, user2q);
        assertEquals("2 appels declenche le même resultat :) Et sans de nouvelle requete Meta",
                     user2q, transfert.getUserToQuarantineQuery(connectionMock));

        // Mock stuff
        verifyMetadataCall();
    }


    public void test_buildQuery() throws Exception {
        mockMetadataCall();

        transfert = new TransfertData("QUARANTINE", "USER");

        // USER TO QUARANTINE
        String user2q = transfert.getUserToQuarantineQuery(connectionMock);
        assertEquals(USER_TO_Q, user2q);
        assertEquals("2 appels declenche le même resultat :) Et sans de nouvelle requete Meta",
                     user2q, transfert.getUserToQuarantineQuery(connectionMock));

        // QUARANTINE TO USER
        String q2user = transfert.getQuarantineToUserQuery(connectionMock);
        assertEquals(Q_TO_USER, q2user);
        assertEquals("2 appels declenche le même resultat :) Et sans de nouvelle requete Meta",
                     q2user, transfert.getQuarantineToUserQuery(connectionMock));

        // Mock stuff
        verifyMetadataCall();
    }


    private void verifyMetadataCall() {
        // Check Mock
        connectionControl.verify();
        metaDataControl.verify();
        rsControl.verify();
    }


    private void mockMetadataCall() throws SQLException {
        // Mock : con.getMetaData(); + close();
        connectionMock.getMetaData();
        connectionControl.setReturnValue(metaDataMock, 1);
        connectionControl.replay();

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
        connectionControl = MockControl.createControl(Connection.class);
        connectionMock = (Connection)connectionControl.getMock();

        metaDataControl = MockControl.createControl(DatabaseMetaData.class);
        metaDataMock = (DatabaseMetaData)metaDataControl.getMock();

        rsControl = MockControl.createControl(ResultSet.class);
        rsMock = (ResultSet)rsControl.getMock();
    }
}
