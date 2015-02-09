
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author duncan
 */
public class readXML {
    public ArrayList<ArrayList<String>> readXML(HttpServletResponse response) throws IOException{
        ArrayList<ArrayList<String>> result= new ArrayList<ArrayList<String>>();
        
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse("http://localhost:8080/SIS_Module/recommenderModuleResult.xml");
            //           out.println("<table border=2><tr><th>Name</th><th>Address</th></tr>");
            NodeList id = doc.getElementsByTagName("id");
            NodeList name = doc.getElementsByTagName("name");
            NodeList description = doc.getElementsByTagName("description");
            NodeList url = doc.getElementsByTagName("url");
            NodeList film_director = doc.getElementsByTagName("film_director");
            NodeList film_director_type = doc.getElementsByTagName("film_director_type");
            NodeList movie_type = doc.getElementsByTagName("movie_type");
            NodeList movie_genre1 = doc.getElementsByTagName("movie_genre1");
            NodeList movie_genre2 = doc.getElementsByTagName("movie_genre2");
            NodeList movie_genre3 = doc.getElementsByTagName("movie_genre3");
            NodeList country_of_origin = doc.getElementsByTagName("country_of_origin");
            NodeList year_of_product = doc.getElementsByTagName("year_of_production");
            NodeList content_advice = doc.getElementsByTagName("content_advice");

            for (int i = 0; i <id.getLength(); i++) {
                System.out.println(id.item(i).getFirstChild().getNodeValue());
                System.out.println(name.item(i).getFirstChild().getNodeValue());
                System.out.println(description.item(i).getFirstChild().getNodeValue());
                System.out.println(url.item(i).getFirstChild().getNodeValue());
                System.out.println(film_director.item(i).getFirstChild().getNodeValue());
                System.out.println(film_director_type.item(i).getFirstChild().getNodeValue());
                System.out.println(movie_type.item(i).getFirstChild().getNodeValue());
                System.out.println(movie_genre1.item(i).getFirstChild().getNodeValue());
                System.out.println(movie_genre2.item(i).getFirstChild().getNodeValue());
                System.out.println(movie_genre3.item(i).getFirstChild().getNodeValue());
                System.out.println(country_of_origin.item(i).getFirstChild().getNodeValue());
                System.out.println(year_of_product.item(i).getFirstChild().getNodeValue());
                System.out.println(content_advice.item(i).getFirstChild().getNodeValue());
                ArrayList<String> temp= new ArrayList<String>();
                temp.add(id.item(i).getFirstChild().getNodeValue());
                temp.add(name.item(i).getFirstChild().getNodeValue());
                temp.add(description.item(i).getFirstChild().getNodeValue());
                temp.add(url.item(i).getFirstChild().getNodeValue());
                temp.add(film_director.item(i).getFirstChild().getNodeValue());
                temp.add(film_director_type.item(i).getFirstChild().getNodeValue());
                temp.add(movie_type.item(i).getFirstChild().getNodeValue());
                temp.add(movie_genre1.item(i).getFirstChild().getNodeValue());
                temp.add(movie_genre2.item(i).getFirstChild().getNodeValue());
                temp.add(movie_genre3.item(i).getFirstChild().getNodeValue());
                temp.add(country_of_origin.item(i).getFirstChild().getNodeValue());
                temp.add(year_of_product.item(i).getFirstChild().getNodeValue());
                temp.add(content_advice.item(i).getFirstChild().getNodeValue());
                result.add(temp);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("exception in topkFunctionUpdate.java");
        }
        
        return result;
    }
}
