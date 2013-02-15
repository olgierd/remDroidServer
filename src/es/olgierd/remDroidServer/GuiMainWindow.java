/*
 * Author: Olgierd Pilarczyk
 */

package es.olgierd.remDroidServer;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class GUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private Configuration cfg = null;
    private RemDroidServer rs = null;
    public Thread serverThread = null;

    // GUI ELEMENTS
    public JPanel panel = new JPanel();

    // bottom Buttons
    private JButton startButton = new JButton("Start");
    private JButton stopButton = new JButton("Stop");
    private JButton exitButton = new JButton("Wyjdź");
    private JButton disconnectButton = new JButton("Rozłącz");

    // static labels
    private JLabel ipLabel = new JLabel("IP:");
    private JLabel statusLabel = new JLabel("Status: ");
    private JLabel vertLabel = new JLabel("Pion: ");
    private JLabel horiLabel = new JLabel("Poz: ");
    private JLabel clientLabel = new JLabel("Klient:");

    // dynamic labels
    private JLabel ipLabelVal = new JLabel("127.0.0.1");
    private JLabel statusLabelVal = new JLabel("OK");
    private JLabel vertLabelVal = new JLabel("0");
    private JLabel horiLabelVal = new JLabel("0");
    private JLabel clientLabelVal = new JLabel("-");

    private void disableGuiChanges() {
	startButton.setEnabled(false);
    }
    
    private void enableGuiChanges() {
	startButton.setEnabled(true);
    }
    
    // ustawia status błędu
    public void setStatus(String text) {
	statusLabelVal.setText(text);
	disableGuiChanges();
	this.repaint();
    }
    
    // pokazuje informację o położeniu kursora
    public void setXYLabels(int x, int y) {
	vertLabelVal.setText(Integer.toString(x));
	horiLabelVal.setText(Integer.toString(y));
    }
    
    // ustawia informację o kliencie
    public void setClient(String ia) {
	clientLabelVal.setText(ia);
    }
    
    // czyści informację o kliencie
    public void resetClient() {
	clientLabelVal.setText("-");
    }

    
    private Configuration getConfiguration() throws Exception {

	Configuration cfg = new Configuration();

	cfg.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	cfg.localPort = 22222;

	return cfg;
    }

    // pobiera lokalny adres IP
    private String getLocalIpAddress() {

	try {
	    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

		NetworkInterface intf = en.nextElement();

		for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {

		    InetAddress inetAddress = enumIpAddr.nextElement();

		    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
			return ((Inet4Address) inetAddress).getHostAddress().toString();

		}
	    }
	} catch (SocketException ex) {
	    System.out.println("Failed to obtain IP!");
	}

	return "Failed to obtain IP.";
    }
    
    public void setWidgetsSize() {

	// guziczki
	startButton.setBounds(0, 145, 120, 30);
	stopButton.setBounds(130, 145, 120, 30);
	exitButton.setBounds(260, 145, 120, 30);
	disconnectButton.setBounds(260, 60, 120, 30);

	// lewy panel
	ipLabel.setBounds(0, 0, 70, 30);
	ipLabelVal.setBounds(70, 0, 130, 30);

	statusLabel.setBounds(0, 20, 70, 30);
	statusLabelVal.setBounds(70, 20, 150, 30);

	vertLabel.setBounds(0, 40, 70, 30);
	vertLabelVal.setBounds(70, 40, 70, 30);

	horiLabel.setBounds(0, 60, 70, 30);
	horiLabelVal.setBounds(70, 60, 70, 30);

	// prawa
	clientLabel.setBounds(200, 0, 70, 30);
	clientLabelVal.setBounds(240, 20, 150, 20);

    }

    // startujemy wątek słuchający
    public void start() {
	
	try {
	    cfg = getConfiguration();
	} catch (Exception e) {
	    System.out.println("getting conf failed");
	    statusLabelVal.setText("Invalid port");
	    return;
	}

	rs = new RemDroidServer(this, cfg);
	serverThread = new Thread(rs);
	serverThread.start();
	
    }
    
    //zatrzymujemy wątek
    private void stopThread() {
	
	if(rs != null)	{
	    rs.stopSocket();
	    rs.stopper = true;
	}
    }
    
    public GUI() {

	panel.setLayout(null);
	setWidgetsSize();
	panel.setSize(400, 200);

	// dodajemy widżety
	panel.add(startButton);
	panel.add(stopButton);
	panel.add(exitButton);
	panel.add(disconnectButton);

	panel.add(ipLabel);
	panel.add(ipLabelVal);

	panel.add(statusLabel);
	panel.add(statusLabelVal);

	panel.add(vertLabel);
	panel.add(vertLabelVal);

	panel.add(horiLabel);
	panel.add(horiLabelVal);
	
	panel.add(clientLabel);
	panel.add(clientLabelVal);

	add(panel);

	// guziczki"

	startButton.addActionListener(new ActionListener() {
	    
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		disableGuiChanges();
		
		start();
	    }
	});
	

	stopButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent m) {
		
		enableGuiChanges();
		stopThread();
		
	    }
	});
	
	
	exitButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		stopThread();
		System.out.println("bye :-)");
		System.exit(0);
	    }
	});
	
	disconnectButton.addActionListener(new ActionListener() {
	    
	    @Override
	    public void actionPerformed(ActionEvent e) {
		cfg.client = null;
		resetClient();
	    }
	});

	// ustawiamy IP serwera
	ipLabelVal.setText(getLocalIpAddress());
	
    }

}

public class GuiMainWindow {

    public static void main(String[] args) throws IOException {
	JFrame window = new GUI();

	window.setSize(400, 200);
	window.setResizable(false);
	window.setVisible(true);
	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}

