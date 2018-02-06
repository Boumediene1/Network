import javax.swing.SwingUtilities;
import controle.GUIControle;

public class Hdcl {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		        createAndShowGUI();
		    }

			private void createAndShowGUI() {
				new GUIControle("HDCL");
			}
		});
		
	}

}
