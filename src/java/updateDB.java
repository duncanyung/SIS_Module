/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author duncan
 */
public class updateDB extends HttpServlet {

    String[] features = qualityTableFeatures.features;

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
            out.println("<title>Servlet updateDB</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet updateDB at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
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
        processRequest(request, response);
    }

    public ArrayList<String> getFBFromRequest(HttpServletRequest request,int i) {
        ArrayList<String> FB = new ArrayList<String>();
        for (int j = 0; j < features.length - 1; j++)
            FB.add(request.getParameter(features[j] + (i + 1)));
        
        return FB;
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
        
        try {
            int resultSize = Integer.parseInt(request.getParameter("resultSize"));

            //get DB connection and statement
            DBConnection DBConn = new DBConnection();            
            Statement stmt1 = DBConn.createDBStatement();
            Statement stmt2 = DBConn.createDBStatement();

            //do following for every resource in the result set
            for (int i = 0; i < resultSize; i++) {
                //get FB of a resource from user(request)
                ArrayList<String> FB = getFBFromRequest(request,i);               

                int resourceID = Integer.parseInt(request.getParameter("ID" + (i + 1)).trim());

                System.out.println("Feedback from User: ResourceID=" + resourceID + " Service Quality=" + FB.get(0)
                        + " Price=" + FB.get(1) + " Transportation=" + FB.get(2) + " Content=" + FB.get(3));

                //update values in resource quality table
                updateResourcesTable(stmt1, stmt2, resourceID, FB);
            }
            //update the top-k function for the user
            updateTopKFunctionTable(stmt1, stmt2, request);
            DBConn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateResourcesTable(Statement stmt1, Statement stmt2, int resourceID, ArrayList<String> FB) {
        try {
            String sql = "Select * from resources where resourceID=" + resourceID + ";";
            ResultSet rs = stmt1.executeQuery(sql);

            while (rs.next()) {
                //read attribute and count from DB
                ArrayList<Double> counts = new ArrayList<Double>();
                ArrayList<Double> quality = new ArrayList<Double>();
                for (int i = 0; i < features.length - 1; i++) {
                    quality.add(Double.parseDouble(rs.getString(features[i])));
                    counts.add(Double.parseDouble(rs.getString(features[i] + "_count")));
                }

                for (int i = 0; i < FB.size(); i++) {
                    if (!FB.get(i).equals("")) {
                        quality.set(i, (quality.get(i) * counts.get(i) + Double.parseDouble(FB.get(i)) / (counts.get(i) + 1)));
                        counts.set(i, counts.get(i) + 1);
                    }
                }

                sql = "";
                //update the ResourcesTable using update sql statement
                for (int i = 0; i < features.length - 1; i++) {
                    sql = sql + features[i] + "='" + quality.get(i) + "'," + features[i] + "_count='" + counts.get(i) + "',";
                }

                //features array has 1 more entry(IO_Score) than quality,counts array
                sql = "UPDATE resources SET " + sql + features[features.length - 2] + "='" + quality.get(features.length - 2) + "',"
                        + features[features.length - 2] + "_count='" + counts.get(features.length - 2)
                        + "' WHERE resourceID=" + resourceID + ";";

                stmt2.executeUpdate(sql);
                System.out.println("Update Resources Table DONE!");
            }
        } catch (Exception e) {
            System.out.println("Exception in updateResourcesTable()");
            e.printStackTrace();
        }
    }

    public void computeFeedbackAndQuality(HttpServletRequest request, Statement stmt1, int resultSize, ArrayList<Double> diff, ArrayList<Double> avgDiff, ArrayList<Double> qualityT, ArrayList<Integer> FBCount) {
        try {
            for (int i = 0; i < resultSize; i++) {
                ArrayList<String> FB = new ArrayList<String>();
                for (int j = 0; j < features.length - 1; j++) {
                    FB.add(request.getParameter(features[j] + (i + 1)));
                }

                int resourceID = Integer.parseInt(request.getParameter("ID" + (i + 1)).trim());

                String sql = "Select * from resources where resourceID=" + resourceID + ";";
                ResultSet rs = stmt1.executeQuery(sql);

                while (rs.next()) {
                    //read attribute and count from DB
                    for (int j = 0; j < features.length - 1; j++) {
                        qualityT.add(Double.parseDouble(rs.getString(features[j])));
                    }
                }

                //initialize diff
                for (int j = 0; j < features.length - 1; j++) {
                    diff.add(0.0);
                    FBCount.add(0);
                }

                for (int j = 0; j < features.length - 1; j++) {
                    if (!FB.get(j).equals("")) {
                        diff.set(j, diff.get(j) + qualityT.get(j) - Double.parseDouble(FB.get(j)));
                        FBCount.set(j, FBCount.get(j) + 1);
                    }
                }
            }

            //compute avg. diff of feedbacks and values in quality table for each feature
            for (int j = 0; j < features.length - 1; j++) {
                if (FBCount.get(j) != 0) {
                    avgDiff.add(diff.get(j) / (double) FBCount.get(j));
                } else {
                    avgDiff.add(0.0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Double> computeNewTopKFunction(Statement stmt2, int userID, ArrayList<Double> avgDiff) {
        ArrayList<Double> newTopkF = new ArrayList<Double>();
        try {
            String sql = "Select * from topKFunctions where userID=" + userID + ";";
            ResultSet rs = stmt2.executeQuery(sql);
            while (rs.next()) {
                for (int i = 0; i < features.length - 1; i++)//IO_Score not need update (only normalize)
                    newTopkF.add(Double.parseDouble(rs.getString(features[i])) * (1 + avgDiff.get(i) / 10));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newTopkF;
    }

    public void normalizeTopKfunction(ArrayList<Double> newTopkF) {
        double sum = 0;
        //3.1 find the sum
        for (int i = 0; i < newTopkF.size(); i++)
            sum = sum + newTopkF.get(i);
        
        //3.2 normalize
        for (int i = 0; i < newTopkF.size(); i++)
            newTopkF.set(i, newTopkF.get(i) / sum);
    }

    public void writeToTopkfunctionTable(ArrayList<Double> newTopkF, int userID, Statement stmt2) {
        try {
            String sql = "";
            //update the ResourcesTable using update sql statement
            for (int i = 0; i < features.length - 1; i++)
                sql = sql + features[i] + "='" + newTopkF.get(i) + "',";

            //features array has 1 more entry(IO_Score) than quality,counts array
            sql = "UPDATE topKFunctions SET "
                    + sql + features[features.length - 2] + "='" + newTopkF.get(features.length - 2) + "'"
                    + " WHERE userID=" + userID + ";";

            stmt2.executeUpdate(sql);
        } catch (Exception e) {

        }
    }

    public void updateTopKFunctionTable(Statement stmt1, Statement stmt2, HttpServletRequest request) {
        try {
            int resultSize = Integer.parseInt(request.getParameter("resultSize"));
            int userID = Integer.parseInt(request.getParameter("userID"));

            try {
                ArrayList<Double> diff = new ArrayList<Double>();
                ArrayList<Double> avgDiff = new ArrayList<Double>();
                ArrayList<Double> qualityT = new ArrayList<Double>();
                ArrayList<Integer> FBCount = new ArrayList<Integer>();

                //1. for each attribute, compute avg diff between feedback and resource quality
                computeFeedbackAndQuality(request, stmt1, resultSize, diff, avgDiff, qualityT, FBCount);

                //2. update top-k function based on avg diff
                ArrayList<Double> newTopkF = computeNewTopKFunction(stmt2, userID, avgDiff);

                //3. normalize top-k function
                normalizeTopKfunction(newTopkF);

                //4. write it back to top-k function table
                writeToTopkfunctionTable(newTopkF,userID,stmt2);

                System.out.println("Update TopKFunction Table DONE!");

            } catch (Exception e) {
                System.out.println("Exception in updateTopKFunctionTable()");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception when updateTopKFunctionTable");
        }
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
