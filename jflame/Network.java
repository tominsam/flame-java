package jflame;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.*;

interface ServiceWatcher {
    void serviceAdded( Network n, Service s );
    void serviceRemoved( Network n, Service s );
}

class Service {
    private ServiceEvent event;
    
    public Service( ServiceEvent e ) {
        event = e;
    }

    public String name() {
		return event.getName();
	}

    public String address() {
    	return event.getInfo().getHostAddress();
    }
	
	public String info() {
		return event.getInfo().getTextString();
	}
	
	public String type() {
		return event.getType();
	}
	public int port() {
		return event.getInfo().getPort();
	}
}

class Host {
    private ServiceEvent event;
    private Vector services;
    
    public Host( ServiceEvent e ) {
    	event = e;
    	services = new Vector();
    }

    public Vector services() {
    	return services;
    }
    public String name() {
    	return event.getInfo().getServer();
    }
    public String address() {
    	return event.getInfo().getHostAddress();
    }

	public String url() {
		return event.getInfo().getURL();
	}
}

public class Network implements ServiceListener, ServiceTypeListener {
    
    JmDNS jmdns;
    HashMap hosts;
    HashMap services;
    ServiceWatcher watcher;
    
    /** Creates a new instance of Network */
    public Network( ServiceWatcher w ) throws IOException {

    	hosts = new HashMap();
        watcher = w;
        
        services = new HashMap();
        
        //this.jmdns = new JmDNS( InetAddress.getByName("10.20.30.202") );
        this.jmdns = new JmDNS();
        jmdns.addServiceTypeListener(this);
        
       
        // register some well known types. This is just for faster startup
        // - we discover all types on the network given time.
        String list[] = new String[] {
            "_http._tcp.local.",
            "_ftp._tcp.local.",
            "_tftp._tcp.local.",
            "_ssh._tcp.local.",
            "_smb._tcp.local.",
            "_printer._tcp.local.",
            "_airport._tcp.local.",
            "_afpovertcp._tcp.local.",
            "_ichat._tcp.local.",
            "_eppc._tcp.local.",
            "_presence._tcp.local."
        };
        
        for (int i = 0 ; i < list.length ; i++) {
            jmdns.registerServiceType(list[i]);
        }
        
        //ServiceInfo flameService = new ServiceInfo("_flame._tcp.", "JFlame", 1812, "");
        //jmdns.registerService( flameService );
    }
    
    public synchronized void addService( ServiceEvent e ) {
        String servicekey = e.getInfo().getServer() + e.getType() + e.getName();
        if (services.containsKey(servicekey)) return;
        System.out.println("new service "+servicekey);
        String hostkey = e.getInfo().getServer();
        System.out.println( "hostkey is "+hostkey );
        Host host;
        if (hosts.containsKey(hostkey)) {
        	System.out.println("using existing host");
        	host = (Host)hosts.get(hostkey);
        } else {
        	System.out.println("Host is new");
        	host = new Host( e );
        	hosts.put(hostkey, host);
        }
        Service service = new Service(e);
        services.put( servicekey, service );
        servicesForHost(host);
        watcher.serviceAdded( this, service );
    }

    public void servicesForHost( Host h ) {
    	// TODO - don't be so destructive to this list
    	// just add new things, and remove missing things
    	Vector v = h.services();
    	v.removeAllElements();
        Iterator i = services.values().iterator();
        while (i.hasNext()) {
            Service s = (Service)i.next();
            if (s.address().equals( h.address() ))
            	v.addElement( s );
        }
    }

    public synchronized void removeService( ServiceEvent e ) {
        String key = e.getInfo().getHostAddress() + e.getType() + e.getName();
        if (! services.containsKey(key)) return;
        System.out.println("removed service "+key);
        Service service = (Service)services.get( key );
        watcher.serviceRemoved( this, service );
        services.remove( key );
    }
    
    public void serviceTypeAdded(ServiceEvent event) {
        final String type = event.getType();
        System.out.println( "new type "+type);
        jmdns.addServiceListener( type, this );
    }
    
    public void serviceAdded(ServiceEvent event) {
        System.out.println( event.getType() + " / " + event.getName() );
        // get the event info. The callback from this adds the event
        event.getDNS().requestServiceInfo(event.getType(), event.getName(), 0);
        
    }
    
    /**
     * Remove a service.
     */
    public void serviceRemoved(ServiceEvent event) {
        removeService( event );
    }
    
    public void serviceResolved(ServiceEvent event) {
        addService( event );
    }
    
}
