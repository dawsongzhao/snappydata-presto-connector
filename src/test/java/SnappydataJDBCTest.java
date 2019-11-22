import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.snappydata.jdbc.ClientDriver;
import io.snappydata.jdbc.ClientPoolDriver;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import static java.util.Locale.ENGLISH;

public class SnappydataJDBCTest {

    public static Set<String> getSchemaNames(Connection connection)
    {
        // for MySQL, we need to list catalogs instead of schemas
        try (
             ResultSet resultSet = connection.getMetaData().getSchemas()) {
            DatabaseMetaData metaData = connection.getMetaData();
//            System.out.println(" -:"+metaData.getDatabaseMajorVersion());
//            System.out.println(" -:"+metaData.getDatabaseProductVersion());
//            System.out.println(" -:"+metaData.getDatabaseProductName());
//            System.out.println(" -:"+metaData.getSchemaTerm());
//            System.out.println(" -:"+metaData.getSchemas().toString());
//            System.out.println(" -:"+metaData.getMaxUserNameLength());

            ImmutableSet.Builder<String> schemaNames = ImmutableSet.builder();
            //System.out.println(resultSet.toString());

            while (resultSet.next()) {
                String schemaName = resultSet.getString("TABLE_SCHEM").toLowerCase(ENGLISH);
                if ( resultSet.getString("TABLE_CATALOG") != null ) {
                    String catalogName = resultSet.getString("TABLE_CATALOG").toLowerCase(ENGLISH);
                    System.out.println(catalogName);
                }
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
    protected static String escapeNamePattern(String name, String escape) {
        if (name != null && escape != null) {
            Preconditions.checkArgument(!escape.equals("_"), "Escape string must not be '_'");
            Preconditions.checkArgument(!escape.equals("%"), "Escape string must not be '%'");
            name = name.replace(escape, escape + escape);
            name = name.replace("_", escape + "_");
            name = name.replace("%", escape + "%");
            return name;
        } else {
            return name;
        }
    }
    public static ResultSet getTables(Connection connection, String schemaName, String tableName)
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

    public static void main(String[] args)throws SQLException {

        //String url = "jdbc:snappydata://172.19.101.82:1527";
        //ClientDriver driver = new ClientDriver();
//        Properties connectionProperties = new Properties();
//        connectionProperties.setProperty("driver", "io.snappydata.jdbc.ClientDriver");

        String url = "jdbc:snappydata:pool://172.19.101.82:1527";
        ClientPoolDriver driver = new ClientPoolDriver();
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("driver", "io.snappydata.jdbc.ClientPoolDriver");
        connectionProperties.setProperty("pool.initialSize", "2");
        connectionProperties.setProperty("pool.minIdle", "2");
        connectionProperties.setProperty("pool.maxIdle", "5");
        Connection connection = driver.connect(url, connectionProperties);
        System.out.println(getSchemaNames(connection).toString());
        ResultSet resultSet = getTables(connection,"APP",null);
        while (resultSet.next()){
            String tableCat = resultSet.getString("TABLE_CAT");
            String tableSchem = resultSet.getString("TABLE_SCHEM");
            String tableName = resultSet.getString("TABLE_NAME");
            System.out.println(tableCat+":"+tableSchem+":"+tableName);
        }
        System.out.println(getTables(connection,"APP",null));
    }
}
