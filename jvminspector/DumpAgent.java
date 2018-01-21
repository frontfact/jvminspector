package jvminspector;

import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.tools.Tool;
import sun.jvm.hotspot.tools.jcore.ClassDump;

public final class DumpAgent extends Tool {

	public static enum Mode {
		VM_CLASS_LIST,
		VM_CLASS_DUMP
	};
	
	private final Mode mode_;
	private final String full_class_name_;
	private final String output_path_;
	private final Set<String> classnames_;
	
	
	public DumpAgent(Mode mode, String full_class_name, String output_path) {
		mode_ = mode;
		full_class_name_ = full_class_name;
		output_path_ = output_path;
		classnames_ = new TreeSet<>();
	}
	
	public Set<String> get_classnames() {
		return classnames_;
	}

	public void execute_custom(String PID) {
		try {
			Method m = Tool.class.getDeclaredMethod("start", String[].class);
			m.setAccessible(true);
			m.invoke(this, ((Object)(new String[]{PID})));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} finally {
			stop();
		}
	}
	
	@Override
	public void run() {
		VM.getVM().getSystemDictionary().classesDo(klass -> {
			String className = klass.getName().asString().replace('/', '.');
			switch (mode_) {
				case VM_CLASS_LIST:
					classnames_.add(className);
					break;
				case VM_CLASS_DUMP:
					if (className.equals(full_class_name_)) {
						String filter = className.substring(0, className.lastIndexOf("."));
						ClassDump dumper = new ClassDump(null, filter);
						dumper.setOutputDirectory(output_path_);
						dumper.run();
					}
					break;
			}
		});
	}
	
	public static Set<String> list_classes(VirtualMachineDescriptor descriptor) {
		return dump_private(Mode.VM_CLASS_LIST, descriptor, null, null);
	}
	public static void dump_class(VirtualMachineDescriptor descriptor, String full_class_name, String output_path) {
		System.out.println("Dumping "+full_class_name+" in "+output_path);
		dump_private(Mode.VM_CLASS_DUMP, descriptor, full_class_name, output_path);
		try {
			Files.walk(Paths.get(output_path))
					.filter(file -> file.toString().endsWith(".class"))
					.forEach(file -> {
						ConsoleDecompiler.main(new String[]{file.toString(), output_path});
					});
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static Set<String> dump_private(Mode mode, VirtualMachineDescriptor descriptor, String full_class_name, String output_path) {
		Set<String> result = Collections.EMPTY_SET;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try (AutoCloseable release_executor = executor::shutdown)
		{
			Future<Set<String>> future = executor.submit(() -> {
				System.out.println("Attaching agent...");
				long startTime = System.currentTimeMillis();
				DumpAgent agent = new DumpAgent(mode, full_class_name, output_path);
				agent.execute_custom(descriptor.id());
				long elapsedTime = (new Date()).getTime() - startTime;
				System.out.println("Agent out : "+(elapsedTime/1000.0)+"s");
				return agent.get_classnames();
			});
			result = future.get();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}
}
