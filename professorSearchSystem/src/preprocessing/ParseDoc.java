package preprocessing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.IOException;
import java.util.Map;


public class ParseDoc {
  public static Document parseHtml(File file) throws IOException{
    Document doc = Jsoup.parse(file, "UTF-8");
    return doc; 
  }
  
  public static String parseTitle(Document doc) throws IOException{
    if (doc != null) {
      String title = doc.title();
      if (title != null) {
        return title;
      }
    }
   
    return null;
  }
  
  public static String parseContent(Document doc) throws IOException{
    if (doc != null) {
      if (doc.body() != null) {
        String content = doc.body().text();
        if (content != null) {
          return content;
        }
      }
    }
    
    return null; 
  }
  
  public static String getDocId(File file) {
    String docId = ""; 
    String pathName = file.getName();
    char[] pathArray = pathName.split("\\.")[0].toCharArray();
    for (int i = 0; i < pathArray.length; i++) {
      if (i == 0) {
        continue;
      }
      docId += pathArray[i];
    }
    return docId;
  }
  
  public static String getUrl(String docId, Map<String, String> urlMap) {
    String url = urlMap.get(docId);
    return url;
    
  }
}
