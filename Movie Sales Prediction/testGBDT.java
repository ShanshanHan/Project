import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.text.*;

// faxing, zhubian, zhuyan, daoyan预测score，dailyScreen, dailyScreenPercent

public class testGBDT
{
	public static void main(String[] args) throws IOException
	{
//		addPeople();

//		getAttrFile("dailyScreen");
//		getAttrFile("dailyScreenPct");
//		getAttrFile("score");
//		getAttrFile("box");
//		String treeFile = "tree_dailyScreen.csv";
//		predictValue("dailyScreen");
//		predictValue("dailyScreenPct");
//		predictValue("score");
//		predictBox();
	}
	
	public static int predictBox() throws IOException{
		String treeFile = "tree_box.csv";
		String inFile = "movieInfo_box.csv";
//		String outFile = "predit_" + attrName + ".csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		BufferedReader in2;
		String s;
		String releaseDate, zhuyan, zhizuo, faxing, daoyan, dailyScreen, score;
		String temp[], temp2[];
		s = in.readLine();
		String s2;
//		attrs[0] = 3;//dailyScreen
//		attrs[1] = 4;//dailyScreenPct
//		attrs[2] = 5;//themeScore
//		attrs[3] = 6;//score
//		attrs[4] = 7;//zhuyan
//		attrs[5] = 8;//zhizuo
//		attrs[6] = 9;//faxing
//		attrs[7] = 10;//daoyan
//		attrs[8] = 1;//dateScore
		String fatherAttr = "", fatherAttrVal = "", fileName = "";
		double rootSplitVal = 0, predictVal = 0;
		int rootSplitAttr;
		String LorR = "", resTree = "0";
		while((s = in.readLine())!=null){
			temp = s.split(",");
			releaseDate = temp[1];
			zhuyan = temp[2];
			zhizuo = temp[3];
			faxing = temp[4];
			daoyan = temp[5];
			dailyScreen = temp[6];
			score = temp[7];
			fatherAttrVal = "";
			fatherAttr = "";
			predictVal = 0;
			in2 = new BufferedReader(new FileReader(treeFile));
			s2 = in2.readLine();
			while((s2 = in2.readLine())!=null){
				temp2 = s2.split(",");//树生成的每一条记录
				if(!temp2[0].equals(fileName)){
					predictVal += Double.valueOf(resTree);
					fileName = temp2[0];
					fatherAttr = temp2[1];
					fatherAttrVal = temp2[2];
				}
				
				if((temp2[1].equals(fatherAttr) 
						&& temp2[2].equals(fatherAttrVal))
						||(fatherAttr.equals("") 
						&& fatherAttrVal.equals(""))){
					if(temp2[1].equals("4")){//zhuyan
						if(Double.valueOf(zhuyan)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("5")){//zhizuo
						if(Double.valueOf(zhizuo)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("6")){//zhizuo
						if(Double.valueOf(faxing)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("7")){//daoyan
						if(Double.valueOf(daoyan)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("8")){//releaseDay
						if(Double.valueOf(releaseDate)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("0")){//dailyScreen
						if(Double.valueOf(dailyScreen)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("3")){//score
						if(Double.valueOf(score)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					
					if(temp2[3].equals(LorR)){
						fatherAttr = temp2[4];
						fatherAttrVal = temp2[5];
						if(temp2[1].equals("4")){//zhuyan
							if(Double.valueOf(zhuyan)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(temp2[1].equals("5")){//zhizuo
							if(Double.valueOf(zhizuo)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(temp2[1].equals("6")){//faxing
							if(Double.valueOf(faxing)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(temp2[1].equals("7")){//daoyan
							if(Double.valueOf(daoyan)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(temp2[1].equals("8")){//releaseDay
							if(Double.valueOf(releaseDate)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(temp2[1].equals("0")){//dailyScreen
							if(Double.valueOf(dailyScreen)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(temp2[1].equals("3")){//score
							if(Double.valueOf(score)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
					}
				}
			}
			in2.close();
			System.out.println(predictVal);	
		}
		
		
		in.close();
		return 0;
	}
	
	public static int predictValue(String attrName) throws IOException{
		String treeFile = "tree_" + attrName + ".csv";
		String inFile = "movieInfo_" + attrName + ".csv";
		String outFile = "predit_" + attrName + ".csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		BufferedReader in2;
		String s;
		String releaseDate, zhuyan, zhizuo, faxing, daoyan;
		String temp[], temp2[];
		s = in.readLine();
		String s2;
//		attrs[0] = -5;//theme
//		attrs[1] = 7;//zhuyan
//		attrs[2] = -8;//zhizuo
//		attrs[3] = 9;//faxing
//		attrs[4] = 10;//daoyan
//		attrs[5] = 1;//releaseDay score
		String fatherAttr = "", fatherAttrVal = "", fileName = "";
		double rootSplitVal = 0, predictVal = 0;
		int rootSplitAttr;
		String LorR = "", resTree = "0";
		while((s = in.readLine())!=null){
			temp = s.split(",");
			releaseDate = temp[1];
			zhuyan = temp[2];
			zhizuo = temp[3];
			faxing = temp[4];
			daoyan = temp[5];
			fatherAttrVal = "";
			fatherAttr = "";
			predictVal = 0;
			in2 = new BufferedReader(new FileReader(treeFile));
			s2 = in2.readLine();
			while((s2 = in2.readLine())!=null){
				temp2 = s2.split(",");//树生成的每一条记录
				if(!temp2[0].equals(fileName)){
					predictVal += Double.valueOf(resTree);
					fileName = temp2[0];
					fatherAttr = temp2[1];
					fatherAttrVal = temp2[2];
				}
				
				if((temp2[1].equals(fatherAttr) 
						&& temp2[2].equals(fatherAttrVal))
						||(fatherAttr.equals("") 
						&& fatherAttrVal.equals(""))){
					if(temp2[1].equals("1")){//zhuyan
						if(Double.valueOf(zhuyan)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("2")){//zhizuo
						if(Double.valueOf(zhizuo)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("3")){//faxing
						if(Double.valueOf(faxing)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("4")){//daoyan
						if(Double.valueOf(daoyan)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[1].equals("5")){//releaseDay
						if(Double.valueOf(releaseDate)<=Double.valueOf(temp2[2])){
							LorR = "L";
						}else LorR = "R";
					}
					if(temp2[3].equals(LorR)){
						fatherAttr = temp2[4];
						fatherAttrVal = temp2[5];
						if(fatherAttr.equals("1")){//zhuyan
							if(Double.valueOf(zhuyan)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(fatherAttr.equals("2")){//zhizuo
							if(Double.valueOf(zhizuo)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(fatherAttr.equals("3")){//faxing
							if(Double.valueOf(faxing)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(fatherAttr.equals("4")){//daoyan
							if(Double.valueOf(daoyan)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
						if(fatherAttr.equals("5")){//releaseDay
							if(Double.valueOf(releaseDate)<=Double.valueOf(fatherAttrVal)){
								resTree = temp2[6];
							}else resTree = temp2[7];
						}
					}
				}
			}
			in2.close();
			System.out.println(predictVal);	
		}
		in.close();
		return 0;
	}
	
	public static int getAttrFile(String attrName) throws IOException{
		String inFile = "movieInfo.csv";
		String peopleFile1 = "zhuyan.csv";
		String peopleFile2 = "zhizuo.csv";
		String peopleFile3 = "faxing.csv";
		String peopleFile4 = "daoyan.csv";
		String outFile = "movieInfo_" + attrName + ".csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		FileWriter writer = new FileWriter(outFile, true);
		BufferedWriter out = new BufferedWriter(writer);
		String zhuyan = "-", zhizuo = "-", faxing = "-", bianju = "-", daoyan = "-";
		String s, s2, lineOut;
		String temp[];
		writer.write("name, date, 主演, 制作, 发行, 导演\r\n");
		in.readLine();//title;
		while((s = in.readLine())!=null){
			temp = s.split(",");//"movie,date,主演,制作,发行,导演\r\n"
			zhuyan = getPeopleScore(temp[2], "zhuyan");
			zhizuo = getPeopleScore(temp[3], "zhizuo");
			faxing = getPeopleScore(temp[4], "faxing");
			daoyan = getPeopleScore(temp[5], "daoyan");
			lineOut = temp[0] + "," //name
				 	+ temp[1] + "," //date
				    + zhuyan + ","
				    + zhizuo + ","
				    + faxing + ","
				    + daoyan + "\r\n"; 
			writer.write(lineOut);
		}
		in.close();
		out.close();
		writer.close();
		return 0;
	}
	
	public static String getPeopleScore(String names, String relation) throws IOException{//names: temp[6]等
		BufferedReader in;
		String inFile = relation + ".csv";
		String temp2[];
		String s;
		int i;
		double res_temp = 1;
		double res_sum_temp = 0;
		String temp[];
		String scoreStr = "-";
		String scores[];
		String score = "-", defaultValue = "";
		String temp3[];
		double avg = 0;
		int counter = 0;
		
		//计算默认值
		in = new BufferedReader(new FileReader(inFile));
		while((s = in.readLine())!=null){
			temp3 = s.split(",");
			avg += Double.valueOf(temp3[1]);
			counter++;
		}
		in.close();
		defaultValue = "" + (avg/counter);
		if(!names.equals("-")){
			temp2 = names.split("/");
			in = new BufferedReader(new FileReader(inFile));
			int counter_find = 0;
				
			while((s = in.readLine())!=null){
				temp = s.split(",");//temp[0]人名  temp[1]score
				for(i = 0; i < temp2.length; i++){//用temp2[i]在infile中找
					if(temp2[i].equals(temp[0])){
						counter_find++;
						
						if(scoreStr.equals("-")){
							scoreStr = temp[1];
						}else{
							scoreStr = scoreStr + "/" +temp[1];
						}
					}
				}
			}
			in.close();
			if(counter_find == 0){
				counter_find++;
				scoreStr = defaultValue;
			}
			for(i = counter_find; i < temp2.length; i++){
				scoreStr = scoreStr + "/" + defaultValue;
			}
			scores = scoreStr.split("/");
			for(i = 0; i < scores.length; i++){
				res_sum_temp += Double.valueOf(scores[i]);
				
			}
			score = res_sum_temp / scores.length + "";
		}
		else {score = defaultValue;}
	
		return score;
	}
	
	//第i个属性值
	public static double getAvgAttr(int i, String fileName) throws IOException{
		double avg = 0;
		double valueSum = 0;
		int counter = 0;
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String s;
		String temp[];
		double value;
		s = in.readLine();//title
		
		while((s=in.readLine())!=null){
			temp = s.split(",");
			if(!temp[i].equals("-")){
				value = Double.valueOf(temp[i]);
				valueSum += value;
				counter ++;
			}
		}
		in.close();
		avg = valueSum / counter;
		return avg;
	}

	
	public static int addPeople() throws IOException{
		String inFile = "uncoming_metadata.csv";
		String inFile2 = "uncoming_relation.csv";
		String outFile = "movieInfo.csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		BufferedReader in2;
		FileWriter writer = new FileWriter(outFile,true);
		BufferedWriter out = new BufferedWriter(writer);
		String s, s2;
		String temp[], temp2[];
		s = in.readLine();
		String movieName = "", zhuyan = "-", zhizuo = "-", zhizuoren = "-", faxing = "-", daoyan = "-", currentMovie;
		boolean isFind = false;
		String lineOut = "";
		writer.write("movie,date,主演,制作,发行,导演, 制作人\r\n");
		while((s = in.readLine())!= null){
			isFind = false;
			zhuyan = "-";
			zhizuo = "-";
			faxing = "-";
			daoyan = "-";
			zhizuoren = "-";
			temp = s.split(",");
			currentMovie = temp[0];
			in2 = new BufferedReader(new FileReader(inFile2));
			in2.readLine();
			while((s2 = in2.readLine())!=null){
				temp2 = s2.split(",");
				if(isFind && (!temp2[0].equals(currentMovie))){
					writer.write(lineOut);
					break;
				}
				if(temp2[0].equals(currentMovie)){
					isFind = true;
					if(temp2[2].equals("主演")){
						if(zhuyan.equals("-")){
							zhuyan = temp2[1];
						}else zhuyan = zhuyan + "/" + temp2[1];
					}
					if(temp2[2].equals("发行")){
						if(faxing.equals("-")){
							faxing = temp2[1];
						}else faxing = faxing + "/" + temp2[1];
					}
					if(temp2[2].equals("导演")){
						if(daoyan.equals("-")){
							daoyan = temp2[1];
						}else daoyan = daoyan + "/" + temp2[1];
					}
					if(temp2[2].equals("制作")){
						if(zhizuo.equals("-")){
							zhizuo = temp2[1];
						}else zhizuo = zhizuo + "/" + temp2[1];
					}
				}
				lineOut = s + "," + zhuyan + "," + zhizuo + ","
						+ faxing + "," + daoyan + "," + zhizuoren +"\r\n";
			
			}
			in2.close();
		}
//		lineOut = s + "," + zhuyan + "," + zhizuo + ","
//				+ faxing + "," + daoyan + "\r\n";
		System.out.println(lineOut);
		writer.write(lineOut);
		in.close();
		writer.close();
		out.close();
		return 0;
	}

}