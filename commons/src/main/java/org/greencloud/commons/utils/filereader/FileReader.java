package org.greencloud.commons.utils.filereader;

import static java.io.File.separator;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.greencloud.commons.exception.InvalidScenarioException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Class with method that allow to parse the indicated file
 */
public class FileReader {

	/**
	 * Method reads a file from a selected path
	 *
	 * @param filePath path to the file
	 * @return File
	 */
	public static File readFile(final String filePath) {
		try (InputStream inputStream = FileReader.class.getClassLoader().getResourceAsStream(filePath)) {
			final File scenarioTempFile = File.createTempFile("test", ".txt");
			copyInputStreamToFile(inputStream, scenarioTempFile);
			return scenarioTempFile;
		} catch (IOException | NullPointerException e) {
			throw new InvalidScenarioException("Invalid file name.", e);
		}
	}

	/**
	 * Method reads files from a selected path
	 *
	 * @param filesPath path to the files
	 * @return File
	 */
	public static List<File> readAllFiles(final String filesPath) {
		final PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver(
				FileReader.class.getClassLoader());

		try {
			final Resource[] resources = patternResolver.getResources(filesPath);

			if (resources.length == 0) {
				return Collections.emptyList();
			}

			return Arrays.stream(resources).map(resource -> {
				try {
					return resource.getFile().listFiles();
				} catch (IOException e) {
					throw new InvalidScenarioException("File could not be read.", e);
				}
			}).flatMap(Stream::of).filter(File::isFile).map(file -> {
				try {
					final File scenarioTempFile = File.createTempFile("test_" + file.getName(), ".txt");
					copyInputStreamToFile(new FileInputStream(file), scenarioTempFile);
					return scenarioTempFile;
				} catch (IOException e) {
					throw new InvalidScenarioException("File could not be read.", e);
				}
			}).toList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	/**
	 * Method verifies if the system was started from the JAR or IDE
	 *
	 * @return boolean indicating if the system was started from jar
	 */
	public static boolean isLoadedInJar() {
		final String classPath = FileReader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		return classPath.endsWith(".jar");
	}

	/**
	 * Method builds correct path of the given resource file
	 *
	 * @param pathElements elements of the path
	 * @return String path to the resource
	 */
	public static String buildResourceFilePath(final String... pathElements) {
		if (isLoadedInJar()) {
			return String.join("/", pathElements);
		}
		return String.join(separator, pathElements);
	}
}
