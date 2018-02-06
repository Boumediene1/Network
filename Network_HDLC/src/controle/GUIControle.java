package controle;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import modele.Client;
import modele.Server;
import vue.GUI;

public class GUIControle implements ActionListener {
	private Server server;
	private Client client;
	private GUI gui;
	private String file2Send;
	
	public GUIControle(String string) {
		gui = new GUI(string, this);
		gui.setVisible(true);
		file2Send = "";
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Start Server"))
	//				System.out.println();
			EventQueue.invokeLater(new Runnable() {
	            //@Override
	            public void run() {
	            	server = new Server(gui);
					server.start();
	            }
	        });
		else if(e.getActionCommand().equals("Send File to Server"))
			EventQueue.invokeLater(new Runnable() {
	            //@Override
	            public void run() {
	            	try{
	            		client = new Client(gui, file2Send);
	            		client.start();
	            	}catch(IllegalThreadStateException e){
	            		client.afficher(e.getMessage());
	            	}
	            }
	        });
		else if(e.getActionCommand().equals("Stop Server"))
			server.stopServer();
		else if(e.getActionCommand().equals("Disconnect from Server"))
			client.stopClient();
		else if (e.getActionCommand().equals("Load File")){
			file2Send = gui.fileChooser("Texte a envoyer");
		}
	}

}
