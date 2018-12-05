import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class HttpClient {
	public static void main(String[] args) throws UnknownHostException, IOException, EOFException {
		while(true){
			ArrayList<String> requestList = new ArrayList<>();
			String request="";
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
			
			if(requestList.contains("help")){
				
				if(requestList.contains("get")){
					System.out.println("usage: httpc get [-v] [-h key:value] URL\nGet executes a HTTP GET request for"
							+ " a given URL.\n	-v Prints the detail of the response such as protocol, status, and headers."
							+ "\n	-h key:value Associates headers to HTTP Request with the format 'key:value'.");
				}
				else if(requestList.contains("post")){
					System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\nPost executes a HTTP "
							+ "POST request for a given URL with inline data or from file.\n	-v Prints the detail of the response "
							+ "such as protocol, status, and headers.\n	-h key:value Associates headers to HTTP Request with the "
							+ "format 'key:value'.\n	-d string Associates an inline data to the body HTTP POST request."
							+ "\n	-f file Associates the content of a file to the body HTTP POST request.\nEither [-d] or [-f] "
							+ "can be used but not both.");
				}
				else{
					System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n"
							+ "Usage:\n	httpc command [arguments]\nThe commands are:\n	get  executes a HTTP GET request and prints the response.\n"
							+ "	post executes a HTTP POST request and prints the response.\n	help prints this screen.\n\nUse \"httpc help [command]\" "
							+ "for more information about a command.");
				}
		}
		else{
			if(requestList.contains("-d") && requestList.contains("-f")){
				System.out.println("Either [-d] or [-f] can be used but not both.");
				continue;
			}
			int urlOffset=1;
			if(requestList.contains("-o")){
				urlOffset=3;
			}
			String url =  requestList.get(requestList.size()-urlOffset);
			if(url.contains("\'")){
				url=url.replace("\'", "");
			}
			// getting host from url
			String host = new URL(url).getHost();
			int port = new URL(url).getPort();
			Socket client = new Socket(host, port);
			DataInputStream in = new DataInputStream(client.getInputStream());
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			if(requestList.contains("-d") && requestList.contains("-f")){
				System.out.println("Either [-d] or [-f] can be used but not both.");
				continue;
			}
			//getting client type
			out.writeUTF(request);
			String clientType = requestList.get(0);
			out.writeUTF(clientType);
			// getting request method
			String method=requestList.get(1).toUpperCase();
			out.writeUTF(method);
			out.writeUTF(url);
			out.writeUTF(host);
			
			String inlineData=new String();
			String fileData = new String();
			// Sending inline data
			if(requestList.contains("-d")) {
				inlineData=requestList.get(requestList.indexOf("-d")+1);
				if(inlineData.contains("\'")){
				inlineData=inlineData.replace("\'", "");
				}
				out.writeUTF(inlineData);
			}
			
			else if(requestList.contains("-f")){
				File file=new File(requestList.get(requestList.indexOf("-f")+1));
				BufferedReader br = new BufferedReader(new FileReader(file)); 
				String st;
				while ((st = br.readLine()) != null){
					fileData = fileData + st;
				}
				out.writeUTF(fileData);
			}
			
			// Code for header manipulation starts, if request has -h command (adding header information to the request)
			if(requestList.contains("-h")){
				if(!requestList.contains("-d") && !requestList.contains("-f")){
					int noOfHeaders=requestList.size()-1-requestList.indexOf("-h")-1;
					if(requestList.contains("-o")){
						noOfHeaders = noOfHeaders-2;
					}
					out.writeUTF(String.valueOf(noOfHeaders));
					for (int i = 1; i <= noOfHeaders; i++) {
						out.writeUTF(requestList.get(requestList.indexOf("-h")+i)+"\r\n");
						}
					}
					else if(requestList.contains("-d") || requestList.contains("-f")){
						int noOfHeaders=0;
						if(requestList.contains("-d")){
						noOfHeaders=requestList.indexOf("-d")-requestList.indexOf("-h")-1;
						out.writeUTF(String.valueOf(noOfHeaders));
					}
					else if(requestList.contains("-f")){
						noOfHeaders=requestList.indexOf("-f")-requestList.indexOf("-h")-1;
						out.writeUTF(String.valueOf(noOfHeaders));
					}
					for (int i = 1; i <= noOfHeaders; i++) {
						out.writeUTF(requestList.get(requestList.indexOf("-h")+i)+"\r\n");
						}
				}
			} // Code for header manipulation ends
			
			if(requestList.contains("-o")){
				String filePath=requestList.get(requestList.size()-1);
				
				FileWriter file=new FileWriter(filePath,true);
				BufferedWriter bw=new BufferedWriter(file);
				PrintWriter pw1=new PrintWriter(bw);
				
				if(requestList.contains("-v")){
				pw1.print(in.readUTF());	
				}
				// if request does not contain 'verbose'(-v) command
				pw1.print(in.readUTF());
				
				pw1.flush();
				pw1.close();
			}
			else{
			if(request.contains("-v")){
				System.out.println(in.readUTF());
			}
			System.out.println(in.readUTF());
			}
		}
		}
		
	}
}