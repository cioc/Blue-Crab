package blue_crab.Tests;

import java.util.HashMap;

import org.junit.Test;

import rice.p2p.commonapi.Id;

import blue_crab.BlueCrab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FileStoreTest {
	int replicas = 4;
	String storage_directory = "/home/charles/bluecrab/";
	int test_node_count = 10;
	int test_port = 9001;
	BlueCrab crab;
	
	
	@Test
	public void test() throws Exception {
		this.crab = new BlueCrab(replicas, test_port, null, test_node_count, storage_directory);
		Id id = this.crab.setFromString("i like to eat tacos!");
		assertFalse(id == null);
		String val = this.crab.get(id);
		assertEquals("i like to eat tacos!", val);
		Id r1id = this.crab.setFromString("where are we going to go?");
		assertFalse(id == null);
		id = this.crab.setFromString("who is the cat on the wall");
		assertFalse(id == null);
		id = this.crab.setFromString("there is not much to do here");
		assertFalse(id == null);
		id = this.crab.setFromString("kill the rabbit!");
		assertFalse(id == null);
		
		id = this.crab.setFromFile("/home/charles/Dropbox/eclipse/blue_crab/testfiles/testSearchFile1.txt");
		assertFalse(id == null);
		System.out.println("File 1 stored with id: "+id.toStringFull());
		
		id = this.crab.setFromFile("/home/charles/Dropbox/eclipse/blue_crab/testfiles/testSearchFile2.txt");
		assertFalse(id == null);
		System.out.println("File 2 stored with id: "+id.toStringFull());
		
		HashMap<Id,String> results = this.crab.search("raygun", 2000);
		assertEquals(1, results.keySet().size());
		for (Id x: results.keySet()) {
			System.out.println(x.toStringFull()+" | "+results.get(x));
			assertEquals("testSearchFile1.txt", results.get(x));
		}
		HashMap<Id,String> results2 = this.crab.search("stormfront", 2000);
		assertEquals(1, results2.keySet().size());
		for (Id x: results2.keySet()) {
			System.out.println(x.toStringFull()+" | "+results2.get(x));
			assertEquals("testSearchFile2.txt", results2.get(x));
		}
	}
}
