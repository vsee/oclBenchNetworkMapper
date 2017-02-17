package lancs.dividend.oclBenchMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JOptionPane;

public class OclMapperClient {

	private int port;

	public OclMapperClient(int port) {
		this.port = port;
	}

	public void start() throws IOException {
		String serverAddress = JOptionPane
				.showInputDialog("Enter IP Address of a machine that is\n"
						+ "running the date service on port " + port + ":");
		Socket s = new Socket(serverAddress, port);
		BufferedReader input = new BufferedReader(new InputStreamReader(
				s.getInputStream()));
		String answer = input.readLine();
		JOptionPane.showMessageDialog(null, answer);
		s.close();
		System.exit(0);
	}

}
