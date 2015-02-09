/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author duncan
 */
public class SIS_Module extends HttpServlet {
    String[] features = qualityTableFeatures.features;
    String URLforUpdateDB = "http://localhost:8080/SIS_Module/updateDB";
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SIS_Module</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SIS_Module at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    public boolean isTextNode(Node n) {
        return n.getNodeName().equals("#text");
    }

    public void makeAndSendHTMLResponse(HttpServletResponse response,ArrayList<ArrayList<String>> result,int userID){
        try{
            PrintWriter out = response.getWriter();
            response.setContentType("Text/html");
            out.println();
            //out.println("<%@ page language='java' contentType='text/html; charset=ISO-8859-1' pageEncoding='ISO-8859-1'%>");
            //out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>");

            out.println("<html>");
            out.println("<head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=ISO-8859-1'>");
            out.println("<title>Login</title>");
            out.println("</head>");
            out.println("<body>");

            out.println("<h1> SIS Module Result </h1>");
            out.println("<body>");

            out.println("<form action='" + URLforUpdateDB + "' method='post'>");
            out.println("<INPUT VALUE='"+result.size()+"' TYPE='hidden' NAME='resultSize'>");
            out.println("<INPUT VALUE='"+userID+"' TYPE='hidden' NAME='userID'>");
            for(int i=0; i<result.size(); i++){
                out.println((i+1)+"."+result.get(i).get(0)+" "+result.get(i).get(1)+"<br>");
                String htmlStat="<p><b>Feedback(0-10):</b>";
                for(int j=0; j<features.length-1; j++)
                    htmlStat=htmlStat+"     "+features[j]+"<INPUT size='5' TYPE='text' NAME='"+features[j]+(i+1)+"'>";

                htmlStat=htmlStat+"<INPUT VALUE='"+result.get(i).get(0)+"' TYPE='hidden' NAME='ID"+(i+1)+"'></p>";
                out.println(htmlStat);
            }

            out.println("<input type='submit' value='Submit Feedback'></input>  <input type='reset' value='Reset'></input>");
            out.println("</form>");
            out.println("<p></p>");
            //out.println("<p><a href='register.jsp'>register</a></p>");

            out.println("</body>");
            out.println("</html>");
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    //this function sort the result according to the topKScore of the result
    //[Duncan] not checked the customComparator!!!
    public ArrayList<ArrayList<String>> sortResultBasedOnTopKScore(ArrayList<ArrayList<String>> result,ArrayList<Double> topKScore){
        ArrayList<pair> pArray=new ArrayList<pair>();
        for(int i=0; i<result.size(); i++){
            pair p=new pair(result.get(i),topKScore.get(i));
            pArray.add(p);
        }
        Collections.sort(pArray,new CustomComparator());

        ArrayList<ArrayList<String>> sortedResult= new ArrayList<ArrayList<String>>();
        for(int i=0; i<pArray.size(); i++)
            sortedResult.add(pArray.get(i).first);
        
        return sortedResult;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //read recommender module result from xml file and put it into result
        readXML XMLReader = new readXML();
        ArrayList<ArrayList<String>> result = XMLReader.readXML(response);
        
        //assume userID is 1 (need to read it from response in the future)
        int userID=Integer.parseInt(request.getParameter("userID"));
        
        //compute topk score for all result
        computeTopKScore comTopkScore = new computeTopKScore();
        ArrayList<Double> topkScores = comTopkScore.computeTopKScore(response,result,userID,features);

        //sort recommender result based on topkScores (high to low)
        result=sortResultBasedOnTopKScore(result,topkScores);//arrayList is pass by reference
        
        //display result to user and a jsp form to get user feedback 
        //(the form is submitted to another servlet to do update)
        makeAndSendHTMLResponse(response,result,userID);
        
        System.out.println("finish doGet in SISModule.java");
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
