package blue_crab;

import java.io.IOException; 
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
		public Operation(String type, String key, String data) {
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
			if (pieces[0].equals("SET")){
				if (line.length() > 4) {
					if (pieces.length > 2) {
						return new Operation("SET",pieces[1], line.substring(4+pieces[1].length()));
					} else {
						return null;
					}
				} else {
					return null;
				}
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
				String result = crab.get(key);
				System.out.println(result);
			} catch (Exception e) {
				System.err.println("Error during GET operation: "+e.getMessage());
			}
			
		}
		if (op.type.equals("SET")) {
			try {
				Id id = crab.set(op.data);
				if (id != null) {
					this.keyToIdMap.put(op.getKey(), id);
				}
			} catch (Exception e) {
				System.err.println("Error during SET operation: "+e.getClass()+" | "+e.getMessage());
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
	
	public void start() {
		System.out.println("Welcome to the Blue Crab Console");
		System.out.println("Options: SET <string> | GET <string>");
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
