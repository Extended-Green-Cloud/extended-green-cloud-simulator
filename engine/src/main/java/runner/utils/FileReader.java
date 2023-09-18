package runner.utils;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.greencloud.commons.exception.InvalidScenarioException;

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
			throw new InvalidScenarioException("Invalid scenario file name.", e);
		}
	}
}
