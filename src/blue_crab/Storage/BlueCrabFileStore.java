package blue_crab.Storage;

import java.util.HashMap;

/*
 *  manages file store
 */
/*
 *  SEE THE FILE SENDING PASTRY TUTORIAL
 */
public class BlueCrabFileStore<K,V> extends HashMap<K, V> {
	private static final long serialVersionUID = 45738L;
	private String storage_directory;
	private String meta_name;
	
	public BlueCrabFileStore(String directory, String meta_data_file) {
		super();
		this.storage_directory = directory;
		this.meta_name = meta_data_file;
	}
}
