package com.yingzi.bigdata.presto.snappydata.frame;

import com.facebook.presto.plugin.jdbc.*;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.VarcharType;
import com.google.common.collect.ImmutableSet;
import io.snappydata.jdbc.ClientPoolDriver;

import javax.inject.Inject;
import java.sql.*;
import java.util.Properties;
import java.util.Set;

import static com.facebook.presto.plugin.jdbc.DriverConnectionFactory.basicConnectionProperties;
import static com.facebook.presto.spi.StandardErrorCode.NOT_SUPPORTED;
import static com.facebook.presto.spi.type.RealType.REAL;
import static com.facebook.presto.spi.type.TimeWithTimeZoneType.TIME_WITH_TIME_ZONE;
import static com.facebook.presto.spi.type.TimestampType.TIMESTAMP;
import static com.facebook.presto.spi.type.TimestampWithTimeZoneType.TIMESTAMP_WITH_TIME_ZONE;
import static com.facebook.presto.spi.type.VarbinaryType.VARBINARY;
import static com.facebook.presto.spi.type.Varchars.isVarcharType;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.util.Locale.ENGLISH;

public class SnappydataClient extends BaseJdbcClient
{
    @Inject
    public SnappydataClient(JdbcConnectorId connectorId, BaseJdbcConfig config, SnappydataConfig mySqlConfig) throws SQLException
    {
        super(connectorId, config, "`", connectionFactory(config, mySqlConfig));
    }

    private static ConnectionFactory connectionFactory(BaseJdbcConfig config, SnappydataConfig mySqlConfig)
            throws SQLException
    {
        Properties connectionProperties = basicConnectionProperties(config);
        connectionProperties.setProperty("driver", "io.snappydata.jdbc.ClientPoolDriver");
        connectionProperties.setProperty("pool.initialSize", String.valueOf(mySqlConfig.getInitialSize()));
        if (mySqlConfig.isRemoveAbandoned()) {
            connectionProperties.setProperty("pool.removeAbandoned", String.valueOf(mySqlConfig.isRemoveAbandoned()));
            connectionProperties.setProperty("pool.removeAbandonedTimeout", String.valueOf(mySqlConfig.getRemoveAbandonedTimeout().toMillis()));
        }
        if (mySqlConfig.getMaxIdle() > 0) {
            connectionProperties.setProperty("pool.maxIdle", String.valueOf(mySqlConfig.getMaxIdle()));
        }
        if (mySqlConfig.getMinIdle() > 0) {
            connectionProperties.setProperty("pool.minIdle", String.valueOf(mySqlConfig.getMinIdle()));
        }
        if (mySqlConfig.getMaxWait() != null) {
            connectionProperties.setProperty("pool.maxWait", String.valueOf(mySqlConfig.getMaxWait().toMillis()));
        }

        return new DriverConnectionFactory(new ClientPoolDriver(), config.getConnectionUrl(), connectionProperties);
    }

    @Override
    public Set<String> getSchemaNames()
    {
        // for MySQL, we need to list catalogs instead of schemas
        try (Connection connection = connectionFactory.openConnection();
             ResultSet resultSet = connection.getMetaData().getSchemas()) {
            ImmutableSet.Builder<String> schemaNames = ImmutableSet.builder();
            while (resultSet.next()) {
                String schemaName = resultSet.getString("TABLE_SCHEM").toLowerCase(ENGLISH);
                // skip internal schemas
                //if (!schemaName.equals("information_schema") && !schemaName.equals("mysql")) {
                    schemaNames.add(schemaName);
                //}
            }
            return schemaNames.build();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void abortReadConnection(Connection connection)
            throws SQLException
    {
        // Abort connection before closing. Without this, the MySQL driver
        // attempts to drain the connection by reading all the results.
        connection.abort(directExecutor());
    }

    @Override
    public PreparedStatement getPreparedStatement(Connection connection, String sql)
            throws SQLException
    {
        connection.setAutoCommit(false);
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setFetchSize(1000);
        return statement;
    }

//    @Override
//    protected ResultSet getTables(Connection connection, String schemaName, String tableName)
//            throws SQLException
//    {
//        // MySQL maps their "database" to SQL catalogs and does not have schemas
//        DatabaseMetaData metadata = connection.getMetaData();
//        String escape = metadata.getSearchStringEscape();
//        return metadata.getTables(
//                schemaName,
//                null,
//                escapeNamePattern(tableName, escape),
//                new String[] {"TABLE", "VIEW"});
//    }

    @Override
    protected ResultSet getTables(Connection connection, String schemaName, String tableName)
            throws SQLException
    {
        DatabaseMetaData metadata = connection.getMetaData();
        String escape = metadata.getSearchStringEscape();
        return metadata.getTables(
                connection.getCatalog(),
                escapeNamePattern(schemaName, escape),
                escapeNamePattern(tableName, escape),
                new String[] {"TABLE", "VIEW", "MATERIALIZED VIEW", "FOREIGN TABLE"});
    }

//    @Override
//    protected SchemaTableName getSchemaTableName(ResultSet resultSet)
//            throws SQLException
//    {
//        // MySQL uses catalogs instead of schemas
//        return new SchemaTableName(
//                resultSet.getString("TABLE_CAT").toLowerCase(ENGLISH),
//                resultSet.getString("TABLE_NAME").toLowerCase(ENGLISH));
//    }

    @Override
    protected String toSqlType(Type type)
    {
        if (REAL.equals(type)) {
            return "float";
        }
        if (TIME_WITH_TIME_ZONE.equals(type) || TIMESTAMP_WITH_TIME_ZONE.equals(type)) {
            throw new PrestoException(NOT_SUPPORTED, "Unsupported column type: " + type.getDisplayName());
        }
        if (TIMESTAMP.equals(type)) {
            return "datetime";
        }
        if (VARBINARY.equals(type)) {
            return "mediumblob";
        }
        if (isVarcharType(type)) {
            VarcharType varcharType = (VarcharType) type;
            if (varcharType.isUnbounded()) {
                return "longtext";
            }
            if (varcharType.getLengthSafe() <= 255) {
                return "tinytext";
            }
            if (varcharType.getLengthSafe() <= 65535) {
                return "text";
            }
            if (varcharType.getLengthSafe() <= 16777215) {
                return "mediumtext";
            }
            return "longtext";
        }

        return super.toSqlType(type);
    }
}
