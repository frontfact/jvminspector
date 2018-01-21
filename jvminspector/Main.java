
package jvminspector;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Main {
	
	public static final Logger logger = Logger.getLogger("");
	
    public static void main(String[] args)
    {
		String jdk_registry_key = "HKLM\\SOFTWARE\\JavaSoft\\Java Development Kit";
		String str_jdk_version = extract_registry_value(jdk_registry_key, "CurrentVersion");
		System.out.println("Detected JDK version: "+str_jdk_version);
		String jdk_path = extract_registry_value(jdk_registry_key+"\\"+str_jdk_version, "JavaHome");
		System.out.println("Detected JDK installation: "+jdk_path);
		if (jdk_path.isEmpty()) {
			System.out.println("No JDK detected");
			return;
		}
		String existing_java_lib_path = System.getProperty("java.library.path");
		String new_java_lib_path = Paths.get(jdk_path, "bin").toString()+";"+Paths.get(jdk_path, "jre", "bin").toString()+";"+existing_java_lib_path;
		System.out.println("java.library.path = "+new_java_lib_path);
		System.setProperty("java.library.path", new_java_lib_path);

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
    
	private static String extract_registry_value(String registry_key, String value_name) {
		try {
			ProcessBuilder builder = new ProcessBuilder("reg", "query", registry_key, "/v", value_name);
			Process reg = builder.start();
			try (BufferedReader output = new BufferedReader(new InputStreamReader(reg.getInputStream()))) {
				// remove empty lines and keep lines containing 'REVISION'
				Stream<String> matches = output.lines().filter(l -> !l.isEmpty()).filter(l -> l.contains(value_name));
				// get first match
				Optional<String> opt = matches.findFirst();
				if (opt.isPresent()) {
					// split line in fields
					String[] all_fields = opt.get().split(" ");
					// filter empty fields and get 3rd field (REVISION value)
					String[] fields = Stream.of(all_fields).filter(f -> !f.isEmpty()).toArray(String[]::new);
					return fields[2];
				}
			}
		} catch (IOException ex) {
			Main.logger.log(Level.SEVERE, null, ex);
		}
		return "";
	}
}
