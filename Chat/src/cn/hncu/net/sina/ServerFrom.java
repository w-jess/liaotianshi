package cn.hncu.net.sina;
 
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
 
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
 
 
public class ServerFrom extends JFrame{
	
	private JTextArea area;//���ߵ��û���Ϣ��ʾ
	private DefaultListModel<String> dataModel;	//���ߵ��û��б���ʾ
	
	//ע����û���������ͬ
	//���ڴ洢���е��û�,�������ע���"�û���"��keyֵ,ͨ�ŵ�socket��valueֵ
	private Map<String, Socket> userMap=new HashMap<String, Socket>();
	
	public ServerFrom() {
		setTitle("���������");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Toolkit toolkit=Toolkit.getDefaultToolkit();
		Dimension dim=toolkit.getScreenSize();
		int runWidth=500;
		int runHeight=400;
		int width=(int) dim.getWidth();
		int height=(int) dim.getHeight();
		//���ý��������ʾ
		setBounds(width/2-runWidth/2, height/2-runHeight/2, runWidth, runHeight);
		
		area=new JTextArea();
		area.setEditable(false);
		getContentPane().add(new JScrollPane(area),BorderLayout.CENTER);
		
		//�б���ʾ
		dataModel=new DefaultListModel<String>();
		JList<String> list=new JList<String>(dataModel);
		JScrollPane scroll=new JScrollPane(list);
		scroll.setBorder(new TitledBorder("����"));
		scroll.setPreferredSize(new Dimension(100, this.getHeight()));
		getContentPane().add(scroll,BorderLayout.EAST);
		
		//�˵�
		JMenuBar menuBar=new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu menu=new JMenu("����(C)");
		menu.setMnemonic('C');		//���ÿ�ݼ�Ϊ Alt+C
		menuBar.add(menu);
		//����
		final JMenuItem itemRun=new JMenuItem("����");
		//��ݼ� Ctrl+R
		itemRun.setAccelerator(KeyStroke.getKeyStroke('R', KeyEvent.CTRL_MASK));	
				itemRun.setActionCommand("run");
				menu.add(itemRun);
		//�˳�
		JMenuItem itemExit=new JMenuItem("�˳�");
		itemExit.setAccelerator(KeyStroke.getKeyStroke('E', KeyEvent.CTRL_MASK));
		itemExit.setActionCommand("exit");
		menu.add(itemExit);
		
		itemRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if("run".equals(e.getActionCommand())){
					startServer();
					itemRun.setEnabled(false);
				}
			}
		});
		
		
		setVisible(true);
	}
	
	private void startServer() {
		try {
			System.out.println("����������");
			ServerSocket server=new ServerSocket(8080);
			area.append("����������:"+server);
			
			//��������һ���߳�������ͻ�������
			new ServerThread(server).start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	class ServerThread extends Thread{
		
		private ServerSocket server;
		public ServerThread(ServerSocket server) {
			this.server=server;
		}
 
		@Override
		public void run() {
			try {
				while(true){
					Socket s=server.accept();
					//��ȡ�ͻ��˵�һ����������������Ϣ
//					BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
//					if(br.readLine()!=null){
//						String userName=br.readLine();
//					}
					Scanner sc=new Scanner(s.getInputStream());
					if(sc.hasNext()){
						String userName=sc.next();
						area.append("\r\n"+userName+"�����ˡ�"+s);
						dataModel.addElement(userName);
//						userMap.put(userName, s);	//�ں����ڰ�����û����뵽�����к�һ��,��������������Ϣ�������û�ʱ,�Ͳ����жϲ��������Լ��ˡ�
						
						//��¼�ɹ�
						//��ר�ſ�һ���߳����ڸ����ĳһ���ͻ���ͨѶ
						//���ݽ��տͻ��˷�����Э���ж�,�ͻ��˽��е���ʲô��������
						new ClientThread(s).start();
						
						//���������û�����������
						sendMsgToAll(userName);
						//����Ϣ�������ߵ��û�����Ϣ������¼������ͻ���
						sendMsgToSelf(s);
						
						userMap.put(userName, s);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	public void sendMsgToAll(String userName) throws IOException{		//������쳣������,��Ϊ�������������λ��ץ��IOException
		//����map�����г��˸��û�֮��Ŀͻ�--��ʱ��¼���û���û�м��뵽������,���п���ֱ�ӱ��������û�
		Iterator<Socket> it = userMap.values().iterator();
		while(it.hasNext()){
			Socket s=it.next();
			PrintWriter pw=new PrintWriter(s.getOutputStream(),true);
			//��������ͻ��˷�����Ϣ��ʽ��ƣ�
			//����ؼ���@#���ͷ�@#��Ϣ����
			String msg="msg@#server@#"+userName+"��¼��";	//������ʾ�õ�.
			pw.println(msg);
			msg="cmdAdd@#server@#"+userName;	//���ڸ��ͻ���ά�������û��б��õ�
			pw.println(msg);
			
//			pw.close();
//			s.close();
		}
	}
	
	public void sendMsgToSelf(Socket s) throws IOException{
		
		PrintWriter pw = new PrintWriter(s.getOutputStream(),true);
		Iterator<String> it = userMap.keySet().iterator();
		while(it.hasNext()){
			String userName=it.next();
			System.out.println("map:"+userMap);
			//�����û���ǰ�����û���Ϣ,����Ҫ������ʾ��Ϣ��ֻ��Ҫ���͸��ͻ��˸��������б����Ϣ
			String msg="cmdAdd@#server@#"+userName;
			pw.println(msg);
		}
		
//		pw.close();
	}
 
	//ר�����ڸ�ĳһ���û�ͨѶ���߳�
	class ClientThread extends Thread{
		private Socket s;
		public ClientThread(Socket s) {
			this.s=s;
		}
		@Override
		public void run() {
			try {
				//���ݽ��տͻ��˷�����Э���ж�,�ͻ��˽��е���ʲô��������
				Scanner sc=new Scanner(s.getInputStream());
				while(sc.hasNextLine()){
					String msg=sc.nextLine();
					String msgs[]=msg.split("@#");
					//�򵥷��ڡ�
					if(msgs==null || msgs.length!=4){
						System.out.println("ͨѶ�쳣:"+msg);
						return;
					}
					
					if("on".equals(msgs[0])){//��ʾ�ͻ��˵������ǣ�����˷�����Ϣ
						sendMsgToSb(msgs);
						
					}else if("exit".equals(msgs[0])){//��ʾ�ͻ��˷��͵�������:�˳�(����)
						area.append("\r\n"+msgs[3]+"������"+s);
						dataModel.removeElement(msgs[3]);
						userMap.remove(msgs[3]);
						
						//֪ͨ�����������ߵ��û�,***�˳���
						sendSbExitMsgToAll(msgs);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//����ؼ���@#���շ�@#��Ϣ����@#���ͷ�
	public void sendMsgToSb(String[] msgs) throws IOException {
		//�����Ƿ���������,Ҳ�����Ƿ���ĳһ����
		if("ȫ��".equals(msgs[1])){
			//����������(Ⱥ��)
			Iterator<String> it = userMap.keySet().iterator();
			while(it.hasNext()){
				String userName=it.next();
				String msg=null;
				if(userName.equals(msgs[3])){
					msg="msg@#"+"��"+"@#˵:"+msgs[2];
				}else{
					msg="msg@#"+msgs[3]+"@#˵:"+msgs[2];
				}
				Socket s=userMap.get(userName);
				//msg@#��Ϣ������@#��Ϣ����
				PrintWriter pw=new PrintWriter(s.getOutputStream(), true);
				pw.println(msg);
			}
		}else{
			//���͸�ĳһ����
			String userName=msgs[1];
			Socket s=userMap.get(userName);
			//msg@#��Ϣ������@#��Ϣ����
			String msg="msg@#"+msgs[3]+"@#���Ķ���˵:"+msgs[2];
			PrintWriter pw=new PrintWriter(s.getOutputStream(), true);
			pw.println(msg);
			
			//�ڷ����Լ�
			Socket s2 = userMap.get(msgs[3]);
			PrintWriter pw2 = new PrintWriter(s2.getOutputStream(), true);
			String str2 = "msg@#"+"��"+"@#�� "+userName+"˵:"+msgs[2];
			pw2.println(str2);
		}
	}
 
	//֪ͨ�����������ߵ��û�,***�˳���
	//1) msg @# server @# �û�[userName]�˳���  (���ͻ�����ʾ�õ�)
	//2) cmdRed@#server @# userName (���ͻ���ά�������û��б��õ�)
	public void sendSbExitMsgToAll(String[] msgs) throws IOException {
		Iterator<String> it=userMap.keySet().iterator();
		while(it.hasNext()){
			String userName=it.next();
			Socket s=userMap.get(userName);
			PrintWriter pw=new PrintWriter(s.getOutputStream(), true);
			String msg="msg@#server@#�û�["+msgs[3]+"]�˳���";
			pw.println(msg);
			msg="cmdRed@#server@#"+msgs[3];
			pw.println(msg);
		}
	}
 
	public static void main(String[] args) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		new ServerFrom();
	}
 
}