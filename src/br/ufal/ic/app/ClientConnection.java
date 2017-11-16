package br.ufal.ic.app;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnection implements Runnable {

	private Socket clientSocket;
	private BufferedReader in = null;

	private static String diretoryWorking = System.getProperty("user.dir");
	private static String fileSeparator = System.getProperty("file.separator");
	private static String serverFolder = diretoryWorking + fileSeparator + "server_files";
	private static String clientFolder = diretoryWorking + fileSeparator + "client_files";

	public ClientConnection(Socket client) {
		this.clientSocket = client;
	}

	@Override
	public void run() {

		try {

			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String clientSelection;

			while ((clientSelection = in.readLine()) != null) {
				switch (clientSelection) {
				case "1":
					receiveFile();
					break;
				case "2":
					String outGoingFileName;
					while ((outGoingFileName = in.readLine()) != null) {
						sendFile(outGoingFileName);
					}
					break;
				default:
					System.out.println("Incorrect command received.");
					break;
				}
				in.close();
				break;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public void receiveFile() {
		try {
			int bytesRead;

			DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

			String fileName = clientData.readUTF();
			OutputStream output = new FileOutputStream(serverFolder + fileSeparator + "received_from_client_"
					+ new Random().nextInt(101) + "_" + fileName);
			long size = clientData.readLong();
			byte[] buffer = new byte[1024];
			while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				output.write(buffer, 0, bytesRead);
				size -= bytesRead;
			}

			output.close();
			clientData.close();

			System.out.println("\nFile " + fileName + " received from client.");
		} catch (IOException ex) {
			System.err.println("Client error. Connection closed.");
		}
	}

	public void sendFile(String fileName) throws IOException {

		// handle file read
		File myFile = new File(serverFolder + fileSeparator + fileName);
		byte[] mybytearray = new byte[(int) myFile.length()];

		FileInputStream fis = new FileInputStream(myFile);
		BufferedInputStream bis = new BufferedInputStream(fis);

		DataInputStream dis = new DataInputStream(bis);

		dis.read(mybytearray, 0, mybytearray.length);

		// handle file send over socket
		OutputStream os = clientSocket.getOutputStream();

		// Sending file name and file size to the server
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(myFile.getName());
		dos.writeLong(mybytearray.length);
		dos.write(mybytearray, 0, mybytearray.length);
		dos.flush();
		System.out.println("\nFile " + fileName + " sent to client.");

	}
}