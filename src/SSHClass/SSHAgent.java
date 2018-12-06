package SSHClass;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
  
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
  
public class SSHAgent {
	//ipAddress
	private String ip;
	//login userName
	private String username;
	//login password
	private String password;
	//remote port
	public static final int DEFAULT_SSH_PORT = 22; 
	//output
	private ArrayList<String> stdout;
  
	public SSHAgent(final String ip, final String username, final String password) {
		this.ip = ip;
		this.username = username;
		this.password = password;
		stdout = new ArrayList<String>();
	}
  

	public int execute(final String command) {
		int returnCode = 0;
		JSch jsch = new JSch();
		MyUserInfo userInfo = new MyUserInfo();
  
		try {
			//创建session并且打开连接，因为创建session之后要主动打开连接
			Session session = jsch.getSession(username, ip, DEFAULT_SSH_PORT);
			session.setPassword(password);
			session.setUserInfo(userInfo);
			session.connect();
  
			//打开通道，设置通道类型，和执行的命令
			Channel channel = session.openChannel("exec");
			ChannelExec channelExec = (ChannelExec)channel;
			channelExec.setCommand(command);
  
			channelExec.setInputStream(null);
			BufferedReader input = new BufferedReader(new InputStreamReader
					(channelExec.getInputStream()));
  
			channelExec.connect();
			System.out.println("The remote command is :" + command);
  
			//接收远程服务器执行命令的结果
			String line;
			while ((line = input.readLine()) != null) { 
				stdout.add(line); 
			} 
			input.close(); 
  
			// 得到returnCode
			if (channelExec.isClosed()) { 
				returnCode = channelExec.getExitStatus(); 
			} 
  
			// 关闭通道
			channelExec.disconnect();
			//关闭session
			session.disconnect();
  
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnCode;
	}

	public ArrayList<String> getStandardOutput() {
		return stdout;
	}
  
/*	public static void main(final String [] args) { 
		SSHAgent shell = new SSHAgent("192.168.0.57", "root", "123456");
		shell.execute("uname -s -r -v");
  
		ArrayList<String> stdout = shell.getStandardOutput();
		for (String str : stdout) { 
			System.out.println(str); 
		} 
	} */
}