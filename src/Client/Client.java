package Client;

import java.io.*;
import java.net.*;
import java.util.*;

import Both.*;

public class Client {
	private boolean menu = false;
	private ServerSocket serversocket;
	private Socket getfromServer;
	private Socket connectServer;
	private String username;
	private String password;
	Scanner input;
	int check;
	int clients;
	private String IP = "127.0.0.1";
	
	public Client(){
		try {
			connectServer = new Socket(IP, 4000);
			input = new Scanner(System.in);
			System.out.println("Do you want to Sign Up or Log In??\nChoose 1 for Sign Up and 2 for Log In!");
			check = Integer.parseInt(input.nextLine());
			if(check == 2){
				System.out.println("Please give Username!!");
				username = input.nextLine();
				System.out.println("Please give Password");
				password = input.nextLine();
				SendLogInPacket(username, password);
				if(getACK())
					menu = true;
			} else if(check == 1){
				SendSignUpPacket();						//dinoume ston kainouyrgio xrhsth to epomeno dia8esimo noumero username!!
				getSignUpInfo();
				System.out.println("Do you want to log in?? Give 1 for yes and 2 for no!!");
				check = Integer.parseInt(input.nextLine());
				if(check == 1){
					System.out.println("Please give Username!!");
					username = input.nextLine();
					System.out.println("Please give Password");
					password = input.nextLine();
					if(connectServer.isClosed())connectServer = new Socket(IP, 4000);
					SendLogInPacket(username, password);
					if(getACK())
					menu = true;
				}else if(check == 2){
					menu = false;
				}
			} else{
				menu = false;
				System.err.println("Wrong Choice!! System Exits!!");
			}
			if(menu){
				while(check != 7){
					System.out.println("1) What Do you Think??\n2) Search for a Song\n3) Upload a new Song\n"
							+ "4) Check Page of a Follower\n5) Follow a Person\n6) Delete a Follower\n7) Exit");
					check =0;
					check = Integer.parseInt(input.nextLine());
					if(check == 1){
						System.out.println("Write your thoughts");
						writeTweet(input.nextLine());
					}else if(check ==2){
						System.out.println("Write the song title you want to search for!!");
						SearchSong(input.nextLine());
					}else if(check == 3){
						UploadSong(username);
					}else if(check == 4){
						CheckPage();
					}else if(check == 5){
						Follow(username);
					}else if(check == 6){
						DeleteFollower(username);
					}else{
						check = 7;
					}
				}
			}
		} catch (UnknownHostException e) {
			System.err.println("Wrong IP Adress!!!");
		} catch (IOException e) {
			System.err.println("Connection Failed " + e.getMessage());
		}catch(InputMismatchException e){
			System.err.println("Wrong Input!! System will exit!!");
		}catch(NumberFormatException e){
			System.err.println("Wrong Input!! System will exit!!");;
		}
		finally {
			try {
				System.out.println("Thank you!! Have a good Day!!! :) :) ");
				connectServer.close();
			} catch (IOException e) {
				System.out.println("Error on closing the Socket!!!");
			}
		}	
	}
	
	private void SearchSong(String songTitle) {
		try{
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(14, username , songTitle + ".mp3"));
			out.flush();
			if(out != null){
				out.close();
			}
			
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();							//perimenoume gia ACK oti einai etoimos na lavei
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			Packet received = (Packet) in.readObject();
			if(in!= null){
				in.close();
			}
			if(received.getClientsWithSong().length() > 0){
				System.out.println(received.getClientsWithSong());
				System.out.println("If you want to download it press 1 or 2 for no!!");
				int choice = Integer.parseInt(input.nextLine());
				if(choice == 1){
					if(received.getFollowingwithSong().size() == 0){	//to following pou gurname einai apo autous pou akoou8ei poioi to exoun
						System.out.println("You cant Download the song because \nyou are not following one of the clients who have it!!!\nFollow one of the above and try again!!");
					}else{
						System.out.println("You can download it from:");
						received.getFollowingwithSong().forEach(s -> System.out.println("Client " + s));
						System.out.println("Write client's number to download it");
						DownloadSong(input.nextLine(), songTitle);
					}
				}
			}else{
				System.out.println("No one has " + songTitle + " on server!!\n");
			}
		}catch(IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Packet not Found!!");
		}finally{
			try{
				serversocket.close();
				getfromServer.close();
			}catch(IOException e){
				System.err.println("Error on closing sockets!!");
			}
		}
	}

	private void DownloadSong(String fromwho, String songTitle) {	
		Packet received;
		ArrayList<Packet> packets = new ArrayList<Packet>();
		try {
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(16));
			out.flush();	
			System.out.println("Esteila aithsh!!");//aithsh gia download
			if(out != null){
				out.close();
			}
			if(serversocket!= null){
				serversocket.close();
				getfromServer.close();
			}
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();							//perimenoume gia ACK oti einai etoimos na steilei
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			received = (Packet) in.readObject();	
			System.out.println("ACK!");
			if(in!= null){
				in.close();
			}
			
			if(received.getPacketCode() == 17){
				if(connectServer.isClosed()){
					connectServer = new Socket(IP, 4000);
				}
				out = new ObjectOutputStream(connectServer.getOutputStream());
				out.writeObject(new Packet(19, fromwho, songTitle));		//stelneis ton titlo tou kommatiou
				out.flush();
				System.out.println(songTitle);
				if(serversocket!= null){
					serversocket.close();
					getfromServer.close();
				}
				serversocket = new ServerSocket(5000);
				getfromServer = serversocket.accept();					//perimenoume to prwto paketo
				in = new ObjectInputStream(getfromServer.getInputStream());
				
				boolean retransmission = false;
				while((received = (Packet) in.readObject()).getPacketCode() == 20){		//oso stelnei
					if(received.getDownloadPacket() != 0 || retransmission){
							packets.add(received);
							out.writeObject(new Packet(21, received.getDownloadPacket()));
							out.flush();
					
					}
					
					System.out.println(received.getDownloadPacket());
					retransmission = true;
					getfromServer = serversocket.accept();
					in = new ObjectInputStream(getfromServer.getInputStream());
					
				}
				packets.add(received);
				out.writeObject(new Packet(22, received.getDownloadPacket()));
				out.flush();
				File f = new File(System.getProperty("user.dir") + "\\" + username + "\\Audio\\" + songTitle + ".mp3");
				f.createNewFile();
				OutputStream outbytes = new BufferedOutputStream(new FileOutputStream(f));
				byte[] song = new byte[packets.get(0).getReceivedbytes().length * (packets.size() - 1) + packets.get(packets.size() -1).getReceivedbytes().length];
				for(int i = 0; i < packets.size(); i++){
					for(int j = 0; j < packets.get(i).getReceivedbytes().length; j++){
						song[i * packets.get(i).getReceivedbytes().length + j] = packets.get(i).getReceivedbytes()[j];
					}
				}
				
				outbytes.write(song);
				outbytes.flush();
				if(outbytes != null){
					outbytes.close();
				}
				
				System.out.println("Song " + songTitle + " downloaded succesfully!!!");
			}else{
				System.out.println("Server cant start Downloading... Try again later!!");
			}	
		}catch(IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Packet not Found!!");
		} finally{
			try{
				serversocket.close();
				getfromServer.close();
			}catch(IOException e){
				System.err.println("Error on closing sockets!!");
			}
		}
	}

	private void CheckPage() {
		System.out.println("You are following: ");
		ArrayList<Integer> following = getFollowingfromServer(username);
		if(following.size() != 0){
			for(int i = 0; i < following.size(); i++){
				System.out.println("Client " + following.get(i));
			}
			System.out.println("\nIf you want to check one of them write its number\nOtherwise write -1!!");
			String choice = input.nextLine();
			if(following.contains(Integer.parseInt(choice))){
				System.out.println(GetProfile(username, choice));
			}else{
				check = 7;
			}
		}else{
			System.out.println("You are not following anyone...!!1");
		}
	}
	
	private void Follow(String clientName){
		System.out.println("You are following:\n");
		ArrayList<Integer> following = getFollowingfromServer(clientName);
		following.stream().forEach(s-> System.out.println("Client " + s));
		System.out.println("You are not following:\n");
		for(int i = 1; i < clients + 1; i++){
			if(!following.contains(i) && i != Integer.parseInt(clientName.substring(6).trim()))
				System.out.println("Client " + i);
		}
		
		try{
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			System.out.println("Choose which client you want to follow!!");
			out.writeObject(new Packet(13, clientName , input.nextLine()));
			out.flush();
			if(out != null){
				out.close();
			}
		}catch(IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getClass());
		}
		
	}
	
	

	private void DeleteFollower(String clientName) {
		System.out.println("Your Followers are:\n");
		ArrayList<Integer> followers = getFollowersfromServer(clientName);
		if(followers.size() != 0){
			for(int i = 0; i < followers.size(); i++){
				System.out.println("Client " + followers.get(i));
			}
			System.out.println("Choose which one you want to delete!!!\nWrite his number!");
			String choice = input.nextLine();
			if(followers.contains(Integer.parseInt(choice))){
				followers.get(followers.indexOf(Integer.parseInt(choice)));
				System.out.println("Follower delete!!\n");
				DeleteFollowerFromServer(clientName, choice);
			}else{
				check = 7;
			}
		}else{
			System.out.println("You are not following anyone...!!1");
		}
	}

	private void DeleteFollowerFromServer(String clientName, String choice) {
		try{
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(12, clientName , choice));
			out.flush();
			if(out != null){
				out.close();
			}
		}catch(IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getClass());
		}
	}

	private void UploadSong(String clientName) {
		File f = new File(System.getProperty("user.dir") + "\\" + clientName + "\\Audio");
		File[] audiofiles = f.listFiles(new AudioFiltering());
		if(audiofiles.length == 0){
			System.out.println("No song on your Directory\nAdd one and come again :)!!"); 
			return;
		}
		for(int i = 0; i <audiofiles.length; i++){
			System.out.println("Audio " + (i + 1) + " is " + audiofiles[i].getName());
		}
		System.out.println();
		System.out.println("Which song do you want to upload??\nGive its number or give -1 for exit!!!");
		int choice = Integer.parseInt(input.nextLine());
		if(choice > 0 && choice <= audiofiles.length){
			f = new File(audiofiles[choice - 1].getAbsolutePath());
			SendSong(f);
		}else{
			return;
		}
	}

	private void SendSong(File f) {
		ObjectOutputStream out;
		OutputStream dataout;
		Packet received;
		BufferedInputStream buffering;
		try {
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(8, username,f.getName() , f.length()));		//paketo me to onoma tragoudiou kai to megethos
			out.flush();
			if(out!= null){
				out.close();
			}
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();							//perimenoume gia ACK oti einai etoimos na lavei
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			received = (Packet)in.readObject();
			if(received.getPacketCode() == 9){			
				byte[] bytesofsong = new byte[(int) f.length()];		//desmeuoume ton xwro gia to tragoudi se bytes
				buffering = new BufferedInputStream(new FileInputStream(f));
				buffering.read(bytesofsong, 0, (int)f.length());
				if(connectServer.isClosed()){
					connectServer = new Socket(IP, 8000);
				}
				dataout = connectServer.getOutputStream();
				dataout.write(bytesofsong, 0, (int)f.length());
				dataout.flush();
				if(dataout != null && buffering != null){
					dataout.close();
					buffering.close();
				}
				writeTweet(username + " posted " + f.getName());
			}else{
				System.err.println("Server is busy\nYou cant't upload the Song " + f.getName() + "\nTry again later");
			}
		} catch (IOException e) {
			System.err.println("Error On Sending the Song " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot Define the Packet");
		}finally{
			try{
				serversocket.close();
				getfromServer.close();
				connectServer.close();
			}catch(IOException e){
				System.err.println("Cannot Close Sockets!!");
			}
		}
	}



	private String GetProfile(String clientName, String choice) {
		Packet received = null; 
		try{
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(6, clientName , choice));
			out.flush();
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			received = (Packet)in.readObject();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getClass());
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot Create the packet!!!"); 
		}finally {
			try{
				serversocket.close();
				getfromServer.close();
		} catch (IOException e) {
			System.err.println("Error on closing Connection with Server!!");
		}
		}
		return received.getProfile();
	}



	private ArrayList<Integer> getFollowingfromServer(String client) {
		Packet received = null;
		try{
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(4, client, ""));
			out.flush();
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			received = (Packet)in.readObject();
			clients = received.getClients();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getClass());
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot Create the packet!!!"); 
		}finally {
			try{
				serversocket.close();
				getfromServer.close();
		} catch (IOException e) {
			System.err.println("Error on closing Connection with Server!!");
		}
		}
		return received.getFollowing();
			
	}

	private ArrayList<Integer> getFollowersfromServer(String client) {
		Packet received = null;
		try{
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(10, client, ""));
			out.flush();
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			received = (Packet)in.readObject();
			clients = received.getClients();
			if (!(out==null)){
				out.close();
			}
		}catch (IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getClass());
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot Create the packet!!!"); 
		}finally {
			try{
				serversocket.close();
				getfromServer.close();
		} catch (IOException e) {
			System.err.println("Error on closing Connection with Server!!");
		}
		}
		return received.getFollowers();
	
	}
	


	private boolean getACK(){
		boolean check = false;
		try {
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			Packet received = (Packet)in.readObject();
			if(received.getPacketCode() == 1){
				UpdateDir(received.getUsername());
				System.out.println("Welcome Client " + received.getUsername().substring(6));
				check = true;
			}else if(received.getPacketCode() == 2){
				System.out.println("Wrong Username of Password!!! Cannot Log in!!" );
				check = false;
			}
			if (!(in==null)){
				in.close();
			}
		} catch (IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getClass());
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot Create the packet!!!"); 
		}finally {
			try{
				serversocket.close();
				getfromServer.close();
		} catch (IOException e) {
			System.err.println("Error on closing Connection with Server!!");
		}
		}
		return check;
	}
	
	private void getSignUpInfo(){
		try {
			serversocket = new ServerSocket(5000);
			getfromServer = serversocket.accept();
			ObjectInputStream in = new ObjectInputStream(getfromServer.getInputStream());
			Packet received = (Packet)in.readObject();
			System.out.println("Your Username is " + received.getUsername() + " \nPassWord is " + received.getPassword());
			if (!(in==null)){
				in.close();
			}
		} catch (IOException e) {
			System.err.println("Cannot Open Socket!!\n" + e.getClass());
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot Create the packet!!!"); 
		}finally {
			try{
				serversocket.close();
				getfromServer.close();
		} catch (IOException e) {
			System.err.println("Error on closing Connection with Server!!");
		}
		}
	}
	
	private void SendLogInPacket(String user, String pass){
		try{
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(0, user, pass));
			out.flush();
			if (!(out==null)){
				out.close();
			}
		}catch(IOException e){
			System.err.println("Error on Sending the Packet");
			e.printStackTrace();
		}
	}
	
	private void SendSignUpPacket(){
		try{
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			out.writeObject(new Packet(-1));
			out.flush(); 
			if (!(out==null)){
				out.close();
			}
		}catch(IOException e){
			System.err.println("Error on Sending the Packet");
			e.printStackTrace();
		}
	}
	
	private void UpdateDir(String clientName){
		File f = new File(System.getProperty("user.dir") + "\\" + clientName);
		f.mkdir();
		f = new File(f.getAbsolutePath() + "\\Audio");
		f.mkdir();
		f = new File(System.getProperty("user.dir") + "\\" + clientName);
		try{
			File createTxt = new File(f.getPath() + ("\\Profile_" + clientName + ".txt"));
			createTxt.createNewFile();
			createTxt = new File(f.getPath() + ("\\Home_" + clientName + ".txt"));
			createTxt.createNewFile();
		}catch(IOException e){
			System.out.println("Cannot Create " + clientName + " folders on " + f.getPath());
		}
	}
	
	private void writeTweet(String theTweet){
		File f;
		BufferedWriter writer;
		try{
			if(connectServer.isClosed()){
				connectServer = new Socket(IP, 4000);
			}
			ObjectOutputStream out = new ObjectOutputStream(connectServer.getOutputStream());
			f = new File(System.getProperty("user.dir") + "\\Client" + username.substring(6).trim() + "\\" + "Profile_Client" + username.substring(6).trim() + ".txt");
			out.writeObject(new Packet(3, username, theTweet));
			out.flush();
			synchronized(f){
				writer = new BufferedWriter(new FileWriter(f.getAbsolutePath(), true));
				Date date = new Date();
				writer.write(theTweet + "\non: " + date + "\n");
			}
			if(out != null){
				out.close();
			}
			if(writer != null){
				writer.close();
			}
			} catch (UnknownHostException e) {
				System.err.println("Wrong IP Adress!!!");
			} catch (IOException e) {
				System.err.println("Error on Writting the Tweet");
			}
	}
	
	public static void main(String[] args){
		new Client();
	}
}
