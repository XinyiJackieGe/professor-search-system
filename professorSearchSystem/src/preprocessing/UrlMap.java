package preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UrlMap {
  private static Map<String, String> urlMap;
  
//  public UrlMap(File urlPaths) throws IOException {
//    urlMap = createUrlMap(urlPaths);
//  }
  
  public static Map<String, String> getMap(){
    return urlMap;
  }
  
  
  public static void createUrlMap(File urlPaths) throws IOException {
    Map<String, String> map = new HashMap<>();
    for (File file : urlPaths.listFiles()) {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = reader.readLine();
      String[] urlInfo = line.split(" ");
      reader.close();
      map.put(urlInfo[0], urlInfo[1]); 
    }
    
    urlMap= map;
  }
}
