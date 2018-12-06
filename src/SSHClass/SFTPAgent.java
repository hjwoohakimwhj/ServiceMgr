package SSHClass;
import java.util.Map;
import java.util.Properties;

//import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.HashMap;

public class SFTPAgent {
    private Session session = null;
    private Channel channel = null;
    private final Map<String, String> sftpDetails;
    
    public SFTPAgent(String ftpHost,String ftpUserName,String ftpPassword,String port) {
    	sftpDetails = new HashMap<String,String>(){
    		{
    			put("ftpHost", ftpHost);
    			put("ftpUserName", ftpUserName);
    			put("ftpPassword", ftpPassword);
    			put("port", port);
    		}
    	};
    }
    
    //default port 22
    public SFTPAgent(String ftpHost,String ftpUserName,String ftpPassword) {
    	this(ftpHost,ftpUserName,ftpPassword,"22");
    }
    
    public SFTPAgent() {
    	this("192.168.0.57","root","123456","22");
    }
    
    public ChannelSftp getChannel(int timeout) throws JSchException {

        String ftpHost = sftpDetails.get("ftpHost");
        String ftpPort = sftpDetails.get("port");
        String ftpUserName = sftpDetails.get("ftpUserName");
        String ftpPassword = sftpDetails.get("ftpPassword");

        JSch jsch = new JSch(); // 创建JSch对象
        session = jsch.getSession(ftpUserName, ftpHost,Integer.valueOf(ftpPort));

        session.setPassword(ftpPassword);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(timeout);
        session.connect();

        channel = session.openChannel("sftp");
        channel.connect();
        
        return (ChannelSftp) channel;
    }

    public void closeChannel() throws Exception {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }
    
    //can be called repeated
    public void transfer(String src,String dst,int timeout)throws Exception {
    	ChannelSftp chSftp = getChannel(timeout);   	
        chSftp.put(src, dst, ChannelSftp.OVERWRITE);        
        chSftp.quit();
        closeChannel();
    }
    

    
/*    public static void main(String[] args) throws Exception {
    	SFTPAgent test = new SFTPAgent("192.168.0.57","root","123456");
        
        String src = "E:\\many.txt"; // 本地文件名
        String dst = "/home/"; // 目标文件名
              
        ChannelSftp chSftp = test.getChannel(60000);
        
        *//**
         * 代码段1
        OutputStream out = chSftp.put(dst, ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
        byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
        int read;
        if (out != null) {
            System.out.println("Start to read input stream");
            InputStream is = new FileInputStream(src);
            do {
                read = is.read(buff, 0, buff.length);
                if (read > 0) {
                    out.write(buff, 0, read);
                }
                out.flush();
            } while (read >= 0);
            System.out.println("input stream read done.");
        }
        **//*
        
        chSftp.put(src, dst, ChannelSftp.OVERWRITE); // 代码段2
        
        // chSftp.put(new FileInputStream(src), dst, ChannelSftp.OVERWRITE); // 代码段3
        
        chSftp.quit();
        test.closeChannel();
    }*/
}