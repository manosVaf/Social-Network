package Server;

import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.io.*;

import Both.*;
import Client.AudioFiltering;

public class ClientService extends Thread {
	private Socket localSocket;
	private Socket sendtoClient;
	ObjectInputStream in;
	ObjectOutputStream out;
	private ArrayList<ArrayList<Integer>> followers;
	private ArrayList<ArrayList<Integer>> following;
	private ArrayList<Packet> packets;
	private Packet counter;
	private boolean retransimission = false;
	
	public ClientService(Socket socket, ArrayList<ArrayList<Integer>> Followers, ArrayList<ArrayList<Integer>> Following){
		localSocket = socket;
		followers = Followers;
		following = Following;
	}
	
	public void run(){
		try{
			in = new ObjectInputStream(localSocket.getInputStream());
			Packet received = (Packet)in.readObject();
			if(received.getPacketCode() == -1){
				SendSignUpInfo();	
			}else if(received.getPacketCode() == 3){			//lavame kainourgio tweet pame kai enhmerwnoume ta arxeia!!
				UpdateFiles(received.getClientName(), received.getTweet());
			}else if(received.getPacketCode() == 4 || received.getPacketCode() == 10){
				if(received.getPacketCode() == 4){
					FindandSendFollowing(received.getClientName());
				}else if(received.getPacketCode() == 10){
					FindandSendFollowers(received.getClientName());
				}
			}else if(received.getPacketCode() == 6){
				FindandSendProfile(received.getClientName(), received.getNumberofFollwing());
			}else if(received.getPacketCode() == 8){
				SendACKandReceiveSong(received.getClientName(), received.getSongname(), received.getSongsize());
			}else if(received.getPacketCode() == 0){
				if(checkClient(received)){
					received.setPacketCode(1);
					sendACK(received);					//an einai o client swstos gurname to packet me code 1!
					System.out.println("A Person connected with us!!!");
					IPcatalog(received, localSocket.getInetAddress().getHostAddress(),localSocket.getPort());
				}else{
					received.setPacketCode(2);
					sendACK(received);					//an la8os gurname me code 2!
				}
			}else if(received.getPacketCode() == 12){
				DeleteFollower(received.getClientName(), received.getFollowertoDelete());
			}else if(received.getPacketCode() == 13){
				AddFollower(received.getClientName(), received.getProfile());
			}else if(received.getPacketCode() == 14){
				SendClientsWithSong(received.getClientName(), received.getSongname());
			}else if(received.getPacketCode() == 16){
				SendAckforDownload();	
			}else if(received.getPacketCode() == 19){
				CutSongtoPackets(received.getClientName(), received.getSongname());
				StopAndWait();
			}
			if (!(in==null)){
				in.close();
			}
		}catch (IOException e) {
			System.err.println("Cannot Open Socket!!");
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot Create the packet!!!"); 
		} catch (InterruptedException e) {
			System.err.println("Thread for receiving song has been interrupted!!");
		}
	}
	
	private void StopAndWait(){
		int packetsend = 0;
		try{
			Packet received = null;
			
			localSocket.setSoTimeout(5000);
			if(!retransimission){
				sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
				out = new ObjectOutputStream(sendtoClient.getOutputStream());
				out.writeObject(packets.get(0));
				counter = packets.get(0);
				out.flush();
				packetsend++;
				if(out != null){
					out.close();
				}
			}
			while(true){
				if(packetsend == packets.size()){
					break;
				}
				while((received = (Packet)in.readObject()).getPacketCode() == 21){
						sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
						out = new ObjectOutputStream(sendtoClient.getOutputStream());
						out.writeObject(packets.get(received.getDownloadPacket() + 1));
						out.flush();
						counter = packets.get(received.getDownloadPacket() + 1);
						packetsend++;
						break;
						
				}	
			}		
		}catch(SocketTimeoutException e){
			try{
				if(packetsend < packets.size() - 2){
					System.out.println("Timeout!!! Sending the same packet again!!");
					sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
					out = new ObjectOutputStream(sendtoClient.getOutputStream());
					out.writeObject(counter);
					out.flush();
					packetsend++;
					retransimission = true;
					if(out!= null){
						out.close();
					}
					StopAndWait();
				}
			}catch(IOException f){}
		}catch (ClassNotFoundException e) {
			System.err.println("Packet Not Found!!");
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				sendtoClient.close();
			} catch (IOException e) {
				System.err.println("Cannot close socket!!");
			}
		}
				
				
			
	}

	private void CutSongtoPackets(String fromwho, String songtitle) {
		File f;
		BufferedInputStream inbytes;
		try{
			f = new File(System.getProperty("user.dir") + "\\Clients\\Client" + fromwho + "\\Audio\\" + songtitle + ".mp3");
			packets = new ArrayList<Packet>((int)f.length()/65536 + 1);
			inbytes = new BufferedInputStream(new FileInputStream(f));
			for(int i = 0; i < (int)f.length()/65536 + 1; i++){
				byte[] bytes = new byte[65536];
				if(i == packets.size() - 1){
					inbytes.read(bytes, 0, (int)f.length() - (i-1)*65536);
				}else{
					inbytes.read(bytes, 0,65536);
				}
				packets.add(new Packet(20, i, bytes));
			}
			packets.get(packets.size() - 1).setPacketCode(22);
		}catch(IOException e){
			System.out.println("Error on reading the file");
		}
	}

	private void SendAckforDownload() {
		try {
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			
			out = new ObjectOutputStream(sendtoClient.getOutputStream());
			out.writeObject(new Packet(17));
			out.flush();
			if(out!= null){
				out.close();
			}
		} catch (IOException e) {
			System.err.println("Error on sending the Packet to Client!!" + e.getLocalizedMessage());
		}
		
	}

	private void SendClientsWithSong(String clientName, String songname) {
		File f; 
		File[] audiofiles;
		String clientswithSong = "";
		ArrayList<Integer> followingwithSong = new ArrayList<Integer>();
		for(int i = 1; i <= followers.size(); i++){
			f = new File(System.getProperty("user.dir") + "\\Clients\\Client" + i + "\\Audio\\");
			audiofiles = f.listFiles(new AudioFiltering());
			if(audiofiles.length != 0){
				for(int j = 0; j < audiofiles.length; j++){
					if(audiofiles[j].getName().equalsIgnoreCase(songname)){
						if(i != followers.size()){
							clientswithSong += "Client " + i + "\n";
						}else{
							clientswithSong += i;
						}
						if(following.get(Integer.parseInt(clientName.substring(6).trim()) - 1).contains(i)){
							followingwithSong.add(i);
						}
					}
				}
			}
		}
		
		try {
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			out = new ObjectOutputStream(sendtoClient.getOutputStream());
			out.writeObject(new Packet(15, followingwithSong , clientswithSong));
			out.flush();
			if(out!= null){
				out.close();
			}
		} catch (IOException e) {
			System.err.println("Error on sending the Packet to Client!!");
		}
		
		
	}

	private void AddFollower(String clientName, String profile) {
		File f;
		BufferedReader reader;
		BufferedWriter writer;
		String linebeforeDelete= "";
		String lineafterDelete = "";
		String linetoChange = "";
		try{
			f = new File("SocialGraph.txt");
			synchronized(f){
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				linetoChange = reader.readLine();
				while(!linetoChange.substring(0, 1).trim().equals(profile)){
					linebeforeDelete +=  linetoChange +"\n";
					linetoChange = reader.readLine();
				}
				String end = reader.readLine();
				while(end != null){
					lineafterDelete += end  + "\n";
					end = reader.readLine();
				}
				if(reader!= null) reader.close();
				linetoChange = linetoChange.substring(0, linetoChange.length() - 1).concat(clientName.substring(6).trim() + "\n"); 
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
				if(lineafterDelete.length() != 0){
					writer.write(linebeforeDelete + linetoChange + lineafterDelete.substring(0, lineafterDelete.length() - 1));
				}else{
					writer.write(linebeforeDelete + linetoChange.substring(0, linetoChange.length() - 1));
				}
				writer.flush();			
				if(writer != null) writer.close();
			}
			Thread t = new ParseGraph(followers, following);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				System.err.println("Thread Parse Interrupted!!");
			}
		}catch(NullPointerException e){
			System.err.println("File not Found!!");
		} catch (FileNotFoundException e) {
			System.err.println("Error on File");
		} catch (IOException e) {
			System.err.println("Error on working in the file!!!");
		}
	}

	private void DeleteFollower(String clientName, int followertoDelete) {
		File f;
		BufferedReader reader;
		BufferedWriter writer;
		String linebeforeDelete= "";
		String lineafterDelete = "";
		String linetoChange = "";
		try{
			f = new File("SocialGraph.txt");
			synchronized(f){
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				linetoChange = reader.readLine();
				while(!linetoChange.substring(0, 1).trim().equals(clientName.substring(6).trim())){
					linebeforeDelete +=  linetoChange +"\n";
					linetoChange = reader.readLine();
				}
				String end = reader.readLine();
				while(end != null){
					lineafterDelete += end  + "\n";
					end = reader.readLine();
				}
				if(reader!= null) reader.close();
				int pos = 0;
				StringTokenizer token = new StringTokenizer(linetoChange, " ");
				while(token.hasMoreTokens()){
					if(Integer.parseInt(token.nextToken()) == followertoDelete)break;
					pos += 2;
				}
				linetoChange = linetoChange.substring(0,pos).trim().concat(linetoChange.substring(pos + 1) + "\n");
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
				if(lineafterDelete.length() != 0){
					writer.write(linebeforeDelete + linetoChange + lineafterDelete.substring(0, lineafterDelete.length() - 1));
				}else{
					writer.write(linebeforeDelete + linetoChange.substring(0, linetoChange.length() - 1));
				}
				writer.flush();			
				if(writer != null) writer.close();
			}
			Thread t = new ParseGraph(followers, following);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				System.err.println("Thread Parse Interrupted!!");
			}
		}catch(NullPointerException e){
			System.err.println("File not Found!!");
		} catch (FileNotFoundException e) {
			System.err.println("Error on File");
		} catch (IOException e) {
			System.err.println("Error on working in the file!!!");
		}
	}

	private void SendACKandReceiveSong(String clientName, String songname, long songsize) throws InterruptedException {
		ServerSocket serversocket = null;
		Thread t = null;
		try {
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			out = new ObjectOutputStream(sendtoClient.getOutputStream());
			out.writeObject(new Packet(9, "", ""));
			out.flush();
			if(out!= null){
				out.close();
			}
			serversocket = new ServerSocket(8000);
			Socket waitsong = serversocket.accept();
			t = new SongThread(waitsong, clientName,songname, songsize);
			t.start();
		} catch (IOException e) {
			System.out.println("Error on Sending Packet");
		}finally{
			try {
				t.join();
				serversocket.close();
			} catch (IOException e) {
				System.err.println("Error on closing socket!!");
			} catch (InterruptedException e) {
				System.err.println("Thread Interrupted!!!");
			}
		}
	}

	private void FindandSendProfile(String clientName, String number) {
		String profile = "";
		String line;
		File f;
		BufferedReader reader;
		int innerPosition;
		try{
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			out = new ObjectOutputStream(sendtoClient.getOutputStream());
			
			synchronized(following){
				innerPosition = following.get(Integer.parseInt(clientName.substring(6).trim()) - 1).indexOf(Integer.parseInt(number));
			}
			
			f = new File(System.getProperty("user.dir") + "\\Clients\\Client" + following.get(Integer.parseInt(number)).get(innerPosition)
				+ "\\" + "Profile_Client" + following.get(Integer.parseInt(number)).get(innerPosition) + ".txt");
			synchronized(f){
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));			
				line = reader.readLine();
				while(line != null){
					if(line.startsWith("on")){
						profile += "\n" +line + "\n";
					}else{
						profile += line + "\n";
					}
					line = reader.readLine();
				}
			}
			if(reader != null){
				reader.close();
			}
			if(profile.equals("")){
				profile = "Client " + Integer.parseInt(number) + " haven't made any tweets!!\n";
			}
			Packet tosend = new Packet(7, profile, "");
			out.writeObject(tosend);
			out.flush();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e){	
			System.err.println("Cannot Send Packet to Client!!" + e.getCause());
		}
	}
	
	private void FindandSendFollowing(String clientName) {
		Packet tosend;
		try{
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			out = new ObjectOutputStream(sendtoClient.getOutputStream());
			synchronized(following){
				tosend = new Packet(5, following.get(Integer.parseInt(clientName.substring(6).trim()) -1 ), following.size());
			}
			out.writeObject(tosend);
			out.flush();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e){	
			System.err.println("Cannot Send Packet to Client!!" + e.getCause());
		}
	}

	private void FindandSendFollowers(String clientName) {
		Packet tosend;
		try{
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			out = new ObjectOutputStream(sendtoClient.getOutputStream());
			synchronized(followers){
				tosend = new Packet(11, followers.get(Integer.parseInt(clientName.substring(6).trim()) -1 ), followers.size());
			}
			out.writeObject(tosend);
			out.flush();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e){	
			System.err.println("Cannot Send Packet to Client!!" + e.getCause());
		}
	}
	
	private void UpdateFiles(String client, String tweet) {
		File f = new File(System.getProperty("user.dir") + "\\Clients\\Client" + client.substring(6).trim() + "\\" + "Profile_Client" + client.substring(6).trim() + ".txt");
		BufferedWriter writer;
			try{
				synchronized(f){
					writer = new BufferedWriter(new FileWriter(f.getAbsolutePath(), true));
					Date date = new Date();
					writer.write("\n" + tweet + "\non: " + date);
					writer.flush();
					for(int i = 0; i < followers.get(Integer.parseInt(client.substring(6).trim()) - 1).size(); i++){
						f = new File(System.getProperty("user.dir") + "//Clients//Client" + followers.get(Integer.parseInt(client.substring(6).trim()) -1).get(i) 
							+ "//" + "Home_Client" + followers.get(Integer.parseInt(client.substring(6).trim()) -1).get(i) + ".txt");
						writer = new BufferedWriter(new FileWriter(f.getAbsolutePath(), true));
 						writer.write("Client " + client.substring(6).trim() +  " has written\n" + tweet + "\non: " + date + "\n");
						writer.flush();
					}
					if(writer != null){
						writer.close();
					}
				}
			} catch (IOException e) {
				System.err.println("Error on Writting the Tweet");
			}
	}
	
	private void UpdateGraph(Packet packet) {
		BufferedWriter writer = null;
		File f;
		try{
			f = new File("SocialGraph.txt");
			synchronized(f){
				writer = new BufferedWriter(new FileWriter(f.getAbsolutePath(), true));
				writer.write("\n" + packet.getUsername().substring(6) + " ");
			}
		}catch(NullPointerException e){
			System.err.println("File Not Found!!" + e.getLocalizedMessage());
		}catch(IOException e){
			System.err.println("Error on Writting or Reading File");
		}finally{
			try{
				writer.close();
			}catch(IOException e){
				System.err.println("Cannot Close BufferedWriter Stream");
			}
		}
	}

	private void SendSignUpInfo() {
		try{
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			out = new ObjectOutputStream(sendtoClient.getOutputStream());	
			Packet tosend = getSignUpInfo();
			out.writeObject(tosend);
			UpdateGraph(tosend);
			CreateDirForNewClient(tosend.getUsername());
			out.flush();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e){	
			System.err.println("Cannot Send Packet to Client!!" + e.getCause());
		}
	}

	private void CreateDirForNewClient(String clientName) {
		File f = new File(System.getProperty("user.dir") + "\\Clients\\" + clientName);
		f.mkdir();
		try{
			File createTxt = new File(f.getPath() + ("\\Profile_" + clientName + ".txt"));
			createTxt.createNewFile();
			createTxt = new File(f.getPath() + ("\\Home_" + clientName + ".txt"));
			createTxt.createNewFile();
		}catch(IOException e){
			System.out.println("Cannot Create " + clientName + " folders on " + f.getPath());
		}
	}

	private Packet getSignUpInfo() {
		Packet toSend = null;
		int code;
		BufferedReader reader = null;
		File f;
		String line;
		String lastline = "";
		try{
			f = new File("SocialGraph.txt");
			synchronized(f){
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));			
				line = reader.readLine();
				while ((line = reader.readLine()) != null) 
				{
					lastline = line;
				}
				code = Integer.parseInt(lastline.substring(0, 2).trim());
			}
			toSend = new Packet(0, ("Client " + (code + 1)), ("Client " + (code + 1) + "pass")); 
		}catch(NullPointerException e){
			System.err.println("File Not Found!!" + e.getLocalizedMessage());
		}catch(IOException e){
			System.err.println("Error on Writting or Reading File");
		}finally{
			try{
				reader.close();
			}catch(IOException e){
				System.err.println("Cannot Close BufferedReader Stream");
			}
		}
		return toSend;
	}

	
	private void sendACK(Packet packet){			//stelnei ACK gia ton client
		try{
			sendtoClient = new Socket(localSocket.getInetAddress().getHostAddress(), 5000);
			out = new ObjectOutputStream(sendtoClient.getOutputStream());
			out.writeObject(packet);
			out.flush();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e){
			System.err.println("Cannot Send Packet to Client!!" + e.getCause());
		}
	}
	
	private boolean checkClient(Packet packet){		//tsekaroume ston grafo an uparxei o client kai prepei na einai username Client X kai to pass prepei na einai ClientXpass
		boolean test = false;
		BufferedReader reader = null;
		File f;
		String line = "";
		try{
			f = new File("SocialGraph.txt");
			synchronized(f){
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));			
				line = reader.readLine();
				while(line != null){
					if(line.startsWith(packet.getUsername().substring(6))){
						if(packet.getPassword().equalsIgnoreCase((packet.getUsername() + "pass")))
							test = true;
							break;
					}
					line = reader.readLine();
				}
			}
		}catch(NullPointerException e){
 			System.err.println("File Not Found!!" + e.getClass().toString());
		}catch(IOException e){
			System.err.println("Error on Writting or Reading File");
		}finally{
			try{
				reader.close();
			}catch(IOException e){
				System.err.println("Cannot Close BufferedReader Stream");
			}
		}
		return test;
	}
	
	private void IPcatalog(Packet packet, String IP, int port){
		BufferedWriter writer = null;
		BufferedReader reader = null;
		String line;
		
		File ips = new File(System.getProperty("user.dir") + "/IPCatalog");
		if(ips.exists()){		
			File overwriteFile = new File(ips.getPath() + "\\IPs.txt");
			if(!overwriteFile.exists()){
				try{
					if(overwriteFile.createNewFile()){
						writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(overwriteFile)));	
						writer.write(packet.getUsername() + "\n" + "\tThe IP is: " + IP + " and the port is: " + port + "\n");		//grafoume thn kainourgia sundesh	
					}	
				}catch(IOException e){
					System.err.println("Cannot Create IPs.txt File on " + ips.getPath());
				}catch(NullPointerException e){
					System.err.println("File Not Found!!" + e.getLocalizedMessage());
				}finally{
					try{
						writer.close();
					}catch(IOException e){
						System.err.println("Cannot Close BufferedWriter Stream");
					}
				}
			}else{
			try{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(overwriteFile)));			
				line = reader.readLine();
				while(line != null){
					reader.readLine();
					line = reader.readLine();
				}
				try{
					reader.close();
				}catch(IOException e){
					System.err.println("Cannot Close BufferedReader Stream");
				}
				
				writer = new BufferedWriter(new FileWriter(overwriteFile.getAbsolutePath(), true));
				writer.write(packet.getUsername() + "\n" +  "\tThe IP is: " + IP + " and the port is: " + port + "\n");
				
			}catch(NullPointerException e){
				System.err.println("File Not Found!!" + e.getLocalizedMessage());
			}catch(IOException e){
				System.err.println("Error on Writting or Reading File");
			}finally{
				try{
					writer.close();
				}catch(IOException e){
					System.err.println("Cannot Close BufferedWriter Stream");
				}
			}
			}
		}else{						//an o fakelos den uparxei ton dhmiourgoume kai ftiaxnoume kai ena arxeio txt me to ip
			ips.mkdirs();
			try{
				File newFile = new File(ips.getPath() + "\\IPs.txt");		//dhmioyrgia IPs.txt
				if(newFile.createNewFile()){
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)));	
					writer.write(packet.getUsername() + "\n" + "\tThe IP is: " + IP + " and the port is: " + port + "\n");		//grafoume thn kainourgia sundesh	
				}	
			}catch(IOException e){
				System.err.println("Cannot Create IPs.txt File on " + ips.getPath());
			}catch(NullPointerException e){
				System.err.println("File Not Found!!" + e.getLocalizedMessage());
			}finally{
				try{
					writer.close();
				}catch(IOException e){
					System.err.println("Cannot Close BufferedWriter Stream");
				}
			}
			
		}
	}
}