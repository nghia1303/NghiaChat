package com.nghiachat.nghiachat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class ClientWindow extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextArea history;
	private DefaultCaret caret;

	private Client client;
	private Thread run, listen;
	private boolean running = false;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	private OnlineUsers users;
	
	public ClientWindow(String name, String address, int port) {
		setTitle("Nghia Chat Client");
		client = new Client(name, address, port);
		boolean connect = client.openConnection(address);

		if (!connect) {
			System.out.println("Connection failed!");
			console("Connection failed!");
		}

		createWindow();
		console("Attempting a connection to " + address + ":" + port + ", user: " + name);
		String connection = "/c/" + name + "/e/";
		client.send(connection.getBytes());
		users = new OnlineUsers();
		running = true;
		run = new Thread(this, "Running Thread");
		run.start();

	}

	private void send(String message, boolean text) {
		if (message.equals(""))
			return;
		if (text == true) {
			message = client.getName() + ": " + message;
			message = "/m/" + message + "/e/";
			//System.out.println(message);
			txtMessage.setText("");
		}
		client.send(message.getBytes());

	}

	public void run() {
		listen();
	}

	public void listen() {
		listen = new Thread("Listen") {
			public void run() {
				while (running) {
					String message = client.receive();
					// System.out.println("\n" + message.length());
					if (message.startsWith("/c/")) {
						client.setID(Integer.parseInt(message.split("/c/|/e/")[1]));
						console("Successfully Connected to Server! ID: " + client.getID());
					} else if (message.startsWith("/m/")) {
						String text = message.substring(3);
						text = text.split("/e/")[0];
						console(text);
					}
					else if (message.startsWith("/i/")) {
						String text = "/i/" + client.getID() + "/e/";
						send(text,false);
					}
					else if (message.startsWith("/u/")) {
						String[] u = message.split("/u/|/n/|/e/");
						
						users.update(Arrays.copyOfRange(u,1,u.length - 1));
					}
				}
			}
		};
		listen.start();
	}

	public void console(String message) {
		history.append(message + "\n\r");
	}

	private void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 580);
		setLocationRelativeTo(null);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmOnlineUsers = new JMenuItem("Online Users");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				users.setVisible(true);
			}
		});
		mnFile.add(mntmOnlineUsers);
		
		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 34, 818, 30, -2 };
		gbl_contentPane.rowHeights = new int[] { 50, 480, 50 };
		contentPane.setLayout(gbl_contentPane);

		history = new JTextArea();
		JScrollPane scroll = new JScrollPane(history);
		caret = (DefaultCaret) history.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 0, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 3;
		scrollConstraints.gridheight = 2;
		scrollConstraints.weighty = 1;
		scrollConstraints.weightx = 1;
		scrollConstraints.insets = new Insets(0, 20, 0, 20);
		history.setEditable(false);
		contentPane.add(scroll, scrollConstraints);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(txtMessage.getText(), true);
			}
		});

		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send(txtMessage.getText(), true);
				}
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		
		gbc_txtMessage.weightx = 1;
		gbc_txtMessage.weighty = 0;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		gbc_btnSend.weightx = 0;
		gbc_btnSend.weighty = 0;
		contentPane.add(btnSend, gbc_btnSend);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String disconnect = "/d/" + client.getID() + "/e/";
				send(disconnect, false);
				running = false;
				client.close();
			}
		});

		setVisible(true);
		txtMessage.requestFocusInWindow();
	}
}
