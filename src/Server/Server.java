package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	private ServerSocket serversocket;
	private Socket socket;
	private ArrayList<ArrayList<Integer>> followers = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> following = new ArrayList<ArrayList<Integer>>();;
	
	public static void main(String[] args){
		new Server().openServer();	
	}
	
	public void openServer(){
		new ParseGraph(followers, following).start();
		try {
			serversocket = new ServerSocket(4000, 7);
			while(true){
				socket = serversocket.accept();
				Thread t = new ClientService(socket, followers, following);
				t.start();
			}
		
		
		} catch (IOException e) {
			System.err.println("Server couldn't open on port 4000\n\n");
		}finally{
			try{
				serversocket.close();
				socket.close();
			}catch(IOException e){
				System.err.println("Error on closing the ServerSocket and the Socket!!!");
			}
		}
	}
}
