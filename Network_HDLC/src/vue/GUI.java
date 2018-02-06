package vue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import controle.GUIControle;

@SuppressWarnings("serial")
public class GUI extends JFrame {
	private JButton startServer, startClient, loadFile;
	private JLabel serverLabel, clientLabel, serverIcon, clientIcon, serverStatus, clientStatus;
	private JSpinner serverPort, clientPort;
	JLabel clientFilename;
	private JTextField clientHostname;
	
	private JPanel serverPanel, clientPanel, textoutputPanel;
	private JTextArea textoutput, clientToServer, serverToClient;
	
//	private GUIControle controle;
		
	private int height, width, ratio;
	ArrayList<JLabel> c2s;
	
	public GUI(String name, GUIControle controle){
		super(name);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width,screenSize.height);
		height = (int) this.getSize().getHeight();
		width = (int) this.getSize().getWidth();
		ratio = 85;
				
		Container content = getContentPane();
		((JComponent) content).setBorder(BorderFactory.createEmptyBorder(height/10, width/10, height/10, width/10));
		content.setLayout(new GridLayout(1,3,width/50,0));
		
		/*
		 * SERVER PANEL
		 */
		serverPanel = new JPanel();
		serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
		serverStatus = addLabeledLabel(serverPanel, "Status : ", "OFFLINE", Color.RED);

		ImageIcon sIcon = createImageIcon("images/server.png", "a server representation");
		serverLabel = new JLabel("Serveur");
		serverIcon = new JLabel(sIcon);
		serverLabel.setFont(new Font("Arial", Font.PLAIN, (int)screenSize.getWidth()/(ratio/2)));
		
		serverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		serverIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
		serverPanel.add(serverLabel);
		serverPanel.add(serverIcon);
		
		SpinnerNumberModel portOptions = new SpinnerNumberModel(8000, //initial value
                8000, //min
                9000, //max
                10); 
		serverPort = addLabeledSpinner(serverPanel, "Listening Port : ", portOptions);
		
		startServer = new JButton("Start Server");
		startServer.setFont(new Font("Arial", Font.PLAIN, (int)screenSize.getWidth()/ratio));
		startServer.addActionListener(controle);
		startServer.setAlignmentX(Component.CENTER_ALIGNMENT);
		serverPanel.add(startServer);
		serverToClient = new JTextArea(30,30);
		JScrollPane scrollPaneS = new JScrollPane(serverToClient);
		serverPanel.add(scrollPaneS, BorderLayout.PAGE_END);
		
		/*
		 * CLIENT PANEL
		 */
		clientPanel = new JPanel(){
			public Dimension getPreferredSize() {
			       return this.getSize();
			   };
		};
		clientPanel.setLayout(new BoxLayout(clientPanel, BoxLayout.Y_AXIS));		
		clientStatus = addLabeledLabel(clientPanel, "Status : ", "OFFLINE", Color.RED);

		ImageIcon cIcon = createImageIcon("images/client.png", "a client representation");
		clientLabel = new JLabel("Client", JLabel.CENTER);
		clientIcon = new JLabel(cIcon);
		clientLabel.setFont(new Font("Arial", Font.PLAIN, (int)screenSize.getWidth()/(ratio/2)));
		
		clientLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		clientIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
		clientPanel.add(clientLabel);
		clientPanel.add(clientIcon);
		
		
		clientHostname = addLabeledInput(clientPanel, "Host : ");
		SpinnerNumberModel portOptions2 = new SpinnerNumberModel(8000, //initial value
                8000, //min
                9000, //max
                10); 
		clientPort = addLabeledSpinner(clientPanel, "Connecting Port : ", portOptions2);
		
		clientFilename = addLabeledLabel(clientPanel, "File : ","No file selected",Color.BLACK);
		
		loadFile = new JButton("Load File");
		loadFile.setFont(new Font("Arial", Font.PLAIN, (int)screenSize.getWidth()/ratio));
		loadFile.setAlignmentX(Component.CENTER_ALIGNMENT);
		loadFile.addActionListener(controle);

		clientPanel.add(loadFile);
		
		startClient = new JButton("Send File to Server");
		startClient.setFont(new Font("Arial", Font.PLAIN, (int)screenSize.getWidth()/ratio));
		startClient.addActionListener(controle);
		
		startClient.setAlignmentX(Component.CENTER_ALIGNMENT);
		clientPanel.add(startClient);
				
		clientToServer = new JTextArea(30,30);
		JScrollPane scrollPaneC = new JScrollPane(clientToServer);
		clientPanel.add(scrollPaneC, BorderLayout.PAGE_END);
		
		/*
		 * TEXT OUTPUT PANEL
		 */
		textoutputPanel = new JPanel();
		textoutput = new JTextArea();
		JScrollPane to = new JScrollPane(textoutput);
		textoutputPanel.setLayout(new BoxLayout(textoutputPanel, BoxLayout.Y_AXIS));
		textoutputPanel.add(new JLabel("Text Reconstructred On Server"));
		textoutputPanel.add(to);
		
		add(serverPanel);
		add(clientPanel);
		add(textoutputPanel);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private ImageIcon createImageIcon(String path, String description) {
		try{
			Image image = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource(path));
			return new ImageIcon(image.getScaledInstance(height/6, height/6, 0));
		}catch(NullPointerException e){
			showMessage("L'image "+path+" est introuvable"); 	
			return null;
		}
	}
	
	private JSpinner addLabeledSpinner(Container c, String label, SpinnerModel model) {
		JPanel spinnerPanel = new JPanel();
		JLabel l = new JLabel(label);
		spinnerPanel.add(l);
		
		JSpinner spinner = new JSpinner(model);
		l.setLabelFor(spinner);
		spinnerPanel.add(spinner);
		
		c.add(spinnerPanel);
		spinnerPanel.setPreferredSize(spinnerPanel.getPreferredSize());
		spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));

		return spinner;
	}
	
	private JTextField addLabeledInput(Container c, String label) {
		JPanel inputPanel = new JPanel();
		JLabel l = new JLabel(label);
		inputPanel.add(l);
		
		JTextField input = new JTextField("",15);
		l.setLabelFor(input);
		inputPanel.add(input);
		c.add(inputPanel);
		return input;
	}
	
	private JLabel addLabeledLabel(Container c, String label, String text, Color couleur) {
		JPanel labelPanel = new JPanel();
		JPanel labelPanel2 = new JPanel();

		labelPanel.setLayout(new BorderLayout());
		JLabel l = new JLabel(label);
		l.setVerticalAlignment(JLabel.BOTTOM);
		labelPanel2.add(l);
		
		JLabel textLabel = new JLabel(text);
		textLabel.setForeground(couleur);
		l.setLabelFor(textLabel);
		labelPanel2.add(textLabel);

		labelPanel.add(labelPanel2, BorderLayout.PAGE_END);
		c.add(labelPanel);
		labelPanel.setPreferredSize(labelPanel.getPreferredSize());
		return textLabel;
	}
	
	public String fileChooser(String title) {
		String filename = null;
		JFileChooser chooser = new JFileChooser(title);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION){
	    	filename = chooser.getSelectedFile().getAbsolutePath();
	    	clientFilename.setText(chooser.getSelectedFile().getName());
	    }
	    return filename;
	}

	public int addNewWord(String mot, String title) {
	    String[] options = new String[] {"Yes to All", "Yes", "No", "No to All"};
		int response = JOptionPane.showOptionDialog(null, "Voulez-vous ajouter "+mot+" au dictionnaire?", title,
		        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
		        null, options, options[1]);
		return response;
	}

	public void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message);
	}
	
	public int getServerPort(){
		return (int) serverPort.getValue();
	}

	public void changeServerStatus(boolean online){
		serverPort.setEnabled(!online);
		if(online){
			startServer.setText("Stop Server");
			serverStatus.setText("ONLINE");
			serverStatus.setForeground(Color.GREEN);
		}
		else{
			startServer.setText("Start Server");
			serverStatus.setText("OFFLINE");
			serverStatus.setForeground(Color.RED);
		}
	}

	public String getClientHostname() {
		return clientHostname.getText();
	}

	public int getClientPortNumber() {
		return (int) clientPort.getValue();
	}

	public String getClientFilename() {
		return clientFilename.getText();
	}

	public void changeClientStatus(boolean online) {
		clientHostname.setEnabled(!online);
		clientPort.setEnabled(!online);
		loadFile.setEnabled(!online);
		if(online){
			startClient.setText("Disconnect from Server");
			clientStatus.setText("ONLINE");
			clientStatus.setForeground(Color.GREEN);
		}
		else{
			startClient.setText("Send File to Server");
			clientStatus.setText("OFFLINE");
			clientStatus.setForeground(Color.RED);
		}
		
	}
	
	public void simulateTransfer(){
		for (Component c : clientToServer.getComponents()) {
		    if (c instanceof JLabel) { 
		       ((JLabel)c).setText("ok");
		       repaint();
		       try {
				Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		       ((JLabel)c).setText("");
		    }
		}
	}

	public void log(boolean client, String message) {
		if(client)
			clientToServer.append(message);
		else
			serverToClient.append(message);
		
	}
	public void logn(boolean client, String message) {
		log(client, message);
		if(client) 
			clientToServer.append("\n");
		else 
			serverToClient.append("\n");
	}
	
	public void rebuildText(String c) {
		textoutput.append(c);
	}
	
}
