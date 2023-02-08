import java.util.HashMap;
import java.util.Map;

public class ConfigurationConstants {

	public final static String OCP_CONFIG_DIR = "O:\\Workspaces\\telco_4.16.1\\ocp-conf";
	public final static String PLATFORM_FILE_NAME = "deployment-platform.properties";
	public final static String CONFIGURATION_FILE_NAME_FORMAT = "%s-%s-params.properties";

	public final static Map<String, String> PLATFORM_CONFIG = new HashMap<String, String>() {
		private static final long serialVersionUID = -5914334867163888750L;
		{
			put("PLATFORM", "OS4");
		}
	};

	public final static Map<String, String> CPU_CONFIG = new HashMap<String, String>() {
		private static final long serialVersionUID = -5914334867163888750L;
		{
			put("CPU_LIMIT", "1000m");
		}
	};

	public final static Map<String, String> PODS_CONFIG = new HashMap<String, String>() {
		private static final long serialVersionUID = 1111097521008579797L;
		{
			put("NUM_REPLICAS", "1");
			put("CPU_REQUEST", "25m");
		}
	};

	public final static Map<String, String> MEMORY_CONFIG = new HashMap<String, String>() {
		private static final long serialVersionUID = 1703210671397916260L;
		{
			put("MEMORY_REQUEST", "768Mi");
			put("MEMORY_LIMIT", "768Mi");
		}
	};
}
