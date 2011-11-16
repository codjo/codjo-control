/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.easymock.MockControl;
/**
 * Description of the Class
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.5 $
 */
public class EntityHelperTest extends TestCase {
    private MockControl connectionControl;
    private MockPropertyConverter converter;
    private EntityHelper helper;
    private EntityHelper helperCplx;
    private Connection mockConnection;
    private ResultSet mockResultSet;
    private PreparedStatement mockStatement;
    private MockControl resultsetControl;
    private MockControl statementControl;


    public EntityHelperTest(String testCaseName) {
        super(testCaseName);
    }


    public void test_buildSQLInsert_update() {
        assertEquals("insert into #CTRL"
                     + " (QUARANTINE_ID, AUDIT_ID, CODE, ENTITY_DATE, EXTERNAL, LABEL)"
                     + " values (?, ?, ?, ?, ?, ?)", helper.buildSQLInsert("#CTRL"));
        assertEquals("update #CTRL"
                     + " set AUDIT_ID=? , CODE=? , ENTITY_DATE=? , EXTERNAL=? , LABEL=?"
                     + " where QUARANTINE_ID=?", helper.buildSQLUpdate("#CTRL"));
    }


    public void test_buildSQLInsert_Cplx() {
        String expected =
              "insert into #CTRL" + " (QUARANTINE_ID, CODE, LINK)" + " values (?, ?, ?)";

        assertEquals(expected, helperCplx.buildSQLInsert("#CTRL"));

        assertEquals("update #CTRL" + " set CODE=? , LINK=?" + " where QUARANTINE_ID=?",
                     helperCplx.buildSQLUpdate("#CTRL"));
    }


    public void test_updateIntoTable() throws Exception {
        String query =
              "update #CTRL"
              + " set AUDIT_ID=? , CODE=? , ENTITY_DATE=? , EXTERNAL=? , LABEL=?"
              + " where QUARANTINE_ID=?";

        MockEntity entity = mockExecuteQuery(query, false);

        activateMock();

        helper.updateTable(mockConnection, entity, "#CTRL");

        verifyMock();
    }


    public void test_updateIntoTable_Cplx() throws Exception {
        String query = "update #CTRL set CODE=? , LINK=? where QUARANTINE_ID=?";

        MockComplexEntity entity = mockExecuteCplxQuery(query, false);

        activateMock();

        helperCplx.updateTable(mockConnection, entity, "#CTRL");

        verifyMock();
    }


    public void test_insertIntoTable() throws Exception {
        String query =
              "insert into #CTRL"
              + " (QUARANTINE_ID, AUDIT_ID, CODE, ENTITY_DATE, EXTERNAL, LABEL)"
              + " values (?, ?, ?, ?, ?, ?)";

        MockEntity entity = mockExecuteQuery(query, true);

        activateMock();

        helper.insertIntoTable(mockConnection, entity, "#CTRL");

        verifyMock();
    }


    public void test_insertIntoTable_Cplx() throws Exception {
        String query =
              "insert into #CTRL" + " (QUARANTINE_ID, CODE, LINK)" + " values (?, ?, ?)";

        MockComplexEntity entity = mockExecuteCplxQuery(query, true);

        activateMock();

        helperCplx.insertIntoTable(mockConnection, entity, "#CTRL");

        verifyMock();
    }


    public void test_insertIntoTable_Cplx_null() throws Exception {
        String query =
              "insert into #CTRL" + " (QUARANTINE_ID, CODE, LINK)" + " values (?, ?, ?)";

        mockCreatePrepareStatement(query);
        mockStatement.setInt(1, 1);
        mockStatement.setObject(2, 1, Types.INTEGER);
        mockStatement.setNull(3, Types.INTEGER);
        mockStatement.executeUpdate();
        statementControl.setReturnValue(1, 1);
        mockCleanup();
        activateMock();

        MockComplexEntity entity = new MockComplexEntity();
        entity.setCode(1);
        entity.setLink(null);

        helperCplx.insertIntoTable(mockConnection, entity, "#CTRL");

        verifyMock();
    }


    public void test_iterator() throws Exception {
        String query =
              "select QUARANTINE_ID, ERROR_TYPE, ERROR_LOG, AUDIT_ID, CODE, ENTITY_DATE, EXTERNAL, LABEL"
              + " from #CTRL where ERROR_TYPE <=0";
        mockConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                       ResultSet.CONCUR_UPDATABLE);
        connectionControl.setReturnValue(mockStatement, 1);
        mockStatement.executeQuery(query);
        statementControl.setReturnValue(mockResultSet, 1);
        activateMock();

        EntityIterator iter = helper.iterator(mockConnection, "#CTRL");

        verifyMock();

        assertNotNull(iter);
    }


    public void test_toSqlName() {
        assertEquals("PIMS", helper.toSqlName("pims"));
        assertEquals("PIMS_CODE", helper.toSqlName("pimsCode"));
        assertEquals("EXT_VAL_VL", helper.toSqlName("extValVl"));
        assertEquals("L_NL_FLAG", helper.toSqlName("LNlFlag"));
        assertEquals("UNE_U_R_L", helper.toSqlName("UneURL"));
    }


    public void test_updateObject() throws Exception {
        String query = "select * from #CTRL where QUARANTINE_ID=1";

        mockCreateStatement();
        mockCreateResultSet(query);

        mockResultSet.next();
        resultsetControl.setReturnValue(true, 1);

        mockResultSet.getInt("ERROR_TYPE");
        resultsetControl.setReturnValue(0, 1);

        mockResultGet("AUDIT_ID", new BigDecimal("1"));
        mockResultGet("CODE", 2);
        mockResultGet("ENTITY_DATE", null);
        mockResultGet("EXTERNAL", Boolean.FALSE);
        mockResultGet("LABEL", "bobo");

        mockStatement.close();
        statementControl.setVoidCallable(1);
        activateMock();

        MockEntity entity = new MockEntity();
        EntityResultState err = helper.updateObject(mockConnection, entity, "#CTRL");
        assertEquals(EntityResultState.NO_ERROR, err.getErrorType());

        verifyMock();

        assertEquals(new BigDecimal("1"), entity.getAuditId());
        assertEquals(new Integer(2), entity.getCode());
        assertEquals(null, entity.getEntityDate());
        assertEquals(Boolean.FALSE, entity.getExternal());
        assertEquals("bobo", entity.getLabel());
    }


    public void test_updateObject_Cplx() throws Exception {
        String query = "select * from #CTRL where QUARANTINE_ID=1";

        mockCreateStatement();
        mockCreateResultSet(query);

        mockResultSet.next();
        resultsetControl.setReturnValue(true, 1);

        mockResultSet.getInt("ERROR_TYPE");
        resultsetControl.setReturnValue(0, 1);

        mockResultGet("CODE", 1);
        mockResultGet("LINK", 2);

        mockStatement.close();
        statementControl.setVoidCallable(1);

        converter.loadedObject = new MockEntity(2);
        activateMock();

        MockComplexEntity entity = new MockComplexEntity();
        EntityResultState err = helperCplx.updateObject(mockConnection, entity, "#CTRL");
        assertEquals(EntityResultState.NO_ERROR, err.getErrorType());

        verifyMock();
        assertTrue(converter.loadCalled);
        assertEquals(new Integer(1), entity.getCode());
        assertEquals(new Integer(2), entity.getLink().getCode());
    }


    public void test_updateObject_controlError() throws Exception {
        doTestUpdateObjectControlError(helper, new MockEntity());
    }


    public void test_updateObject_controlError_Cplx()
          throws Exception {
        doTestUpdateObjectControlError(helperCplx, new MockComplexEntity());
    }


    public void test_updateResultSet() throws Exception {
        MockEntity obj = new MockEntity();
        obj.setAuditId(new BigDecimal("1111"));
        obj.setCode(2222);
        obj.setEntityDate(null);
        obj.setExternal(Boolean.FALSE);
        obj.setLabel("bobo");

        int index = 4;
        mockResultUpdate(index++, obj.getAuditId());
        mockResultUpdate(index++, obj.getCode());
        mockResultUpdate(index++, obj.getEntityDate());
        mockResultUpdate(index++, obj.getExternal());
        mockResultUpdate(index, obj.getLabel());

        activateMock();

        helper.updateResultSet(mockResultSet, obj, null);

        verifyMock();
    }


    public void test_updateResultSet_controlError()
          throws Exception {
        MockEntity obj = new MockEntity();
        obj.setAuditId(new BigDecimal("1111"));
        obj.setCode(2222);
        obj.setEntityDate(null);
        obj.setExternal(Boolean.FALSE);
        obj.setLabel("bobo");
        DefaultEntityResultState entityResultState = new DefaultEntityResultState(69, "erreur 69");

        int index = 2;
        mockResultSet.updateInt(index++, entityResultState.getErrorType());
        resultsetControl.setVoidCallable(1);
        mockResultSet.updateString(index, entityResultState.getErrorLog());
        resultsetControl.setVoidCallable(1);

        activateMock();

        helper.updateResultSet(mockResultSet, obj, entityResultState);

        verifyMock();
    }


    @Override
    protected void setUp() throws Exception {
        converter = new MockPropertyConverter("link", "code");
        helper = new EntityHelper(EntityHelperTest.MockEntity.class);
        helperCplx = new EntityHelper(EntityHelperTest.MockComplexEntity.class);
        helperCplx.setConverters(new PropertyConverter[]{converter});
        initMockStuff();
    }


    @Override
    protected void tearDown() {
    }


    private void activateMock() {
        connectionControl.replay();
        statementControl.replay();
        resultsetControl.replay();
    }


    private void initMockStuff() {
        connectionControl = MockControl.createControl(Connection.class);
        mockConnection = (Connection)connectionControl.getMock();
        statementControl = MockControl.createControl(PreparedStatement.class);
        mockStatement = (PreparedStatement)statementControl.getMock();
        resultsetControl = MockControl.createControl(ResultSet.class);
        mockResultSet = (ResultSet)resultsetControl.getMock();
    }


    private void mockCleanup() throws SQLException {
        mockStatement.getWarnings();
        statementControl.setReturnValue(null, 1);
        mockStatement.close();
        statementControl.setVoidCallable(1);
    }


    private void mockCreatePrepareStatement(final String query)
          throws SQLException {
        mockConnection.prepareStatement(query);
        connectionControl.setReturnValue(mockStatement, 1);
    }


    private void mockCreateResultSet(final String query)
          throws SQLException {
        mockStatement.executeQuery(query);
        statementControl.setReturnValue(mockResultSet, 1);
    }


    private void mockCreateStatement() throws SQLException {
        mockConnection.createStatement();
        connectionControl.setReturnValue(mockStatement, 1);
    }


    private void mockResultGet(final String col, final Object val)
          throws SQLException {
        mockResultSet.getObject(col);
        resultsetControl.setReturnValue(val, 1);
    }


    private void mockResultUpdate(final int index, final Object val)
          throws SQLException {
        if (val == null) {
            mockResultSet.updateNull(index);
            resultsetControl.setVoidCallable(1);
        }
        else {
            mockResultSet.updateObject(index, val);
            resultsetControl.setVoidCallable(1);
        }
    }


    private void doTestUpdateObjectControlError(EntityHelper entityHelper, Object entity)
          throws Exception {
        String query = "select * from #CTRL where QUARANTINE_ID=1";

        mockCreateStatement();
        mockCreateResultSet(query);

        mockResultSet.next();
        resultsetControl.setReturnValue(true, 1);

        mockResultSet.getInt("ERROR_TYPE");
        resultsetControl.setReturnValue(1, 1);
        mockResultSet.getString("ERROR_LOG");
        resultsetControl.setReturnValue("erreur", 1);

        mockStatement.close();
        statementControl.setVoidCallable(1);
        activateMock();

        EntityResultState ex = entityHelper.updateObject(mockConnection, entity, "#CTRL");
        assertNotNull("Ligne en erreur !", ex);
        assertEquals(1, ex.getErrorType());
        assertEquals("erreur", ex.getErrorLog());
        verifyMock();
    }


    private MockEntity mockExecuteQuery(String query, boolean pkFirst)
          throws SQLException {
        mockCreatePrepareStatement(query);
        int idx = 0;
        if (pkFirst) {
            mockStatement.setInt(++idx, 1);
        }
        mockStatement.setObject(++idx, new BigDecimal("1"), Types.NUMERIC);
        mockStatement.setObject(++idx, 2, Types.INTEGER);
        mockStatement.setObject(++idx, Timestamp.valueOf("2002-02-02 10:00:00"),
                                Types.TIMESTAMP);
        mockStatement.setObject(++idx, Boolean.FALSE, Types.BIT);
        mockStatement.setNull(++idx, Types.VARCHAR);
        if (!pkFirst) {
            mockStatement.setInt(++idx, 1);
        }

        mockStatement.executeUpdate();
        statementControl.setReturnValue(1, 1);
        mockCleanup();

        MockEntity entity = new MockEntity();
        entity.setAuditId(new BigDecimal("1"));
        entity.setCode(2);
        entity.setEntityDate(Timestamp.valueOf("2002-02-02 10:00:00"));
        entity.setExternal(Boolean.FALSE);
        entity.setLabel(null);
        return entity;
    }


    private MockComplexEntity mockExecuteCplxQuery(String query, boolean pkFirst)
          throws SQLException {
        mockCreatePrepareStatement(query);
        int idx = 0;
        if (pkFirst) {
            mockStatement.setInt(++idx, 1);
        }
        mockStatement.setObject(++idx, 1, Types.INTEGER);
        mockStatement.setObject(++idx, 2, Types.INTEGER);
        if (!pkFirst) {
            mockStatement.setInt(++idx, 1);
        }
        mockStatement.executeUpdate();
        statementControl.setReturnValue(1, 1);
        mockCleanup();

        MockEntity subEntity = new MockEntity();
        subEntity.setCode(2);

        MockComplexEntity entity = new MockComplexEntity();
        entity.setCode(1);
        entity.setLink(subEntity);
        return entity;
    }


    private void verifyMock() throws AssertionFailedError {
        connectionControl.verify();
        statementControl.verify();
        resultsetControl.verify();
    }


    public class MockComplexEntity {
        private Integer code;
        private MockEntity link;


        public void setCode(Integer pimscode) {
            this.code = pimscode;
        }


        public void setLink(MockEntity link) {
            this.link = link;
        }


        public Integer getCode() {
            return code;
        }


        public MockEntity getLink() {
            return link;
        }
    }

    /**
     * Exemple d'entité pour les tests.
     *
     * @author $Author: gonnot $
     * @version $Revision: 1.5 $
     */
    public class MockEntity {
        private BigDecimal auditId;
        private Integer code;
        private Timestamp entityDate;
        private Boolean external;
        private String label;


        public MockEntity() {
        }


        public MockEntity(Integer code) {
            setCode(code);
        }


        public void setAuditId(BigDecimal auditId) {
            this.auditId = auditId;
        }


        public void setCode(Integer pimscode) {
            this.code = pimscode;
        }


        public void setEntityDate(Timestamp entityDate) {
            this.entityDate = entityDate;
        }


        public void setExternal(Boolean external) {
            this.external = external;
        }


        public void setLabel(String label) {
            this.label = label;
        }


        public BigDecimal getAuditId() {
            return auditId;
        }


        public Integer getCode() {
            return code;
        }


        public Timestamp getEntityDate() {
            return entityDate;
        }


        public Boolean getExternal() {
            return external;
        }


        public String getLabel() {
            return label;
        }
    }

    public class MockPropertyConverter extends PropertyConverter {
        boolean loadCalled = false;
        Object loadedObject = null;


        public MockPropertyConverter(String propertyName, String primaryKey) {
            super(propertyName, primaryKey);
        }


        @Override
        public Object load(Class clazz, Object pk) {
            loadCalled = true;
            return loadedObject;
        }
    }
}
