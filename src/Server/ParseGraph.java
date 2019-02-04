package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ParseGraph extends Thread{
	private ArrayList<ArrayList<Integer>> followers;
	private ArrayList<ArrayList<Integer>> following;

	public ParseGraph(ArrayList<ArrayList<Integer>> Followers, ArrayList<ArrayList<Integer>> Following){
		followers = Followers;
		following = Following;
	}
	
	public void run(){
		Parse();
	}
	
	private void Parse(){
		if(followers.size() > 0 && following.size() > 0){
			followers.clear();
			following.clear();
		}
		BufferedReader reader = null;
		File f;
		String line;
		String followersss;
		try{
			f = new File("SocialGraph.txt");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));			
			line = reader.readLine();
			int clients = 0;
			while(line != null) 
		    {
			   CreateDir(line.substring(0, 1).trim());
		       followersss = line.substring(1).trim();
		       FillFollowers(followersss);
		       line = reader.readLine();
		       clients++;
		    }
			
			FillFollowing(clients);
		}catch(NullPointerException e){
			System.err.println("File Not Found!!");
		}catch(IOException e){
			System.err.println("Error on Writting or Reading File");
		}finally{
			try{
				reader.close();
			}catch(IOException e){
				System.err.println("Cannot Close BufferedReader Stream");
			}
		}
	}
	private void FillFollowers(String Followers){
		   StringTokenizer token = new StringTokenizer(Followers, " ");
	       ArrayList<Integer> innerList = new ArrayList<Integer>();
	       while(token.hasMoreTokens()){
	    	   innerList.add(Integer.parseInt(token.nextToken()));
	       }
	       followers.add(innerList);   
	}
	
	private void FillFollowing(int clients){
		for(int i = 0; i < clients; i++){
			following.add(new ArrayList<Integer>()); 				
		}
		BufferedReader reader = null;
		File f;
		String line;
		String ifollow;
		try{
			f = new File("SocialGraph.txt");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));			
			line = reader.readLine();
			while(line != null) 
		    {
				ifollow = line.substring(0, 1).trim();
				line = line.substring(1).trim();
				StringTokenizer token = new StringTokenizer(line, " ");
				while(token.hasMoreTokens()){
					following.get(Integer.parseInt(token.nextToken()) - 1).add(Integer.parseInt(ifollow));
				}
		       line = reader.readLine();
		    }
		}catch(NullPointerException e){
			System.err.println("File Not Found!!");
		}catch(IOException e){
			System.err.println("Error on Writting or Reading File");
		}finally{
			try{
				reader.close();
			}catch(IOException e){
				System.err.println("Cannot Close BufferedReader Stream");
			}
		}
		
	}
	
	private void CreateDir(String clientName){
		File f = new File(System.getProperty("user.dir") + "\\Clients");
		f.mkdirs();
		f = new File(f.getAbsolutePath() + "\\Client" + clientName);
		if(f.exists())return;
		f.mkdirs();
		try{
			File createTxt = new File(f.getAbsolutePath() + ("\\Profile_Client" + clientName + ".txt"));
			createTxt.createNewFile();
			createTxt = new File(f.getAbsolutePath() + ("\\Home_Client" + clientName + ".txt"));
			createTxt.createNewFile();
			createTxt = new File(f.getAbsolutePath() + "\\Audio");
			createTxt.mkdir();
		}catch(IOException e){
			System.out.println("Cannot Create Client" + clientName + " folders on " + f.getAbsolutePath());
		}
	}
}
