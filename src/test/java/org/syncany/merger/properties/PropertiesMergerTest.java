package org.syncany.merger.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.syncany.plugins.merge.FileVersionContent;
import org.syncany.plugins.transfer.StorageException;

public class PropertiesMergerTest {

	PropertiesMerger merger;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		PropertiesMergerSettings settings = new PropertiesMergerSettings();
		merger = new PropertiesMerger(settings, "user1");
	}

	@Test
	public void nonConflictingProperties() {
		FileVersionContent commonAncestorFile = createVersion("Prop1 = value1\n");
		FileVersionContent latestRemoteFile = createVersion("Prop1 = value1\n");
		File localFile = createFile("Prop1 = value1\n");
		
		merger.merge(latestRemoteFile, localFile, commonAncestorFile);

//		assertEquals(URI.create("/a"), UriBuilder.fromRoot("/a").build());

	}

	private File createFile(String content) {
		// TODO Auto-generated method stub
		return null;
	}

	private FileVersionContent createVersion(final String content) {
		// TODO Auto-generated method stub
		return new FileVersionContent() {

			@Override
			public File getFile() throws StorageException, IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public InputStream openInputStream() throws FileNotFoundException, StorageException, IOException {
				// TODO Auto-generated method stub
				return null;   // new BufferedInputStream
			}};
	}


}
