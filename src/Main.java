import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Main {

	static int  ProcessID = 1 ;
	static String[] memory = new String[40];
	static Queue<Integer> readyQ = new LinkedList<>();
	static Queue<Integer> blockedQ = new LinkedList<>();
	static Queue<Integer> userInput = new LinkedList<>();
	static Queue<Integer> userOutput = new LinkedList<>();
	static Queue<Integer> file = new LinkedList<>();
	static boolean flaguI = false;
	static boolean flaguO = false;
	static boolean flagf = false;
	static Hashtable<Integer , Integer> inputs = new Hashtable<Integer , Integer>();
	static int timeslice = 2;
	static int cycles = 0;
	static String program1 = "";
	static String program2 = "";
	static String program3 = "";
	static int Scycles ;
	static int Tcycles ;

	public static void CodeParser(String program) {
		System.out.println("Process "+ program +" has arrived in cycle "+ cycles);
		FileReader p;
		try {
			ArrayList<String>instructions = new ArrayList<>();

			p = new FileReader(program);
			BufferedReader br = new BufferedReader(p);
			String line = br.readLine() ;
			while(line != null) {
				instructions.add(line);
				line =br.readLine() ;
			}
			br.close();
			// size of p = 4(pcp) + 3( var) + instructions.size()
			int size = 7 + instructions.size();
			int av = 0 ;
			int i ;
			int diff = 0;
			for( i = 0 ; i < memory.length ; i++) {

				if( memory[i] == null) {
					av++ ;
				}
				else if(memory[i].contains("-")){
					String[] bounds = memory[i].split("-");
					if(memory[i+3].equals("finished")) {
						av += Integer.parseInt(bounds[1]) - Integer.parseInt(bounds[0]) + 1;
					}
					else {
						av = 0;
					}
					i = Integer.parseInt(bounds[1]);
				}
				else {
					av++;
				}
				if(av == size ) {
					diff = 0;
					break;
				}
				else if(av > size) {
					diff = av - size;
					break;
				}
			}
			if(av < size ) { //memory not enough
				FileWriter disk = new FileWriter("Disk" , true);
				BufferedWriter br1 = new BufferedWriter(disk);
				String inss = "";
				for(int j = 0 ; j < instructions.size() ; j ++) {
					inss = inss + "," + instructions.get(j);
				}
				br1.write(ProcessID + "," + 0 + ",ready,null,null,null" + inss + "\n");
				br1.close();
			}
			else {
				int x =i ;
				i = i - size - diff +1 ;
				//int x = i+size;
				//pcp  i to i+4
				inputs.put (ProcessID, i);
				memory[i] = i +"-"+ x  ; // Memory Boundaries
				i++;
				memory[i++] = ProcessID + "" ;
				memory[i++] = "0";  //  Program Counter
				memory[i++] = "ready" ; //Process State

				// var i+4 to i+7

				memory[i++] = "null";
				memory[i++] = "null";
				memory[i++] = "null";
				//i+=3;

				//instructions i+7 to i+size

				for(int j = 0 ; j < instructions.size() ; j ++) {
					memory[i++] = instructions.get(j);
				}



			}
			readyQ.add(ProcessID);
			for(int k =0 ; k < readyQ.size()-1 ; k++) {
				readyQ.add(readyQ.poll());
			}
			ProcessID++;

		}catch (IOException e) {
			// TODO: handle exception
		}
	}

	private static void scheduler() {
		if(!readyQ.isEmpty()) {
			int id = readyQ.poll();

			if(!inputs.containsKey(id)) {
				try {
					fromdisktomem(id);
				} catch (IOException e) {

				}
			}
			//				System.out.println("id :" + id);
			int pos = inputs.get(id);
			int counter = Integer.parseInt(memory[pos+2]);
			boolean end = false; 
			boolean blocked = false;
			for(int i = 0; i < timeslice ; i++) {
				int x = pos+7+counter;

				System.out.println("Process "+ id  + " is executing instruction "+ memory[x] );

				if(execute(memory[pos+7+counter],id,pos)) {
					counter++;
					String[] bounds = memory[pos].split("-");
					if((pos+7+counter) <= (Integer.parseInt(bounds[1])  ) ) {
						memory[pos+2] = "" + (Integer.parseInt(memory[pos+2]) + 1);
					}
					else {
						memory[pos+3] = "finished";
						/// end 
						
						System.out.println("process " + id + " is finished");
						printQueues();
						printMem();
						end = true;
						break;
					}

				}
				else {
					System.out.println("Block process "+ id  );
					blockedQ.add(id);
					printQueues();
					memory[pos+3] = "blocked";
					blocked = true;
					break;
				}
			}
			if(!end && blocked == false) {
				readyQ.add(id);
				printQueues();
			}
		}	


	}

	private static void printQueues() {
		// TODO Auto-generated method stub

		System.out.print("Ready Queue :" );
		for(int i =0 ; i< readyQ.size() ; i ++) {
			int temp = readyQ.poll();
			System.out.print(" ["+temp + "]"   );
			readyQ.add(temp);
		}
		System.out.println("");

		System.out.print("Blocked Queue :" );
		for(int i =0 ; i< blockedQ.size() ; i ++) {
			int temp = blockedQ.poll();
			System.out.print(" ["+temp + "]"  );
			blockedQ.add(temp);
		}
		System.out.println("");

	}

	private static void fromdisktomem(int id) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader("Disk"));
		String line = br.readLine();
		String[] content = null;
		while(line != null) {
			content = line.split(",");
			if(content[0].equals(id + "")) {
				break;
			}
			line = br.readLine();
		}
		br.close();

		int size = content.length +1;
		int av = 0;
		int i;
		int diff = 0;
		boolean flag = false;

		for(i = 0 ; i < memory.length ; i++) {

			if( memory[i] == null) {
				av++ ;
			}
			else if(memory[i].contains("-")){
				String[] bounds = memory[i].split("-");
				if(memory[i+3].equals("finished")) {
					av += Integer.parseInt(bounds[1]) - Integer.parseInt(bounds[0]) + 1;
				}
				else {
					av = 0;
				}
				i = Integer.parseInt(bounds[1]);
			}
			else {
				av++;
			}
			if(av == size ) {
				flag = true;
				diff = 0;
				break;
			}
			else if(av > size) {
				flag = true;
				diff = av - size;
				break;
			}
		}

		if(flag) {
			System.out.println("Move process "+ id+ " to memory");
			i = i - size - diff + 1 ;
			//pcp  i to i+4
			inputs.put (Integer.parseInt(content[0]), i);
			int x = i + size - 1;
			memory[i] = i +"-"+ x  ; // Memory Boundaries
			i++;
			memory[i++] = content[0] + "" ;
			memory[i++] = content[1];  //  Program Counter
			memory[i++] = "ready"; //content[2] ; //Process State

			// var i+4 to i+7
			memory[i++] = content[3];
			memory[i++] = content[4];
			memory[i++] = content[5];

			//instructions i+7 to i+size

			for(int j = 6 ; j < content.length ; j ++) {
				memory[i++] = content[j];
			}
			FileReader Disk = new FileReader("Disk");
			BufferedReader br1 = new BufferedReader(Disk);
			FileWriter temp = new FileWriter("temp",true);
			BufferedWriter bw = new BufferedWriter(temp);
			String line1 = br1.readLine();
			String[] content1 = null;

			while(line1 != null) {
				content1 = line1.split(",");
				if(!content1[0].equals(id + "")) {
					bw.write(line1+ "\n");
				}
				line1 = br1.readLine();
			}
			br1.close();
			bw.close();

			File disk = new File("Disk");
			disk.delete();
			File Temp = new File("temp");
			Temp.renameTo(new File("Disk"));

		}

		else {
			frommemtodisk();
			fromdisktomem(id);
		}


	}

	private static void frommemtodisk() throws IOException {
		int i = 0;
		for(i = 0 ; i < memory.length ; i++) {
			if(memory[i] != null) {
				if(memory[i].contains("-")) {
					if(memory[i+3].equals("finished")) {
						String[] fbounds = memory[i].split("-");
						i = Integer.parseInt(fbounds[1]);
					}
					else {
						break;
					}
				}
			}
		}


		int x = i;
		String[] bounds = memory[i].split("-");

		//		for(int j = 0 ; j< memory.length ; j++) {
		//			System.out.print( j+" =  " +memory[j]+",");
		//		}
		//		System.out.println("bounds = "+ memory[i]);
		//		System.out.println("---------------------------------------------------------");

		int size = Integer.parseInt(bounds[1]) - Integer.parseInt(bounds[0]); 
		FileWriter disk = new FileWriter("Disk" , true);
		BufferedWriter br = new BufferedWriter(disk);
		String inss = "";
		for(int j = i+7 ; j <= i+size ; j ++) {
			inss = inss + "," + memory[j];
		}

		i++;
		System.out.println("Move process "+ memory[i] + " to disk");
		//br.write(ProcessID + "," + 0 + ",ready,null,null,null" + inss);
		inputs.remove(Integer.parseInt(memory[i]));
		br.write(memory[i++] + "," + memory[i++] + "," + memory[i++] + "," + memory[i++] + "," + memory[i++] + "," + memory[i++] + inss + "\n");
		br.close();
		int z = x+size ; 
		while(x <= z) {
			memory[x] = null;
			x++;
		}


	}

	private static boolean execute(String ins,int id,int start){

		boolean flag = true; 
		String[] content = ins.split(" ");

		cycles++;
		printMem();
		if(cycles == Scycles) {
			CodeParser(program2);
		}
		if(cycles == Tcycles) {
			CodeParser(program3);
		}
		if(ins.substring(0,3).equals("sem")) {
			if(ins.charAt(3)== 'W') {
				if(content[1].equals("userInput")) {
					if(flaguI) {
						userInput.add(id);
						readyQ.remove(id);
						return false;
					}
					else{
						System.out.println("Process " + id + " is requesting userInput");
						flaguI = true;
					}
				}
				else if(content[1].equals("userOutput")) {
					if(flaguO) {
						userOutput.add(id);
						readyQ.remove(id);
						return false;
					}
					else{
						System.out.println("Process " + id + " is requesting userOutput");
						flaguO = true;
					}
				}
				else if(content[1].equals("file")) {
					if(flagf) {
						file.add(id);
						readyQ.remove(id);
						return false;
					}
					else{
						System.out.println("Process " + id + " is requesting file");
						flagf = true;
					}
				}
			}
			else {

				if(content[1].equals("userInput")) {
					Integer s = userInput.poll();
					if(s != null) {
						blockedQ.remove(s);
						readyQ.add(s);
						if(inputs.containsKey(s)){
							int index = inputs.get(s) +3 ;
							memory[index] = "ready";
						}
					}
					flaguI = false;
				}
				else if(content[1].equals("userOutput")) {
					Integer s = userOutput.poll();
					if(s != null) {
						blockedQ.remove(s);
						readyQ.add(s);
						if(inputs.containsKey(s)){
							int index = inputs.get(s) +3 ;
							memory[index] = "ready";
						}
					}
					flaguO = false;
				}
				else if(content[1].equals("file")) {
					Integer s = file.poll();
					if(s != null) {
						blockedQ.remove(s);
						readyQ.add(s);
						if(inputs.containsKey(s)){
							int index = inputs.get(s) +3 ;
							memory[index] = "ready";
						}
					}
					flagf = false;
				}
			}
		}
		else if(content[0].equals("print")){
			if(content[1].equals(memory[start+4].substring(0, 1))) {
				System.out.println(memory[start+4].substring(2));
			}
			else if(content[1].equals(memory[start+5].substring(0, 1))) {
				System.out.println(memory[start+5].substring(2));
			}
			else if(content[1].equals(memory[start+6].substring(0, 1))) {
				System.out.println(memory[start+6].substring(2));
			}
		}

		else if(content[0].equals("printFromTo")) {
			int var1 = 0;
			for(int i = start+4 ; i < start+7 ; i++) {
				if(content[1].equals(memory[i].substring(0, 1))) {
					var1 = Integer.parseInt(memory[i].substring(2));
				}
			}
			int var2 = 0;
			for(int i = start+4 ; i < start+7 ; i++) {
				if(content[2].equals(memory[i].substring(0, 1))) {
					var2 = Integer.parseInt(memory[i].substring(2));
				}
			}

			var1++ ;
			while(var1 < var2) {
				System.out.println(var1++);
			}

		}

		else if(content[0].equals("writeFile")) {
			try {
				String s= "" ;
				for(int i = start+4 ; i < start+7 ; i++) {
					if(content[1].equals(memory[i].substring(0, 1))) {
						s += memory[i].substring(2);
						break ;
					}
				}

				PrintWriter br = new PrintWriter(new FileWriter(s) , true);
				for(int i = start+4 ; i < start+7 ; i++) {
					if(content[2].equals(memory[i].substring(0, 1))) {
						br.write(memory[i].substring(2));
						break;
					}

				}
				br.close();
			} catch (IOException e) {
				System.out.println("no file with name " + content[1]);
			}	
		}

		else if(content[0].equals("assign")) {
			if(content[2].equals("input")) {
				System.out.println("Please enter a value");
				Scanner sc = new Scanner(System.in);
				for(int i = start+4 ; i < start+7 ; i++) {
					if(memory[i].equals("null")) {
						memory[i] = content[1] + "=" + sc.next();
						break;
					}
				}
				cycles++;
				printMem();
				if(cycles == Scycles) {
					CodeParser(program2);
				}
				if(cycles == Tcycles) {
					CodeParser(program3);
				}
			}

			else if(content[2].equals("readFile")) {
				String s = "";
				cycles++;
				printMem();

				if(cycles == Scycles) {
					CodeParser(program2);
				}
				if(cycles == Tcycles) {
					CodeParser(program3);
				}
				String s1= "" ;

				for(int i = start+4 ; i < start+7 ; i++) {
					if(content[3].equals(memory[i].substring(0, 1))) {
						s1 += memory[i].substring(2);
						break ;
					}
				}

				try {

					BufferedReader br = new BufferedReader(new FileReader(s1));
					String line = br.readLine() ;
					while(line != null) {
						s += line + " ";
						line = br.readLine() ;
					}

					br.close();
				} catch(IOException e) {
					System.out.println("no file with name " + s1);
					return true;
				}
				for(int i = start+4 ; i < start+7 ; i++) {
					if(memory[i].equals("null")) {
						memory[i] = content[1] + "=" + s;
						break ;
					}
				}

			}
			else {
				for(int i = start+4 ; i < start+7 ; i++) {
					if(memory[i].equals("null")) {
						memory[i] = content[1] + "=" + content[2];
						break;
					}
				}
			}
			//			else {
			//				for(int i = start+4 ; i < start+7 ; i++) {
			//					if(content[2].equals(memory[i].substring(0, 1))) {
			//						memory[i] = content[2] + "=" + ;
			//					}
			//				}
			//			}
		}
		return flag;
	}


	private static void printMem(){

		System.out.println("Memory in cycle "+ cycles + ":" );
		System.out.print("[ ");
		for(int i =0 ; i < memory.length ; i++) {
			if(i==39)
				System.out.print(i + " = "+ memory[i]);
			else
				System.out.print(i + " = "+ memory[i]+" ,");
			if(i% 5 == 0 && i !=0)
				System.out.println();
		}
		System.out.println("]");
		System.out.println("");
	}

	private static void os() {
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Please enter timeslice ");
		
		timeslice =Integer.parseInt(sc.nextLine()) ;
		
		System.out.println("Please enter 1st program name");
		program1= sc.nextLine();
		System.out.println("Please enter 2nd program name");
		program2= sc.nextLine();
		System.out.println("Please enter 3rd program name");
		program3= sc.nextLine();
		
		// 2 Program_1.txt Program_2.txt  Program_3.txt  2 4   10  20  
		System.out.println("Please enter 2nd program receive cycle");
		Scycles = sc.nextInt();
		System.out.println("Please enter 3rd program receive cycle");
		Tcycles = sc.nextInt();
//		program1 = "Program_1.txt";
//		program2 = "Program_2.txt";
//		program3 = "Program_3.txt";
		System.out.println("Time slice = "+ timeslice);
		while(!readyQ.isEmpty() || ProcessID <= 1 ) {
			if(cycles == 0) {
				CodeParser(program1);
				
			}
			scheduler();
		}

	}


	public static void main(String[] args) {
		Main.os();






	}
}
