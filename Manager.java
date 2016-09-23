/**
 * 	FICHERO: Manager.java
 * 	DESCRIPCIÓN: Gestiona el programa principal que lanzará
 * 				procesos en cuanto se ejecute de la siguiente
 * 				forma:
 * 				java -jar 467716_530162_SSDDp4.jar [-d] id fichero_red
 */
package ssdd.ms;

public class Manager {

	public static void main(String[] args){
		int id;
		String file;
		if (args.length==3){
			id = Integer.parseInt(args[1]);
			file = args[2];
			new Process(id, file, true);
		}
		else{
			id = Integer.parseInt(args[0]);
			file = args[1];
			new Process(id, file, false);
		}
	}
}
