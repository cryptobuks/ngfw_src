{
    "uniqueId": "ssl-inspector-UCVS4IhqjG",
    "category": "HTTPS Inspector",
    "description": "The number of ignored sessions grouped by site.",
    "displayOrder": 201,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderByColumn": "value",
    "orderDesc": true,
    "units": "hits",
    "pieGroupColumn": "ssl_inspector_detail",
    "pieSumColumn": "count(*)",
    "conditions": [
        {
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "column": "ssl_inspector_status",
            "operator": "=",
            "value": "IGNORED"
        }
    ],
    "readOnly": true,
    "table": "sessions",
    "title": "Top Ignored Sites",
    "type": "PIE_GRAPH"
}
