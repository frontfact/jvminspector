
package jvminspector;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.logging.Logger;

public class Main {
	
	public static final Logger logger = Logger.getLogger("");
	
    public static void main(String[] args)
    {
        EventQueue.invokeLater(
                () ->
                {
                    MainWindow window = new MainWindow();
                    final Toolkit toolkit = Toolkit.getDefaultToolkit();
                    final Dimension screenSize = toolkit.getScreenSize();
                    final int x = (screenSize.width - window.getWidth()) / 2;
                    final int y = (screenSize.height - window.getHeight()) / 2;
                    window.setLocation(x, y);
                    window.setVisible(true);
                }
        );
    }
}
