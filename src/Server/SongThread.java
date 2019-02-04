package Server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SongThread extends Thread{
	private String clientName;
	private String songname;
	private long songsize;
	ServerSocket serversocket;
	Socket waitsong;
	BufferedOutputStream writesong;
	InputStream datain;
	
	public SongThread(Socket c, String clientName, String songname, long songsize){
		this.clientName = clientName;
		this.songname = songname;
		this.songsize = songsize;
		waitsong = c;
	}
	
	public void run(){
		File f = new File(System.getProperty("user.dir") + "\\Clients\\" + clientName + "\\Audio");
		if(!f.exists()) f.mkdir();
		f = new File(f.getAbsolutePath() + "\\" + songname);
		try{
			datain = waitsong.getInputStream();
			byte[] songbytes = new byte[(int)songsize];
			int readenbytes = datain.read(songbytes, 0, 1024);
			int nextread = readenbytes;
			while(readenbytes > 0){
				if(((int)songsize - nextread) < 1024){
					readenbytes = datain.read(songbytes, nextread, (int)songsize - nextread);
				}else{
					readenbytes = datain.read(songbytes, nextread, 1024);
				}
				if(readenbytes > 0)nextread = nextread + readenbytes;
			}
			
			if(datain != null){
				datain.close();
			}
			writesong = new BufferedOutputStream(new FileOutputStream(f));
			writesong.write(songbytes, 0, (int)songsize);
			writesong.flush();
			
			if(writesong != null){
				writesong.close();
			}
		
		} catch (IOException e) {
			System.err.println("Cannot open server to wait for song!!");
		}
	}
}
