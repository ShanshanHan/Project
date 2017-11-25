import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.text.*;

public class dealDataGBDT
{
	public static void main(String[] args) throws IOException, ParseException
	{
		//dealBox();
		//addTheme();
		//dealScore();
//		dealRelation();
//		dealPeople("编剧", "bianju");//计算每个演员的权重
//		dealPeople("主演", "zhuyan");//计算每个演员的权重
//		dealPeople("制作", "zhizuo");//计算每个演员的权重
//		dealPeople("制作人", "zhizuoren");//计算每个演员的权重
//		dealPeople("发行", "faxing");//计算每个演员的权重
//		dealPeople("导演", "daoyan");//计算每个演员的权重
//		insertPeople();
//		addScores();
//		dealPeopleScore();
//		dealMissingData();
	}
	
	public static int dataNormalization(String inFile, int column) throws IOException{
		String outFile = "total7_dataNormalization.csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		FileWriter writer = new FileWriter(outFile,true);
		BufferedWriter out = new BufferedWriter(writer);
		writer.write("movie, date, total, dailyScreen, dailyScreenPct, themeScore, score, zhuyan, zhizuo, faxing, daoyan\r\n");
		String temp[];
		String s, lineOut;
		double value;
		s = in.readLine();
		int i;
		while((s = in.readLine())!=null){
			lineOut = "";
			temp = s.split(",");
			value = Double.valueOf(temp[column]);
			value = scoreNormalization(value);
			for(i = 0; i < 10; i++){
				if(i != column){
					lineOut = lineOut + temp[i] + ",";
				}
				if(i == column){
					lineOut = lineOut + value + ",";
				}
			}
			if(column == 10){
				lineOut = lineOut + value + "\r\n";
			}else {lineOut = lineOut + temp[10] + "\r\n";}
			System.out.println(lineOut);
			writer.write(lineOut);
		}
		in.close();
		out.close();
		writer.close();
		
		return 0;
	}
	
	public static double scoreNormalization(double score){
		score = (score - 2.22) * (double)(10000 - 1000)/(9.3-2.22) + 1000;
		return score;
	}
	

	public static int addScores() throws IOException{//计算theme score, screen 等的默认值
		String inFile = "total4_addPeople.csv";
		String outFile = "total5_addScores.csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		String temp[], currentThemes[];
		//计算各类电影的权值；用该类电影的平均票房求得
		Vector themes = new Vector();
		double themeScore_avg[] = new double[100];
		int theme_counter[] = new int[100];
		String s;
		double avgScore = 0, currentScore, temp0;
		int index;
		int i, j;
		double totalSum = 0;
		int counter = 0;
		s = in.readLine();
		//name;date;box;dailyScreen;dailyScreenPct;themeScore;score;zhuyan;zhizuo;faxing;bianju;daoyan

		while((s = in.readLine())!= null){
			temp = s.split(",");
			if(!temp[5].equals("-")){
			currentThemes = temp[5].split("/");//电影类型
			for(i = 0; i < currentThemes.length; i++){
				//currentThemes = current
				index = themes.indexOf(currentThemes[i]);
				if(index == -1){
					themes.add(currentThemes[i]);
					themeScore_avg[themes.size()-1] = Double.valueOf(temp[2]);
					theme_counter[themes.size()-1] = 1;
				}
				else{
					themeScore_avg[index] += Double.valueOf(temp[2]);
					theme_counter[index]++;
				}
			}
		}
		}
		in.close();
		double sum_total = 0;
		int counter_total = 0;
		for(i = 0; i<themes.size(); i++){
			sum_total += themeScore_avg[i];
			counter_total += theme_counter[i];
		}
		//默认值，对应theme为"-"的电影
		double themeScore_default = sum_total/counter_total;
		for(i = 0; i < themes.size(); i++){
			//每一类电影的score
			themeScore_avg[i] = themeScore_avg[i]/theme_counter[i];
		}
		//----------存储每种类型电影的权值-------
		String lineOut;
		FileWriter writer = new FileWriter("themeWeight.csv",true);
		BufferedWriter out = new BufferedWriter(writer);
		for(i = 0; i < themes.size(); i++){
			lineOut = themes.elementAt(i) + "," + themeScore_avg[i] + "\r\n";
			writer.write(lineOut);
		}
		writer.write("default" + "," + themeScore_default + "\r\n");
		writer.close();
		out.close();
		//-------------------插入默认值------------------
		String dailyScreen_default = "" + getAvgAttr(3, inFile);
		String dailyScreenPct_default = "" + getAvgAttr(4, inFile);
		
		in = new BufferedReader(new FileReader(inFile));
		writer = new FileWriter(outFile,true);
		out = new BufferedWriter(writer);
		s = in.readLine();
		double themeScore = 0;
		String releaseDay = "";
		String movieName, total, dailyScreen, dailyScreenPct, theme, score;
		writer.write("movie, date, total, dailyScreen, dailyScreenPct, themeScore, score, zhuyan, zhizuo, faxing, bianju, daoyan\r\n");
		int k;
		double themeSum_temp = 0;
		
		while((s = in.readLine())!=null){
			themeSum_temp = 0;
			temp = s.split(",");
			if((!temp[6].equals("-"))){
//				if((!temp[6].equals("-"))&&(!temp[2].equals("0.1"))){
				movieName = temp[0];
				total = temp[2];
				releaseDay = temp[1];
				dailyScreen = temp[3];
				dailyScreenPct = temp[4];
				theme = temp[5];
				score = temp[6];
				if(dailyScreen.equals("-")){
					dailyScreen = dailyScreen_default;
				}
				if(dailyScreenPct.equals("-")){
					dailyScreenPct = dailyScreenPct_default;
				}
				//----------计算themeScore-----------
				if(theme.equals("-")){
					themeScore = themeScore_default;
				}else{
					currentThemes = theme.split("/");
					for(j = 0; j < currentThemes.length; j++){
						k = themes.indexOf(currentThemes[j]);
						themeSum_temp += themeScore_avg[k];
					}
					themeScore = themeSum_temp/currentThemes.length;
				}				
				writer.write(movieName + "," + releaseDay + ","
						+ total +","
						+ dailyScreen + ","
						+ dailyScreenPct + ","
						+ themeScore + ","
						+ score + "," 
						+ temp[7] + ","
						+ temp[8] + ","
						+ temp[9] + ","
						+ temp[10] + ","
						+ temp[11] + "\r\n");
			}
		}
		in.close();
		writer.close();
		out.close();
		return 0;
	}
	
	

	//由于编剧所含实例过少，该属性直接删除
	public static int dealPeopleScore() throws IOException{//对照bianju.csv等文件，计算themeScore,peopleScore
		String outFile = "total6_addPeopleScore.csv";
		BufferedReader in = new BufferedReader(new FileReader("total5_addScores.csv"));
		FileWriter writer = new FileWriter(outFile, true);
		BufferedWriter out = new BufferedWriter(writer);
		String zhuyan = "-", zhizuo = "-", faxing = "-", bianju = "-", daoyan = "-";
		String s, s2, lineOut;
		String temp[];
		writer.write("name, releaseDate, box, dailyScreen, dailyScreenPct, themeScore, score, zhuyan, zhizuo, faxing, daoyan\r\n");
		in.readLine();//title;
		double zhuyan_default = getAvgAttr(1, "zhuyan.csv"), 
				zhizuo_default = getAvgAttr(1, "zhizuo.csv"), 
				faxing_default = getAvgAttr(1, "faxing.csv"), 
				daoyan_default = getAvgAttr(1, "daoyan.csv");
		System.out.println(zhuyan_default+"------");
		System.out.println(zhizuo_default+"------");
		System.out.println(faxing_default+"------");
		System.out.println(daoyan_default+"------");
		
		while((s = in.readLine())!=null){
			temp = s.split(",");//"name, box, dailyScreen, dailyScreenPct, themeScore, score, zhuyan(6), zhizuo, faxing, bianju, daoyan\r\n"
			if(!temp[7].equals("-")){zhuyan = getPeopleScore(temp[7], "zhuyan", zhuyan_default);}
			else{zhuyan = zhuyan_default + "";}
			if(!temp[8].equals("-")){zhizuo = getPeopleScore(temp[8], "zhizuo", zhizuo_default);}
			else{zhizuo = zhizuo_default + "";}
			if(!temp[9].equals("-")){faxing = getPeopleScore(temp[9], "faxing", faxing_default);}
			else{faxing = faxing_default + "";}
//			if(!temp[10].equals("-")){bianju = getPeopleScore(temp[10], "bianju");}
			if(!temp[11].equals("-")){daoyan = getPeopleScore(temp[11], "daoyan", daoyan_default);}
			else{daoyan = daoyan_default + "";}
			lineOut = temp[0] + "," //name
				 	+ temp[1] + "," //date
				  	+ temp[2] + "," //box 
				  	+ temp[3] + ","//dailyScreen
				   	+ temp[4] + "," //dailyScreenPct
				    + temp[5] + "," //themeScore
				    + temp[6] + "," //score
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

	public static String getPeopleScore(String names, String relation, double defaultValue) throws IOException{//names: temp[6]等
		BufferedReader in;
		String inFile = relation + ".csv";
		String temp2[];
		String s;
		int i;
		double res_temp = 1;
		String temp[];
		temp2 = names.split("/");
		String scoreStr = "";
		String scores[];
		String score = "-";
		in = new BufferedReader(new FileReader(inFile));
		int counter_find = 0;
		
		while((s = in.readLine())!=null){
			temp = s.split(",");//temp[0]人名  temp[1]score
			for(i = 0; i < temp2.length; i++){//用temp2[i]在infile中找
				if(temp2[i].equals(temp[0])){
					counter_find++;
					if(scoreStr.equals("")){
						scoreStr = temp[1];
					}else{
						scoreStr = scoreStr + "/" +temp[1];
					}
				}
			}
		}

		in.close();
		System.out.println(scoreStr);
		for(i = counter_find; i < temp2.length; i++){
			scoreStr = scoreStr + "/" + defaultValue;
		}
		System.out.println(scoreStr);
		
		scores = scoreStr.split("/");
		for(i = 0; i < scores.length; i++){//score = 1-(1-a)*(1-b)...
			res_temp = res_temp * (1 - Double.valueOf(scores[i]));
		}
		score = 1-res_temp + "";
		return score;
	}

	public static int insertPeople() throws IOException{//和relation表连接；加入发行人等属性；去掉theme属性；生成total_addPeople.csv文件
		String outFile = "total4_addPeople.csv";
		BufferedReader in = new BufferedReader(new FileReader("total3_addScore.csv"));
		BufferedReader in2;// = new BufferedReader(new FileReader("newRelation.csv"));
		FileWriter writer = new FileWriter(outFile, true);
		BufferedWriter out = new BufferedWriter(writer);
		writer.write("name, date, box, dailyScreen, dailyScreenPct, themeScore, score, zhuyan, zhizuo, faxing, bianju, daoyan\r\n");
		String s, s2, lineOut;
		boolean isFind = false;
		String temp[], temp2[];
		String relation = "", peopleName = "", movieName = "", zhuyan = "-", zhizuo = "-", zhizuoren = "-", faxing = "-", bianju = "-", daoyan = "-";
		s = in.readLine();//title;
		int k = 0; 
		while((s = in.readLine()) != null){
			k++;
			zhuyan = "-";
			zhizuo = "-";
			faxing = "-";
			bianju = "-";
			daoyan = "-";
			zhizuoren = "-";
//			System.out.println(s);
			temp = s.split(",");
			movieName = temp[0];
			isFind = false;
			in2 = new BufferedReader(new FileReader("newRelation.csv"));//movie	 name	 type	 totalBox
			in2.readLine();
			while((s2 = in2.readLine()) != null){
				temp2 = s2.split(",");
				if(temp2[0].equals(movieName)){
					isFind = true;
					relation = temp2[2];
					if(relation.equals("主演")){
						if(!zhuyan.equals("-")){
							zhuyan = zhuyan + "/" + temp2[1];
						}else{
							zhuyan = temp2[1];
						}
					}
					if(relation.equals("制作")){
						if(!zhizuo.equals("-")){
							zhizuo = zhizuo + "/" + temp2[1];
						}else{
							zhizuo = temp2[1];
						}
					}
					if(relation.equals("制作人")){
						if(!zhizuoren.equals("-")){
							zhizuoren = zhizuoren + "/" + temp2[1];
						}else{
							zhizuoren = temp2[1];
						}
					}
					if(relation.equals("发行")){
						if(!zhuyan.equals("-")){
							faxing = zhuyan + "/" + temp2[1];
						}else{
							faxing = temp2[1];
						}
					}
					if(relation.equals("导演")){
						if(!daoyan.equals("-")){
							daoyan = zhuyan + "/" + temp2[1];
						}else{
							daoyan = temp2[1];
						}
					}
					if(relation.equals("编剧")){
						if(!bianju.equals("-")){
							bianju = zhuyan + "/" + temp2[1];
						}else{
							bianju = temp2[1];
						}
					}
				}
				if(isFind && (!temp2[0].equals(movieName))){
					in2.close();
					break;
				}
			}
			in2.close();
			lineOut = temp[0] + "," + temp[1] + "," + temp[2] + "," +temp[3] + "," + temp[4] + ","
					+ temp[5] + "," + temp[6] +"," + zhuyan +"," 
					+ zhizuo + "," + faxing + "," + bianju + "," + daoyan + "," + zhizuoren + "\r\n";
			writer.write(lineOut);
			//"name, box, dailyScreen, dailyScreenPct, theme, themeScore, score, zhuyan, zhizuo, faxing, bianju, daoyan\r\n"

		}
		out.close();
		writer.close();
		//in2.close();
		in.close();
		return 0;
	}
	
	//relation = 主演|制作|发行|导演|编剧
	//outFileName = zhuyan|zhizuo|faxing|daoyan|bianju
	public static int dealPeople(String relation, String outFileName) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader("newRelation.csv"));
		BufferedReader in2 = new BufferedReader(new FileReader("newRelation2.csv"));
		String outFile1 = outFileName+"_temp.csv";
		FileWriter writer = new FileWriter(outFile1,true);
		BufferedWriter out = new BufferedWriter(writer);
		String temp[], temp2[];
		String s = in.readLine();
		String s2;
		double boxAvg = 0, boxSum = 0;
		int counter = 0;
		double max = 0;
		Vector v_people = new Vector();
		String lineOut;
		while((s = in.readLine())!= null){
			temp = s.split(",");
			if((temp[2].equals(relation)) && (v_people.indexOf(temp[1]) == -1)){//模糊匹配 eg.制作，制作人
				counter = 1;
				boxSum = Double.valueOf(temp[3]);
				in2 = new BufferedReader(new FileReader("newRelation2.csv"));
				s2 = in2.readLine();
				while((s2 = in2.readLine())!=null){
					temp2 = s2.split(",");
					if((temp2[1].equals(temp[1]))
							&&(temp2[2].equals(relation))
							&&(!(temp[0].equals(temp2[0]))) ){
						v_people.addElement(temp[1]);
						boxSum += Double.valueOf(temp2[3]);
						counter++;
					}
				}
				in2.close();
				boxAvg = boxSum / counter;
				lineOut = temp[1]+","+ boxAvg + "\r\n";
				writer.write(lineOut);
			}	
		}
		in.close();
		writer.close();
		out.close();
		
		double score;
		String outFile = outFileName+".csv";
		in = new BufferedReader(new FileReader(outFile1));
		writer = new FileWriter(outFile,true);
		out = new BufferedWriter(writer);
		while((s = in.readLine())!=null){
			temp = s.split(",");
			score = Double.valueOf(temp[1]);
			lineOut = temp[0] + "," + score + "\r\n";
			writer.write(lineOut);
		}
		in.close();
		writer.close();
		out.close();
		
		return 0;
	}
	public static double getMaxBox(String file) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(file));
		double max = 0;
		String s;
		String temp[];
		double a;
		while((s = in.readLine())!=null){
			temp = s.split(",");
			a = Double.valueOf(temp[1]);
			if(a > max){
				max = a;
			}
		}
		in.close();
		return max;
	}
	
	public static boolean isNumeric(String str, int j){//判断前j位是否为数字
	  for (int i = 0; i < j; i++){
	   if (!Character.isDigit(str.charAt(i))){
	    return false;
	   }
	  }
	  return true;
	 }
	
	public static String dealTheme(String theme){
		theme = theme.trim();
		theme = theme.replace("]", "");  //去掉所有标点
		theme = theme.replace("[", "");  //去掉所有标点
		return theme;
	}

	public static String dealMovieName(String movieName){
		movieName = movieName.trim();
		movieName = movieName.replace(",", "");  //去掉所有标点
		movieName = movieName.replace("，", "");  //去掉所有标点
		movieName = movieName.replace("？", "");  //去掉所有标点
		movieName = movieName.replace("?", "");  //去掉所有标点
		movieName = movieName.replace("“", "");
		movieName = movieName.replace(";", "");
		movieName = movieName.replace("”", "");
		movieName = movieName.replace("\"", "");
		movieName = movieName.replace("&middot;", "");
		movieName = movieName.replace("&middot", "");
		movieName = movieName.replace("·", "");
		
		int pos = movieName.indexOf(" ");
		if(pos > 0){
			movieName = movieName.substring(0, pos);
		}
		return movieName;
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
			//System.out.println(i+": "+s);
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
	
	public static int dealRelation() throws IOException{//连接，得到每个电影对应的box
		String fileName = "relation.csv";
		String fileName2 = "total3_addScore.csv";
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		BufferedReader in_total = new BufferedReader(new FileReader(addScoresfileName2));
		FileWriter writer = new FileWriter("newRelation.csv",true);
		BufferedWriter out = new BufferedWriter(writer);
		String s = in.readLine();
		String temp[], temp2[];
		String currentMovieName, name, relationType, s2;
		int i;
		boolean isFind = false;
		String movieName = "", box = "", lineOut = "";
		writer.write("movie, name, type, totalBox\r\n");
		while((s = in.readLine())!= null){
			temp = s.split(",");
			currentMovieName = temp[0];
			for(i = 1; i<temp.length - 3; i++){
				currentMovieName += temp[i];
			}
			currentMovieName = dealMovieName(currentMovieName);
			name = temp[i];
			relationType = temp[i+1];
			
			if(!currentMovieName.equals(movieName)){
				movieName = currentMovieName;
				isFind = false;
				//在total表中找box
				in_total = new BufferedReader(new FileReader(fileName2));
				while((s2 = in_total.readLine())!=null){
					temp2 = s2.split(",");
					if(temp2[0].equals(movieName)){
						box = temp2[2];//getBox
						//System.out.println(box);
						isFind = true;
						break;
					}
				}
			}
			if(isFind){
				lineOut = movieName +"," + name + "," +relationType + "," + box + "\r\n";
				writer.write(lineOut);
			}
		}
		in.close();
		in_total.close();
		writer.close();
		out.close();
		return 0;
	}
	
	public static int dealMissingData() throws IOException{
		String inFile = "total.csv";
		String outFile = "total_temp.csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		//FileWriter writer = new FileWriter(outFile,true);
		//BufferedWriter out = new BufferedWriter(writer);
		String temp[], currentType[];
		//计算各类电影的权值；用该类电影的平均票房求得
		Vector type = new Vector();
		double typeScore_avg[] = new double[100];
		int type_counter[] = new int[100];
		String s;
		double avgScore = 0, currentScore, temp0;
		int index;
		int i, j;
		double totalSum = 0;
		int counter = 0;
		s = in.readLine();
		while((s = in.readLine())!= null){
			temp = s.split(",");
			if(!temp[4].equals("-")){
			currentType = temp[4].split("/");//电影类型
			for(i = 0; i < currentType.length; i++){
				index = type.indexOf(currentType[i]);
				if(index == -1){
					type.add(currentType[i]);
					//typeScore_sum.add(temp[1]);//total 票房
					typeScore_avg[type.size()-1] = Double.valueOf(temp[1]+"");
					type_counter[type.size()-1] = 1;
				}
				else{
					typeScore_avg[index] += Double.valueOf(temp[1]+"");
					type_counter[index]++;
				}
			}
		}
		}
		in.close();
		double sum_total = 0;
		int counter_total = 0;
		for(i = 0; i<type.size(); i++){
			sum_total += typeScore_avg[i];
			counter_total += type_counter[i];
		}
		//默认值，对应theme为"-"的电影
		double themeScore_default = sum_total/counter_total;
		
		for(i = 0; i < type.size(); i++){
			//每一类电影的score
			typeScore_avg[i] = typeScore_avg[i]/type_counter[i];
		}
		
		//-------------------插入默认值------------------
		String dailyScreen_default = "" + getAvgAttr(3, "total.csv");
		String dailyScreenPct_default = "" + getAvgAttr(4, "total.csv");
		
		in = new BufferedReader(new FileReader(inFile));
		FileWriter writer = new FileWriter(outFile,true);
		BufferedWriter out = new BufferedWriter(writer);
		s = in.readLine();
		//String temp[];
		double themeScore = 0;
		String movieName, total, dailyScreen, dailyScreenPct, theme, score;
		writer.write("movie, total, dailyScreen, dailyScreenPct, theme, themeScore, score\r\n");
		int k;
		double themeSum_temp = 0;
		
		while((s = in.readLine())!=null){
			themeSum_temp = 0;
			temp = s.split(",");
			if((!temp[5].equals("-"))){
//				if((!temp[5].equals("-"))&&(!temp[1].equals("0.1"))){
				movieName = temp[0];
				total = temp[1];
				dailyScreen = temp[2];
				dailyScreenPct = temp[3];
				theme = temp[4];
				score = temp[5];
				if(dailyScreen.equals("-")){
					dailyScreen = dailyScreen_default;
				}
				if(dailyScreenPct.equals("-")){
					dailyScreenPct = dailyScreenPct_default;
				}
				//----------计算themeScore-----------
				if(theme.equals("-")){
					themeScore = themeScore_default;
				}else{
					currentType = theme.split("/");
					for(j = 0; j < currentType.length; j++){
						k = type.indexOf(currentType[j]);
						themeSum_temp += typeScore_avg[k];
					}
					themeScore = themeSum_temp/currentType.length;
				}				
				writer.write(movieName + ","
						+ total +","
						+ dailyScreen + ","
						+ dailyScreenPct + ","
						+ theme + ","
						+ themeScore + ","
						+ score + "\r\n");
			}
		}
		in.close();
		writer.close();
		out.close();
		return 0;
	}
	
	

	public static int addTheme() throws IOException{
		String fileName1 = "metadata.csv";
		String fileName2 = "newBoxTotal.csv";
		String scoreFile = "";
		String division = ",";
		StringTokenizer Information;
		BufferedReader in_theme = new BufferedReader(new FileReader(fileName1));
		BufferedReader in_box = new BufferedReader(new FileReader(fileName2));
		FileWriter writer = new FileWriter("total2_addTheme.csv",true);
		BufferedWriter out = new BufferedWriter(writer);
		String s, s2;
		s2 = in_box.readLine();//title;
		//s = in_theme.readLine();//title
		String movieName;
		String temp[], temp2[];
		int len = 0;
		int i, j;
		boolean isFind = false;
		String lineOut = "";
		writer.write("name, releaseDate, total, dailyScreem, dailyScreenPct, theme\r\n");
		String theme, boxTotal, releaseDay, box, movieName_theme;
		while((s2=in_box.readLine())!=null){
			lineOut = s2;
			temp2 = s2.split(",");
			movieName = temp2[0];
			in_theme = new BufferedReader(new FileReader(fileName1));
			while((s = in_theme.readLine()) != null){
				temp = s.split(",");
				movieName_theme = temp[0];
				//releaseDay = temp[3];
				len = temp.length;
				for(i=1; i<len-3; i++){
					movieName_theme += temp[i]; 
				}
				
				movieName_theme = dealMovieName(movieName_theme);
				if(movieName_theme.equals(movieName)){
					isFind = true;
					theme = temp[i];
					//System.out.println(theme);
					boxTotal = temp[i+1];
					releaseDay = temp[i+2];
					//if(releaseDay == "") releaseDay = "-";
					if(theme.equals("")) theme = "-";
					if(boxTotal.equals("")) boxTotal = "-";
					lineOut = lineOut + "," + theme + "\r\n";
					//writer.write(lineOut);
					break;
				}
			}
			if(!isFind){
				
				lineOut = lineOut + ",-\r\n";
			}	
			isFind = false;
			writer.write(lineOut);
		}
		in_theme.close();
		in_box.close();
		out.close();
		writer.close();
		
		
		in_theme = new BufferedReader(new FileReader(fileName1));
		writer = new FileWriter("total2_addTheme.csv",true);
		out = new BufferedWriter(writer);
		in_theme.readLine();
		while((s = in_theme.readLine())!= null){
			isFind = false;
			temp = s.split(",");
			movieName_theme = temp[0];
			len = temp.length;
			for(i=1; i<len-3; i++){
				movieName_theme += temp[i]; 
			}
			theme = temp[i];
			releaseDay = temp[i+2];
			theme = dealTheme(theme);
			box = dealTheme(temp[i+1]);
			
			movieName_theme = dealMovieName(movieName_theme);
			in_box = new BufferedReader(new FileReader(fileName2));
			while((s2 = in_box.readLine()) != null){
				temp2 = s2.split(",");
				movieName = temp2[0];
				if(movieName_theme.equals(movieName)){
					isFind = true;
					break;
				}
			}
			if(!isFind){
				theme = dealTheme(theme);
				box = dealTheme(box);
				if(box.equals("0")){
					box = "";
				}
				if((!(box.equals("")))&&(!(theme.equals("")))){
					lineOut = movieName_theme + "," + releaseDay + "," + box + ",-,-,"+theme+"\r\n";
					writer.write(lineOut);
				}	
			}	

		}
		in_theme.close();
		in_box.close();
		out.close();
		writer.close();
		
		return 0;
	}

	public static int getScore() throws IOException{//计算每个电影的平均豆瓣评分
		String fileName="score.csv";
		String division = ",";
		StringTokenizer Information;
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		FileWriter writer = new FileWriter("newScore.csv",true);
		BufferedWriter out = new BufferedWriter(writer);
		
		int i, j=0;
		String lineOut;
		String s;
		in.readLine();
		String movieName = "";
		double movieScore = 0;
		double currentScore = 0;
		int movieCounter = 0;
		s = in.readLine();
		String temp, currentMovie = "";
		double avgScore = 0;
		
		while(true){
			Information = new StringTokenizer(s,division);
			if(Information.hasMoreTokens()){
				currentMovie = Information.nextToken().trim();
				//csv用“,”分隔; 处理电影名中带有“,”的特殊情况
				while(true){	
					temp = Information.nextToken();
					if(!temp.substring(0, 3).equals("201")){
						currentMovie = currentMovie + ","+temp;
					}
					else{
						currentScore = Double.valueOf(Information.nextToken().trim());
						break;
					}
				}
				currentMovie = dealMovieName(currentMovie);
				if(!currentMovie.equals(movieName)){
					if(movieName.length()!=0){
						avgScore = (movieScore/movieCounter);
						lineOut = movieName + ","+avgScore+"\r\n";
						writer.write(lineOut);
					}
					movieName = currentMovie; 
					movieScore = currentScore;
					movieCounter = 1;
				}
				else{
					movieScore += currentScore;
					movieCounter++;
				}
			}
			else break;
			s = in.readLine();
			if(s == null){
				lineOut = movieName + ","+(movieScore/movieCounter)+"\r\n";
				writer.write(lineOut);
				break;
			}
		}
		in.close();
		writer.close();
		out.close();
		return 0;
	}

	public static int dealScore() throws IOException{
		String fileName1 = "newScore.csv";
		String fileName2 = "total2_addTheme.csv";
		StringTokenizer Information;
		BufferedReader in_score = new BufferedReader(new FileReader(fileName1));
		BufferedReader in_box = new BufferedReader(new FileReader(fileName2));
		FileWriter writer = new FileWriter("total3_addScore.csv",true);
		BufferedWriter out = new BufferedWriter(writer);
		String s, s2;

		String movieName;
		String temp[], temp2[];
		int len = 0;
		int i, j;
		boolean isFind = false;
		String lineOut = "";
		writer.write("name, releaseDay, total, dailyScreem, dailyScreenPct, theme, score\r\n");
		String theme, dailyScreen, dailyScreenPct, total, releaseDay, box, movieName_theme;
		String movieName_score = "", score = "";
		s = in_box.readLine();//title;
		
		while((s = in_box.readLine())!= null){
			isFind = false;
			temp = s.split(",");
			movieName = temp[0];
			total = temp[1];
			dailyScreen = temp[2];
			dailyScreenPct = temp[3];
			theme = temp[4];
			movieName = dealMovieName(movieName);
			in_score = new BufferedReader(new FileReader(fileName1));
			while((s2 = in_score.readLine()) != null){
				temp2 = s2.split(",");
				movieName_score = temp2[0];
				if(movieName_score.equals(movieName)){
					isFind = true;
					score = temp2[1];
					break;
				}
			}
			if(isFind){
				lineOut = s + "," + score + "\r\n";
			}
			else lineOut = s + ",-\r\n";
			writer.write(lineOut);
		}
		in_score.close();
		in_box.close();
		out.close();
		writer.close();
		return 0;
	}

	
public static int dealBox() throws IOException, ParseException{
	//累加求得票房；脏数据丢掉；结果存入newBox.csv
	String fileName="box.csv";
	String division = ",";
	StringTokenizer Information;
	BufferedReader in = new BufferedReader(new FileReader(fileName));
	FileWriter writer = new FileWriter("newBoxTotal.csv",true);//总票房
	BufferedWriter out = new BufferedWriter(writer);
	
	int i, j=0;
	String lineOut;
	String s;
	lineOut = "movieName,date,boxtotal,dailyScreens(total),dailyScreenPct\r\n";
	writer.write(lineOut);
	in.readLine();
	String movieName = "";
	double movieBox = 0;
	double currentDailyBox = 0;
	int dayCounter = 0;
	int currentReleaseDay = 0;
	s = in.readLine();
	String currentMovie = "";
	boolean isSameDay0 = false; 
	boolean isAllDaysIllegal = true; 
	String temp_date = "";
	double screen = 0, screenPercent = 0, 
		   currentScreen = 0, currentScreenPercent = 0;
	int temp = -1;
	String str;
	double temp1 = 0, temp2 = 0, temp3 = 0;
	int releaseDaySum = 0;
	String releaseDay = "-";
	
	String currentDate = "";
	SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");  
    Date strToDate;  
    GregorianCalendar gc = new GregorianCalendar();
	
	int k = 0;
	
	while(true){
		
		if(!s.substring(0, 1).equals(",")){
		Information = new StringTokenizer(s,division);
		if(Information.hasMoreTokens()){
			currentMovie = Information.nextToken().trim();//电影名
			//System.out.print(currentMovie);
			//csv用“,”分隔; 处理电影名中带有“,”的特殊情况
			while(true){	
				temp_date = Information.nextToken();
				if(!temp_date.substring(0, 3).equals("201")){
					currentMovie = currentMovie+temp_date; 
				}
				else{
					break;
				}
			}
			currentMovie = dealMovieName(currentMovie);//去除标点符号等
			
			if(!currentMovie.equals(movieName)){
				if((movieName.length()!= 0) && (releaseDaySum > 0) &&  (dayCounter > 5) && (!isAllDaysIllegal)){//防止出现release day全0的daily box
					//考虑有很多条releaseDay=0的情况
					lineOut = movieName + "," + releaseDay + ","
							+ (15*movieBox/(dayCounter)) + "," 
							+ (15*screen/(dayCounter)) + ","
							+ (15*screenPercent/(dayCounter)) 
							//+ ","+ dayCounter
							+"\r\n";
					writer.write(lineOut);
					releaseDay = "-";
				}
				releaseDaySum = 0;
				dayCounter = 0;
				isAllDaysIllegal = true;
				movieName = currentMovie;
				movieBox = 0;
				screen = 0; 
				screenPercent = 0;
				currentReleaseDay = -1;
				currentDate = "";//////////////加这个字段主要是用来处理day0///////
			}
			
			temp = Integer.valueOf(Information.nextToken().trim());//releaseDay;
			
			//release day<=14才处理
			if(temp <= 14){
				if(temp > 0 && releaseDay.equals("-")){//用不为0的日期计算release Day
					strToDate = sdf.parse(temp_date);
					gc = new GregorianCalendar();
					gc.setTime(strToDate);
					gc.add(5,-temp);//日期减temp天
					gc.set(gc.get(Calendar.YEAR),gc.get(Calendar.MONTH),gc.get(Calendar.DATE));
					releaseDay = sdf.format(gc.getTime());
				}
				dayCounter++;
				releaseDaySum += temp;
				isAllDaysIllegal = false;
				temp1 = Double.valueOf(Information.nextToken().trim()); //currentDailyBox
				Information.nextToken();//totalBox
				temp2 = Double.valueOf(Information.nextToken(). trim());//currentScreen
				temp3 = Double.valueOf(Information.nextToken().trim()); //currentScreenPercent
				
				//release day同为0
				if((temp == 0)&&(currentReleaseDay == 0)){
					if(temp_date == currentDate){//两个为0的是同一天
						isSameDay0 = true;//取最大的数据
					}else{//取最新数据
						dayCounter--;
						currentDate = temp_date;
						movieBox -= currentDailyBox;
						currentDailyBox = temp1;
						screen -= currentScreen; 
						currentScreen = temp2;
						screenPercent -= currentScreenPercent;
						currentScreenPercent = temp3;
					}
				}

				if(((currentReleaseDay == temp) 
					&& (currentReleaseDay != 0))
					||(isSameDay0)){//数据预处理，如果有连续两天 相同
					dayCounter--;
					//取最大的
					if(currentDailyBox - temp1 <= 0){
						movieBox -= currentDailyBox;
						currentDailyBox = temp1;
					}
					if(currentScreen - temp2 <= 0){
						screen -= currentScreen; 
						currentScreen = temp2;
					}
					if(currentScreenPercent - temp3 <= 0){
						screenPercent -= currentScreenPercent;
						currentScreenPercent = temp3;
					}
					isSameDay0 = false;
				}else{
					currentReleaseDay = temp;
					currentDailyBox = temp1;
					currentScreen = temp2;
					currentScreenPercent = temp3;
				}
				movieBox += currentDailyBox;
				screen += currentScreen; 
				screenPercent += currentScreenPercent;
			}	
		}
	}
	s = in.readLine();
	
	if(s == null){
		lineOut = movieName + "," + releaseDay + ","
				+ (15*movieBox/(dayCounter)) + "," 
				+ (15*screen/(dayCounter)) + ","
				+ (15*screenPercent/(dayCounter))//+"," + dayCounter
				+ "\r\n";
		
		writer.write(lineOut);
		break;
	}

	}
	in.close();
	writer.close();
	out.close();
	return 0;
}	
}