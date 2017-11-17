package br.ufal.ic.app;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class FileClient {

	private static Socket sock;
	private static String fileName;
	private static PrintStream os;

	private static String diretoryWorking = System.getProperty("user.dir");
	private static String fileSeparator = System.getProperty("file.separator");
	private static String serverFolder = diretoryWorking + fileSeparator + "server_files";
	private static String clientFolder = diretoryWorking + fileSeparator + "client_files";

	private static File dir;
	private static File files;
	private static File fileList[] = {};
	private static File fileChoose;

	static Scanner read = new Scanner(System.in);

	public FileClient() throws IOException {

		try {
			sock = new Socket("localhost", 4444);

		} catch (Exception e) {
			System.err.println("Cannot connect to the server, try again later.");
			System.exit(1);
		}

		os = new PrintStream(sock.getOutputStream());

		menu();

	}

	public static void menu() throws IOException {

		int opt;

		do {

			System.out.println("\nChoose one option: \n");
			System.out.println("1. Send file.");
			System.out.println("2. Receive file.");
			System.out.println("3. Close connection.");
			opt = read.nextInt();

			switch (opt) {
			case 1:
				os.println("1");
				sendFile();
				break;
			case 2:
				dir = new File(serverFolder);
				fileList = dir.listFiles();

				System.out.println("List of files: ");

				for (int i = 0; i < fileList.length; i++) {
					files = fileList[i];
					System.out.println(i + " - " + files.getName());
				}

				System.out.print("Enter the code corresponding to the file you want to receive: ");
				int codFile = read.nextInt();

				fileChoose = fileList[codFile];
				fileName = fileChoose.getName();

				os.println("2");

				os.println(fileName);

				receiveFile(fileName);

				break;
			case 3:
				System.out.println("Connection closed!");
				sock.close();
				break;
			default:
				System.out.println("Incorrect command received.");
				break;
			}

			os.close();
			new FileClient();

		} while (opt != 3);

	}

	public static void sendFile() throws IOException {

		dir = new File(clientFolder);
		fileList = dir.listFiles();

		System.out.println("List of files: ");

		for (int i = 0; i < fileList.length; i++) {
			files = fileList[i];
			System.out.println(i + " - " + files.getName());
		}

		System.out.println("Enter the code corresponding to the file you want to receive: ");
		int codFile = read.nextInt();

		fileChoose = fileList[codFile];

		fileName = fileChoose.getName();

		File myFile = new File(clientFolder + fileSeparator + fileName);
		byte[] mybytearray = new byte[(int) myFile.length()];

		FileInputStream fis = new FileInputStream(myFile);
		BufferedInputStream bis = new BufferedInputStream(fis);

		DataInputStream dis = new DataInputStream(bis);

		dis.read(mybytearray, 0, mybytearray.length);

		OutputStream os = sock.getOutputStream();

		// Sending file name and file size to the server
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(myFile.getName());
		dos.writeLong(mybytearray.length);
		dos.write(mybytearray, 0, mybytearray.length);
		dos.flush();
		System.out.println("\nFile " + fileName + " sent to Server.");

	}

	public static void receiveFile(String fileName) throws IOException {
		try {
			int bytesRead;
			InputStream in = sock.getInputStream();

			DataInputStream clientData = new DataInputStream(in);

			fileName = clientData.readUTF();
			OutputStream output = new FileOutputStream(clientFolder + fileSeparator + "received_from_server_"
					+ new Random().nextInt(101) + "_" + fileName);
			long size = clientData.readLong();
			byte[] buffer = new byte[1024];

			while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				output.write(buffer, 0, bytesRead);
				size -= bytesRead;
			}

			output.close();
			in.close();
			System.out.println("\nFile " + fileName + " received from Server.");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {

		new FileClient();

	}

}