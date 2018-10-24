import org.jfree.ui.RefineryUtilities;
import java.util.*;

public class simulator {
	
	/*-------------------------random------------------------------*/
	
	public static int random_CPU_request_producer(){
		//return(0~47)
		Random random = new Random();	
		int ran=random.nextInt();
		if(ran<0){
			ran=-ran;
		}
		System.out.println("ran:"+ran%48);
		return ran%48;		
	}

	public static int random_in_set_alloc_producer(){
		//return(0~3)
		Random random = new Random();
		int ran=random.nextInt();
		if(ran<0){
			ran=-ran;
		}
		return ran%4;
	}
	
	public static int random_seed(int seed){
		//return(0~seed)
		Random random = new Random();
		int ran=random.nextInt();
		if(ran<0){
			ran=-ran;
		}
		return ran%seed;
	}
	
	public static int threshold(int ncm){
		//return priority;
		return ncm/30;
	}

	/*--------------------------MCT-----------------------------*/

	public static void init_MCT_r(MCT[] mct_r){
		//init MCT_r
		for(int i=0;i<4;i++){
			mct_r[i].index=i;
			mct_r[i].tag=-1;
		}
	}

	public static void init_MCT_rp(MCT[] mct_rp){
		//init MCT_rp
		for(int i=0;i<4;i++){
			mct_rp[i].index=i;
			mct_rp[i].tag=-1;
		}
	}

	/*--------------------------memory-----------------------------*/

	public static void init_memory_r(Memory[] memory_r, int[] ncm_per_cycle_r){ //used for first cycle
		//init memory_r
		for(int i=0;i<48;i++){
			memory_r[i].address=i;
			memory_r[i].content="S";
			memory_r[i].ncm=0;
			memory_r[i].priority=0;
		}
		for(int i=0;i<100;i++){
			ncm_per_cycle_r[i]=0;
		}
	}

	public static void init_memory_rp(Memory[] memory_rp, int[] ncm_per_cycle_rp){ //used for first cycle
		//init memory_rp
		for(int i=0;i<48;i++){
			memory_rp[i].address=i;
			memory_rp[i].content="S";
			memory_rp[i].ncm=0;
			memory_rp[i].priority=0;
		}
		for(int i=0;i<100;i++){
			ncm_per_cycle_rp[i]=0;
		}
	}

	public static void update_memory_r(Memory[] memory_r, int[] ncm_per_cycle_r,int cycle_num){ //used for memory_r
		//update memory_r
		for(int i=0;i<48;i++){
			ncm_per_cycle_r[cycle_num]+=memory_r[i].ncm;
			memory_r[i].ncm=0;
		}
	}

	public static void update_memory_rp(Memory[] memory_rp, int[] ncm_per_cycle_rp,int cycle_num){ //used for memory_r
		//update memory_rp
		for(int i=0;i<48;i++){
			ncm_per_cycle_rp[cycle_num]+=memory_rp[i].ncm;
			memory_rp[i].priority=threshold(memory_rp[i].ncm);
			memory_rp[i].ncm=0;
		}
	}

	/*---------------------------cache----------------------------*/


	public static void init_cache_r(Cache[] cache_r){
		//init cache_r
		for(int i=0;i<16;i++){
			cache_r[i].index=i;
			cache_r[i].address=-1;
			cache_r[i].content="NULL";
			cache_r[i].priority=0;
		}
	}

	public static void init_cache_rp(Cache[] cache_rp){
		//init cache_rp
		for(int i=0;i<16;i++){
			cache_rp[i].index=i;
			cache_rp[i].address=-1;
			cache_rp[i].content="NULL";
			cache_rp[i].priority=0;
		}
	}

	public static void update_cache_r(){
		//update cache_r
	}

	public static void update_cache_rp(Cache[] cache_rp, Memory[] memory_rp){
		//update cache_rp
		for(int i=0;i<16;i++){
			for(int j=0;j<48;j++){
				if(cache_rp[i].address==memory_rp[j].address){
					cache_rp[i].priority=memory_rp[j].priority;
				}
			}
		}
	}
	

	/*------------------------request check-------------------------------*/

	public static void request_check_cache_r(int request, int index_set, Cache[] cache_r,Memory[] memory_r,MCT[] mct_r,int[]nm_per_cycle_r, int cycle_num){
		//process the request from CPU
		int hit=0;
		for(int i=(index_set)*4;i<(index_set+1)*4;i++){
			if(request==cache_r[i].address){
				hit=1;	//hit in cache_r
				System.out.println(">>>>>Hit in cache_r!");
			}
		}
		if(hit==0){     //miss in cache_r
			nm_per_cycle_r[cycle_num]++;
			judge_miss_r(request,index_set,memory_r,mct_r);
			alloc_cache_r(request, index_set,memory_r, cache_r, mct_r);
		}
		
	}

	public static void request_check_cache_rp(int request, int index_set,Cache[] cache_rp,Memory[] memory_rp,MCT[] mct_rp,int[] nm_per_cycle_rp, int cycle_num){
		//process the request from CPU
		int hit=0;
		for(int i=index_set*4;i<(index_set+1)*4;i++){
			if(request==cache_rp[i].address){
				hit=1;	//hit in cache_rp
				System.out.println(">>>>>Hit in cache_rp!");
			}
		}
		if(hit==0){		//miss in cache_rp
			nm_per_cycle_rp[cycle_num]++;
			judge_miss_rp(request,index_set,memory_rp,mct_rp);
			alloc_cache_rp(request, index_set,cache_rp, memory_rp,mct_rp);
		}
	}

	public static void alloc_cache_r(int request, int index_set,Memory[] memory_r, Cache[] cache_r, MCT[] mct_r){
		//randomly choose the replaced cache line and allocate
		int replace_index=index_set*4+random_in_set_alloc_producer();
		mct_r[index_set].tag=cache_r[replace_index].address;
		
		cache_r[replace_index].address=memory_r[request].address;
		cache_r[replace_index].content=memory_r[request].content;
		cache_r[replace_index].priority=memory_r[request].priority;
	}

	public static void alloc_cache_rp(int request, int index_set, Cache[] cache_rp, Memory[] memory_rp, MCT[] mct_rp){
		//using priority to choose the replaced cache line and allocate
		int num=0,min=0,random_in_set=0,replace_index=0;
		int[][] tmp = new int[4][2];
		
		for(int i=0;i<4;i++){
			tmp[i][0]=index_set*4+i;
			tmp[i][1]=cache_rp[index_set*4+i].priority;
		}
		min=tmp[0][1];
		for(int j=0;j<4;j++){   //find the minimum in all priorities
			if(min>tmp[j][1]){
				min=tmp[j][1];
			}
		}
		for(int k=0;k<4;k++){
			if(min==tmp[k][1]){
				num++;
			}
		}
		
		random_in_set=random_seed(num)+1;
		for(int m=0;m<4;m++){		//randomly choose the one in all cache lines with same minimal priority
			if(min==cache_rp[index_set*4+m].priority){
				if(random_in_set==1){
					replace_index=index_set*4+m;
					break;
				}
				else{
					random_in_set--;
				}
			}
		}
		mct_rp[index_set].tag=cache_rp[replace_index].address;
		cache_rp[replace_index].address=memory_rp[request].address;
		cache_rp[replace_index].content=memory_rp[request].content;
		cache_rp[replace_index].priority=memory_rp[request].priority;		
	}
	
	/*------------------------miss judge-------------------------------*/



	public static void judge_miss_r(int address, int index_set, Memory[] memory_r, MCT[] mct_r){
		//judge conflict miss
		if(address==mct_r[index_set].tag){
			System.out.println("######Conflict Miss in MCT_r !");
			memory_r[address].ncm++;
		}
		else{
			System.out.println("***Miss in MCT_r !");
		}
	}
	
	public static void judge_miss_rp(int address, int index_set,Memory[] memory_rp, MCT[] mct_rp){
		//judge conflict miss
		if(address==mct_rp[index_set].tag){
			System.out.println("######Conflict Miss in MCT_rp !");
			memory_rp[address].ncm++;
		}
		else{
			System.out.println("***Miss in MCT_rp !");
		}
	}
	
	/*---------------------------statistics-----------------------------------*/
	
	public static void stat(int cycle_num,int[] ncm_per_cycle_r,int[] ncm_per_cycle_rp,int[] nm_per_cycle_r,int[] nm_per_cycle_rp,float[] prop_per_cycle_r,float[] prop_per_cycle_rp){
		//do statistics for result
		for(int i=0;i<cycle_num+1;i++){
			prop_per_cycle_r[i]=(float)ncm_per_cycle_r[i]/(float)nm_per_cycle_r[i]*(float)100.0;
			prop_per_cycle_rp[i]=(float)ncm_per_cycle_rp[i]/(float)nm_per_cycle_rp[i]*(float)100.0;
		}
	}
	

	/*------------------------display function in console-------------------------------*/

	public static void display_memory_r(Memory[] memory_r){
		System.out.println("\n>>>---Memory -r --------------------------------------");
		System.out.println("[Address][Content][ncm][Priority]");
		for(int i=0;i<10;i++){
			System.out.println(" ["+memory_r[i].address+"] "+" ["+memory_r[i].content+"] "+" ["+memory_r[i].ncm+"] "+" ["+memory_r[i].priority+"]");
		}
		for(int i=10;i<48;i++){
				System.out.println("["+memory_r[i].address+"] "+" ["+memory_r[i].content+"] "+" ["+memory_r[i].ncm+"] "+" ["+memory_r[i].priority+"]");
		}
		System.out.println(">>>---------------------------------------------------");
	}


	public static void display_memory_rp(Memory[] memory_rp){
		System.out.println("\n>>>---Memory -rp --------------------------------------");
		System.out.println("[Address][Content][ncm][Priority]");
		for(int i=0;i<10;i++){
			System.out.println(" ["+memory_rp[i].address+"] "+" ["+memory_rp[i].content+"] "+" ["+memory_rp[i].ncm+"] "+" ["+memory_rp[i].priority+"]");
		}
		for(int i=10;i<48;i++){
				System.out.println("["+memory_rp[i].address+"] "+" ["+memory_rp[i].content+"] "+" ["+memory_rp[i].ncm+"] "+" ["+memory_rp[i].priority+"]");
		}
		System.out.println(">>>---------------------------------------------------");
	}

	public static void display_cache_r(Cache[] cache_r){
		System.out.println("\n>>>---Cache -r --------------------------------------");
		System.out.println("[Index][Address][Content][Priority]");
		for(int i=0;i<10;i++){
			System.out.println(" ["+cache_r[i].index+"] "+" ["+cache_r[i].address+"] "+" ["+cache_r[i].content+"] "+" ["+cache_r[i].priority+"]");
		}
		for(int i=10;i<16;i++){
				System.out.println("["+cache_r[i].index+"] "+" ["+cache_r[i].address+"] "+" ["+cache_r[i].content+"] "+" ["+cache_r[i].priority+"]");
		}
		System.out.println(">>>---------------------------------------------------");
	}

	public static void display_cache_rp(Cache[] cache_rp){
		System.out.println("\n>>>---Cache -rp --------------------------------------");
		System.out.println("[Index][Address][Content][Priority]");
		for(int i=0;i<10;i++){
			System.out.println(" ["+cache_rp[i].index+"] "+" ["+cache_rp[i].address+"] "+" ["+cache_rp[i].content+"] "+" ["+cache_rp[i].priority+"]");
		}
		for(int i=10;i<16;i++){
				System.out.println("["+cache_rp[i].index+"] "+" ["+cache_rp[i].address+"] "+" ["+cache_rp[i].content+"] "+" ["+cache_rp[i].priority+"]");
		}
		System.out.println(">>>---------------------------------------------------");
	}

	public static void display_MCT_r(MCT[] mct_r){
		System.out.println("\n>>>---MCT -r -----------------------------------------");
		System.out.println("[Index]  [Tag]");
		for(int i=0;i<4;i++){
				System.out.println("["+mct_r[i].index+"] "+" ["+mct_r[i].tag+"]");
		}
		System.out.println(">>>---------------------------------------------------");
	}

	public static void display_MCT_rp(MCT[] mct_rp){
		System.out.println("\n>>>---MCT -rp -----------------------------------------");
		System.out.println("[Index]  [Tag]");
		for(int i=0;i<4;i++){
				System.out.println("["+mct_rp[i].index+"] "+" ["+mct_rp[i].tag+"]");
		}
		System.out.println(">>>---------------------------------------------------");
	}
	
	public static void display_trend_r(int cycle_num, int[] ncm_per_cycle_r, int[] nm_per_cycle_r, float[] prop_per_cycle_r){
		System.out.println(">>>---Conflict Miss Trends -r ------------------------");
		for(int i=0;i<cycle_num+1;i++){
			System.out.print("->"+ncm_per_cycle_r[i]);
		}
		System.out.print("\n");
		for(int i=0;i<cycle_num+1;i++){
			System.out.print("->"+nm_per_cycle_r[i]);
		}
		System.out.print("\n");
		for(int i=0;i<cycle_num+1;i++){
			System.out.print("->"+prop_per_cycle_r[i]+"%");
		}
		System.out.print("\n");
		System.out.println(">>>---------------------------------------------------");
	}
	
	public static void display_trend_rp(int cycle_num, int[] ncm_per_cycle_rp, int[] nm_per_cycle_rp, float[] prop_per_cycle_rp){
		System.out.println(">>>---Conflict Miss Trends -rp ------------------------");
		for(int i=0;i<cycle_num+1;i++){
			System.out.print("->"+ncm_per_cycle_rp[i]);
		}
		System.out.print("\n");
		for(int i=0;i<cycle_num+1;i++){
			System.out.print("->"+nm_per_cycle_rp[i]);
		}
		System.out.print("\n");
		for(int i=0;i<cycle_num+1;i++){
			System.out.print("->"+prop_per_cycle_rp[i]+"%");
		}
		System.out.print("\n");
		System.out.println(">>>---------------------------------------------------");
	}

	/*------------------------main function-------------------------------*/
	
	public static void main(String[] args){
		
		int[] ncm_per_cycle_r = new int[100]; //total number of conflict miss -r
		int[] ncm_per_cycle_rp = new int[100]; //total number of conflict miss -rp
		
		int[] nm_per_cycle_r = new int[100]; //total number of miss -r
		int[] nm_per_cycle_rp = new int[100]; //total number of miss -rp
		
		float[] prop_per_cycle_r = new float[100]; //proportion of conflict miss -r
		float[] prop_per_cycle_rp = new float[100]; //proportion of conflict miss -rp
		
		int Continue=0; //loop's number of running 
		int cycle_num=0; //record number of cycles

		Memory[] memory_r= new Memory[48];
		Memory[] memory_rp=new Memory[48];
		
		for(int i=0;i<48;i++){
			memory_r[i]=new Memory();
			memory_rp[i]=new Memory();
		}

		Cache[] cache_r = new Cache[16];
		Cache[] cache_rp = new Cache[16];
		
		for(int i=0;i<16;i++){
			cache_r[i]=new Cache();
			cache_rp[i]=new Cache();
		}

		MCT[] mct_r = new MCT[4];
		MCT[] mct_rp = new MCT[4];
		
		for(int i=0;i<4;i++){
			mct_r[i]=new MCT();
			mct_rp[i]=new MCT();
		}		
		
		init_memory_r(memory_r, ncm_per_cycle_r);
		init_memory_rp(memory_rp, ncm_per_cycle_rp);
		
		init_cache_r(cache_r);
		init_cache_rp(cache_rp);
		
		init_MCT_r(mct_r);
		init_MCT_rp(mct_rp);
		
		
		
		int request; //cpu request
		int index_set; //request's cache set index
		
		long start_time; //to calculate time of running per cycle
		long end_time;
		
		Continue=30; //running 30 cycles
		
		//draw chart
		LineChart lineChart = new LineChart("Trends for Proportion of Conflict miss",prop_per_cycle_r,prop_per_cycle_rp,cycle_num);

		while(Continue>0){
			start_time=System.currentTimeMillis();
			for(int i=0;i<500000;i++){ //each cycle process 500,000 requests from cpu
				request = random_CPU_request_producer();
				index_set=request%4;
				
				// request for memory_r
				request_check_cache_r(request, index_set,cache_r,memory_r,mct_r,nm_per_cycle_r,cycle_num);
				
				// request for memory_rp
				request_check_cache_rp(request, index_set,cache_rp,memory_rp, mct_rp,nm_per_cycle_rp,cycle_num);			
				
			}
			
			end_time=System.currentTimeMillis();
	
			//update memory and cache
			update_memory_r(memory_r, ncm_per_cycle_r,cycle_num);
			update_memory_rp(memory_rp, ncm_per_cycle_rp,cycle_num);
			
			update_cache_r();
			update_cache_rp(cache_rp, memory_rp);
			
			//statistic
			stat(cycle_num,ncm_per_cycle_r,ncm_per_cycle_rp,nm_per_cycle_r,nm_per_cycle_rp,prop_per_cycle_r,prop_per_cycle_rp);
			
			//display
			display_memory_r(memory_r);
			display_cache_r(cache_r);
			display_MCT_r(mct_r);
			
			display_memory_rp(memory_rp);
			display_cache_rp(cache_rp);
			display_MCT_rp(mct_rp);
			
			System.out.println("**************** Result *******************");
			System.out.println();
			System.out.println(">>{This Cycle Running Time: "+(long)(end_time-start_time)/1000+"s}");
			System.out.println();
			display_trend_r(cycle_num,ncm_per_cycle_r,nm_per_cycle_r,prop_per_cycle_r);
			display_trend_rp(cycle_num,ncm_per_cycle_rp,nm_per_cycle_rp,prop_per_cycle_rp);
			System.out.println("*******************************************");
			
			// update and draw chart
			lineChart.updateChart(prop_per_cycle_r, prop_per_cycle_rp, cycle_num);
			lineChart.pack();
			RefineryUtilities.centerFrameOnScreen(lineChart);
			lineChart.setVisible(true);
			
			cycle_num++;
		
			Continue--;
		}
	}
}
