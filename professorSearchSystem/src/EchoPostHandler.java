import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import preprocessing.ParseDoc;
import indexing.PositionalPorterStopAnalyzer;
import ranking.QuerySearch;
// import org.apache.commons.text.StringEscapeUtils;

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
    
    Analyzer analyzer = new PositionalPorterStopAnalyzer();
    String queryTerms = "";
    TokenStream stream = analyzer.tokenStream("contents", new StringReader(queryTermsTemp));
    TermAttribute term = stream.addAttribute(TermAttribute.class);
    while (stream.incrementToken()) {
      queryTerms += term.term() + " ";
    }
    queryTerms.trim();
    System.out.println(queryTerms);

    // Send response.
    File indexPath = new File("data/invertedIndexStem");

    // Search query.
    Directory dir = SimpleFSDirectory.open(indexPath);
    //    System.out.println(dir);
    IndexSearcher searcher = new IndexSearcher(dir);
    String response =
        "<!DOCTYPE html>\n"
            + "<html><body>\n"
            + "<h3>Relevant Professors:</h3><br>Search for: "
            + queryTerms
            + "<br>\n"
            + "<p>";

    TopDocs phraseDocs = null;
    TopDocs booleanDocs = null;
    try {
      phraseDocs = QuerySearch.outputPhraseQueryResults(dir, searcher, queryTerms);
      booleanDocs = QuerySearch.outputCombinedBooleanQueryResults(dir, searcher, queryTerms);

      ScoreDoc[] hitsPhrase = phraseDocs.scoreDocs;
      ScoreDoc[] hitsCombined = booleanDocs.scoreDocs;
      System.out.println(hitsPhrase.length);
      System.out.println(hitsCombined.length);

      if (hitsPhrase.length == 0 && hitsCombined.length == 0) {
        response += "No results found!";
      } else if (hitsPhrase.length == 0 && hitsCombined.length > 0) {
        Map<org.apache.lucene.document.Document, String> missingTemDocs = new HashMap<>();
        for (ScoreDoc retrievedDoc : hitsCombined) {
          int docId = retrievedDoc.doc;
          org.apache.lucene.document.Document doc = searcher.doc(docId);
          String notContain = getNotContainTerms(queryArray, doc);
          System.out.println(notContain);
          if (notContain.equals("Not contains: ")) {
            String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
            if (title.equals("")) {
              title = doc.get("url");
            }
            response += String.format("<a href=\"%s\">%s</a><br>\n", doc.get("url"), title);
          } else {
            missingTemDocs.put(doc, notContain);
          }
        }
        for (Map.Entry<org.apache.lucene.document.Document, String> entry :
            missingTemDocs.entrySet()) {
          org.apache.lucene.document.Document doc = entry.getKey();
          String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
          if (title.equals("")) {
            title = doc.get("url");
          }
          response += String.format("<a href=\"%s\">%s</a>\n", doc.get("url"), title);
          response += "&nbsp;&nbsp&nbsp;&nbsp" + entry.getValue() + "<br>\n";
        }

      } else {
        for (ScoreDoc relDoc1 : hitsPhrase) {
          int docId1 = relDoc1.doc;
          org.apache.lucene.document.Document doc = searcher.doc(docId1);
          String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
          if (title.equals("")) {
            title = doc.get("url");
          }
          response += String.format("<a href=\"%s\">%s</a><br>\n", doc.get("url"), title);
        }
        Set<String> combinedDocSet = new HashSet<>();
        for (ScoreDoc sDoc : hitsCombined) {
          org.apache.lucene.document.Document doc = searcher.doc(sDoc.doc);
          combinedDocSet.add(doc.get("id"));
        }
        Set<String> phraseDocSet = new HashSet<>();
        for (ScoreDoc sDoc : hitsPhrase) {
          org.apache.lucene.document.Document doc = searcher.doc(sDoc.doc);
          phraseDocSet.add(doc.get("id"));
        }
        combinedDocSet.removeAll(phraseDocSet);
        System.out.println(combinedDocSet.size());

        Map<org.apache.lucene.document.Document, String> missingTemDocs = new HashMap<>();
        for (ScoreDoc relDoc : hitsCombined) {
          int docId = relDoc.doc;
          org.apache.lucene.document.Document doc = searcher.doc(docId);
          if (combinedDocSet.contains(doc.get("id"))) {
            String notContain = getNotContainTerms(queryArray, doc);
            if (notContain.equals("Not contains: ")) {
              String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
              if (title.equals("")) {
                title = doc.get("url");
              }
              response += String.format("<a href=\"%s\">%s</a><br>\n", doc.get("url"), title);
            } else {
              missingTemDocs.put(doc, notContain);
            }
          }
        }
        for (Map.Entry<org.apache.lucene.document.Document, String> entry :
            missingTemDocs.entrySet()) {
          org.apache.lucene.document.Document doc = entry.getKey();
          String title = doc.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
          if (title.equals("")) {
            title = doc.get("url");
          }
          response += String.format("<a href=\"%s\">%s</a>\n", doc.get("url"), title);
          response += "&nbsp;&nbsp;&nbsp;&nbsp" + entry.getValue() + "<br>\n";
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

  private String getNotContainTerms(String[] queryArray, org.apache.lucene.document.Document doc)
      throws IOException {
    String htmlId = "d" + doc.get("id") + ".html";
    File file = new File("data/html/" + htmlId);
    org.jsoup.nodes.Document htmlDoc = ParseDoc.parseHtml(file);
    String content = ParseDoc.parseContent(htmlDoc).toLowerCase();
    String notContain = "Not contains: ";
    for (String term : queryArray) {
      if (!content.contains(term)) {
        notContain += term + " ";
      }
    }
    return notContain;
  }
}
