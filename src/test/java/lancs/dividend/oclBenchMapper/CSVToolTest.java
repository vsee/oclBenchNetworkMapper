package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.nio.file.Paths;
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
}
