package es.olgierd.remDroidServer;

import java.awt.Dimension;
import java.net.InetAddress;

class Configuration {

    public final static byte PACKET_MOUSE_DOWN = 0;
    public final static byte PACKET_MOUSE_MOVE = 1;
    public final static byte PACKET_MOUSE_UP = 2;
    public final static byte PACKET_MOUSE_DRAG = 3;
    public final static byte PACKET_SCROLL_UP = 4;
    public final static byte PACKET_SCROLL_DOWN = 5;
    public final static byte PACKET_HELLO = 6;
    public final static byte PACKET_BYEBYE = 7;
    public final static byte PACKET_PING = 100;
    
    public int localPort;
    public Dimension screenSize;
    public InetAddress client;

}
