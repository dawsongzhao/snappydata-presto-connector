package com.yingzi.bigdata.presto.snappydata.frame;

import com.facebook.presto.plugin.jdbc.JdbcPlugin;

public class SnappydataPlugin  extends JdbcPlugin
{
    public SnappydataPlugin()
    {
        super("snappydata", new SnappydataClientModule());
    }
}