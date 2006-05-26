package freenet.node;

import java.io.File;
import java.io.IOException;

import org.tanukisoftware.wrapper.WrapperManager;
import org.tanukisoftware.wrapper.WrapperListener;

import freenet.config.FilePersistentConfig;
import freenet.config.InvalidConfigValueException;
import freenet.config.SubConfig;
import freenet.crypt.DiffieHellman;
import freenet.crypt.RandomSource;
import freenet.crypt.Yarrow;
import freenet.node.Node.NodeInitException;
       

/**
 * 
 * @author nextgens
 *	
 *	A class to tie the wrapper and the node (needed for self-restarting support)
 *
 */
public class NodeStarter
    implements WrapperListener
{
    private Node node;
	static LoggingConfigHandler logConfigHandler;
	private FilePersistentConfig cfg;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    private NodeStarter()
    {
    }
    
    public NodeStarter get(){
    	return this;
    }

    /*---------------------------------------------------------------
     * WrapperListener Methods
     *-------------------------------------------------------------*/
    /**
     * The start method is called when the WrapperManager is signaled by the 
     *	native wrapper code that it can start its application.  This
     *	method call is expected to return, so a new thread should be launched
     *	if necessary.
     *
     * @param args List of arguments used to initialize the application.
     *
     * @return Any error code if the application should exit on completion
     *         of the start method.  If there were no problems then this
     *         method should return null.
     */
    public Integer start( String[] args )
    {
    	if(args.length>1) {
    		System.out.println("Usage: $ java freenet.node.Node <configFile>");
    		return new Integer(-1);
    	}
    	
    	File configFilename;
    	if(args.length == 0) {
    		System.out.println("Using default config filename freenet.ini");
    		configFilename = new File("freenet.ini");
    	} else
    		configFilename = new File(args[0]);
    	
    	// set Java's DNS cache not to cache forever, since many people
    	// use dyndns hostnames
    	java.security.Security.setProperty("networkaddress.cache.ttl" , "300");
    	
    	// set Java's negative DNS cache to 1 minute rather than the default 10 seconds
    	java.security.Security.setProperty("networkaddress.cache.negative.ttl" , "60");
    	  	
    	try{
    		cfg = new FilePersistentConfig(configFilename);	
    	}catch(IOException e){
    		System.out.println("Error : "+e);
    		return new Integer(-1);
    	}
    	
    	// First, set up logging. It is global, and may be shared between several nodes.
    	
    	SubConfig loggingConfig = new SubConfig("logger", cfg);
    	
    	try {
    		logConfigHandler = new LoggingConfigHandler(loggingConfig);
    	} catch (InvalidConfigValueException e) {
    		System.err.println("Error: could not set up logging: "+e.getMessage());
    		e.printStackTrace();
    		return new Integer(-2);
    	}
    	
    	// Setup RNG
    	
    	RandomSource random = new Yarrow();
    	
    	DiffieHellman.init(random);
    	
    	// FIXME : maybe we should keep it even if the wrapper does it
    	
    	Thread t = new Thread(new MemoryChecker(), "Memory checker");
    	t.setPriority(Thread.MAX_PRIORITY);
    	t.start();
    	 
    	 
    	WrapperManager.signalStarting(500000);
    	try {
    		node = new Node(cfg, random, logConfigHandler,this);
    		node.start(false);
    	} catch (NodeInitException e) {
    		System.err.println("Failed to load node: "+e.getMessage());
    		e.printStackTrace();
    		System.exit(e.exitCode);
    	}
    	return null;
    }

    /**
     * Called when the application is shutting down.  The Wrapper assumes that
     *  this method will return fairly quickly.  If the shutdown code code
     *  could potentially take a long time, then WrapperManager.signalStopping()
     *  should be called to extend the timeout period.  If for some reason,
     *  the stop method can not return, then it must call
     *  WrapperManager.stopped() to avoid warning messages from the Wrapper.
     *
     * @param exitCode The suggested exit code that will be returned to the OS
     *                 when the JVM exits.
     *
     * @return The exit code to actually return to the OS.  In most cases, this
     *         should just be the value of exitCode, however the user code has
     *         the option of changing the exit code if there are any problems
     *         during shutdown.
     */
    public int stop( int exitCode )
    {
    	// see #354
    	WrapperManager.signalStopping(120000);
        node.exit();
        
        return exitCode;
    }
    
    public void restart(){
    	WrapperManager.restart();
    }
    
    /**
     * Called whenever the native wrapper code traps a system control signal
     *  against the Java process.  It is up to the callback to take any actions
     *  necessary.  Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT, 
     *    WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or 
     *    WRAPPER_CTRL_SHUTDOWN_EVENT
     *
     * @param event The system control signal.
     */
    public void controlEvent( int event )
    {
        if (WrapperManager.isControlledByNativeWrapper()) {
            // The Wrapper will take care of this event
        } else {
            // We are not being controlled by the Wrapper, so
            //  handle the event ourselves.
            if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT) ||
                (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT) ||
                (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT)){
                WrapperManager.stop(0);
            }
        }
    }
    
    /*---------------------------------------------------------------
     * Main Method
     *-------------------------------------------------------------*/
    public static void main( String[] args )
    {
        // Start the application.  If the JVM was launched from the native
        //  Wrapper then the application will wait for the native Wrapper to
        //  call the application's start method.  Otherwise the start method
        //  will be called immediately.
        WrapperManager.start( new NodeStarter(), args );
    }
}