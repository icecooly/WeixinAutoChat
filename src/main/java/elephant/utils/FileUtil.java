package elephant.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 
 * @author icecooly
 *
 */
public class FileUtil {
	//
	public static void save(String filePath,byte[] content) throws IOException{
		Files.write(Paths.get(filePath), content);
	}
	//
	public static String getContent(File file) throws IOException{
		return new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8);
	}
	//
	public static byte[] getContentBytes(File file) throws IOException{
		return Files.readAllBytes(file.toPath());
	}
}
