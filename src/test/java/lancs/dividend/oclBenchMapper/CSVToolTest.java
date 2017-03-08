package lancs.dividend.oclBenchMapper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import lancs.dividend.oclBenchMapper.utils.CSVResourceTools;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CSVToolTest {

	public final String TEST_CSV = "src/test/resources/test.csv";
	public final String TEST_SKIP_CSV = "src/test/resources/test_skip.csv";
	
	@Test
	public void testReadingHeader() {
		List<String> header = null;
		try {
			header = CSVResourceTools.readHeader(Paths.get(TEST_CSV));
		} catch (IOException e) {
			System.out.println(e);
			Assert.assertFalse(true);
		}
		
		Assert.assertEquals(header.size(), 2);
		Assert.assertEquals(header.get(0), "name");
		Assert.assertEquals(header.get(1), "age");
	}
	
	@Test
	public void testReadingHeaderSkip() {
		List<String> header = null;
		try {
			header = CSVResourceTools.readHeader(Paths.get(TEST_SKIP_CSV), 2, ';');
		} catch (IOException e) {
			System.out.println(e);
			Assert.assertFalse(true);
		}
		
		Assert.assertEquals(header.size(), 2);
		Assert.assertEquals(header.get(0), "colour");
		Assert.assertEquals(header.get(1), "amount");
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testReadHeaderNull() {
		try {
			CSVResourceTools.readHeader(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testReadHeaderSkip() {
		try {
			CSVResourceTools.readHeader(Paths.get(TEST_CSV),-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testReadingRecords() {
		List<List<String>> records = null;
		try {
			records = CSVResourceTools.readRecords(Paths.get(TEST_CSV));
		} catch (IOException e) {
			System.out.println(e);
			Assert.assertFalse(true);
		}
		
		Assert.assertEquals(records.size(), 3);
		Assert.assertEquals(records.get(0).size(), 2);
		Assert.assertEquals(records.get(0).get(0), "Jack");
		Assert.assertEquals(records.get(0).get(1), "25");
		
		Assert.assertEquals(records.get(1).size(), 2);
		Assert.assertEquals(records.get(1).get(0), "Ernie");
		Assert.assertEquals(records.get(1).get(1), "30");
		
		Assert.assertEquals(records.get(2).size(), 2);
		Assert.assertEquals(records.get(2).get(0), "Bert");
		Assert.assertEquals(records.get(2).get(1), "32");
	}
	
	@Test
	public void testReadingRecordsSkip() {
		List<List<String>> records = null;
		try {
			records = CSVResourceTools.readRecords(Paths.get(TEST_SKIP_CSV), 2, ';');
		} catch (IOException e) {
			System.out.println(e);
			Assert.assertFalse(true);
		}
		
		Assert.assertEquals(records.size(), 3);
		Assert.assertEquals(records.get(0).size(), 2);
		Assert.assertEquals(records.get(0).get(0), "red");
		Assert.assertEquals(records.get(0).get(1), "34");
		
		Assert.assertEquals(records.get(1).size(), 2);
		Assert.assertEquals(records.get(1).get(0), "green");
		Assert.assertEquals(records.get(1).get(1), "12");
		
		Assert.assertEquals(records.get(2).size(), 2);
		Assert.assertEquals(records.get(2).get(0), "brown");
		Assert.assertEquals(records.get(2).get(1), "21");
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testReadRecordsNull() {
		try {
			CSVResourceTools.readRecords(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testReadRecordsSkip() {
		try {
			CSVResourceTools.readRecords(Paths.get(TEST_CSV),-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testWritingHeader() {
		String[] header = { "test", "head" };
		Path output = null;
		try {
			output = Files.createTempFile("csvToolTest", ".csv").toAbsolutePath();

			OutputStreamWriter outWriter = new OutputStreamWriter(new FileOutputStream(output.toFile()));
			CSVResourceTools.writeRawHeader(outWriter, header);
			outWriter.close();

			List<String> checkHeader = CSVResourceTools.readHeader(output);
			Assert.assertEquals(checkHeader.size(), 2);
			Assert.assertEquals(checkHeader.get(0), header[0]);
			Assert.assertEquals(checkHeader.get(1), header[1]);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			if(output != null) {
				try {
					Files.delete(output);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	@Test
	public void testWritingFile() {
		String[] header = { "test", "head" };
		List<String[]> records = new ArrayList<>();
		records.add(new String[] { "new", "record" });
		records.add(new String[] { "another", "record" });

		Path output = null;
		try {
			output = Paths.get("/tmp/csvToolTest" + System.currentTimeMillis() + ".csv");
			
			CSVResourceTools.writeCSVFile(output, header, records);

			List<String> checkHeader = CSVResourceTools.readHeader(output);
			Assert.assertEquals(checkHeader.size(), 2);
			Assert.assertEquals(checkHeader.get(0), header[0]);
			Assert.assertEquals(checkHeader.get(1), header[1]);
			
			List<List<String>> checkRecords = CSVResourceTools.readRecords(output);
			Assert.assertEquals(checkRecords.size(), 2);
			Assert.assertEquals(checkRecords.get(0).size(), 2);
			Assert.assertEquals(checkRecords.get(0).get(0), "new");
			Assert.assertEquals(checkRecords.get(0).get(1), "record");
			Assert.assertEquals(checkRecords.get(1).size(), 2);
			Assert.assertEquals(checkRecords.get(1).get(0), "another");
			Assert.assertEquals(checkRecords.get(1).get(1), "record");

		} catch (IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			if(output != null) {
				try {
					Files.delete(output);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void testWritingRecords() {
		List<String[]> records = new ArrayList<>();
		records.add(new String[] { "new", "record" });
		records.add(new String[] { "another", "record" });

		Path output = null;
		try {
			output = Files.createTempFile("csvToolTest", ".csv").toAbsolutePath();

			OutputStreamWriter outWriter = new OutputStreamWriter(new FileOutputStream(output.toFile()));
			CSVResourceTools.writeRawHeader(outWriter, new String[] { "bla", "bla" });
			outWriter.close();

			outWriter = new OutputStreamWriter(Files.newOutputStream(output, StandardOpenOption.APPEND, StandardOpenOption.WRITE));
			CSVResourceTools.writeRawRecords(outWriter, records);
			outWriter.close();

			List<List<String>> checkRecords = CSVResourceTools.readRecords(output);
			
			Assert.assertEquals(checkRecords.size(), 2);
			Assert.assertEquals(checkRecords.get(0).size(), 2);
			Assert.assertEquals(checkRecords.get(0).get(0), "new");
			Assert.assertEquals(checkRecords.get(0).get(1), "record");
			
			Assert.assertEquals(checkRecords.get(1).size(), 2);
			Assert.assertEquals(checkRecords.get(1).get(0), "another");
			Assert.assertEquals(checkRecords.get(1).get(1), "record");

		} catch (IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			if(output != null) {
				try {
					Files.delete(output);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWriteHeaderHeadNull() {
		try {
			CSVResourceTools.writeRawHeader(new OutputStreamWriter(new FileOutputStream("/tmp/bla")), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWriteHeaderWriterNull() {
		CSVResourceTools.writeRawHeader(null, new String[] {"bla"});
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWriteRecsRecNull() {
		try {
			CSVResourceTools.writeRawRecords(new OutputStreamWriter(new FileOutputStream("/tmp/bla")), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWriteRecsWriterNull() {
		List<String[]> records = new ArrayList<>();
		records.add(new String[] { "new", "record" });
		records.add(new String[] { "another", "record" });

		CSVResourceTools.writeRawRecords(null, records);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWriteFileHeadNull() {
		List<String[]> records = new ArrayList<>();
		records.add(new String[] { "new", "record" });
		records.add(new String[] { "another", "record" });
		
		CSVResourceTools.writeCSVFile(Paths.get("/tmp/bla" + System.currentTimeMillis()), null, records);
	}
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWriteFileRecNull() {
		CSVResourceTools.writeCSVFile(Paths.get("/tmp/bla" + System.currentTimeMillis()),  new String[] {"bla"}, null);
	}
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWriteFilePathNull() {
		List<String[]> records = new ArrayList<>();
		records.add(new String[] { "new", "record" });
		records.add(new String[] { "another", "record" });
		
		CSVResourceTools.writeCSVFile(null, new String[] {"bla"}, records);
	}
}
