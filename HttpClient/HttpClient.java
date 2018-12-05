import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class HttpClient {
public static void main(String[] args) throws UnknownHostException, IOException, EOFException {
	boolean redirect=false;
	while(true){
		String request;
		if(redirect){
			request = "httpc get -v http://httpbin.org/get?";
		}
		else{
	System.out.print(">");
	Scanner sc=new Scanner(System.in);
	request = sc.nextLine();
	if(request.isEmpty() || request.length()==0){
		System.out.println("Invalid Command");
		continue;
	}
	}
	String[] requestArray = request.split(" ");
	ArrayList<String> requestList = new ArrayList<>();
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
	// getting url from the request
		if(requestList.contains("-d") && requestList.contains("-f")){
			System.out.println("Either [-d] or [-f] can be used but not both.");
			continue;
		}
	int urlOffset=1;
	if(requestList.contains("-o")){
		urlOffset=3;
	}
	String url =  requestList.get(requestList.size()-urlOffset).substring(0, requestList.get(requestList.size()-urlOffset).lastIndexOf('/')+1);
	if(url.contains("\'")){
		url=url.replace("\'", "");
	}
	// getting host from url
	String host = new URL(url).getHost();
	//String name = "{\"Assignment\": 1}";
	Socket client = new Socket(host, 80);
	OutputStream outStream = client.getOutputStream();
	
	// getting request method
	String method=requestList.get(1).toUpperCase();
	//getting parameters(text after host)
	String parameters=requestList.get(requestList.size()-urlOffset).substring(requestList.get(requestList.size()-urlOffset).lastIndexOf('/')+1);
	if(parameters.contains("\'")){
		parameters=parameters.replace("\'", "");
	}
	
	PrintWriter pw = new PrintWriter(outStream);
	// Preparing request by adding method and parameters
	pw.print(method+" "+parameters+" HTTP/1.1\r\n");
	// Adding host to request
	pw.print("Host: "+host+"\r\n");
	String inlineData=new String();
	StringBuffer fileData=new StringBuffer();
	
	if(requestList.contains("-d")) {
		inlineData=requestList.get(requestList.indexOf("-d")+1);
		if(inlineData.contains("\'")){
		inlineData=inlineData.replace("\'", "");
		}
		pw.print("Content-Length: "+inlineData.length()+"\r\n");
	}
	else if(requestList.contains("-f")){
		File file=new File(requestList.get(requestList.indexOf("-f")+1));
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		String st;
		while ((st = br.readLine()) != null){
			fileData.append(st);
		}
		pw.println("Content-Length: "+fileData.length()+"\r\n");
	}
	
	// Code for header manipulation starts, if request has -h command (adding header information to the request)
	if(requestList.contains("-h")){
		if(!requestList.contains("-d") && !requestList.contains("-f")){
			int noOfHeaders=requestList.size()-1-requestList.indexOf("-h")-1;
			for (int i = 1; i <= noOfHeaders; i++) {
				pw.print(requestList.get(requestList.indexOf("-h")+i)+"\r\n");
				}
			}
			else if(requestList.contains("-d") || requestList.contains("-f")){
				int noOfHeaders=0;
				if(requestList.contains("-d")){
				noOfHeaders=requestList.indexOf("-d")-requestList.indexOf("-h")-1;
			}
			else if(requestList.contains("-f")){
				noOfHeaders=requestList.indexOf("-f")-requestList.indexOf("-h")-1;
			}
			for (int i = 1; i <= noOfHeaders; i++) {
				pw.print(requestList.get(requestList.indexOf("-h")+i)+"\r\n");
				}
		}
	} // Code for header manipulation ends
	
	// Code for adding in-line data to the request
	if(requestList.contains("-d")){
		pw.print("\r\n");
		pw.print(inlineData);
	}else if(requestList.contains("-f")){
		pw.print(fileData);
		pw.print("\r\n");
	}else{
		pw.print("\r\n");
	}
	pw.flush();
	
	// Printing response to the console
	BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
	String t;
	// if request contains 'verbose'(-v) command
	String statusCode = br.readLine();
	//String statusCode ="HTTTP/1.1 307 MOVED";
	String[] strArr=statusCode.split(" ");
	if(strArr[1].contains("3")){
		// if redirect code in status
		redirect=true;
		continue;
	}
	redirect=false;
	
	// printing response to the mentioned file
	if(requestList.contains("-o")){
		String filePath=requestList.get(requestList.size()-1);
		
		FileWriter file=new FileWriter(filePath,true);
		BufferedWriter bw=new BufferedWriter(file);
		PrintWriter pw1=new PrintWriter(bw);
		
		if(requestList.contains("-v")){
			pw1.println(statusCode);
		while((t = br.readLine()) !=null ) {
			pw1.println(t);
			if(t.equals("}"))
				break;
			}
		}
		// if request does not contain 'verbose'(-v) command
		else{
			int flag=0;
			while((t = br.readLine()) !=null ) {
				if(t.trim().equals("{")) flag=1;
				if(flag==1){
					pw1.println(t);
					if(t.equals("}"))
						break;
				}
			}
		}
		pw1.flush();
		pw1.close();
	}
	else{
	if(requestList.contains("-v")){
		System.out.println(statusCode);
	while((t = br.readLine()) !=null ) {
		System.out.println(t);
		if(t.equals("}"))
			break;
		}
	}
	// if request does not contain 'verbose'(-v) command
	else{
		int flag=0;
		while((t = br.readLine()) !=null) {
			if(t.trim().equals("{")) flag=1;
			if(flag==1){
			System.out.println(t);
			if(t.equals("}"))
				break;
			}
		}
		}
	}
	br.close();
	client.close();
	}
	}
}

}
