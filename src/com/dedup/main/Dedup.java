package com.dedup.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dedup {

	
	static final Set<Integer> keys = new HashSet<Integer>();
	static int MATCH_THRESHOLD = 80;
	static HashMap<String,String> argsMap=new HashMap<String,String>();
	public static void main(String[] args) {
		BufferedReader br = null;
//		new CustomCompare().compare("WOOD", "WOOD");
		try{
			if(args.length<4)
				System.out.println("Arguments: -f <full filepath> -k <key index 1,key index 2,key index3...> -s <match score>");
			else{
				for(Integer i=0;i<args.length-1;i++){
					if(args[i].indexOf("-")==0 && args[i+1].indexOf("-")!=0){
						argsMap.put(args[i].substring(1), args[++i]);
					}
				}
				if(!argsMap.containsKey("f") || !argsMap.containsKey("k")){
					System.out.println("Arguments: -f <full filepath> -k <key index 1,key index 2,key index3...> -s <match score>");
					return;
				}
				if(argsMap.containsKey("s")){
					try{
						MATCH_THRESHOLD=Integer.valueOf(argsMap.get("s"));
					}catch(NumberFormatException ex){
						System.out.println("Input score is not an integer. Defaulting to 80..");
						MATCH_THRESHOLD=80;
					}
				}
				System.out.println("Using a match score of "+MATCH_THRESHOLD+"\n");
				
				br = new BufferedReader(new InputStreamReader(new FileInputStream(argsMap.get("f"))));
				//br = new BufferedReader(new InputStreamReader(new FileInputStream("F:/docs/Accounts.csv")));
				for(String key:argsMap.get("k").split(","))
					keys.add(Integer.valueOf(key));

					
				String line="";
				
				
				List<String> dataToCompare = new ArrayList<>();
				while((line=br.readLine())!=null){
					String[] columns=line.split(",");
					int i=1;line="";
					for(String column:columns){
						if(keys.contains(i))
							line += ","+column;
						i++;
					}
					if(line.length()>0)
						dataToCompare.add(line.substring(1));
				}
				CustomCompare compare = new CustomCompare();
			
				Collections.sort(dataToCompare);
				
				
				String preVal="";int i=0;
				String finalPreval="", finalVal = "";
				HashMap<Integer,String> dupRows = new HashMap<Integer,String>();
				for(String data: dataToCompare){
					String[] columns = data.split(",");
					String val = columns[0];
					int match=compare.compare(val, preVal);
					finalVal = val;
					finalPreval = preVal;int j =1;
					while(match > MATCH_THRESHOLD && j < columns.length){
						String subval = columns[j];
						String subprevVal = dataToCompare.get(i-1).split(",")[j++];
						match=compare.compare(subval, subprevVal);
						finalVal += " "+subval;
						finalPreval += " "+subprevVal;
					}
					if(match>MATCH_THRESHOLD){
						if(!dupRows.containsKey(i))
							dupRows.put(i, finalPreval);
						dupRows.put(i+1,finalVal);
					}else if(!dupRows.isEmpty()){
						//System.out.println("Possible duplicates on rows "+String.join(",", (String[])dupRows.toArray()));
						String header="Possible duplicates on ",content="";
						for(Integer rno:dupRows.keySet()){
							header+=rno+", ";
							content+=dupRows.get(rno)+"\n";
						}
						System.out.println(header.replaceAll(", $", "").replaceAll(",([^,]+)$", " &$1"));
						System.out.println(content);
						dupRows.clear();
					}
					preVal=val;i++;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(br!=null)
					br.close();
			}catch(Exception ex){
				
			}
		}

	}

}

class CustomCompare implements Comparator<String>{

	@Override
	public int compare(String o1, String o2) {
		o1=o1.replaceAll("(?i)industries", "").replaceAll("(?i)corporation", "").replaceAll("(?i)corp", "").
				replaceAll("(?i)private", "").replaceAll("(?i)pvt", "").replaceAll("(?i)limited", "").replaceAll("(?i)the", "").replaceAll("\\s", "");
		o2=o2.replaceAll("(?i)industries", "").replaceAll("(?i)corporation", "").replaceAll("(?i)corp", "").
				replaceAll("(?i)private", "").replaceAll("(?i)pvt", "").replaceAll("(?i)limited", "").replaceAll("(?i)the", "").replaceAll("\\s", "");
		if(o1.length()==0||o2.length()==0)
			return 0;
		int ln = Math.min(o1.length(), o2.length());
		
		char[] c1=o1.toCharArray();
		char[] c2=o2.toCharArray();
		int[] lengths=new int[c2.length];
		int[] prevlengths = new int[c2.length];
		int longestMatch=0;
		for(int i=0;i<c1.length;i++){
			for(int j=0;j<c2.length;j++)
				if(c1[i]!=c2[j])
					lengths[j]=0;
				else{
						lengths[j] = ((j-1)>=0)?prevlengths[j-1]+1:1;
						longestMatch = lengths[j] > longestMatch? lengths[j] : longestMatch;
				}
			prevlengths=lengths.clone();
		}
		return (longestMatch*100)/ln;
	}
	
}