import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ranking.QuerySearch;
//import org.apache.commons.text.StringEscapeUtils;

/** Post Handler to receive post request, search query and output relevant links. */
public class EchoPostHandler implements HttpHandler {
  @Override
  public void handle(HttpExchange he) throws IOException {

    InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
    BufferedReader br = new BufferedReader(isr);
    String query = br.readLine();
//    query = StringEscapeUtils.unescapeHtml4(query);
    query = query.toLowerCase().split("=")[1];
    System.out.println(query);

    String[] queryArray = null;
    queryArray = query.split("\\+");
    StringBuilder queryT = new StringBuilder();
    for (int i = 0; i < queryArray.length; i++) {
      queryT.append(queryArray[i]);
      if (i < queryArray.length - 1) {
        queryT.append(" ");
      }
    }
    String queryTerms = queryT.toString();
    System.out.println(queryTerms);

    // Send response.
    File indexPath = new File("data/invertedIndex");
    
    // Search query.
    Directory dir = SimpleFSDirectory.open(indexPath);
    System.out.println(dir); 
    IndexSearcher searcher = new IndexSearcher(dir);
    String response =
        "<!DOCTYPE html>\n" + "<html><body>\n" + "<h3>Relevant Professors:</h3>\n" + "<p>";

    TopDocs phraseDocs;
    TopDocs booleanDocs;
    try {
      phraseDocs = QuerySearch.outputPhraseQueryResults(dir, searcher, queryTerms);
      booleanDocs = QuerySearch.outputCombinedBooleanQueryResults(dir, searcher, queryTerms);
      ScoreDoc[] hitsPhrase = phraseDocs.scoreDocs;
      ScoreDoc[] hitsCombined = booleanDocs.scoreDocs;
      System.out.println(hitsPhrase.length);
      System.out.println(hitsCombined.length);
      if (hitsPhrase.length == 0 && hitsCombined.length > 0) {
        for (ScoreDoc retrievedDoc : hitsCombined) {
          int docId = retrievedDoc.doc;
          org.apache.lucene.document.Document doc = searcher.doc(docId);
          String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
          if (title.equals("")) {
            title = doc.get("url");
          }
          response += String.format("<a href=\"%s\">%s</a><br>\n", doc.get("url"), title);
        }
      } else if (hitsCombined.length == 0 && hitsPhrase.length > 0) {
        for (ScoreDoc retrievedDoc : hitsPhrase) {
          int docId = retrievedDoc.doc;
          org.apache.lucene.document.Document doc = searcher.doc(docId);
          String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
          if (title.equals("")) {
            title = doc.get("url");
          }
          response += String.format("<a href=\"%s\">%s</a><br>\n", doc.get("url"), title);
        }
      } else {
        ScoreDoc[] hitsLonger = null;
        ScoreDoc[] hitsShorter = null;
        if (hitsPhrase.length >= hitsCombined.length) {
          hitsLonger = hitsPhrase;
          hitsShorter = hitsCombined;
        } else {
          hitsLonger = hitsCombined;
          hitsShorter = hitsPhrase;
        }
        for (ScoreDoc relDoc1 : hitsLonger) {
          int docId1 = relDoc1.doc;
          for (ScoreDoc relDoc2 : hitsShorter) {
            int docId2 = relDoc2.doc;
            if (docId1 == docId2) {
              org.apache.lucene.document.Document doc = searcher.doc(docId1);
              String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
              if (title.equals("")) {
                title = doc.get("url");
              }
              response += String.format("<a href=\"%s\">%s</a><br>\n", doc.get("url"), title);
            }
          }
        }
      }

      searcher.close();
      dir.close();

    } catch (ParseException e) {
      e.printStackTrace();
    }

    //    System.out.println(phraseDocs.getMaxScore());
    //    System.out.println(booleanDocs.getMaxScore());

    response += "</p></body></html>";
    System.out.println(response);

    he.sendResponseHeaders(200, response.length());
    OutputStream os = he.getResponseBody();
    os.write(response.toString().getBytes());
    os.close();
  }
}
