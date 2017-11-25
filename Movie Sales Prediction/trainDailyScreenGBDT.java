import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.text.*;

// faxing, zhubian, zhuyan, daoyan预测score，dailyScreen, dailyScreenPercent

public class trainDailyScreenGBDT
{
	public static void main(String[] args) throws IOException
	{
//		dealRelation();
//		dealPeople("导演", "daoyan");//计算每个演员的权重
//		dealPeople("主演", "zhuyan");//计算每个演员的权重
//		dealPeople("制作", "zhizuo");//计算每个演员的权重
//		dealPeople("制作人", "zhizuoren");//计算每个演员的权重
//		dealPeople("发行", "faxing");//计算每个演员的权重
//		addScores();
//		dealPeopleScore();
//		addDate();
		
		String file = "total7_addDate.csv";
		int attrNum = 6;
		int i, j;
		int attrs[] = new int[attrNum];
		double mse = 0;
		int outFileIndex = 0;
		FileWriter writer2 = new FileWriter("tree_dailyScreen.csv",true);
		BufferedWriter out2 = new BufferedWriter(writer2);
		writer2.write("tree, 父亲节点分裂属性, 父亲节点分裂属性值, L or R, 当前分裂属性 , 分裂属性值, 均值L, 均值R\r\n");
		writer2.close();
		out2.close();
		String lastMse = "";
		//预测dailyScreen
		while(outFileIndex < 500){// themeScore	 zhuyan	 zhizuo	 faxing	 daoyan
			attrs[0] = -5;//theme
			attrs[1] = 7;//zhuyan
			attrs[2] = 8;//zhizuo
			attrs[3] = 9;//faxing
			attrs[4] = 10;//daoyan
			attrs[5] = 1;//releaseDay score
			mse = train(outFileIndex, attrs, 3);
			if(lastMse.equals(mse+ "")){
				break;
			}
			lastMse = mse + "";
			System.out.println(mse+"-----"+outFileIndex);
			if(mse <=10000){break;}
			outFileIndex++;
		}
	}
	

	public static int addDate() throws IOException{
		String inFile = "total6_addPeopleScore.csv";
		String outFile = "total7_addDate.csv";
		double avgs[] = calDate();
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		FileWriter writer = new FileWriter(outFile,true);
		BufferedWriter out = new BufferedWriter(writer);
		String s = in.readLine();
		int month;
		String temp[], temp2[];
		double dateScore;
		writer.write("name,dateScore,box,dailyScreen,dailyScreenPct,themeScore,"
				+ "score,zhuyan,zhizuo,faxing,daoyan\r\n");
		while((s = in.readLine())!=null){
			temp = s.split(",");//date = temp[1]
			temp2 = temp[1].split("/");
			if(temp2[1].substring(0, 1).equals("0")){
				temp2[1] = temp2[1].substring(1, 2);
			}
			month = Integer.valueOf(temp2[1]);
			dateScore = avgs[month-1];
			writer.write(temp[0] + ","
					+ dateScore + ","
					+ temp[2] + ","
					+ temp[3] + ","
					+ temp[4] + ","
					+ temp[5] + ","
					+ temp[6] + ","
					+ temp[7] + ","
					+ temp[8] + ","
					+ temp[9] + ","
					+ temp[10] + "\r\n");
		}
		in.close();
		writer.close();
		out.close();
		return 0;
	}
	
	public static double[] calDate() throws IOException{
		String inFile = "total6_addPeopleScore.csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		double boxes[] = new double[12];
		double avgs[] = new double[12];
		int counters[] = new int[12];
		double deviations[] = new double[12];
		String s = in.readLine();
		String temp[], temp2[];
		String date;
		int month, i, day;
		for(i = 0; i < 12; i++){
			boxes[i] = 0;
			counters[i] = 0;
			deviations[i] = 0;
			avgs[i] = 0;
		}
		String tempStr;
		while((s = in.readLine())!=null){
			temp = s.split(",");
			date = temp[1];//temp[2] box
			temp2 = date.split("/");
			tempStr = temp2[1]+temp2[2];
			if(tempStr.substring(0, 1).equals("0")){
				tempStr = tempStr.substring(1, 4);
			}
			day = Integer.valueOf(tempStr);//month + day
			if(temp2[1].substring(0, 1).equals("0")){
				temp2[1] = temp2[1].substring(1, 2);
			}
			month = Integer.valueOf(temp2[1]);
			counters[month-1]++;
			boxes[month-1] += Double.valueOf(temp[3]);	
		}
		for(i = 0; i < 12; i++){
			if(counters[i] > 0)
			avgs[i] = boxes[i] / counters[i];
		}
		in.close();
		
		in = new BufferedReader(new FileReader(inFile));
		s = in.readLine();
		while((s = in.readLine())!=null){
			temp = s.split(",");
			date = temp[1];//temp[2] box
			temp2 = date.split("/");
			tempStr = temp2[1]+temp2[2];
			if(tempStr.substring(0, 1).equals("0")){
				tempStr = tempStr.substring(1, 4);
			}
			day = Integer.valueOf(tempStr);
			if(temp2[1].substring(0, 1).equals("0")){
				temp2[1] = temp2[1].substring(1, 2);
			}
			month = Integer.valueOf(temp2[1]);
			deviations[month-1] += Math.pow((Double.valueOf(temp[3]) - avgs[month-1]), 2);
		}
		for(i = 0; i < 12; i++){
			month = i+1;
			deviations[i]/=counters[i];
			deviations[i] = Math.sqrt(deviations[i]);
//			System.out.println("month: "+month + "; avg = " + avgs[i] + "; dev=" + deviations[i]);
		}
		return avgs;
	}
	
	//attrs 属性集
	public static double train(int outFileIndex, int attrs[], int objAttr) throws IOException{//训练一整棵树
		String inFile = "", outFile = outFileIndex + ".csv";
		int inFileIndex = 0;
		
		//--------------读残差----------------
		if(outFileIndex == 0){
			inFile = "total7_addDate.csv";
		}else {
			inFileIndex = outFileIndex - 1;
			inFile = inFileIndex + ".csv";
		}
		
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		String s;
		int maxArraySize = 1500;
		String temp[];
		MovieRecord[] records = new MovieRecord[maxArraySize];
		if(inFile.equals("total7_addDate.csv")){
			s = in.readLine();//只有第一个文件有title
		}
		int j = 0;
		double box, dailyScreen, dailyScreenPct, themeScore, zhuyan, zhizuo, faxing, daoyan;
		String movieName, score;
		Double releaseDayScore;

		//把文件读取到数组中
		while((s = in.readLine())!=null){
//			System.out.println(s);
			temp = s.split(",");
			movieName = temp[0];
			releaseDayScore = Double.valueOf(temp[1]);
			box = Double.valueOf(temp[2]);
			dailyScreen = Double.valueOf(temp[3]);
			dailyScreenPct = Double.valueOf(temp[4]);
			themeScore = Double.valueOf(temp[5]);
			score = temp[6];
			zhuyan = Double.valueOf(temp[7]);
			zhizuo = Double.valueOf(temp[8]);
			faxing = Double.valueOf(temp[9]);
			daoyan = Double.valueOf(temp[10]);
			records[j] = new MovieRecord();
			records[j].setMovieName(movieName);
			records[j].setReleaseDate(releaseDayScore);
			records[j].setBox(box);
			records[j].setY(dailyScreen);
			records[j].setDailyScreen(dailyScreen);
			records[j].setDailyScreenPct(dailyScreenPct);
			records[j].setThemeScore(themeScore);
			records[j].setScore(score);
			records[j].setZhuyan(zhuyan);
			records[j].setZhizuo(zhizuo);
			records[j].setFaxing(faxing);
			records[j].setDaoyan(daoyan);
			// records[j].setDepth(0);
			j++;
		}//j: length
		in.close();
		
		Split split = new Split();
		split.setAttr(-1);
	
		trainTree(records, j, attrs, inFile, outFile, split, "-", objAttr);
		//----------计算生成的文件的mse-----------
		double cancha;
		double mse = 0;
		in = new BufferedReader(new FileReader(outFile));
		s = in.readLine();
		int k = 0;
		while((s = in.readLine())!=null){
			temp = s.split(",");
			cancha = Double.valueOf(temp[objAttr]);
			mse += Math.pow(cancha, 2);
			k++;
		}
		mse/=k;
		in.close();
		return mse;//返回mse
	}

	//len指多少条有效数据  treeType: R or L or "-"
	public static double trainTree(MovieRecord records[], int len, 
			int attrs[], String inFile, String outFile, Split fatherSplit, String treeType, int objAttr) throws IOException{//训练子树
		int i, j;
		Split[] splits = new Split[len];
		Split split, bestSplit;
		split = new Split();
		bestSplit = new Split();
		bestSplit.setValue(-1);
		bestSplit.setMse(-1);
		
		for(i = 0; i < attrs.length; i++){
			if(attrs[i] > 0){
				if(i == 5){
					for(j = 0; j < len; j++){
						splits[j] = new Split();
						splits[j].setValue(records[j].getReleaseDate());
						splits[j].setY(records[j].getY());
					}
				}
				else if(i == 0){
					for(j = 0; j < len; j++){
						splits[j] = new Split();
						splits[j].setValue(records[j].getThemeScore());
						splits[j].setY(records[j].getY());
					}
				}
				else if(i == 1){
					for(j = 0; j < len; j++){
						splits[j] = new Split();
						splits[j].setValue(records[j].getZhuyan());
						splits[j].setY(records[j].getY());
					}
				}
				else if(i == 2){
					for(j = 0; j < len; j++){
						splits[j] = new Split();
						splits[j].setValue(records[j].getZhizuo());
						splits[j].setY(records[j].getY());
						
					}
				}
				else if(i == 3){
					for(j = 0; j < len; j++){
						splits[j] = new Split();
						splits[j].setValue(records[j].getFaxing());
						splits[j].setY(records[j].getY());
					}
				}
				else if(i == 4){
					for(j = 0; j < len; j++){
						splits[j] = new Split();
						splits[j].setValue(records[j].getDaoyan());
						splits[j].setY(records[j].getY());
					}
				}
				split = getSplit_Attr(splits);//求分割点
				if((bestSplit.getMse() < 0) || (split.getMse() < bestSplit.getMse())){
					bestSplit.setAttr(i);//第i个属性为分割点
					bestSplit.setValue(split.getValue());
					bestSplit.setMse(split.getMse());
				}
			}
		}//当前分割点为属性attr[i]的，值为split.getvalue()的点

		int attr = bestSplit.getAttr();
		
//		System.out.println(bestSplit.getAttr()+";"+bestSplit.getValue());
		double splitVal = bestSplit.getValue();
		
		if(treeType.equals("L")){
			fatherSplit.setAttrL(attr);
			fatherSplit.setAttrValueL(splitVal);
			bestSplit.setDepth(1+fatherSplit.getDepth());
		}
		if(treeType.equals("R")){
			fatherSplit.setAttrR(attr);
			fatherSplit.setAttrValueR(splitVal);
			bestSplit.setDepth(1+fatherSplit.getDepth());
		}
		if(treeType.equals("-")){
			bestSplit.setDepth(0);
		}
//		System.out.println("Depth="+bestSplit.getDepth());
		attrs[attr] = -1;
		MovieRecord records1[] = new MovieRecord[len];
		MovieRecord records2[] = new MovieRecord[len];
		int len1 = 0, len2 = 0;
		double val;

		for(i = 0; i < len; i++){
			val = getValue_record(attr, records[i]);
			if(val <= splitVal){//――>L
				// System.out.print("L");
				records1[len1] = new MovieRecord();
				records1[len1].setMovieName(records[i].getMovieName());
				records1[len1].setReleaseDate(records[i].getReleaseDate());
				records1[len1].setY(records[i].getY());
				records1[len1].setDailyScreen(records[i].getDailyScreen());
				records1[len1].setDailyScreenPct(records[i].getDailyScreenPct());
				records1[len1].setThemeScore(records[i].getThemeScore());
				records1[len1].setScore(records[i].getScore());
				records1[len1].setZhuyan(records[i].getZhuyan());
				records1[len1].setFaxing(records[i].getFaxing());
				records1[len1].setZhizuo(records[i].getZhizuo());
				records1[len1].setDaoyan(records[i].getDaoyan());
				len1++;
			}else{//――>R
				// System.out.print("R");
				records2[len2] = new MovieRecord();
				records2[len2].setMovieName(records[i].getMovieName());
				records2[len2].setReleaseDate(records[i].getReleaseDate());
				records2[len2].setY(records[i].getY());
				records2[len2].setDailyScreen(records[i].getDailyScreen());
				records2[len2].setDailyScreenPct(records[i].getDailyScreenPct());
				records2[len2].setThemeScore(records[i].getThemeScore());
				records2[len2].setScore(records[i].getScore());
				records2[len2].setZhuyan(records[i].getZhuyan());
				records2[len2].setFaxing(records[i].getFaxing());
				records2[len2].setZhizuo(records[i].getZhizuo());
				records2[len2].setDaoyan(records[i].getDaoyan());
				len2++;
			}	
		}

		double mse = 0;
		boolean noNodes = false;
		String avgTreeL = "-", avgTreeR = "-";
		if(len1 >= 1){
			avgTreeL = getNodeAvg(records1, 0, len1-1) + "";
			fatherSplit.setPredictL(Double.valueOf(avgTreeL));//左子树均值
		}
		if(len2>=1){
			avgTreeR = getNodeAvg(records2, 0, len2-1) + "";
			fatherSplit.setPredictR(Double.valueOf(avgTreeR));//右子树均值
		}
		if(!((len1 >= 1) && (len2 >= 1))){
			noNodes = true;
		}
		if(!noNodes){
			String child = "";
			if(treeType.equals("L")){
				child = "L," + fatherSplit.getAttrL() + ","
						+ fatherSplit.getAttrValueL() + ",";
			}else{
				child = "R," + fatherSplit.getAttrR() + ","
						+ fatherSplit.getAttrValueR() + ",";
			}
			child = child + avgTreeL + ", " + avgTreeR;
			if((fatherSplit.getAttr()!=-1)){//bestSplit不是根节点
				String outFile2 = "tree_dailyScreen.csv";
				FileWriter writer2 = new FileWriter(outFile2,true);
				BufferedWriter out2 = new BufferedWriter(writer2);
				String lineOut = outFile + "," + fatherSplit.getAttr() + "," //分裂点属性
				+ fatherSplit.getValue() + ","	//分裂点的值
				+ child + "\r\n";
				writer2.write(lineOut);
				writer2.close();
				out2.close();
			}
		}
		
		if((bestSplit.getDepth() >= 2) || (noNodes)){
			for(i = 0; i < len; i++){
				val = getValue_record(attr, records[i]);
				if((val <= splitVal) && (!avgTreeL.equals("-"))){//――>L
					records[i].setY(records[i].getY()-Double.valueOf(avgTreeL));
				}
				if((val > splitVal) && (!avgTreeL.equals("-"))){//――>R
					records[i].setY(records[i].getY()-Double.valueOf(avgTreeR));
				}	
			}
			
			FileWriter writer = new FileWriter(outFile,true);
			BufferedWriter out = new BufferedWriter(writer);
			String lineOut = "";
			//writer.write("movie, box, dailyScreen, dailyScreenPct, -, themeScore, score\r\n");
			for(i = 0; i < len; i++){
				lineOut = records[i].getMovieName()+ ","
						+ records[i].getReleaseDate() + ","
						+ records[i].getBox() + ","
						+ records[i].getY() + ","
						+ records[i].getDailyScreenPct() + ","
						+ records[i].getThemeScore() + ","
						+ records[i].getScore() + ","
						+ records[i].getZhuyan() + ","
						+ records[i].getZhizuo() + ","
						+ records[i].getFaxing() + ","
						+ records[i].getDaoyan() + "\r\n";
				writer.write(lineOut);
			}
			writer.close();
			out.close();
			return 0;
		}	
		int attrs2[] = new int[6];
		for(int k = 0; k < 6; k ++){
			attrs2[k] = attrs[k];
		}
		trainTree(records1, len1, attrs, inFile, outFile, bestSplit, "L", objAttr);//bestSplit = father
		trainTree(records2, len2, attrs2, inFile, outFile, bestSplit, "R", objAttr);
		
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
				if(!temp[7].equals("-")){
					zhuyan = getPeopleScore(temp[7], "zhuyan", zhuyan_default);
					}
				else{zhuyan = zhuyan_default + "";}
				if(!temp[8].equals("-")){zhizuo = getPeopleScore(temp[8], "zhizuo", zhizuo_default);}
				else{zhizuo = zhizuo_default + "";}
				if(!temp[9].equals("-")){faxing = getPeopleScore(temp[9], "faxing", faxing_default);}
				else{faxing = faxing_default + "";}
				if(!temp[10].equals("-")){daoyan = getPeopleScore(temp[10], "daoyan", daoyan_default);}
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
			double res_sum_temp = 0;
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
			if(counter_find == 0){
				counter_find++;
				scoreStr = defaultValue + "";
			}
			
			for(i = counter_find; i < temp2.length; i++){
				scoreStr = scoreStr + "/" + defaultValue;
			}
			
			scores = scoreStr.split("/");
			for(i = 0; i < scores.length; i++){//score = 1-(1-a)*(1-b)...
//				res_temp = res_temp * (1 - Double.valueOf(scores[i]));
				res_sum_temp += Double.valueOf(scores[i]);
			}
			score = res_sum_temp / scores.length + "";

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
		double totalSum = 0, dailyScreenSum = 0, dailyScreenPctSum = 0;
		int counter = 0, counter_dailyScreen = 0, counter_dailyScreenPct = 0;
		double dailyScreen_default = 0, dailyScreenPct_default = 0;
		s = in.readLine();
		//name;date;box;dailyScreen;dailyScreenPct;themeScore;score;zhuyan;zhizuo;faxing;bianju;daoyan
		while((s=in.readLine())!=null){
			temp = s.split(",");
			if(!temp[3].equals("-") && !temp[4].equals("-")){
				dailyScreenSum += Double.valueOf(temp[3]);
				counter_dailyScreen ++;
				dailyScreenPctSum += Double.valueOf(temp[4]);
				counter_dailyScreenPct++;
			}
		}
		in.close();
		dailyScreen_default = dailyScreenSum/counter_dailyScreen;
		dailyScreenPct_default = dailyScreenPctSum/counter_dailyScreenPct;
		in = new BufferedReader(new FileReader(inFile));
		s = in.readLine();
		while((s = in.readLine())!= null){
			temp = s.split(",");
			
			if((!temp[5].equals("-"))){
			currentThemes = temp[5].split("/");//电影类型
			for(i = 0; i < currentThemes.length; i++){
				index = themes.indexOf(currentThemes[i]);
				if(index == -1){
					themes.add(currentThemes[i]);
					if(temp[3].equals("-")){
						themeScore_avg[themes.size()-1] = dailyScreen_default;
					}else
						themeScore_avg[themes.size()-1] = Double.valueOf(temp[3]);
					theme_counter[themes.size()-1] = 1;
				}
				else{
					if(temp[3].equals("-")){
						themeScore_avg[themes.size()-1] = dailyScreen_default;
					}else
						themeScore_avg[index] += Double.valueOf(temp[3]);
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
		
		in = new BufferedReader(new FileReader(inFile));
		writer = new FileWriter(outFile,true);
		out = new BufferedWriter(writer);
		s = in.readLine();
		double themeScore = 0;
		String releaseDay = "";
		String movieName, total, dailyScreen, dailyScreenPct, theme, score;
		writer.write("movie, date, total, dailyScreen, dailyScreenPct, themeScore, score, zhuyan, zhizuo, faxing, daoyan\r\n");
		int k;
		double themeSum_temp = 0;
		
		while((s = in.readLine())!=null){
			themeSum_temp = 0;
			temp = s.split(",");
			movieName = temp[0];
			total = temp[2];
			releaseDay = temp[1];
			dailyScreen = temp[3];
			dailyScreenPct = temp[4];
			if(dailyScreen.equals("-")){dailyScreen = dailyScreen_default + "";}
			if(dailyScreenPct.equals("-")){dailyScreenPct = dailyScreenPct_default + "";}
			theme = temp[5];
			score = temp[6];
			//----------计算themeScore-----------
			if(!theme.equals("-")){
				currentThemes = theme.split("/");
				for(j = 0; j < currentThemes.length; j++){
					k = themes.indexOf(currentThemes[j]);
					themeSum_temp += themeScore_avg[k];
				}
				themeScore = themeSum_temp/currentThemes.length;
			}else themeScore = themeScore_default;

			lineOut = movieName + "," + releaseDay + ","
					+ total +","
					+ dailyScreen + ","
					+ dailyScreenPct + ","
					+ themeScore + ","
					+ score + "," 
					+ temp[7] + ","
					+ temp[8] + ","
					+ temp[9] + ","
					+ temp[11] + "\r\n";
			writer.write(lineOut);
		}
		in.close();
		writer.close();
		out.close();
		return 0;
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

	public static int dealRelation() throws IOException{//连接，得到每个电影对应的box
		String fileName = "relation.csv";
		String fileName2 = "total3_addScore.csv";
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		BufferedReader in_total = new BufferedReader(new FileReader(fileName2));
		FileWriter writer = new FileWriter("newRelation.csv",true);
		BufferedWriter out = new BufferedWriter(writer);
		String s = in.readLine();
		String temp[], temp2[];
		String currentMovieName, name, relationType, s2;
		String dailyScreen = "", dailyScreenPct = "", score = "";
		int i;
		boolean isFind = false;
		String movieName = "", box = "", lineOut = "";
		writer.write("movie, name, type, dailyScreen, dailyScreenPct, score\r\n");
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
						dailyScreen = temp2[3];
						dailyScreenPct = temp2[4];
						score = temp2[6];
						isFind = true;
						break;
					}
				}
			}
			if(isFind){
				lineOut = movieName +"," + name + "," +relationType + ","
						+ dailyScreen + "," + dailyScreenPct + "," + score + "\r\n";
				writer.write(lineOut);
			}
		}
		in.close();
		in_total.close();
		writer.close();
		out.close();
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
		double dailyScreenSum = 0;
		String dailyScreenAvg = "-";
		int counter_dailyScreen = 0;
		double max = 0;
		Vector v_people = new Vector();
		String lineOut;
		while((s = in.readLine())!= null){
			temp = s.split(",");
			if((!temp[3].equals("-"))&&(temp[2].equals(relation)) && (v_people.indexOf(temp[1]) == -1)){//模糊匹配 eg.制作，制作人
				dailyScreenSum = Double.valueOf(temp[3]);
				counter_dailyScreen = 1;
				in2 = new BufferedReader(new FileReader("newRelation2.csv"));
				s2 = in2.readLine();
				while((s2 = in2.readLine())!=null){
					temp2 = s2.split(",");
					if((!temp2[3].equals("-"))&&(temp2[1].equals(temp[1]))
							&&(temp2[2].equals(relation))
							&&(!(temp[0].equals(temp2[0]))) ){
						v_people.addElement(temp[1]);
						if(!temp[3].equals("-")){
							dailyScreenSum += Double.valueOf(temp2[3]);
							counter_dailyScreen++;
						}
					}
				}
				in2.close();
				if(counter_dailyScreen != 0){dailyScreenAvg = (dailyScreenSum / counter_dailyScreen) + "";}
				lineOut = temp[1]+","+ dailyScreenAvg + "\r\n";
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
	
	

	public static double getMaxBox(String file, int column) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(file));
		double max = 0;
		String s;
		String temp[];
		double a;
		while((s = in.readLine())!=null){
			temp = s.split(",");
			a = Double.valueOf(temp[column]);
			if(a > max){
				max = a;
			}
		}
		in.close();
		return max;
	}
	
	
	static double getValue_record(int attr, MovieRecord record){
		double val = 0;
		if(attr == 0){val = record.getThemeScore();}
		if(attr == 1){val = record.getZhuyan();}
		if(attr == 2){val = record.getFaxing();}
		if(attr == 3){val = record.getZhizuo();}
		if(attr == 4){val = record.getDaoyan();}
		if(attr == 5){val = record.getReleaseDate();}
		return val;
	}
	
	//计算mse,求分割点
	public static Split getSplit_Attr(Split[] splits){//求分割点
		Split split = new Split();
		double mse = -1;
		int i, j;
		int len = splits.length;
		double value = 0;
		sort(splits, 0, len - 1);//递增排序
		int pos = 0;
		double avg_L = 0, avg_R = 0;// = getAvg(value);
		double mse_L = 0, mse_R = 0, mse_total = 0;
		for(i = 0; i < len; i++){//attr[i]为flag，<=attr[i]的为L
			avg_L = getAvg(splits, 0, i);
			avg_R = getAvg(splits, i+1, len-1);
			mse_L = 0;
			mse_R = 0;
			for(j = 0; j <= i; j++){
				mse_L += Math.pow((avg_L - splits[j].getY()), 2);
			}
			for(j = i+1; j <= len-1; j++){
				mse_R += Math.pow((avg_R - splits[j].getY()), 2);
			}
			mse_total = mse_L + mse_R;
			
			if((mse > mse_total)||(mse < 0)){
				//pos = i;
				mse = mse_total;
				value = splits[i].getValue();
			}
		}
		split.setMse(mse);
		split.setValue(value);
		return split;
	}
	
	public static double getNodeAvg(MovieRecord[] records, int begin, int end){
		double sum = 0;
		int counter = 0;
		double avg = 0;
		for(int i = begin; i <= end; i++){
			sum += records[i].getY();
			counter++;
		}
		avg = sum / counter;
		return avg;
	}
	
	
	public static double getAvg(Split[] splits, int begin, int end){
		double sum = 0;
		int counter = 0;
		double avg = 0;
		for(int i = begin; i <= end; i++){
			sum += splits[i].getY();
			counter++;
		}
		avg = sum / counter;
		return avg;
	}

	public static int partition(Split[] splits, int lo, int hi){
		 //固定的切分方式
        double key = splits[lo].getValue();
        double key2 = splits[lo].getY();
        while(lo<hi){
            while(splits[hi].getValue()>=key && hi>lo){//从后半部分向前扫描
                hi--;
            }
            splits[lo].setY(splits[hi].getY());
            splits[lo].setValue(splits[hi].getValue());
            while(splits[lo].getValue()<=key && hi>lo){//从前半部分向后扫描
                lo++;
            }
            splits[hi].setValue(splits[lo].getValue());
            splits[hi].setY(splits[lo].getY());
        }
        splits[hi].setValue(key);
        splits[hi].setY(key2);
        return hi;
    }
    
    public static void sort(Split[] splits, int lo ,int hi){
        if(lo>=hi){
            return ;
        }
        int index=partition(splits,lo,hi);
        sort(splits, lo,index-1);
        sort(splits, index+1,hi); 
    }
}

class Split{
	public int pos;
	public double value;
	public int attr;
	public double mse;//用左右所有元素计算得到
	public double y;
	public int depth;
	public double boxAvgL;
	public double boxAvgR;
	public int attrL;
	public int attrR;
	public double attrValueL;
	public double attrValueR; 
	public double predictL;
	public double predictR;

	public double getPredictL(){return predictL;}
	public void setPredictL(double avgTreeL){this.predictL =  predictL;}
	public double getPredictR(){return predictR;}	
	public void setPredictR(double avgTreeR){this.predictR =  avgTreeR;}


	public int getAttrL(){return attrL;}
	public void setAttrL(int attrL){this.attrL = attrL;}
	public int getAttrR(){return attrR;}	
	public void setAttrR(int attrR){this.attrR = attrR;}
	public double getAttrValueL(){return attrValueL;}
	public void setAttrValueL(double attrValueL){this.attrValueL = attrValueL;}
	public double getAttrValueR(){return attrValueR;}
	public void setAttrValueR(double attrValueR){this.attrValueR = attrValueR;}
	public int getDepth(){return depth;}
	public void setDepth(int depth){this.depth = depth;}
	public int getPos(){return pos;}  
	public void setPos(int pos){this.pos = pos;}  
	public double getValue(){return value; }  
	public void setValue(double value){this.value = value;}  
	public double getMse(){return mse;}  
	public void setMse(double mse){this.mse = mse;}  
	public int getAttr(){return attr;}  
	public void setAttr(int attr){this.attr = attr;}  
	public double getY(){return y;}
	public void setY(double y){this.y = y;}
}

class MovieRecord{
	public String movieName;
	public double releaseDate;
	public double y; //因变量，box, score, dailyScreen, dailyScreenPct;   =box时:票房或残差
	public double box;
	public double dailyScreen;
	public double dailyScreenPct;
	public double themeScore;
	public String score;
	public double predictedBox;//avg
	public double zhuyan;
	public double faxing;
	public double zhizuo;
	public double daoyan;
	public int depth;
	public double getBox(){return box;}
	public void setBox(double box){this.box = box;}
	public int getDepth(){return depth;}
	public void setDepth(int depth){this.depth = depth;}
	public String getMovieName(){return movieName;}
	public void setMovieName(String movieName){this.movieName = movieName;}
	public double getReleaseDate(){return releaseDate;}
	public void setReleaseDate(Double releaseDayScore){this.releaseDate = releaseDayScore;}
	public double getY(){return y;}
	public void setY(double y){this.y = y;}
	public double getDailyScreen(){return dailyScreen;}
	public void setDailyScreen(double dailyScreen){this.dailyScreen = dailyScreen;}
	public double getDailyScreenPct(){return dailyScreenPct;}
	public void setDailyScreenPct(double dailyScreenPct){this.dailyScreenPct = dailyScreenPct;}
	public double getThemeScore(){return themeScore;}
	public void setThemeScore(double themeScore){this.themeScore = themeScore;}
	public String getScore(){return score;}
	public void setScore(String score){this.score = score;}
	public double getPredictedBox(){return predictedBox;}
	public void setPredictedBox(double predictedBox){this.predictedBox = predictedBox;}
	public double getZhuyan(){return zhuyan;}
	public void setZhuyan(double zhuyan){this.zhuyan = zhuyan;}
	public double getFaxing(){return faxing;}
	public void setFaxing(double faxing){this.faxing = faxing;}
	public double getZhizuo(){return zhizuo;}
	public void setZhizuo(double zhizuo){this.zhizuo = zhizuo;}
	public double getDaoyan(){return daoyan;}
	public void setDaoyan(double daoyan){this.daoyan = daoyan;}
}