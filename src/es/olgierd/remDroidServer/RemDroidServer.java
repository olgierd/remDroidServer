package es.olgierd.remDroidServer;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;

public class RemDroidServer implements Runnable {

    private final static int SPEED_LIMIT = 130;

    private Robot r = null;
    private GUI gui;
    private DatagramSocket socket = null;
    private byte[] recvData;
    private DatagramPacket dp;
    private Point location;
    private Configuration cfg;
    private int lastx, lasty;
    private byte lastType;
    private long lastDown = 0;
    private int diffx, diffy;

    public boolean stopper;
    private Point p;

    public void stopSocket() {
	if (socket != null) {
	    socket.close();
	}
    }

    public RemDroidServer(GUI ui, Configuration conf) {

	gui = ui;
	cfg = conf;

	try {
	    r = new Robot();
	    socket = new DatagramSocket(22222);
	} catch (Exception e) {
	    ui.setStatus("SOCKET FAILED");
	    System.out.println(e.getMessage());
	}

	recvData = new byte[5];
	dp = new DatagramPacket(recvData, 0, recvData.length, null, 0);

    }

    // obliczenie wektora przesunięcia
    private Point getCoordsTouchpad() {

	p = new Point(0, 0);

	int x, y; // odebrane z telefonu współrzędne

	// zrzucamy z byte do intów
	x = recvData[0] & 0xFF | recvData[1] << 8;
	y = recvData[2] & 0xFF | recvData[3] << 8;

	
	if (recvData[4] == Configuration.PACKET_MOUSE_MOVE) {

	    // obliczamy przesunięcie
	    diffx = (x - lastx);
	    diffy = (y - lasty);

	    // aplikujemy przyspieszenie
	    if (Math.abs(diffx) > 10)
		diffx *= Math.abs(diffx / 10.0);
	    if (Math.abs(diffy) > 10)
		diffy *= Math.abs(diffy / 10.0);

	    // ograniczamy je z góry
	    if(diffx > SPEED_LIMIT) diffx = SPEED_LIMIT;
	    if(diffx < -SPEED_LIMIT) diffx = -SPEED_LIMIT;
	    if(diffy > SPEED_LIMIT) diffy = SPEED_LIMIT;
	    if(diffy < -SPEED_LIMIT) diffy = -SPEED_LIMIT;
	    
	    p.x += diffx;
	    p.y += diffy;

	}

	lastx = x;
	lasty = y;

	return p;

    }

    public void run() {

	// główna pętla
	while (stopper == false) {

	    try {
		socket.receive(dp);
	    } catch (Exception e) {
	    }

	    recvData = dp.getData();

	    // jeżeli pakiet to ping -> odpowiadamy
	    if (recvData[4] == Configuration.PACKET_PING) {

		dp.setAddress(dp.getAddress());
		dp.setPort(22224);
		try {
		    socket.send(dp);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		continue;
	    }

	    
	    // jeżeli pakiet to "HELLO" i nikt nie jest podłączony - przyłączamy klienta
	    if(cfg.client == null && recvData[4] == Configuration.PACKET_HELLO) {
		cfg.client = dp.getAddress();
		gui.setClient(dp.getAddress().getHostAddress());
//		System.out.println(dp.getAddress().getHostAddress() + " connected!");
		continue;
	    }
	    
	    // odrzucamy pakiety od innych klientów, jeśli jakiś jest ustawiony
	    if((cfg.client != null && !dp.getAddress().equals(cfg.client)) || cfg.client == null) {
//		System.out.println("malicious client: " + dp.getAddress().getHostAddress());
		if(cfg.client != null) {
//		    System.out.println("Connected client: " + cfg.client.getHostAddress());
		}
		continue;
	    }
	    
	    // jeżeli pakiet to "BYEBYE" - rozłączamy klienta
	    if(recvData[4] == Configuration.PACKET_BYEBYE) {
		cfg.client = null;
		gui.resetClient();
//		System.out.println(dp.getAddress().getHostAddress() + " disconnected!");
		continue;
	    }
	    
	    Point newLoca = getCoordsTouchpad();

	    //aplikujemy przesunięcie
	    if (location != null) {
		location = new Point(location.x + newLoca.x, location.y + newLoca.y);
	    } else {
		location = new Point(0, 0);
	    }

	    // ograniczamy położenie kursora do rozmiaru ekranu
	    if (location.x < 0)
		location.x = 0;
	    if (location.y < 0)
		location.y = 0;
	    if (location.x > cfg.screenSize.width)
		location.x = cfg.screenSize.width;
	    if (location.y > cfg.screenSize.height)
		location.y = cfg.screenSize.height;

	    switch (recvData[4]) {

	    // ruch kursora
	    case Configuration.PACKET_MOUSE_MOVE:

		gui.setXYLabels(location.x, location.y);
		r.mouseMove(location.x, location.y);
		break;

	    // naciśnięcie (+ obsługa drag and drop)
	    case Configuration.PACKET_MOUSE_DOWN:
		if (new Date().getTime() - lastDown < 200) {
		    r.mousePress(InputEvent.BUTTON1_MASK);
		}
		lastDown = new Date().getTime();
		break;

	    // podniesienie palca
	    case Configuration.PACKET_MOUSE_UP:
		if (lastType == Configuration.PACKET_MOUSE_DOWN) {
		    r.mousePress(InputEvent.BUTTON1_MASK);
		}
		
		r.mouseRelease(InputEvent.BUTTON1_MASK);
		break;

	    // scrolling
	    case Configuration.PACKET_SCROLL_UP:
		r.mouseWheel(-1);
		break;

	    case Configuration.PACKET_SCROLL_DOWN:
		r.mouseWheel(1);
		break;

	    }

	    lastType = recvData[4];

	}

	r.mouseRelease(InputEvent.BUTTON1_MASK);
	socket.close();

    }

}
