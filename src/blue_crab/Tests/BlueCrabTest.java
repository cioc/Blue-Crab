package blue_crab.Tests;

import java.util.HashMap;

import org.junit.Test;

import rice.p2p.commonapi.Id;

import blue_crab.BlueCrab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BlueCrabTest {
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
		HashMap<Id,String> results = this.crab.search("where", 2000);
		assertEquals(1, results.keySet().size());
		for (Id x: results.keySet()) {
			assertEquals("where are we going to go?", results.get(x));
		}
		Id r2id = this.crab.setFromString("there is something where");
		assertFalse(id == null);
		HashMap<Id,String> results2 = this.crab.search("where", 2000);
		assertEquals(2, results2.keySet().size());
		assertEquals("where are we going to go?", results2.get(r1id));
		assertEquals("there is something where", results2.get(r2id));
	}
}
