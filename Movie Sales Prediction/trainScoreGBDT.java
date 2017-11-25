import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.text.*;

// faxing, zhubian, zhuyan, daoyanԤ��score��dailyScreen, dailyScreenPercent

public class trainScoreGBDT
{
	public static void main(String[] args) throws IOException
	{
//		dealRelation();


//		dealPeople("����", "daoyan");//����ÿ����Ա��Ȩ��
//		dealPeople("����", "zhuyan");//����ÿ����Ա��Ȩ��
//		dealPeople("����", "zhizuo");//����ÿ����Ա��Ȩ��
//		dealPeople("������", "zhizuoren");//����ÿ����Ա��Ȩ��
//		dealPeople("����", "faxing");//����ÿ����Ա��Ȩ��
		
//		addScores();
//		dealPeopleScore();
//		addDate();
		
		String file = "total7_addDate.csv";
		int attrNum = 6;
		int i, j;
		int attrs[] = new int[attrNum];
		double mse = 0;
		int outFileIndex = 0;
		FileWriter writer2 = new FileWriter("tree_score.csv",true);
		BufferedWriter out2 = new BufferedWriter(writer2);
		writer2.write("tree, ���׽ڵ��������, ���׽ڵ��������ֵ, L or R, ��ǰ�������� , ��������ֵ, ��ֵL, ��ֵR\r\n");
		writer2.close();
		out2.close();
		//Ԥ��dailyScreen
		String lastMse = "";
		while(outFileIndex < 10){// themeScore	 zhuyan	 zhizuo	 faxing	 daoyan
			attrs[0] = -5;//theme
			attrs[1] = 7;//zhuyan
			attrs[2] = 8;//zhizuo
			attrs[3] = 9;//faxing
			attrs[4] = 10;//daoyan
			attrs[5] = 1;//releaseDay score
			mse = train(outFileIndex, attrs, 6);
			if(lastMse.equals(mse + "")){break;}
			lastMse = mse + "";
			System.out.println(mse+"-----"+outFileIndex);
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
			boxes[month-1] += Double.valueOf(temp[6]);	
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
			deviations[month-1] += Math.pow((Double.valueOf(temp[6]) - avgs[month-1]), 2);
		}
		for(i = 0; i < 12; i++){
			month = i+1;
			deviations[i]/=counters[i];
			deviations[i] = Math.sqrt(deviations[i]);
//			System.out.println("month: "+month + "; avg = " + avgs[i] + "; dev=" + deviations[i]);
		}
		return avgs;
	}
	
	//attrs ���Լ�
	public static double train(int outFileIndex, int attrs[], int objAttr) throws IOException{//ѵ��һ������
		String inFile = "", outFile = outFileIndex + ".csv";
		int inFileIndex = 0;
		
		//--------------���в�----------------
		if(outFileIndex == 0){
			inFile = "total7_addDate.csv";
		}else {
			inFileIndex = outFileIndex - 1;
			inFile = inFileIndex + ".csv";
		}
		
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		String s;
		int maxArraySize = 1300;
		String temp[];
		MovieRecord[] records = new MovieRecord[maxArraySize];
		if(inFile.equals("total7_addDate.csv")){
			s = in.readLine();//ֻ�е�һ���ļ���title
		}
		int j = 0;
		double box, themeScore, zhuyan, zhizuo, faxing, daoyan;
		String movieName, dailyScreen, dailyScreenPct;
		double score;
		Double releaseDayScore;

		//���ļ���ȡ��������
		while((s = in.readLine())!=null){
			temp = s.split(",");
			movieName = temp[0];
			releaseDayScore = Double.valueOf(temp[1]);
			box = Double.valueOf(temp[2]);
			dailyScreen = temp[3];
			dailyScreenPct = temp[4];
			themeScore = Double.valueOf(temp[5]);
			score = Double.valueOf(temp[6]);
			zhuyan = Double.valueOf(temp[7]);
			zhizuo = Double.valueOf(temp[8]);
			faxing = Double.valueOf(temp[9]);
			daoyan = Double.valueOf(temp[10]);
			records[j] = new MovieRecord();
			records[j].setMovieName(movieName);
			records[j].setReleaseDate(releaseDayScore);
			records[j].setBox(box);
//			if(objAttr == 2){records[j].setY(box);}
//			if(objAttr == 6){records[j].setY(score);}
//			if(objAttr == 3){records[j].setY(dailyScreen);}
//			if(objAttr == 4){records[j].setY(dailyScreenPct);}
			records[j].setY(score);
			records[j].setDailyScreen(dailyScreen);
			records[j].setDailyScreenPct(dailyScreenPct);
			records[j].setThemeScore(themeScore);
			records[j].setScore(score);
			records[j].setZhuyan(zhuyan);
			records[j].setZhizuo(zhizuo);
			records[j].setFaxing(faxing);
			records[j].setDaoyan(daoyan);
			j++;
		}//j: length
		in.close();
		
		Split split = new Split();
		split.setAttr(-1);
		
//		System.out.println("---"+attrs[0]+"-"+attrs[1]+"-"+attrs[2]+"-"+attrs[3]+"-");
		trainTree(records, j, attrs, inFile, outFile, split, "-", objAttr);
		//----------�������ɵ��ļ���mse-----------
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
		return mse;//����mse
	}

	//lenָ��������Ч����  treeType: R or L or "-"
	public static double trainTree(MovieRecord records[], int len, 
			int attrs[], String inFile, String outFile, Split fatherSplit, String treeType, int objAttr) throws IOException{//ѵ������
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
				split = getSplit_Attr(splits);//��ָ��
				if((bestSplit.getMse() < 0) || (split.getMse() < bestSplit.getMse())){
					bestSplit.setAttr(i);//��i������Ϊ�ָ��
					bestSplit.setValue(split.getValue());
					bestSplit.setMse(split.getMse());
				}
			}
		}//��ǰ�ָ��Ϊ����attr[i]�ģ�ֵΪsplit.getvalue()�ĵ�

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
		attrs[attr] = -1;
		MovieRecord records1[] = new MovieRecord[len];
		MovieRecord records2[] = new MovieRecord[len];
		int len1 = 0, len2 = 0;
		double val;

		for(i = 0; i < len; i++){
			val = getValue_record(attr, records[i]);
			if(val <= splitVal){//�D�D>L
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
//				records1[len1].setDepth(records[i].getDepth()+1);
				len1++;
			}else{//�D�D>R
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
//				records2[len2].setDepth(records[i].getDepth()+1);
				len2++;
			}	
		}
//		int attrCounter = 0;
//		for(i = 0; i < attrs.length; i++){
//			if(attrs[i] > 0){
//				attrCounter++;
//			}
//		}
		double mse = 0;
		boolean noNodes = false;
		String avgTreeL = "-", avgTreeR = "-";
		if(len1 >= 1){
			avgTreeL = getNodeAvg(records1, 0, len1-1) + "";
			fatherSplit.setPredictL(Double.valueOf(avgTreeL));//��������ֵ
		}
		if(len2>=1){
			avgTreeR = getNodeAvg(records2, 0, len2-1) + "";
			fatherSplit.setPredictR(Double.valueOf(avgTreeR));//��������ֵ
		}
		if(!((len1 >= 1) && (len2 >= 1))){
//			System.out.println(len1+"~~~"+len2);
			noNodes = true;
//			return 0;
		}
		
		
//		if((len1 >= 1) && (len2>=1)){
//			avgTreeL = getNodeAvg(records1, 0, len1-1) + "";
//			fatherSplit.setPredictL(Double.valueOf(avgTreeL));//��������ֵ
//			avgTreeR = getNodeAvg(records2, 0, len2-1) + "";
//			fatherSplit.setPredictR(Double.valueOf(avgTreeR));//��������ֵ
//		}
//		System.out.println(avgTreeL+"---"+avgTreeR);
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
			if((fatherSplit.getAttr()!=-1)){//bestSplit���Ǹ��ڵ�
				String outFile2 = "tree_score.csv";
				FileWriter writer2 = new FileWriter(outFile2,true);
				BufferedWriter out2 = new BufferedWriter(writer2);
				String lineOut = outFile + "," + fatherSplit.getAttr() + "," //���ѵ�����
				+ fatherSplit.getValue() + ","	//���ѵ��ֵ
				+ child + "\r\n";
	//			System.out.println(lineOut);
				writer2.write(lineOut);
				writer2.close();
				out2.close();
			}
		}
		
		if(bestSplit.getDepth() >= 2 || noNodes){
			for(i = 0; i < len; i++){
				val = getValue_record(attr, records[i]);
				if(val <= splitVal){//�D�D>L
					records[i].setY(records[i].getY()-Double.valueOf(avgTreeL));
				}else{//�D�D>R
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
						+ records[i].getDailyScreen() + ","
						+ records[i].getDailyScreenPct() + ","
						+ records[i].getThemeScore() + ","
						+ records[i].getY() + ","
						+ records[i].getZhuyan() + ","
						+ records[i].getZhizuo() + ","
						+ records[i].getFaxing() + ","
						+ records[i].getDaoyan() + "\r\n";
				writer.write(lineOut);
			}
			writer.close();
			out.close();
			return 0;
			//return mse;
		}	
//		String lineOut = "";
//
//		if((len1 <= 1)||(len2 <= 1)){
//			int attrs3[] = new int[5];
//			for(int k = 0; k < 5; k++){
//				attrs3[k] = attrs[k];
//			}
//			FileWriter writer2 = new FileWriter(outFile,true);
//			BufferedWriter out2 = new BufferedWriter(writer2);
//			if(len1 == 1){
//				records1[0].setY(records1[0].getY()-Double.valueOf(avgTreeL));
//				lineOut = records1[0].getMovieName()+ ","
//						+ records1[0].getReleaseDate() + ","
//						+ records1[0].getBox() + ","
//						+ records1[0].getY() + ","
//						+ records1[0].getDailyScreenPct() + ","
//						+ records1[0].getThemeScore() + ","
//						+ records1[0].getScore() + ","
//						+ records1[0].getZhuyan() + ","
//						+ records1[0].getZhizuo() + ","
//						+ records1[0].getFaxing() + ","
//						+ records1[0].getDaoyan() + "\r\n";
//				writer2.write(lineOut);
//				trainTree(records2, len2, attrs3, inFile, outFile, bestSplit, "R", objAttr);
//			}
//			if(len2 == 1){
//				records2[0].setY(records2[0].getY()-Double.valueOf(avgTreeR));
//				lineOut = records2[0].getMovieName()+ ","
//						+ records2[0].getReleaseDate() + ","
//						+ records2[0].getBox() + ","
//						+ records2[0].getY() + ","
//						+ records2[0].getDailyScreenPct() + ","
//						+ records2[0].getThemeScore() + ","
//						+ records2[0].getScore() + ","
//						+ records2[0].getZhuyan() + ","
//						+ records2[0].getZhizuo() + ","
//						+ records2[0].getFaxing() + ","
//						+ records2[0].getDaoyan() + "\r\n";
//				writer2.write(lineOut);
//				trainTree(records1, len1, attrs3, inFile, outFile, bestSplit, "L", objAttr);
//			}
//			writer2.close();
//			out2.close();
//		}
//		if((bestSplit.getDepth() >= 2)){
//			for(i = 0; i < len; i++){
//				val = getValue_record(attr, records[i]);
//				if(val <= splitVal){//�D�D>L
//					records[i].setY(records[i].getY()-Double.valueOf(avgTreeL));
//				}else{//�D�D>R
//					records[i].setY(records[i].getY()-Double.valueOf(avgTreeR));
//				}	
//			}
//			
//			FileWriter writer = new FileWriter(outFile,true);
//			BufferedWriter out = new BufferedWriter(writer);
//			
//			//writer.write("movie, box, dailyScreen, dailyScreenPct, -, themeScore, score\r\n");
//			for(i = 0; i < len; i++){
//				lineOut = records[i].getMovieName()+ ","
//						+ records[i].getReleaseDate() + ","
//						+ records[i].getBox() + ","
//						+ records[i].getY() + ","
//						+ records[i].getDailyScreenPct() + ","
//						+ records[i].getThemeScore() + ","
//						+ records[i].getScore() + ","
//						+ records[i].getZhuyan() + ","
//						+ records[i].getZhizuo() + ","
//						+ records[i].getFaxing() + ","
//						+ records[i].getDaoyan() + "\r\n";
//				writer.write(lineOut);
//			}
//			writer.close();
//			out.close();
//			return 0;
//			//return mse;
//		}	
		int attrs2[] = new int[6];
		for(int k = 0; k < 6; k ++){
			attrs2[k] = attrs[k];
		}
		trainTree(records1, len1, attrs, inFile, outFile, bestSplit, "L", objAttr);//bestSplit = father
		trainTree(records2, len2, attrs2, inFile, outFile, bestSplit, "R", objAttr);
		return 0;
	}
	
	//���ڱ������ʵ�����٣�������ֱ��ɾ��
		public static int dealPeopleScore() throws IOException{//����bianju.csv���ļ�������themeScore,peopleScore
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
//				System.out.println(lineOut);
				writer.write(lineOut);
			}
			in.close();
			out.close();
			writer.close();
			return 0;
		}
		

		public static String getPeopleScore(String names, String relation, double defaultValue) throws IOException{//names: temp[6]��
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
				temp = s.split(",");//temp[0]����  temp[1]score
				for(i = 0; i < temp2.length; i++){//��temp2[i]��infile����
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
			for(i = counter_find; i < temp2.length; i++){
				scoreStr = scoreStr + "/" + defaultValue;
			}
			
			scores = scoreStr.split("/");
			for(i = 0; i < scores.length; i++){//score = 1-(1-a)*(1-b)...
//				res_temp = res_temp * (1 - Double.valueOf(scores[i]));
				res_sum_temp += Double.valueOf(scores[i]);
			}
			score = res_sum_temp / scores.length + "";
//			score = 1-res_temp + "";
//			System.out.println(score);
			return score;
		}

	public static int addScores() throws IOException{//����theme score, screen �ȵ�Ĭ��ֵ
		String inFile = "total4_addPeople.csv";
		String outFile = "total5_addScores.csv";
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		//FileWriter writer = new FileWriter(outFile,true);
		//BufferedWriter out = new BufferedWriter(writer);
		String temp[], currentThemes[];
		//��������Ӱ��Ȩֵ���ø����Ӱ��ƽ��Ʊ�����
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
			if((!temp[5].equals("-"))&&(!temp[6].equals("-"))){
			currentThemes = temp[5].split("/");//��Ӱ����
			for(i = 0; i < currentThemes.length; i++){
				//currentThemes = current
				index = themes.indexOf(currentThemes[i]);
				if(index == -1){
					themes.add(currentThemes[i]);
					themeScore_avg[themes.size()-1] = Double.valueOf(temp[6]);
					theme_counter[themes.size()-1] = 1;
				}
				else{
					themeScore_avg[index] += Double.valueOf(temp[6]);
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
		//Ĭ��ֵ����ӦthemeΪ"-"�ĵ�Ӱ
		double themeScore_default = sum_total/counter_total;
		for(i = 0; i < themes.size(); i++){
			//ÿһ���Ӱ��score
			themeScore_avg[i] = themeScore_avg[i]/theme_counter[i];
		}
		//----------�洢ÿ�����͵�Ӱ��Ȩֵ-------
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
		//String temp[];
		double themeScore = 0;
		String releaseDay = "";
		String movieName, total, dailyScreen, dailyScreenPct, theme, score;
		writer.write("movie, date, total, dailyScreen, dailyScreenPct, themeScore, score, zhuyan, zhizuo, faxing, daoyan\r\n");
		int k;
		double themeSum_temp = 0;
		
		while((s = in.readLine())!=null){
			themeSum_temp = 0;
			temp = s.split(",");
//			if(temp[1].equals("0.1")){
//				System.out.println(s);
//			}
			if(!temp[6].equals("-")){
				movieName = temp[0];
				total = temp[2];
				releaseDay = temp[1];
				dailyScreen = temp[3];
				dailyScreenPct = temp[4];
				theme = temp[5];
				score = temp[6];
				//----------����themeScore-----------
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
//				System.out.println(lineOut);
				writer.write(lineOut);
			}
		}
		in.close();
		writer.close();
		out.close();
		return 0;
	}
	

	
	//��i������ֵ
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
	
	
	public static String dealMovieName(String movieName){
		movieName = movieName.trim();
		movieName = movieName.replace(",", "");  //ȥ�����б��
		movieName = movieName.replace("��", "");  //ȥ�����б��
		movieName = movieName.replace("��", "");  //ȥ�����б��
		movieName = movieName.replace("?", "");  //ȥ�����б��
		movieName = movieName.replace("��", "");
		movieName = movieName.replace(";", "");
		movieName = movieName.replace("��", "");
		movieName = movieName.replace("\"", "");
		movieName = movieName.replace("&middot;", "");
		movieName = movieName.replace("&middot", "");
		movieName = movieName.replace("��", "");
		
		int pos = movieName.indexOf(" ");
		if(pos > 0){
			movieName = movieName.substring(0, pos);
		}
		return movieName;
	}

	public static int dealRelation() throws IOException{//���ӣ��õ�ÿ����Ӱ��Ӧ��box
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
				//��total������box
				in_total = new BufferedReader(new FileReader(fileName2));
				while((s2 = in_total.readLine())!=null){
					temp2 = s2.split(",");
					if(temp2[0].equals(movieName)){
//						box = temp2[2];//getBox
						dailyScreen = temp2[3];
						dailyScreenPct = temp2[4];
						score = temp2[6];
						//System.out.println(box);
						isFind = true;
						break;
					}
				}
			}
			if(isFind){
				lineOut = movieName +"," + name + "," +relationType + ","
						+ dailyScreen + "," + dailyScreenPct + "," + score + "\r\n";
//				System.out.println(lineOut);
				writer.write(lineOut);
			}
		}
		in.close();
		in_total.close();
		writer.close();
		out.close();
		return 0;
	}
	
	
	
	

	//relation = ����|����|����|����|���
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
		double scoreSum = 0;
		String scoreAvg = "-";
		int counter_score = 0;
		double max = 0;
		Vector v_people = new Vector();
		String lineOut;
		while((s = in.readLine())!= null){
			temp = s.split(",");
			if((!temp[5].equals("-"))&&(temp[2].equals(relation)) && (v_people.indexOf(temp[1]) == -1)){//ģ��ƥ�� eg.������������
				scoreSum = Double.valueOf(temp[5]);
				counter_score = 1;
				in2 = new BufferedReader(new FileReader("newRelation2.csv"));
				s2 = in2.readLine();
				while((s2 = in2.readLine())!=null){
					temp2 = s2.split(",");
					if((!temp2[5].equals("-"))&&(temp2[1].equals(temp[1]))
							&&(temp2[2].equals(relation))
							&&(!(temp[0].equals(temp2[0]))) ){
						v_people.addElement(temp[1]);
						if(!temp[5].equals("-")){
							scoreSum += Double.valueOf(temp2[5]);
							counter_score++;
						}
					}
				}
				in2.close();
				if(counter_score != 0){scoreAvg = (scoreSum / counter_score) + "";}
				lineOut = temp[1]+","+ scoreAvg + "\r\n";
				writer.write(lineOut);
			}	
		}
		in.close();
		writer.close();
		out.close();
		
//		max = getMaxBox(outFile1, 1);
//		System.out.println(max);
		double score;
		String outFile = outFileName+".csv";
		in = new BufferedReader(new FileReader(outFile1));
		writer = new FileWriter(outFile,true);
		out = new BufferedWriter(writer);
		while((s = in.readLine())!=null){
			temp = s.split(",");
			score = Double.valueOf(temp[1]);
//			score = Double.valueOf(temp[1]) / max;
			lineOut = temp[0] + "," + score + "\r\n";
			writer.write(lineOut);
//			System.out.println(lineOut);
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
	
	//����mse,��ָ��
	public static Split getSplit_Attr(Split[] splits){//��ָ��
		Split split = new Split();
		double mse = -1;
		int i, j;
		int len = splits.length;
		double value = 0;
		sort(splits, 0, len - 1);//��������
		int pos = 0;
		double avg_L = 0, avg_R = 0;// = getAvg(value);
		double mse_L = 0, mse_R = 0, mse_total = 0;
		for(i = 0; i < len; i++){//attr[i]Ϊflag��<=attr[i]��ΪL
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
		//System.out.println("======="+pos+"----"+mse);
		//split.setPos(pos);
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
//		System.out.println("end = " +end + "---" +avg);
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
		 //�̶����зַ�ʽ
        double key = splits[lo].getValue();
        double key2 = splits[lo].getY();
        while(lo<hi){
            while(splits[hi].getValue()>=key && hi>lo){//�Ӻ�벿����ǰɨ��
                hi--;
            }
            splits[lo].setY(splits[hi].getY());
            splits[lo].setValue(splits[hi].getValue());
            while(splits[lo].getValue()<=key && hi>lo){//��ǰ�벿�����ɨ��
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
	public double mse;//����������Ԫ�ؼ���õ�
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
	public double y; //�������box, score, dailyScreen, dailyScreenPct;   =boxʱ:Ʊ����в�
	public double box;
	public String dailyScreen;
	public String dailyScreenPct;
	public double themeScore;
	public double score;
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
	public String getDailyScreen(){return dailyScreen;}
	public void setDailyScreen(String dailyScreen){this.dailyScreen = dailyScreen;}
	public String getDailyScreenPct(){return dailyScreenPct;}
	public void setDailyScreenPct(String dailyScreenPct){this.dailyScreenPct = dailyScreenPct;}
	public double getThemeScore(){return themeScore;}
	public void setThemeScore(double themeScore){this.themeScore = themeScore;}
	public double getScore(){return score;}
	public void setScore(double score){this.score = score;}
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