package com.nghiachat.nghiachat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {

	private List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();

	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	private final int MAX_ATTEMPS = 5;
	private boolean raw = false;

	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		run = new Thread(this, "Server");
		run.start();
	}

	public void run() {
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
		try (Scanner scanner = new Scanner(System.in)) {
			while (running) {
				String text = scanner.nextLine();
				if (!text.startsWith("/")) {
					sendToAll("/m/Server: " + text + "/e/");
					continue;
				}
				text = text.substring(1);
				if (text.equals("raw")) {
					if (raw) System.out.println("Raw mode off.");
					else System.out.println("Raw mode on.");
					raw = !raw;
				} else if (text.equals("clients")) {
					System.out.println("Clients: ");
					System.out.println("===========");
					for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						System.out.println(c.name.trim() + "(" + c.getID() + ")" + c.address.toString() + ":" + c.port);
					}
					System.out.println("===========");
				} else if (text.startsWith("kick")) {
					String name = text.split(" ")[1];
					int id = -1;
					boolean number = true;
					try {
						id = Integer.parseInt(name);
					} catch (NumberFormatException e) {
						number = false;
					}
					if (number) {
						boolean exist = false;
						for (int i = 0; i < clients.size(); i++) {
							if (clients.get(i).getID() == id) {
								exist = true;
								break;
							}
						}
						if (exist) {
							disconnect(id, true);
						} else
							System.out.println("Client with " + id + " doesn't exist! Check ID number.");
					} else {
						for (int i = 0; i < clients.size(); i++) {
							ServerClient c = clients.get(i);
							if (name.equals(c.name)) {
								disconnect(c.getID(), true);
								break;
							}
						}
					}
				} else if (text.equals("help")) {
					printHelp();
				}
				else if (text.startsWith("quit")) {
					quit();
				}
				else {
					System.out.println("Unknown command.");
					printHelp();
				}
			}
		}
	}

	private void printHelp() {
		System.out.println("Here is a list of all available commands: ");
		System.out.println("===========================================");
		System.out.println("/raw - enable raw mode");
		System.out.println("/clients - shows all connected users");
		System.out.println("/kick [users ID or username] - kicks a user");
		System.out.println("/help - shows this help message");
		System.out.println("/quit - shutdown the server");
	}
	
	private void sendStatus() {
		if (clients.size() <= 0) {
			return;
		}
		String users = "/u/";
		for (int i = 0; i < clients.size() - 1; i++) {
			users += clients.get(i).name + "/n/";
		}
		users += clients.get(clients.size() - 1).name + "/e/";
		sendToAll(users);
	}

	private void manageClients() {
		// Manage clients
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
					// Managing
					sendToAll("/i/server");
					sendStatus();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						if (!clientResponse.contains(clients.get(i).getID())) {
							if (c.attempt >= MAX_ATTEMPS) {
								disconnect(c.getID(), false);
							} else {
								c.attempt++;
							}
						} else {
							Integer clientID = Integer.valueOf(c.getID());
							clientResponse.remove(clientID);
							c.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}

	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (running) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (SocketException e) {
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receive.start();

	}

	private void sendToAll(String message) {
		if (message.startsWith("/m/")) {
			String text = message.substring(3);
			text = text.split("/e/")[0];
			System.out.println(message);
		}

		for (int i = 0; i < clients.size(); i++) {
			ServerClient client = clients.get(i);
			send(message.getBytes(), client.address, client.port);
		}
	}

	private void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	private void send(String message, InetAddress address, int port) {
		message += "/e/";
		send(message.getBytes(), address, port);
	}

	private void process(DatagramPacket packet) {
		String string = new String(packet.getData());
		if (raw)
			System.out.println(string);
		if (string.startsWith("/c/")) {
			// UUID id = UUID.randomUUID();
			int id = UniqueIdentifier.getIdentifier();
			String name = string.split("/c/|/e/")[1];
			System.out.println(name + "(" + id + ") connected!");
			clients.add(new ServerClient(name, packet.getAddress(), packet.getPort(), id));
			String ID = "/c/" + id;
			send(ID, packet.getAddress(), packet.getPort());
		} else if (string.startsWith("/m/")) {
			sendToAll(string);
		} else if (string.startsWith("/d/")) {
			String id = string.split("/d/|/e/")[1];
			disconnect(Integer.parseInt(id), true);
		} else if (string.startsWith("/i/")) {
			clientResponse.add(Integer.parseInt(string.split("/i/|/e/")[1]));
		} else {
			System.out.println(string);
		}
	}

	private void quit() {
		for (int i = 0; i < clients.size(); i++) {
			disconnect(clients.get(i).getID(), true);
		}
		running = false;
		socket.close();
	}

	private void disconnect(int id, boolean status) {
		ServerClient c = null;
		boolean exist = false;
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getID() == id) {
				c = clients.get(i);
				clients.remove(i);
				exist = true;
				break;
			}
		}
		if (!exist)
			return;
		String message = "";
		if (status) {
			message = "Client " + c.name.trim() + " (" + c.getID() + ") @ " + c.address.toString() + ":" + c.port
					+ " disconnected.";
		} else {
			message = "Client " + c.name.trim() + " (" + c.getID() + ") @ " + c.address.toString() + ":" + c.port
					+ " timed out";
		}
		System.out.println(message);
	}
}
