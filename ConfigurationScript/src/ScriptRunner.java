import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScriptRunner {

	public static void main(String[] args) {
		// 1. Get the file
		String fileName = "services.txt";
		InputStream is = getFileFromResource(fileName);

		// 2. Read the file content
		List<String> lines = readFileStream(is);

		// 3. Extract the services names and folder version into a HashMap with a RegEx
		Map<String, String> servicesInformation = getServicesInformation(lines);

		// 4.Read the ocp-config directory to avoid creating duplicated folders
		// Creating a File object for directory
		File ocpConfigDirectory = new File(ConfigurationConstants.OCP_CONFIG_DIR);
		// List of all files and directories
		List<String> ocpConfigFolders = Arrays.asList(ocpConfigDirectory.list());

		// 5. Filter the existing configuration folders
		filterExistingFolders(servicesInformation, ocpConfigFolders);

		// 6. Create the folders now :)
		for (String serviceName : servicesInformation.keySet()) {
			try {

				// 1. Service directory
				String serviceNameDir = String.format("%s\\%s", ConfigurationConstants.OCP_CONFIG_DIR, serviceName);
				Path path = Paths.get(serviceNameDir);
				Files.createDirectories(path);

				// 2. Service Version directory
				String serviceVersion = servicesInformation.get(serviceName);
				String serviceVersionDir = String.format("%s\\%s", serviceNameDir, serviceVersion);
				path = Paths.get(serviceVersionDir);
				Files.createDirectories(path);
				// 2.1. Service Version directory PLATFORM_CONFIG file creation
				String platformFile = String.format("%s\\%s", serviceVersionDir,
						ConfigurationConstants.PLATFORM_FILE_NAME);
				FileWriter platformFileWriter = new FileWriter(platformFile);
				platformFileWriter.write(getConfigProperties(ConfigurationConstants.PLATFORM_CONFIG));
				platformFileWriter.close();

				// 3. Environments folder creation
				List<ConfigurationEnvironment> environments = Stream.of(ConfigurationEnvironment.values())
						.collect(Collectors.toList());
				for (ConfigurationEnvironment env : environments) {
					// 3.1. Configuration Environment directory
					String configEnvironmentDir = String.format("%s\\%s", serviceVersionDir, env);
					path = Paths.get(configEnvironmentDir);
					Files.createDirectories(path);

					// 3.2. Configuration Environment directory file creation
					String configEnvFileName = String
							.format(ConfigurationConstants.CONFIGURATION_FILE_NAME_FORMAT, serviceName, serviceVersion);
					String configEnvFile = String.format("%s\\%s", configEnvironmentDir, configEnvFileName);
					FileWriter configEnvWriter = new FileWriter(configEnvFile);
					// 3.3. Write the pods config on this specific files
					if (ConfigurationEnvironment.ECE.equals(env) || ConfigurationEnvironment.EDR.equals(env) ||
							ConfigurationEnvironment.EPR.equals(env)) {
						configEnvWriter.write(getConfigProperties(ConfigurationConstants.PODS_CONFIG));
					}
					configEnvWriter.write(getConfigProperties(ConfigurationConstants.MEMORY_CONFIG));
					configEnvWriter.close();
				}
						
			} catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		System.out.println("Success! :)");
	}

	private static String getConfigProperties(Map<String, String> platformConfig) {
		StringBuilder config = new StringBuilder();
		platformConfig.forEach((key, value) -> {
			config.append(String.format("%s=%s\n", key, value));
		});
		return config.toString();
	}

	private static InputStream getFileFromResource(String fileName) {
		try {
			return ScriptRunner.class.getClassLoader().getResourceAsStream(fileName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static List<String> readFileStream(InputStream is) {
		try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader)) {
			return reader.lines().collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<String, String> getServicesInformation(List<String> lines) {
		String serviceRegEx = "(\\w{3}-\\w{3}-\\w{3}-\\w*)\\/tags\\/(\\d{1})";
		Pattern pattern = Pattern.compile(serviceRegEx);

		Map<String, String> servicesInformation = new HashMap<>();
		lines.forEach(line -> {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String name = matcher.group(1);
				String version = matcher.group(2);
				servicesInformation.put(name, version);
			}
		});
		return servicesInformation;
	}

	// TODO: In case of existing we should then check if the version of the subfolder
	// is the same as the version that is being check
	// TODO: Filter also the Loc folders
	private static void filterExistingFolders(
			Map<String, String> servicesInformation,
			List<String> opcConfigFolders) {

		Set<String> existingFolders = new HashSet<>();
		Set<String> serviceNames = servicesInformation.keySet();
		serviceNames.stream()
				.filter(serviceName -> {
					if (opcConfigFolders.contains(serviceName)) {
						existingFolders.add(serviceName);
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
		existingFolders.forEach(existingFolder -> servicesInformation.remove(existingFolder));
	}

}
