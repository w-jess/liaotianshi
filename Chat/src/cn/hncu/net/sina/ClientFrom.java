package cn.hncu.net.sina;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
 
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
 
public class ClientFrom extends JFrame implements ActionListener{
	private static String ip="127.0.0.1";
	private static int port=8080;
	
	private JTextField tfdUserName=new JTextField(10);	//�û���ʶ
	private JTextArea allMsg=new JTextArea();	//������Ϣ��ʾ
	private JTextField tfdMsg=new JTextField(10);//������Ϣ��Ϣ��
	private JButton btnSend;	//������Ϣ��ť
	private JButton btnCon;
	
	//�����û��б�
	private DefaultListModel<String> dataModel=new DefaultListModel<String>();
	private JList<String> list=new JList<String>(dataModel);
	
	public ClientFrom() {
		setBounds(300,300,400,300);
		
		addMenuBar();	//��Ӳ˵�
		////////////////////�Ϸ����/////////////
		JPanel northPanel=new JPanel();
		northPanel.add(new JLabel("�û�����"));
		tfdUserName.setText("");
		northPanel.add(tfdUserName);
		
		btnCon=new JButton("����");
		btnCon.setActionCommand("c");
		JButton btnExit=new JButton("�˳�");
		btnExit.setActionCommand("exit");
		northPanel.add(btnCon);
		northPanel.add(btnExit);
		
		getContentPane().add(northPanel,BorderLayout.NORTH);	//�����Ϸ�
		//////////////////�м����////////////////
		JPanel centerPanel=new JPanel(new BorderLayout());
		//��
		allMsg=new JTextArea();
		allMsg.setEditable(false);
		allMsg.setForeground(Color.black);
		allMsg.setFont(new Font("��Բ", Font.BOLD, 14));
		centerPanel.add(new JScrollPane(allMsg));
		//��
		dataModel.addElement("ȫ��");
		list.setSelectedIndex(0);	//����Ĭ��ѡ��λ��
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);	//����ֻ�ܵ�ѡ
		list.setVisibleRowCount(5);		//������ʾ������
		list.setFont(new Font("��Բ", Font.BOLD, 12));
		
		JScrollPane scroll=new JScrollPane(list);		//Ϊlist��ӹ�����
		scroll.setBorder(new TitledBorder("����"));	//Border��ʵ����TitileBorder
		scroll.setPreferredSize(new Dimension(70, allMsg.getHeight()));	//���ù���������ѡ��С
		centerPanel.add(scroll,BorderLayout.EAST);
		//��
		JPanel southPanel=new JPanel();
		southPanel.add(new JLabel("��Ϣ"));
		southPanel.add(tfdMsg);
		
		btnSend=new JButton("����");
		btnSend.setActionCommand("send");
		btnSend.setEnabled(false);
		southPanel.add(btnSend);
		
		centerPanel.add(southPanel,BorderLayout.SOUTH);
		
		//���м����ӵ������
		getContentPane().add(centerPanel);
		
		//�¼�����
		btnCon.addActionListener(this);
		btnExit.addActionListener(this);
		btnSend.addActionListener(this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(tfdUserName.getText()==null || tfdUserName.getText().trim().length()==0){
					int result = JOptionPane.showConfirmDialog(ClientFrom.this, "�㻹û��¼,�Ƿ��˳�");
					if(result==JOptionPane.YES_OPTION){
						System.exit(0);
					}else{
						return;
					}
				}
				System.out.println(tfdUserName.getText()+"�˳�");
				sendExitMsg();
				System.exit(0);
			}
		});
		
		setVisible(true);
	}
	
	private void addMenuBar() {
		JMenuBar menuBar=new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu menu=new JMenu("ѡ��");
		menuBar.add(menu);
		
		JMenuItem itemSet=new JMenuItem("����");
		JMenuItem itemHelp=new JMenuItem("����");
		
		itemSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JDialog setDlg=new JDialog(ClientFrom.this);
				setDlg.setBounds(ClientFrom.this.getX(), ClientFrom.this.getY(), 250, 100);
				setDlg.setLayout(new FlowLayout());
				setDlg.add(new JLabel("������:"));
				final JTextField tfdIP=new JTextField(10);
				tfdIP.setText(ip);
				setDlg.add(tfdIP);
				setDlg.add(new JLabel("�˿�:"));
				final JTextField tfdPort=new JTextField(10);
				tfdPort.setText(port+"");
				setDlg.add(tfdPort);
				
				JButton btnSet=new JButton("����");
				btnSet.setActionCommand("set");
				JButton btnCanel=new JButton("ȡ��");
				btnCanel.setActionCommand("canel");
				setDlg.add(btnSet);
				setDlg.add(btnCanel);
				
				btnSet.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if("set".equals(e.getActionCommand())){
							if(tfdIP.getText()!=null && tfdIP.getText().trim().length()>0){
								ClientFrom.this.ip=tfdIP.getText();
							}
							if(tfdPort.getText()!=null && tfdPort.getText().trim().length()>0){
								try {
									ClientFrom.this.port=Integer.parseInt(tfdPort.getText());
								} catch (NumberFormatException e1) {
									JOptionPane.showMessageDialog(setDlg, "�˿ںŸ�ʽ�������,����������");
								}
							}
							btnCon.setEnabled(true);
							tfdUserName.setEditable(true);
							if(client!=null){
								//���ǰ���Ѿ���¼���û�,�Ͱ��û��˳�
								String msg="exit@#ȫ��@#null@#"+tfdUserName.getText();
								pw.println(msg);
								dataModel.removeElement(tfdUserName.getText());
								list.validate();
								tfdUserName.setText("");
							}
							
							setDlg.dispose();
						}else if("canel".equals(e.getActionCommand())){
							return;
						}
					}
				});
				setDlg.setVisible(true);
			}
		});
		
		itemHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog helpDlg = new JDialog(ClientFrom.this);
				helpDlg.setBounds(ClientFrom.this.getX()+10, ClientFrom.this.getY(), 300, 100);
				JLabel str = new JLabel("����ϵ QQ��123456");
				helpDlg.add(str);
				helpDlg.setVisible(true);
			}
		});
		
		menu.add(itemSet);
		menu.add(itemHelp);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if("c".equals(e.getActionCommand())){
			System.out.println(tfdUserName.getText());
			
			if(tfdUserName.getText()==null || tfdUserName.getText().trim().length()==0){
				JOptionPane.showMessageDialog(this, "�û�������Ϊ��");
				return;
			}
			System.out.println(tfdUserName.getText()+":����ing...");
			connecting();
		}else if("exit".equals(e.getActionCommand())){
			if(tfdUserName.getText()==null || tfdUserName.getText().trim().length()==0){
				int result = JOptionPane.showConfirmDialog(this, "�㻹û��¼,�Ƿ��˳�");
				if(result==JOptionPane.YES_OPTION){
					System.exit(0);
				}else{
					return;
				}
			}
			System.out.println(tfdUserName.getText()+"�˳�");
			sendExitMsg();
		}else if("send".equals(e.getActionCommand())){
			if(tfdMsg.getText()==null){
				JOptionPane.showMessageDialog(this, "������Ϣ����Ϊ��");
				return;
			}
			
			String msg="on@#"+list.getSelectedValue()+"@#"+tfdMsg.getText()+"@#"+tfdUserName.getText();
			pw.println(msg);
		}
	}
	private Socket client;
	private PrintWriter pw;
	private void connecting() {
		//���������������,��userName����������
		try {
			client=new Socket(ip,port);
			//�����û�����������
			btnCon.setEnabled(false);	//���ӳɹ���ص����Ӱ�ť
			String userName=tfdUserName.getText().trim();
			pw=new PrintWriter(client.getOutputStream(),true);
			pw.println(userName);
			//����֮��,���ñ���ΪuserName����
			setTitle(userName+"����");
			
			btnSend.setEnabled(true);		//�򿪷��Ͱ�ť
			tfdUserName.setEditable(false);		//�û����������޸�
			
			//��һ���̵߳������ڸ�������ͨ��
			new ClientThread(client).start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void sendExitMsg() {
		//���������������,��userName����������
		try {
			client=new Socket(ip, port);
			String msg="exit@#ȫ��@#null@#"+tfdUserName.getText();
			pw.println(msg);
			
			System.exit(0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	class ClientThread extends Thread{
		private Socket client;
		public ClientThread(Socket client) {
			this.client=client;
		}
 
		@Override
		public void run() {
			//���շ��������ص���Ϣ
			try {
				Scanner sc=new Scanner(client.getInputStream());
				
				while(sc.hasNext()){
					String msg=sc.nextLine();
					String msgs[]=msg.split("@#");
					if(msgs==null || msgs.length!=3){
						System.out.println("ͨѶ�쳣");
						return;
					}
					
					if("msg".equals(msgs[0])){
						//��ʾ����Ϣ��������ʾ�õ�
						if("server".equals(msgs[1])){
							//��ʾ����Ϣ��ϵͳ��Ϣ
							msg="ϵͳ��Ϣ:"+msgs[2];
							allMsg.append(msg+"\r\n");
						}else{
							//��ʾ����Ϣ������Ϣ
							msg=msgs[1]+msgs[2];
							allMsg.append(msg+"\r\n");
						}
					}else if("cmdAdd".equals(msgs[0])){
						//��ʾ����Ϣ�����������û������б��,����û�
						dataModel.addElement(msgs[2]);
					}else if("cmdRed".equals(msgs[0])){
						//��ʾ����Ϣ�����������û������б��,�Ƴ��û�
						dataModel.removeElement(msgs[2]);
					}
					list.validate();	//��Ҫˢ��list����Ȼ���ܳ���list����ʧ�ܵ�bug
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		new ClientFrom();
	}
}