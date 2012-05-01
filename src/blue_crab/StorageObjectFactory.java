package blue_crab;

import java.io.FileReader;
import java.io.IOException;
import rice.p2p.commonapi.Id;

import rice.pastry.commonapi.PastryIdFactory;

public class StorageObjectFactory {
	public static StorageObject fromFile(PastryIdFactory id_factory, String path) throws IOException{
		//I'm sure that there is a better way to do this
		//http://rosettacode.org/wiki/Read_entire_file#Java
		FileReader inputStream = new FileReader(path);
		StringBuilder strbld = new StringBuilder();
		char[] buf = new char[8192];
		int read = 0;
		while (read >= 0) {
			strbld.append(buf, 0, read);
			read = inputStream.read(buf);
		}
		String val = strbld.toString();
		Id id = id_factory.buildId(val);	
		
		return new StorageObject(id, val, path);
	}
}
