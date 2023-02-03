package com.nghiachat.nghiachat;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtAddress;
	private JLabel lblIPAddress;
	private JLabel lblAddressDesc;
	private JLabel lblPort;
	private JTextField txtPort;
	private JLabel lblPortDesc;

	public Login() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtName = new JTextField();
		txtName.setBounds(50, 48, 183, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblName.setBounds(119, 29, 46, 15);
		contentPane.add(lblName);

		txtAddress = new JTextField();
		txtAddress.setBounds(50, 127, 183, 20);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);

		lblIPAddress = new JLabel("IP Address:");
		lblIPAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblIPAddress.setBounds(107, 107, 69, 15);
		contentPane.add(lblIPAddress);

		lblAddressDesc = new JLabel("(eg: 192.128.1.120)");
		lblAddressDesc.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAddressDesc.setBounds(81, 152, 139, 15);
		contentPane.add(lblAddressDesc);

		lblPort = new JLabel("Port:");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPort.setBounds(119, 195, 36, 20);
		contentPane.add(lblPort);

		txtPort = new JTextField();
		txtPort.setColumns(10);
		txtPort.setBounds(50, 217, 183, 20);
		contentPane.add(txtPort);

		lblPortDesc = new JLabel("(eg: 8120)");
		lblPortDesc.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPortDesc.setBounds(107, 242, 77, 15);
		contentPane.add(lblPortDesc);

		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText();
				String address = txtAddress.getText();
				int port = Integer.parseInt(txtPort.getText());
				login(name, address, port);
			}

		});
		btnLogin.setBounds(91, 307, 89, 23);
		contentPane.add(btnLogin);

	}

	private void login(String name, String address, int port) {
		// TODO Auto-generated method stub
		dispose();
		new Client(name, address, port);

	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
