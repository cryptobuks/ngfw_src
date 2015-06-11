/**
 * $Id$
 */
package com.untangle.uvm.node;

import java.io.Serializable;

import org.json.JSONObject;
import org.json.JSONString;
import org.apache.log4j.Logger;

import com.untangle.uvm.node.SqlCondition;


/**
 * This class represents a unique Event Log query and stores all the information the UI needs to
 * render and exec the query
 */
@SuppressWarnings("serial")
public class EventLogEntry implements Serializable, JSONString
{
    private final Logger logger = Logger.getLogger(getClass());

    private String uniqueId = null;
    private int displayOrder = 9999; /* The order to display this report entry (relative to others) */

    private String name;
    private String table;
    private SqlCondition[] conditions;
    private String[] defaultColumns;
    
    public EventLogEntry( String name, String table, SqlCondition[] conditions )
    {
        this.name = name;
        this.table = table;
        this.conditions = conditions;
    }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId( String newValue ) { this.uniqueId = newValue; }

    public int getDisplayOrder() { return this.displayOrder; }
    public void setDisplayOrder( int newValue ) { this.displayOrder = newValue; }
    
    public String getName() { return this.name; }
    public void setName( String newValue ) { this.name = newValue; }

    public SqlCondition[] getConditions() { return this.conditions; }
    public void setConditions( SqlCondition[] newValue ) { this.conditions = newValue; }

    public String[] getDefaultColumns() { return this.defaultColumns; }
    public void setDefaultColumns( String[] newValue ) { this.defaultColumns = newValue; }
    
    public String getQuery()
    {
        String query = ""; 
        query +=  "SELECT * FROM reports." + this.table + " WHERE true";
        if ( getConditions() != null ) {
            for ( SqlCondition condition : getConditions() ) {
                query += " and " + condition.getColumn() + " " + condition.getOperator() + " " + condition.getValue() + "";
            }
        }
        
        query += " ORDER BY time_stamp DESC";
        return query;
    }

    public String toJSONString()
    {
        JSONObject jO = new JSONObject(this);
        return jO.toString();
    }
}