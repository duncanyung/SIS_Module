
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.sql.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author duncan
 */
public class computeTopKScore {
    
    public double computeIO_Score(ArrayList<ArrayList<String>> result,int i){
        double IO_score;
        if(result.size()<=10)
            IO_score=10-(i+1-1);
        else
            IO_score=10-((10*(i+1-1))/result.size());
        
        return IO_score;
    }
    
    public ArrayList<Double> getTopkFunction(Statement stmt, int userID,String[] features) {
        ArrayList<Double> topkF = new ArrayList<Double>();
        try {
            String sql = "Select * from topKFunctions where userID=" + userID + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                for (int i = 0; i < features.length; i++)
                    topkF.add(Double.parseDouble(rs.getString(features[i])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topkF;
    }
            
    
    public ArrayList<Double> getResourceQuality(Statement stmt, String[] features, int resourceID) {
        ArrayList<Double> quality = new ArrayList<Double>();
        try {
            //get resource quality from Global Service Quality Table based on resource in result
            //assume result.get(i).get(0) is resource id
            String sql = "select * from resources where resourceID=" + resourceID + ";";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                for (int j = 0; j < features.length - 1; j++) {
                    quality.add(Double.parseDouble(rs.getString(features[j])));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quality;
    }
    
    public double computeTopkScore(String[] features, ArrayList<Double> quality,ArrayList<Double> topkF,double IO_score){
        double score = 0;
        for (int j = 0; j < features.length - 1; j++)
            score = score + topkF.get(j) * quality.get(j);
        
        score = score + topkF.get(topkF.size() - 1) * IO_score;
        
        return score;
    }
    
    public ArrayList<Double> computeTopKScore(HttpServletResponse response, ArrayList<ArrayList<String>> result, int userID,String[] features) throws IOException {
        ArrayList<Double> topKScores = new ArrayList<Double>();
        try {
            //create DB connection
            DBConnection DBConn = new DBConnection(); 
            Statement stmt=DBConn.createDBStatement();

            //get top-k function based on userID
            ArrayList<Double> topkF=getTopkFunction(stmt,userID,features);            

            //for each resource in the result, we do following:
            for (int i = 0; i < result.size(); i++) {
                //get resource quality from global quality table baed on resourceID
                ArrayList<Double> quality=getResourceQuality(stmt,features,Integer.parseInt(result.get(i).get(0).trim()));
                
                //compute Input order score based on order of result from recommeder module
                double IO_score=computeIO_Score(result,i);
                
                //compute top-k score of a resource in result
                double topkScore=computeTopkScore(features,quality,topkF,IO_score);

                System.out.println("Score="+topkScore);
                topKScores.add(topkScore);
            }
            DBConn.close();
        } catch (Exception e) {
            System.out.println("Exception when computeTopKScore()");
            e.printStackTrace();   
        }

        //return top-k score
        return topKScores;
    }
}
