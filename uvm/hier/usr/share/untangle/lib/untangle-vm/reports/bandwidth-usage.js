{
    "uniqueId": "network-StzlzfZAp8",
    "category": "Network",
    "description": "The approximate averaged data transfer rate (total, sent, received) over time.",
    "displayOrder": 103,
    "enabled": true,
    "javaClass": "com.untangle.app.reports.ReportEntry",
    "orderDesc": false,
    "units": "bytes/s",
    "readOnly": true,
    "table": "session_minutes",
    "timeDataColumns": [
        "round(coalesce(sum(s2c_bytes + c2s_bytes), 0) / (60*10),1) as total",
        "round(coalesce(sum(c2s_bytes), 0) / (60*10),1) as sent",
        "round(coalesce(sum(s2c_bytes), 0) / (60*10),1) as received"
    ],
    "colors": [
        "#396c2b",
        "#0099ff",
        "#6600ff"
    ],
    "timeDataInterval": "MINUTE",
    "timeStyle": "AREA",
    "title": "Bandwidth Usage",
    "type": "TIME_GRAPH",
    "approximation": "high"
}
