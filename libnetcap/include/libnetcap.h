/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: libnetcap.h,v 1.12 2005/03/09 07:00:10 rbscott Exp $
 */
#ifndef __NETCAP_H_
#define __NETCAP_H_

#include <sys/types.h>
#include <netinet/in.h>
#include <net/if.h>
#include <pthread.h>
#include <semaphore.h>
#include <libipq/libipq.h>
#include <mvutil/lock.h>
#include <mvutil/mailbox.h>

#define NETCAP_DEBUG_PKG      201

#define IPPROTO_ALL  IPPROTO_MAX

#define NETCAP_MAX_IF_NAME_LEN  IF_NAMESIZE/* XXX */

#define NETCAP_MAX_INTERFACES   32 /* XXX */

#define NC_INTF_MAX NC_INTF_15

typedef u_int netcap_intfset_t;

typedef enum {
    /**
     * this flag to only half open the connection
     * the server socket value will be -1 in this case, and
     * you must call the callback to complete the connection
     */
    NETCAP_FLAG_SRV_UNFINI = 1,
    /**
     * this flag means call the tcp hook before the client connection is accepted
     * note, you must call the callback to complete the connection
     */
    NETCAP_FLAG_CLI_UNFINI = 2,
    /**
     * this flag is to anti subscribe when subscribing
     * All anti subscription override all subscriptions
     */
    NETCAP_FLAG_ANTI_SUBSCRIBE = 4,
    /**
     * this flag means to block all currently active connections
     * instead of only redirecting new connections
     */
    NETCAP_FLAG_BLOCK_CURRENT = 8,
    /**
     * this flag to call sudo when inserting fw rules
     * you can give the option of sudo'ing all calls
     * this is so you can run your app as non root
     * /etc/sudoers must be set so that no passwd is required
     */
    NETCAP_FLAG_SUDO = 16,
    /**
     * Local antisubscribe
     */
    NETCAP_FLAG_LOCAL_ANTI_SUBSCRIBE = 32,
    /**
     * Fake subscription
     * Will subscribe to whatever, but is_subset always returns false
     */
    NETCAP_FLAG_IS_FAKE = 64
} netcap_subscription_flags_t;

typedef enum {
    ACTION_NULL=0,
    CLI_COMPLETE,
    SRV_START_COMPLETE,
    SRV_COMPLETE,
    CLI_RESET,
} netcap_callback_action_t;

typedef enum {
    NC_INTF_UNK = 0,
    NC_INTF_0 = 1,
    NC_INTF_1,
    NC_INTF_2,
    NC_INTF_3,
    NC_INTF_4,
    NC_INTF_5,
    NC_INTF_6,
    NC_INTF_7,
    NC_INTF_8,
    NC_INTF_9,
    NC_INTF_10,
    NC_INTF_11,
    NC_INTF_12,
    NC_INTF_13,
    NC_INTF_14,
    NC_INTF_15    
} netcap_intf_t;

typedef enum {
    SRV_COMPLETE_NONLOCAL_BIND = 1
} netcap_callback_flag_t;

typedef enum {
    CONN_STATE_INCOMPLETE = 1,
    CONN_STATE_COMPLETING,
    CONN_STATE_COMPLETE,
/*     CONN_STATE_SYN_RCVD, */
/*     CONN_STATE_SYN_SENT, */
/*     CONN_STATE_SYNACK_RCVD, */
/*     CONN_STATE_SYNACK_SENT, */
    CONN_STATE_NULL
} netcap_tcp_conn_state_t;

typedef struct netcap_endpoint_t {
    netcap_intf_t  intf;
    struct in_addr host;
    u_short        port;
} netcap_endpoint_t;

typedef struct netcap_endpoints {
    /*! \brief the protocol
     *  this must be IPPROTO_TCP or IPPROTO_UDP \n
     *  you must fill this out before registering the module \n
     */
    /* XXX This should go away since this is specific to a session */
    int protocol;

    netcap_endpoint_t cli;
    netcap_endpoint_t srv;
} netcap_endpoints_t;

typedef struct netcap_pkt {
    /**
     * Protocol
     */
    int proto;

    netcap_endpoint_t src;
    netcap_endpoint_t dst;
    
    /**
     * IP attributes
     */
    u_char ttl;
    u_char tos;

    /**
     * IP options
     */
    char* opts;
    int   opts_len;

    /**
     * The actual data from the packet
     * this points to a different place for different type of pkts
     */
    char* data;    
    int   data_len;

    /**
     * Indicator for whether or not the mark should be used for outgoing
     * packets.  This is only for UDP and is ignored for TCP.
     * 0 for not marked
     * non-zero for is marked.
     */
    int is_marked;
    
    /**
     * netfilter mark
     */
    u_int nfmark;
    
    /**
     * QUEUE specific stuff
     */
    char* buffer; 

    ipq_id_t packet_id;

    /**
     * TCP flags (if a tcp packet)
     */
    u_int8_t th_flags;
#  define TH_FIN	0x01
#  define TH_SYN	0x02
#  define TH_RST	0x04
#  define TH_PUSH	0x08
#  define TH_ACK	0x10
#  define TH_URG	0x20

    /**
     * free to be used by the application
     */
    void* app_data;     

} netcap_pkt_t;

typedef struct netcap_traffic {
    /*! \brief the protocol
     *  this must be IPPROTO_TCP or IPPROTO_UDP \n
     *  you must fill this out before registering the module \n
     */
    int protocol; 

    /**
     * A number indicating the input interface or 0 if it is unknown.
     */
    netcap_intf_t cli_intf;

    /**
     * A number indicating the output interface or 0 if it is unknown.
     */
    netcap_intf_t srv_intf;

    /* The client interface set for a subscription */
    netcap_intfset_t        cli_intfset;
    
    /* The server interface set for a subscription */
    netcap_intfset_t        srv_intfset;

    /*! \brief a flag for shost
     *  indicates whether or not shost is a discrimator
     */
    u_char shost_used;
    u_char shost_netmask_used;
    
    /*! \brief the source host to intercept
     */
    in_addr_t shost;

    /*! \brief the netmask of the source dest
     *  if shost in not used this is ignored
     *  from 1.2.3.x will match this description
     */
    in_addr_t shost_netmask;

    /*! \brief a flag for dhost
     *  indicates whether or not dhost is a discrimator
     */
    u_char dhost_used;
    u_char dhost_netmask_used;

    /*! \brief the destination host to intercept
     */
    in_addr_t dhost;

    /*! \brief the netmask of the source dest
     *  if dhost in not used this is ignored
     *  to 1.2.3.x will match this description
     */
    in_addr_t dhost_netmask;

    /*! \brief the source port to intercept
     *  if 0 then use sport and dport \n
     *  if 1 then use sport_low and sport_high 
     *  and dport_low and dport_high\n
     */
    u_short src_port_range_flag;
    u_short dst_port_range_flag;
    
    /*! \brief the source port to intercept
     *  if 0 libnetcap will treat it as a wildcard (any port) \n
     */
    u_short sport;
    
    /*! \brief the destination port to intercept
     *  if 0 libnetcap will treat it as a wildcard (any port) \n
     */
    u_short dport;

    /*! \brief the low source port to intercept
     */
    u_short sport_low;
    
    /*! \brief the low destination port to intercept
     */
    u_short dport_low;

    /*! \brief the high source port to intercept
     */
    u_short sport_high;
    
    /*! \brief the high destination port to intercept
     */
    u_short dport_high;

} netcap_traffic_t;

typedef struct netcap_session {
    /**
     * this will be IPPROTO_TCP or IPPROTO_UDP \n
     */
    int protocol; 

    int syn_mode; /* 1 = syn_mode, 0 = opaque mode */

    /**
     * alive: Just for UDP!  Only modify if you have a lock on the session table
     */
    short alive;

    /* Indicates whether or not to remove the tuples associated with
     * traf_srv and traf_cli */
    short remove_tuples;

    /**
     * the session_id
     */
    u_int session_id;
    
    /* The mailbox for TCP sessions */
    mailbox_t tcp_mb;
    
    /**
     * the server udp packet mailbox
     * this is not freed in free, or init'd in create
     */
    mailbox_t srv_mb;

    /**
     * the client udp packet mailbox
     * this is not freed in free, or init'd in create
     */
    mailbox_t cli_mb;

    /* the server side traffic description */
    netcap_endpoints_t srv; 

    /* the client side traffic description */
    netcap_endpoints_t cli; 

    /* UDP Session */
    
    /* For UDP sessions, this is a byte that is initialized to the TTL of the first packet 
     * received in the session */
    u_char ttl;
    
    /* For UDP sessions, this is a byte that is initialized to the TOS of the first packet 
     * received in the session */
    u_char tos;

    /* TCP Session */
    u_int seq;

    /* Client information */
    // struct sockaddr_in client;
    int                client_sock;

    /* Server information */
    // struct sockaddr_in server;
    int                server_sock;

    /**
     * flags of this connection
     */
    int flags;

    /**
     * A number indicating the client interface or 0 if it is unknown.
     * 
     */
    netcap_intf_t cli_intf;

    /**
     * A number indicating the server interface or 0 if it is unknown.
     */
    netcap_intf_t srv_intf;

    /**
     * the callback to change the state of client and server connections
     * in the case of SRV_UNFINI or CLI_UNFINI this can be used to complete the
     * connection
     */
    int  (*callback) (struct netcap_session* netcap_sess,
                      netcap_callback_action_t action,
                      netcap_callback_flag_t flags );

    /**
     * The state of this TCP session
     */
    netcap_tcp_conn_state_t cli_state;
    netcap_tcp_conn_state_t srv_state;

    /* Data that is specific to an application */
    void *app_data;
} netcap_session_t;

typedef void (*netcap_tcp_hook_t)  (netcap_session_t* tcp_sess, void* arg);
typedef void (*netcap_udp_hook_t)  (netcap_session_t* netcap_sess, void* arg);
typedef void (*netcap_icmp_hook_t) (netcap_pkt_t* pkt, void* arg);


/**
 * Initialization, and global controls
 */
int netcap_init( int shield_enable );
int   netcap_cleanup (void);
const char* netcap_version (void);
void  netcap_debug_set_level   (int lev);

/**
 * Thread management
 */
void* netcap_thread_donate   (void* arg);
int   netcap_thread_undonate (int thread_id);

/**
 * Hook management
 */
int   netcap_tcp_hook_register   (netcap_tcp_hook_t hook);
int   netcap_tcp_hook_unregister ();
int   netcap_udp_hook_register   (netcap_udp_hook_t hook);
int   netcap_udp_hook_unregister ();
int   netcap_icmp_hook_register   (netcap_icmp_hook_t hook);
int   netcap_icmp_hook_unregister ();

/**
 * Subscription management
 */
int   netcap_subscribe (int flags, void* arg, int proto, 
                        netcap_intfset_t cli_intfset, netcap_intfset_t srv_intfset,
                        in_addr_t* src, in_addr_t* shost_netmask, u_short src_port_min, u_short src_port_max,
                        in_addr_t* dst, in_addr_t* dhost_netmask, u_short dst_port_min, u_short dst_port_max);
int   netcap_unsubscribe     (int traffic_id);
int   netcap_unsubscribe_all (void);
int   netcap_subscription_is_subset ( int sub_id, netcap_traffic_t* traf );

/**
 * Packet Sending (XXX include pkt_create?)
 */
int   netcap_udp_send (char* data, int data_len, netcap_pkt_t* pkt);
int   netcap_icmp_send (char *data, int data_len, netcap_pkt_t* pkt);

/**
 * Resource Freeing 
 */
void          netcap_pkt_free    (netcap_pkt_t* pkt);
void          netcap_pkt_destroy (netcap_pkt_t* pkt);
void          netcap_pkt_raze    (netcap_pkt_t* pkt);

/**
 * UDP and TCP session
 */
int netcap_session_raze(netcap_session_t* session);

/**
 * Interface management
 */
int netcap_interface_intf_verify( netcap_intf_t intf );
int netcap_interface_refresh (void);
int netcap_interface_is_broadcast (in_addr_t addr);
int netcap_interface_is_multicast (in_addr_t addr);
int netcap_interface_is_local (in_addr_t addr);
int netcap_interface_count (void);

in_addr_t* netcap_interface_addrs (void);

/**
 * Session table management
 */

/**
 * Get a session given its ID
 */
netcap_session_t* netcap_sesstable_get ( u_int id );
/**
 * Get the number of open sessions
 */
int               netcap_sesstable_numsessions ( void );
/**
 * get a list of all open sessions
 */
list_t*           netcap_sesstable_get_all_sessions ( void ); 
/**
 * Call the function kill_all_function on all of the sessions in the session table
 */
int               netcap_sesstable_kill_all_sessions ( void (*kill_all_function)(list_t *sessions) );
/**
 * merge the following sessions - document me rbscott XXX
 */
int               netcap_sesstable_merge_udp_tuple( netcap_session_t* netcap_sess,  int protocol, 
                                                    in_addr_t src, in_addr_t dst, 
                                                    u_short sport, u_short dport );


/**
 * netcap_traffic  functions
 */
int  netcap_traffic_bzero(netcap_traffic_t* traf);
int  netcap_traffic_copy (netcap_traffic_t* dst, netcap_traffic_t* src);
int  netcap_traffic_is_subset (netcap_traffic_t* desc, netcap_traffic_t* inst);
int  netcap_traffic_equals (netcap_traffic_t* traf1, netcap_traffic_t* traf2);
void netcap_traffic_debug_print_prefix (int level, netcap_traffic_t* traf, char* prefix);
void netcap_traffic_debug_print (int level, netcap_traffic_t* traf);

int  netcap_endpoints_copy          ( netcap_endpoints_t* dst, netcap_endpoints_t* src );
int  netcap_endpoints_bzero         ( netcap_endpoints_t* tuple );

/**
 * Functions on the interface set
 */

/**
 * Turn all interfaces on
 */
int netcap_intfset_clear ( netcap_intfset_t* intfset);
/**
 * Set an interface in the set.  (This enables one interface)
 */
int netcap_intfset_add   ( netcap_intfset_t* intfset, netcap_intf_t intf);
/**
 * Check an interface in the set. (Returns 1 if set, 0 if not, -1 on error
 */
int netcap_intfset_get   ( netcap_intfset_t* intfset, netcap_intf_t intf);
/**
 * XXX
 */
int netcap_intfset_get_all ( netcap_intfset_t* intfset );
/**
 * XXX
 */
int netcap_intfset_to_string ( char* dst, int dst_len, netcap_intfset_t* intfset);

/**
 * Interface functions
 */
/* Limit traffic to just this subnet */
int netcap_interface_limit_subnet   (char* dev_inside, char* dev_outside);

/**
 * Block all traffic on interface gate to this port.
 */
int netcap_interface_station_port_guard( netcap_intf_t gate, int protocol, char* ports, char* guests );

/**
 * Remove the rule blocking traffic to a port.
 */
int netcap_interface_relieve_port_guard( netcap_intf_t gate, int protocol, char* ports, char* guests );

/* Convert a string representation of an interface to a netcap representation */
int netcap_interface_string_to_intf (char *intf_str, netcap_intf_t *intf);

/* Convert a netcap representation (eg 1) of an interface to a string representation ( eg eth0 ) */
int netcap_interface_intf_to_string (netcap_intf_t intf, char *intf_str,int str_len);

/**
 * Toggle opaque mode
 */
int  netcap_tcp_syn_mode (int toggle);

/**
 * netcap_shield_rep_add_chunk: Add a chunk to the reputation of ip.
 *  ip: The IP to add the chunk against.
 *  protocol: Either IPPROTO_UDP or IPPROTO_TCP.
 *  size: Size of the chunk in bytes.
 */
int   netcap_shield_rep_add_chunk      ( in_addr_t ip, int protocol, u_short size );

/**
 * netcap_shield_rep_end_session: Inform the shield that IP has ended a session.
 */
int   netcap_shield_rep_end_session    ( in_addr_t ip );

/**
 * When calling netcap_init, pass in this value to initialize the shield, otherwise pass in 0
 */
#define NETCAP_SHIELD_ENABLE   0x00E0F00D

/* Load in a new shield configuration */
int   netcap_shield_cfg_load           ( char* buf, int buf_len );

/* Dump out the current status of the shield */
int   netcap_shield_status             ( int conn, struct sockaddr_in *dst_addr );

/**
 * netcap_sched_donate: Donate a thread to the scheduler.
 */
void* netcap_sched_donate ( void* arg );


/**
 * Printing utilities
 * all return static buffers
 */
char* netcap_session_tuple_print     ( netcap_session_t* sess );

/**
 * Print the server(sess.srv.srv.*)/client(sess.cli.cli.*) side two tuple (host and port)
 */
char* netcap_session_srv_tuple_print ( netcap_session_t* sess );
char* netcap_session_cli_tuple_print ( netcap_session_t* sess );

/**
 * Print the server(sess.srv.*)/client(sess.cli.*) side endpoints
 */
char* netcap_session_srv_endp_print ( netcap_session_t* sess );
char* netcap_session_cli_endp_print ( netcap_session_t* sess );

char* netcap_session_fd_tuple_print  ( netcap_session_t* sess );


#endif
