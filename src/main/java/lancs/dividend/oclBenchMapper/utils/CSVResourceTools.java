package lancs.dividend.oclBenchMapper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CSVResourceTools {

	public static final char DEFAULT_SEPARATOR = ',';

	public static List<String> readHeader(Path source) throws IOException {
		return readHeader(source, 0);
    }
	
    public static List<String> readHeader(Path source, int skipLines) throws IOException {
		return readHeader(source, skipLines, DEFAULT_SEPARATOR);
    }
    
    public static List<String> readHeader(Path source, int skipLines, char sep) throws IOException {
    	if(source == null) throw new IllegalArgumentException("Given path must not be null.");

		return readHeader(Files.newBufferedReader(source, Charset.forName("UTF-8")), skipLines, sep);
    }
    
    public static List<String> readHeader(Reader source, int skipLines, char sep) {
    	if(source == null) throw new IllegalArgumentException("Given reader must not be null.");
    	if(skipLines < 0) throw new IllegalArgumentException("Given skip amount must not be negative.");
    	
        try (BufferedReader reader = new BufferedReader(source)) {
            return reader.lines()
            		.skip(skipLines)
                    .findFirst()
                    .map(line -> Arrays.asList(line.split(Character.toString(sep))))
                    .get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }    
    
    
	public static List<List<String>> readRecords(Path source) throws IOException {
		return readRecords(source, 0);
    }
	
    public static List<List<String>> readRecords(Path source, int skipLines) throws IOException {
		return readRecords(source, skipLines, DEFAULT_SEPARATOR);
    }
    
    public static List<List<String>> readRecords(Path source, int skipLines, char sep) throws IOException {
    	if(source == null) throw new IllegalArgumentException("Given path must not be null.");

		return readRecords(Files.newBufferedReader(source, Charset.forName("UTF-8")), skipLines, sep);
    }
    
    public static List<List<String>> readRecords(Reader source, int skipLines, char sep) {
    	if(source == null) throw new IllegalArgumentException("Given reader must not be null.");
    	if(skipLines < 0) throw new IllegalArgumentException("Given skip amount must not be negative.");
    	
        try (BufferedReader reader = new BufferedReader(source)) {      	
            return reader.lines()
            		.skip(skipLines + 1) // one for header
                    .map(line -> Arrays.asList(line.split(Character.toString(sep), -1)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
