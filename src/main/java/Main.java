import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    // CSV - JSON
    String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
    String fileName = "data.csv";
    List<Employee> list = parseCSV(columnMapping, fileName);
    String json = listToJson(list);
    writeString(json, "data.json");
    // XML - JSON
    List<Employee> list1 = parseXML("data.xml");
    String json1 = listToJson(list1);
    writeString(json1, "data2.json");
    // JSON в класс Java
    String json3 = readString("data2.json");
    List<Employee> list2 = jsonToList(json3);
    for (Employee employee : list2) {
      System.out.println(employee.toString());
    }
  }

  private static List<Employee> jsonToList(String json) {
    List<Employee> list = new ArrayList<>();
    JSONParser parser = new JSONParser();
    Object object = null;
    try {
      object = parser.parse(json);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    JSONArray jsonArray = (JSONArray) object;
    for (Object obj : jsonArray) {
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.create();
      Employee employee = gson.fromJson(String.valueOf((JSONObject) obj), Employee.class);
      list.add(employee);
    }
    return list;
  }

  private static String readString(String fileName) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line = reader.readLine();
      return line;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static List<Employee> parseXML(String fileName) {
    List<Employee> list = new ArrayList<>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    Document doc = null;
    try {
      doc = builder.parse(new File(fileName));
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Node root = doc.getDocumentElement();
    read(root, list);

    return list;
  }

  private static void read(Node node, List<Employee> list) {
    NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node_ = nodeList.item(i);
      if (Node.ELEMENT_NODE == node_.getNodeType() && node_.getNodeName() == "employee") {
        NodeList nodeList_ = node_.getChildNodes();
        long id = 0;
        String firstName = null;
        String lastName = null;
        String country = null;
        int age = 0;
        for (int j = 0; j < nodeList_.getLength(); j++) {
          if (Node.ELEMENT_NODE == nodeList_.item(j).getNodeType()) {
            String nodeName = nodeList_.item(j).getNodeName();
            String nodeValue = nodeList_.item(j).getTextContent();
            switch (nodeName) {
              case "id":
                id = Long.parseLong(nodeValue);
                break;
              case "firstName":
                firstName = nodeValue;
                break;
              case "lastName":
                lastName = nodeValue;
                break;
              case "country":
                country = nodeValue;
                break;
              case "age":
                age = Integer.parseInt(nodeValue);
              default:
                break;
            }
          }
        }
        Employee employee = new Employee(id, firstName, lastName, country, age);
        list.add(employee);
      }
    }
  }

  private static void writeString(String json, String fileName) {
    try (FileWriter file = new
            FileWriter(fileName)) {
      file.write(json);
      file.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String listToJson(List<Employee> list) {
    Type listType = new TypeToken<List<Employee>>() {
    }.getType();
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    String json = gson.toJson(list, listType);
    return json;
  }

  private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
    List<Employee> list = null;
    try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
      ColumnPositionMappingStrategy<Employee> strategy =
              new ColumnPositionMappingStrategy<>();
      strategy.setType(Employee.class);
      strategy.setColumnMapping(columnMapping);
      CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
              .withMappingStrategy(strategy)
              .build();
      list = csv.parse();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return list;
  }
}
