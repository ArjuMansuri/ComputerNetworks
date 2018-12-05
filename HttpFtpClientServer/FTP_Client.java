import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class FTP_Client {
	
public static void main(String[] args) throws UnknownHostException, IOException, EOFException {
	String dir = System.getProperty("user.dir");
	ArrayList<String> requestList = new ArrayList<>();
	File file = new File("Downloads");
	file.mkdir();
	while(true){
		String request="";
	// httpfs get/  http://localhost:1234/get?
	System.out.print(">");
	Scanner sc=new Scanner(System.in);
	request = sc.nextLine();
	if(request.isEmpty() || request.length()==0){
		System.out.println("Invalid Command");
		continue;
	}
	
	String[] requestArray = request.split(" ");
	requestList = new ArrayList<>();
	for (int i = 0; i < requestArray.length; i++) {
		requestList.add(requestArray[i]);
	}
	String url ;
	if(request.contains("post")){
		url = requestList.get(2);
	}
	else{
		url =  requestList.get(requestList.size()-1);
	}
	if(url.contains("\'")){
		url=url.replace("\'", "");
	}
	// getting host from url
		String host = new URL(url).getHost();
		int port = new URL(url).getPort();
		Socket client = new Socket(host,port);
		DataInputStream in = new DataInputStream(client.getInputStream());
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		
		out.writeUTF(request);
		String clientType = requestList.get(0);
		out.writeUTF(clientType);
		
		String method=requestList.get(1);
		out.writeUTF(method);
		out.writeUTF(url);
		out.writeUTF(host);
		
		
		if(method.equals("get/")){
				System.out.println(in.readUTF()); // reading response headers
				System.out.println(in.readUTF()); // reading response body
		}
		else if(!method.endsWith("/") && method.contains("get/")){
			if(request.contains("Content-Disposition:attachment")){
				String statusCode=in.readUTF();
				if(!statusCode.equals("404")){
					String fileData = in.readUTF();
					String fileName = in.readUTF();
					
					file = new File(dir+"/Downloads/"+fileName);
					file.createNewFile();
					
					FileWriter fw = new FileWriter(file);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter pw = new PrintWriter(bw);
					
					pw.print(fileData);
					pw.flush();
					pw.close();
				}
					System.out.println(in.readUTF()); // reading response headers
					System.out.println(in.readUTF()); // reading response body
					if(!statusCode.equals("404"))
					System.out.println("File downloaded in "+dir+"\\Downloads");
				}
			else{
				in.readUTF();
				System.out.println(in.readUTF()); // reading response headers
				System.out.println(in.readUTF()); // reading response body
			}
				
			}
		
		else if(!method.endsWith("/") && method.contains("post/")){
				System.out.println(in.readUTF());
				String ans = sc.next();
				if(ans !=null){
					out.writeUTF(ans);
					}
				System.out.println(in.readUTF()); // reading response headers
				System.out.println(in.readUTF()); // reading response body
				}
		
		out.flush();
		out.close();
	

	}
}
}
