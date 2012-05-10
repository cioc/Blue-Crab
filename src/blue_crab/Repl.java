package blue_crab;

import java.io.IOException; 
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import rice.p2p.commonapi.Id;
import java.util.HashMap;

public class Repl {
	private BlueCrab crab;
	private HashMap<String,Id> keyToIdMap;
	
	public Repl(BlueCrab crab){
		this.crab = crab;
		keyToIdMap = new HashMap<String,Id>();
	}
	
	private class Operation {
		public String type;
		public final String data;
		public String key;
		public Operation(String type, String data){
			this.type = type;
			this.data = data;
			this.key = "";
		}
		public Operation(String type, String data, String key) {
			this.type = type;
			this.data = data;
			this.key = key;
		}
		public final String getKey() {
			final String o = this.key;
			return o;
		}
		
		public String toString() {
			if (key.equals("")) {
				return this.type + " | "+this.data;
			} else {
				return this.type + " | "+ this.key + " | " + this.data;
			}
		}
	}
	
	private Operation detectOperation(String line) {
		String[] pieces = line.split(" ");
		if (pieces.length > 0){
			if (pieces[0].equals("GET")) {
				if (line.length() > 4) {
					return new Operation("GET", line.substring(4));
				} else {
					return null;
				}
			}
			if (pieces[0].equals("SEARCH")) {
				if (line.length() > 7) {
					return new Operation("SEARCH", line.substring(7));
				} else {
					return null;
				}
			}
			if (pieces[0].equals("HELP")) {
				return new Operation("HELP", null);
			}
			if (pieces[0].equals("SETF")) {
				if (line.length() > 5) {
					String[] fileParts = line.substring(5).split("/");
					String key = "";
					if (fileParts.length > 0) {
						key = fileParts[fileParts.length - 1];
					} else {
						key = line.substring(5);
					}
					return new Operation("SETF", line.substring(5), key);
				} else {
					return null;
				}
			}
			if (pieces[0].equals("SET")){
				if (line.length() > 4) {
					if (pieces.length > 2) {
						return new Operation("SET", line.substring(4+pieces[1].length()), pieces[1]);
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
			if (pieces[0].equals("EXIT")) {
				System.exit(0);
			}
			return null;
		} else {
			return null;
		}
	}
	
	private void performOperation(Operation op) {
		if (op.type.equals("GET")) {
			try {
				Id key = this.keyToIdMap.get(op.data);
				System.out.println("Get: " + key.toStringFull());
				String result = crab.get(key);
				System.out.println(result);
			} catch (Exception e) {
				System.err.println("Error during GET operation: "+e.getMessage());
			}
		}
		if (op.type.equals("HELP")) {
			printHelp();
		}
		if (op.type.equals("SET")) {
			try {
				Id id = crab.setFromString(op.data);
				if (id != null) {
					this.keyToIdMap.put(op.getKey(), id);
				}
				System.out.println("Stored:" + id.toStringFull());
			} catch (Exception e) {
				System.err.println("Error during SET operation: "+e.getClass()+" | "+e.getMessage());
				e.printStackTrace();
			}
		}
		if (op.type.equals("SETF")) {
			try {
				Id id = crab.setFromFile(op.data);
				if (id != null) {
					this.keyToIdMap.put(op.getKey(), id);
				}
			} catch (FileNotFoundException e) {
				System.err.println("File not found during SETF operation: "+op.data+" | "+e.getClass()+" | "+e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Error during SETF operation: "+e.getClass()+" | "+e.getMessage());
				e.printStackTrace();
			}
		}
		if (op.type.equals("SEARCH")) {
			try {
				HashMap<Id, String> results = crab.search(op.data, 5000);
				System.out.println("Results:");
				for (Id id : results.keySet()) {
					String data = results.get(id);
					System.out.println(id.toString() + " : "+data);
				}
			} catch (Exception e) {
				System.err.println("Search error: "+op.data+" | "+e.getClass()+" | "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private boolean detectExit(String line){
		if (line.length() >= 4) {
			if (line.substring(0,4).equals("EXIT")) {
				return true;
			} else {
				return false; 
			}
		} else {
			return false;
		}
	}
	
	private void printHelp() {
		System.out.println("SET <key> <val> - set a key to a value");
		System.out.println("SETF <filepath> - store a file in the cluster.  key will be the filename");
		System.out.println("GET <key> - get a val from a key");
		System.out.println("SEARCH <string> - search for string");
		System.out.println("HELP - the help command");
		System.out.println("EXIT the program");
	}
	
	public void start() {
		System.out.println("Welcome to the Blue Crab Console");
		System.out.println("HELP for options");
		InputStreamReader console_in = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(console_in);
		while (1 == 1){
			try {
				String line = in.readLine();
				if (detectExit(line))
					break;
				Operation ops = detectOperation(line);
				if (ops != null) {
					System.out.println(ops);
					performOperation(ops);
				} else {
					System.out.println("NOT A VALID OPERATION");
				}
			} catch (IOException e) {
				System.err.println("IOException"+e.getMessage());
				break;
			}
		}
	}
}