package app_ubuntu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.Configuration;
import hlmp.CommLayer.NetUser;
import hlmp.CommLayer.SubProtocolList;
import hlmp.CommLayer.Messages.Message;
import hlmp.CommLayer.Observers.AddUserEventObserverI;
import hlmp.CommLayer.Observers.ExceptionEventObserverI;
import hlmp.CommLayer.Observers.ErrorMessageEventObserverI;
import hlmp.CommLayer.Observers.NetInformationEventObserverI;
import hlmp.CommLayer.Observers.ProcessMessageEventObserverI;
import hlmp.CommLayer.Observers.RefreshLocalUserEventObserverI;
import hlmp.CommLayer.Observers.RefreshUserEventObserverI;
import hlmp.CommLayer.Observers.RemoveUserEventObserverI;
import hlmp.NetLayer.Constants.IpState;
import hlmp.NetLayer.Constants.WifiConnectionState;
import hlmp.NetLayer.Interfaces.WifiHandler;
import hlmp.SubProtocol.Chat.ChatProtocol;
import hlmp.SubProtocol.Chat.ControlI.ChatHandlerI;
import hlmp.SubProtocol.Chat.Messages.GroupChatMessage;
import hlmp.SubProtocol.FileTransfer.FileData;
import hlmp.SubProtocol.FileTransfer.FileInformationList;
import hlmp.SubProtocol.FileTransfer.FileTransferProtocol;
import hlmp.SubProtocol.FileTransfer.ControlI.FileHandlerI;
import hlmp.SubProtocol.FileTransfer.ControlI.FileListHandlerI;
import hlmp.SubProtocol.Ping.PingProtocol;
import hlmp.SubProtocol.Ping.ControlI.PingHandlerI;


public class Chat extends JFrame implements ActionListener, PingHandlerI, ChatHandlerI, 
	NetInformationEventObserverI, ProcessMessageEventObserverI,
	AddUserEventObserverI, RemoveUserEventObserverI, RefreshLocalUserEventObserverI, RefreshUserEventObserverI,
	ExceptionEventObserverI, ErrorMessageEventObserverI, WifiHandler, FileHandlerI, FileListHandlerI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException {
		new Chat().setVisible(true);
	}

	private JTextArea chat = new JTextArea("", 15, 50);
	private JTextArea usuarios = new JTextArea("USUARIOS", 15, 10);
	private JTextField mensaje = new JTextField("", 50);
	
	private JButton enviar = new JButton("Enviar");
	private JTextArea info = new JTextArea(10,60);
	
	private JButton conectar = new JButton("Conectar");
	private JButton desconectar = new JButton("Desconectar");
	private JButton limpiar = new JButton("Limpiar");
	private JButton salir = new JButton("Salir");
	
	private Configuration configuration;
	private PingProtocol pingProtocol;
	private ChatProtocol chatProtocol;
	private FileTransferProtocol fileTransferProtocol;
	private Communication communication;
	
	
	
	public Chat() throws UnknownHostException{
		
		setTitle("HLMP Chat");
		setLayout(new GridLayout(2,1));

		this.chat.setEditable(false);
		this.usuarios.setEditable(false);
		this.info.setEditable(false);
		
		
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		top.add(new JLabel("Chat HLMP "), BorderLayout.NORTH);
		
		JPanel topMid = new JPanel();
		topMid.setLayout(new FlowLayout());
		topMid.add(new JScrollPane(this.chat));
		topMid.add(this.usuarios);
		top.add(topMid, "Center");
		
		JPanel topBot = new JPanel();
		topBot.setLayout(new FlowLayout());
		topBot.add(new JLabel("Mensaje: "));
		topBot.add(this.mensaje);
		topBot.add(this.enviar);
		top.add(topBot, "South");
		
		
		JPanel bot = new JPanel();
		bot.setLayout(new BorderLayout());
		bot.add(new JScrollPane(this.info), BorderLayout.CENTER);
		
		JPanel botBot = new JPanel();
		botBot.setLayout(new FlowLayout());
		botBot.add(this.conectar);
		botBot.add(this.desconectar);
		botBot.add(this.limpiar);
		botBot.add(this.salir);
		bot.add(botBot, "South");
		
		add(top);
		add(bot);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		
		this.salir.addActionListener(this);
		this.conectar.addActionListener(this);
		this.desconectar.addActionListener(this);
		this.limpiar.addActionListener(this);
		
		this.enviar.addActionListener(this);
		this.mensaje.addActionListener(this);
		
		
		this.configuration= new Configuration();
		this.configuration.getNetData().setOpSystem(hlmp.NetLayer.Constants.OpSystemType.UBUNTU1104);

		SubProtocolList subProtocols = new SubProtocolList();
		this.pingProtocol = new PingProtocol(this);
		this.chatProtocol = new ChatProtocol(this);
		this.fileTransferProtocol = new FileTransferProtocol(this, this, this.createFileData());
		
		subProtocols.add(hlmp.SubProtocol.Ping.Types.PINGPROTOCOL, pingProtocol);
		subProtocols.add(hlmp.SubProtocol.Chat.Types.CHATPROTOCOL, chatProtocol);
		this.communication = new Communication(configuration, subProtocols, null, this);

		this.communication.subscribeNetInformationEvent(this);
		this.communication.subscribeExceptionEvent(this);
		this.communication.subscribeAddUserEvent(this);
		this.communication.subscribeRemoveUserEvent(this);
		this.communication.subscribeRefreshUserEvent(this);
		this.communication.subscribeRefreshLocalUserEvent(this);

		
	}


	private FileData createFileData() {
		return null;
	}
	


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(this.salir))
		{
			this.dispose();
			System.exit(0);
		}
		else if(e.getSource().equals(this.conectar))
		{
			this.configuration.getNetData().setOpSystem(hlmp.NetLayer.Constants.OpSystemType.UBUNTU1104);
			this.communication.startEventConsumer();
			this.communication.connect();
		}
		else if(e.getSource().equals(this.desconectar))
		{
            this.communication.disconnect();
            this.communication.stopEventConsumer();
		}
		else if(e.getSource().equals(this.limpiar))
		{
			this.info.setText("");
		}
		else if(e.getSource().equals(this.mensaje) || e.getSource().equals(this.enviar))
		{
			String mensaje = this.mensaje.getText();
			this.chatProtocol.sendMessageEvent(new GroupChatMessage(mensaje));
			String s = "[ "+this.communication.getConfiguration().getNetUser().getName() +" ]: "+mensaje;
			this.chat.append(s + "\n");
			this.mensaje.setText("");
		}
		
	}


	@Override
	public void processMessageUpdate(Message m) {
		
	}


	@Override
	public void netInformationEventUpdate(String s) {
		this.info.append(s + "\n");
		p(s);
	}


	@Override
	public void chatMessageReceived(NetUser netUser, String message) {
		String s = "[ "+netUser.getName() +" ]: "+message;
		this.chat.append(s + "\n");
		
	}


	@Override
	public void groupChatMessageReceived(NetUser netUser, String message) {
		String s = "G[ "+netUser.getName() +" ]: "+message;
		this.chat.append(s + "\n");
	}


	@Override
	public void chatWarninglInformation(String text) {
		this.info.append("CHAT: " + text + "\n");
	}


	@Override
	public void pingResponseMessageReceived(NetUser netUser, long milliseconds) {
		
	}

	@Override
	public void exceptionEventUpdate(Exception e) {
		p("EXCEPTION: " + e.toString() + " " + e);
	}

	@Override
	public void errorMessageEventUpdate(Message m) {
		p("ERROR: " + m.toString());	
	}
	
	private void updateUserList(){
//		String m= "Actualizando lista de usuarios\n";
//		this.info.append(m);
		NetUser[] users = this.communication.getNetUserList().userListToArray();
		
		String s=this.communication.getConfiguration().getNetUser().toString()+"\n";
		for(NetUser u:users){
			s+=u.toString()+"\n";
		}
		this.usuarios.setText(s);
	}


	@Override
	public void removeUserEventUpdate(NetUser netUser) {
		this.updateUserList();
		p("REMOVE USER: "+netUser.toString());
	}


	@Override
	public void addUserEventUpdate(NetUser netUser) {
		this.updateUserList();
		p("ADD USER: "+netUser.toString());
	}


	@Override
	public void refreshUserEventUpdate(NetUser netUser) {
		this.updateUserList();
		p("REFRESH USER: "+netUser.toString());
	}


	@Override
	public void refreshLocalUserEventUpdate(NetUser netUser) {
		this.updateUserList();
	}
	
	public void p(Object o){
		System.out.println(o);
	}


	
	
	
	// HLMP WifiHandler
	
	@Override
	public void connect() {		
	}
	


	@Override
	public void disconnect() {
	}
	


	@Override
	public int getConnectionState() {
		return WifiConnectionState.CONNECTED;
	}
	


	@Override
	public int getIpState() {
		return IpState.VALID;
	}
	


	@Override
	public InetAddress getInetAddress() {	
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						inetAddress = enumIpAddr.nextElement();
						p(inetAddress.getCanonicalHostName());
						p(inetAddress.getHostAddress());
						p(inetAddress.getHostName());
						return inetAddress;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();	
		}
		return null;
	}


	
	
	
	// HLMP FileTransfer
	
	@Override
	public void addFileList(NetUser netUser, FileInformationList fileList) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeFileList(NetUser netUser) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void downloadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void downloadFileOpened(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void downloadFileTransfer(String fileHandlerId, int percent) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void downloadFileComplete(String fileHandlerId, String path) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void downloadFileFailed(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void uploadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void uploadFileOpened(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void uploadFileTransfer(String fileHandlerId, int percent) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void uploadFileComplete(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void uploadFileFailed(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}
}
