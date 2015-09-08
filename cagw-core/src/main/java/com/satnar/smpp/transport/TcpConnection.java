package com.satnar.smpp.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Properties;

import com.satnar.common.LogService;
import com.satnar.common.alarmlog.AlarmCode;
import com.satnar.smpp.client.ChannelMode;

public class TcpConnection extends Connection {
    
    private static final String TCP_ADDRESS      = "tcpAddress";
    private static final String TCP_PORT         = "tcpPort";
    private static final String ESME_LABEL       = "esmeLabel";
    private static final String LAZY_WRITE_WAIT  = "lazyWriteWait";
    private static final String THREAD_POOL_SIZE = "threadPoolSize";
    private static final String NETWORK_IDLE_WAIT = "networkIdleWaitTime";
    
    private Properties          config              = null;
    
    private String              label               = null;
    private String              address             = null;
    private int                 port                = 0;
    private int                 lazyWriteWait       = 0;
    private int                 threadPoolSize      = 0;
    private int                 networkIdleWaitTime = 0;
    
    private SocketChannel       connection          = null;
    private ByteBuffer          sendBuffer       = null;
    private ByteBuffer          readBuffer      = null;
    private ChannelMode         mode                = null;
    private boolean             isShutdownMode      = false;
    
    public TcpConnection(Properties connectionConfig, ChannelMode channelMode) {
        this.config = connectionConfig;
        
        sendBuffer = ByteBuffer.allocate(1024);
        sendBuffer.order(ByteOrder.BIG_ENDIAN);
        
        readBuffer = ByteBuffer.allocate(4096);
        readBuffer.order(ByteOrder.BIG_ENDIAN);
        
        this.mode = channelMode;
        LogService.appLog.info("TcpConnection init with: " + channelMode + ", config: " + this.config);
    }
    
    
    
    @Override
    public void connect() throws SmppTransportException {
        try {
            // First validate the config if properly defined...
            this.validateInitializeConfig();
            
            this.connection = SocketChannel.open();
            this.connection.configureBlocking(false);
            this.connection.connect(new InetSocketAddress(this.address, this.port));
            while (!this.connection.finishConnect())
                continue;
            this.setConnectionState(SmppSessionState.OPEN);
            LogService.appLog.info("TcpConnection-connect:Connected successfully. Address:"+this.address);
            
            LogService.alarm(AlarmCode.SMS_CONNECTED, this.label, this.address);
            
        } catch (AlreadyConnectedException e) {
            LogService.appLog.error("TcpConnection-connect:Illegal Transport State - Already Connected!!",e);
            throw new SmppTransportException("Illegal Transport State - Already Connected!!", e);
        } catch (NoConnectionPendingException e) {
            LogService.appLog.error("TcpConnection-connect:Illegal Transport State - No Connection Pending!!",e);
            throw new SmppTransportException("Illegal Transport State - No Connection Pending!!", e);
        } catch (ConnectionPendingException e) {
            LogService.appLog.error("TcpConnection-connect: Illegal Transport State - Async Connection Pending!!",e);
            throw new SmppTransportException("Illegal Transport State - Async Connection Pending!!", e);
        } catch (ClosedByInterruptException e) {
            LogService.appLog.error("TcpConnection-connect:Illegal Transport State - Concurrent Interrupt Closed this Socket!!",e);
            throw new SmppTransportException("Illegal Transport State - Concurrent Interrupt Closed this Socket!!", e);
        } catch (AsynchronousCloseException e) {
            LogService.appLog.error("TcpConnection-connect:Illegal Transport State - Concurrent Close Attempted!!",e);
            throw new SmppTransportException("Illegal Transport State - Concurrent Close Attempted!!", e);
        } catch (ClosedChannelException e) {
            LogService.appLog.error("TcpConnection-connect:Illegal Transport State - Socket Already CLosed!!",e);
            throw new SmppTransportException("Illegal Transport State - Socket Already CLosed!!", e);
        } catch (UnresolvedAddressException e) {
            LogService.appLog.error("TcpConnection-connect:Transport Failure - Unresolved Address!!",e);
            throw new SmppTransportException("Transport Failure - Unresolved Address!!", e);
        } catch (UnsupportedAddressTypeException e) {
            LogService.appLog.error("TcpConnection-connect:Transport Failure - Unsupported Address Type!!",e);
            throw new SmppTransportException("Transport Failure - Unsupported Address Type!!", e);
        } catch (IllegalArgumentException e) {
            LogService.appLog.error("TcpConnection-connect:Transport Failure - Port is not in valid range for TCP Stack!!",e);
            throw new SmppTransportException("Transport Failure - Port is not in valid range for TCP Stack!!", e);
        } catch (SecurityException e) {
            LogService.appLog.error("TcpConnection-connect:Transport Failure - Security Sandbox Rejects Access to this EndPoint!!",e);
            throw new SmppTransportException("Transport Failure - Security Sandbox Rejects Access to this EndPoint!!", e);
        } catch (IOException e) {
            LogService.appLog.error("TcpConnection-connect:Transport Failure - Localised in TCP Stack",e);
            throw new SmppTransportException("Transport Failure - Localised in TCP Stack", e);
        } catch (Exception e) {
            LogService.appLog.error("TcpConnection-connect:Transport Failure - Unforeseen Realtime Exception!!",e);
            throw new SmppTransportException("Transport Failure - Unforeseen Realtime Exception!!", e);
        } finally {
            try {
                this.connection.close();
            } catch (IOException e) {
                LogService.appLog.error("Socket close failed for esme: " + this.label, e);
            }
        }
        
    }
    
    @Override
    public void disconnect() throws SmppTransportException {
        LogService.appLog.debug(String.format("Esme: %s - disconnecting... Check State: %s", this.label, this.getConnectionState()));
        try {
            switch (this.getConnectionState()) {
                case UNBOUND:
                case INIT_IDLE:
                case OPEN:
                    if (this.connection != null && this.connection.isConnected()) {
                        LogService.appLog.debug(String.format("Esme: %s - Found valid connection... Closing now!", this.label));
                        this.connection.close();
                    }
                    break;
                case BOUND_RX:
                case BOUND_TRX:
                case BOUND_TX:
                    LogService.appLog.debug(String.format("Esme: %s - Found bound_state... Recommend Unbind to avoid SMSC problems!", this.label));
                    throw new SmppTransportException("Unbind First");
                case CLOSED:
                    break;
            }
            
            this.sendBuffer.clear();
            this.readBuffer.clear();
            this.setConnectionState(SmppSessionState.CLOSED);
            LogService.appLog.debug(String.format("Esme: %s - disconnected... Check State: %s", this.label, this.getConnectionState()));
                
        } catch (IOException e) {
            LogService.appLog.error("TcpConnection-connect:Transport Failure - Localised in TCP Stack or Memory Buffers",e);
            //throw new SmppTransportException("Transport Failure - Localised in TCP Stack or Memory Buffers", e);
        } finally {
            try {
                this.connection.close();
            } catch (IOException e) {
                LogService.appLog.error("Socket close failed for esme: " + this.label, e);
            }
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        this.connection = null;
        this.sendBuffer = null;
        this.readBuffer = null;
        System.gc();
        super.finalize();
    }
    
    
    

    @Override
    public void write(ByteBuffer writeBuffer) throws SmppTransportException {
        try {
            int windowSize = writeBuffer.remaining();
            this.connection.write(writeBuffer);
            writeBuffer.clear();
            LogService.appLog.debug(this.getEsmeLabel() + " - Successfully transmitted writeBuffer Window Size: " + windowSize);
        } catch (ClosedByInterruptException e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-write:Socket closed by interruption!!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Socket closed by interruption!!", e);
        } catch (AsynchronousCloseException e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-write:Socket is in closing phase!!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Socket is in closing phase!!", e);
        } catch (ClosedChannelException e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-write:Socket already closed!!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Socket already closed!!", e);
        } catch (NotYetConnectedException e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-write:Socket not yet ready for transmission!!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Socket not yet ready for transmission!!", e);
        } catch (IOException e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-write:Failed transmitting the payload!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Failed transmitting the payload!", e);
        } catch (Exception e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-write:Unknown error!!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Unknown error!!", e);
        }
        
    }

    @Override
    public int read(ByteBuffer readBuffer) throws SmppTransportException {
        int packetSize = 0;
        try {
            readBuffer.clear();
            packetSize = this.connection.read(readBuffer);
            readBuffer.flip();
            if (packetSize == -1) {
                LogService.appLog.error("Seems like the Socket reached End-Of-Stream!! Must shutdown!!");
                this.close();
                throw new SmppTransportException(this.getEsmeLabel() + " - TcpConnection-read:Socket reached EOS!!");
            }
            return packetSize;
        } catch(NotYetConnectedException e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-read:Socket not yet ready for reception!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Socket not yet ready for reception!", e);
        } catch(IOException e) {
            LogService.appLog.error(this.getEsmeLabel() + " - TcpConnection-read:Failed receiving the payload!",e);
            this.close();
            throw new SmppTransportException(this.getEsmeLabel() + " - Failed receiving the payload!", e);
        }
    }

    private void close() {
        if (!this.connection.isConnected()) {
            try {
                this.setConnectionState(SmppSessionState.CLOSED);
                this.connection.close();
            } catch (IOException e) {
                LogService.appLog.error("Socket close failed with exception:", e);
            } catch (SmppTransportException e) {
                LogService.appLog.error("Stack Session State change validation failed with exception:", e);
                           }
        }
    }

    @Override
    public ByteBuffer getSendBuffer() {
        return this.sendBuffer;
    }



    @Override
    public ByteBuffer getReceiveBuffer() {
        return this.readBuffer;
    }



    @Override
    public int getThreadPoolSize() {
        return this.threadPoolSize;
    }



    @Override
    public ChannelMode getMode() {
        return this.mode;
    }



    @Override
    public void setMode(ChannelMode mode) {
        this.mode = mode;
    }



    public void validateInitializeConfig() throws SmppTransportException {
        String param = null;
        
        // check the label...
        param = this.config.getProperty(ESME_LABEL);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppTransportException("'esmeLabel' was not defined or empty!!");
        this.label = param;
        
        // check the port...
        param = this.config.getProperty(TCP_PORT);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppTransportException("'tcpPort' was not defined or empty!!");
        
        try {
            this.port = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            throw new SmppTransportException("'tcpPort' was defined but not integer!! Found: " + param);
        }
        
        // check the network idle wait time...
        param = this.config.getProperty(NETWORK_IDLE_WAIT);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppTransportException("'networkIdleWaitTime' was not defined or empty!!");
        
        try {
            this.networkIdleWaitTime = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            throw new SmppTransportException("'networkIdleWaitTime' was defined but not integer!! Found: " + param);
        }
        
        // check the address...
        param = this.config.getProperty(TCP_ADDRESS);
        if (param == null || param.equalsIgnoreCase("")) {
        	LogService.appLog.debug("TcpConnection-validateInitializeConfig: Missing TCP_ADDRESS");
            throw new SmppTransportException("'tcpAddress' MUST be defined!!");
        }
        
        try {
            InetAddress.getByName(param);
            this.address = param;
        } catch (UnknownHostException e) {
        	LogService.appLog.debug("TcpConnection-validateInitializeConfig: Invalid TCP or DNS Address");
            throw new SmppTransportException("'tcpAddress' is not a valid TCP or DNS Address!! Found: " + param);
        }
        
        // check the lazy write...
        param = this.config.getProperty(LAZY_WRITE_WAIT);
        if (param == null || param.equalsIgnoreCase("")){
        	LogService.appLog.debug("TcpConnection-validateInitializeConfig: lazyWriteWait was not defined or empty");
            throw new SmppTransportException("'lazyWriteWait' was not defined or empty!!");
        }
        try {
            this.lazyWriteWait = Integer.parseInt(param);
        } catch (NumberFormatException e) {
        	LogService.appLog.debug("TcpConnection-validateInitializeConfig: lazyWriteWait  was defined but not integer!!",e);
            throw new SmppTransportException("'lazyWriteWait' was defined but not integer!! Found: " + param);
        }
        
        // check the thread pool...
        param = this.config.getProperty(THREAD_POOL_SIZE);
        if (param == null || param.equalsIgnoreCase("")){
        	LogService.appLog.debug("TcpConnection-validateInitializeConfig: threadPoolSize  was not defined or empty!!");
            throw new SmppTransportException("'threadPoolSize' was not defined or empty!!");
        }
        try {
            this.threadPoolSize = Integer.parseInt(param);
            
            int maxThreadLimit = getMaxThreadLimit();
            if (this.threadPoolSize > maxThreadLimit){
            	LogService.appLog.debug("TcpConnection-validateInitializeConfig: 'threadPoolSize' configured too high to be stable!! Found: " + param + ", Allowed: " + maxThreadLimit);
                throw new SmppTransportException("'threadPoolSize' configured too high to be stable!! Found: " + param + ", Allowed: " + maxThreadLimit);                           
            }
            if (this.threadPoolSize < 5){
            	LogService.appLog.debug("TcpConnection-validateInitializeConfig: 'threadPoolSize' configured too low to be perform!! Found: " + param);
                throw new SmppTransportException("'threadPoolSize' configured too low to be perform!! Found: " + param);
            }
        } catch (NumberFormatException e) {
        	LogService.appLog.debug("TcpConnection-validateInitializeConfig: 'threadPoolSize' was defined but not integer!! Found: " + param);
            throw new SmppTransportException("'threadPoolSize' was defined but not integer!! Found: " + param);
        }
        
    }
    
    private static int getMaxThreadLimit() {
        String currOsName = System.getProperty("os.name");
        
        if (currOsName == null || currOsName.isEmpty())
            return 400;
        
        if (currOsName.contains("inux"))
            return 600;
        
        if (currOsName.contains("nix"))
            return 800;
        
        if (currOsName.contains("olaris"))
            return 1100;
        
        if (currOsName.contains("indows"))
            return 1800;
        
        
        return 400; // absolute fallback
    }
    
    @Override
    public int getNetworkIdleWaitTime() {
        return this.networkIdleWaitTime;
    }


    
    public Properties getConfig() {
        return config;
    }
    
    public void setConfig(Properties config) {
        this.config = config;
    }
    
    @Override
    public String getEsmeLabel() {
        return label;
    }
    
    public void setEsmeLabel(String label) {
        this.label = label;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public int getLazyWriteWait() {
        return lazyWriteWait;
    }

    public void setLazyWriteWait(int lazyWriteWait) {
        this.lazyWriteWait = lazyWriteWait;
    }

    public SocketChannel getConnection() {
        return connection;
    }

    public boolean isShutdownMode() {
        return isShutdownMode;
    }

    public void setShutdownMode(boolean isShutdownMode) {
        this.isShutdownMode = isShutdownMode;
    }

    
    
    
}
