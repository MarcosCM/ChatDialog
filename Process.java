/**
 * 	FICHERO: Ejemplo.java
 * 	DESCRIPCIÓN: Clase para probar las ventanas de chat
 * 				con los procesos.
 */
package ssdd.ms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

public class Process {
	
	private ChatDialog v;
	
	public Process(int id, String file, boolean debug){
		MessageSystem ms = null;
		try {
			ms = new MessageSystem(id, file, debug);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		final Sender s = new Sender(ms);
		final Receiver r = new Receiver(ms);
		
		final ActionListener listener = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String m = v.text();
				if (!m.isEmpty()){
					v.addMessage("Yo: " + m);
					s.sendMessage(new MessageValue(m, true));
				}
			}
		};
		
		v = new ChatDialog(listener);
		v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		r.setChat(v);
		
		new Thread(s).start();
		new Thread(r).start();
	}
}
