package com.yingzi.bigdata.presto.snappydata.frame;

import io.airlift.configuration.Config;
import io.airlift.units.Duration;

import javax.validation.constraints.Min;
import java.util.concurrent.TimeUnit;

public class SnappydataConfig
{
    //https://tibco-computedb.readthedocs.io/en/docv1.1.0/howto/connect_using_jdbc_driver/
    //Default value is maxActive:max(256, availableProcessors * 8).
    private int maxIdle = 3;
    private int minIdle = 1;
    //The initial number of connections that are created when the pool is started.
    // Default value is max(256, availableProcessors * 8).
    private int initialSize = 10;
    //    pool.maxWait	(int) The maximum waiting period, in milliseconds,for establishing a connection
    //    after which an exception is thrown.Default value is 30000 (30 seconds).
    private Duration maxWait = new Duration(30, TimeUnit.SECONDS);
    //pool.removeAbandoned	Flag to remove the abandoned connections, in case they exceed the settings for removeAbandonedTimeout.
    // If set to true a connection is considered abandoned and eligible for removal,
    // if its no longer in use than the settings for removeAbandonedTimeout.
    // Setting this to true can recover db connections from applications that fail to close a connection. The default value is false.
    private boolean removeAbandoned = false;

    //pool.removeAbandonedTimeout	Timeout in seconds before an abandoned connection, that was in use, can be removed.
    // The default value is 60 seconds. The value should be set to the time required for the longest running query in your applications.
    private Duration removeAbandonedTimeout = new Duration(60, TimeUnit.SECONDS);

    //driver	io.snappydata.jdbc.ClientPoolDriver
    //This should be passed through Spark JDBC API for loading and using the driver.
    private String driver = "io.snappydata.jdbc.ClientPoolDriver";

    public String getDriver() {
        return driver;
    }
    public SnappydataConfig setDriver(String driver){
        this.driver = driver;
        return this;
    }


    //private Duration connectionTimeout = new Duration(10, TimeUnit.SECONDS);

    public boolean isRemoveAbandoned()
    {
        return removeAbandoned;
    }

    @Config("pool.removeAbandoned")
    public SnappydataConfig setRemoveAbandoned(boolean removeAbandon)
    {
        this.removeAbandoned = removeAbandon;
        return this;
    }

    @Min(1)
    public int getMaxIdle()
    {
        return maxIdle;
    }

    @Config("pool.maxIdle")
    public SnappydataConfig setMaxIdle(int maxIdle)
    {
        this.maxIdle = maxIdle;
        return this;
    }

    @Min(5)
    public int getInitialSize()
    {
        return initialSize;
    }

    @Config("pool.initialSize")
    public SnappydataConfig setInitialSize(int initialSize)
    {
        this.initialSize = initialSize;
        return this;
    }

    public int getMinIdle()
    {
        return minIdle;
    }

    @Config("pool.minIdle")
    public SnappydataConfig setMinIdle(int minIdle)
    {
        this.minIdle = minIdle;
        return this;
    }

    public Duration getMaxWait()
    {
        return maxWait;
    }

    @Config("pool.maxWait")
    public SnappydataConfig setMaxWait(Duration maxWait)
    {
        this.maxWait = maxWait;
        return this;
    }

    public Duration getRemoveAbandonedTimeout()
    {
        return removeAbandonedTimeout;
    }

    @Config("pool.removeAbandonedTimeout")
    public SnappydataConfig setRemoveAbandonedTimeout(Duration removeAbandonedTimeout)
    {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
        return this;
    }


}
