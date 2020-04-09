package indexing;

import java.io.File;
import java.util.Map;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import preprocessing.ParseDoc;
import preprocessing.UrlMap;

/** A class for building inverted index given a local directory. */
public class Indexing {
  private static Directory directory;

  /**
   * Build inverted indexes with and without stemming.
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    File indexPath1 = new File("data/invertedIndex");
    File indexPath2 = new File("data/invertedIndexStem");
    File docsPath = new File("data/html");
    File urlPath = new File("data/txt");
    UrlMap.createUrlMap(urlPath);
    Map<String, String> urlMap = UrlMap.getMap();

    //    for (Map.Entry<String, String> entry : urlMap.entrySet()) {
    //      if (entry.getValue().equals("http://lfzhao.com")) {
    //        System.out.println(entry.getKey());
    //      }
    //    }

    Indexing.buildIndex(docsPath, indexPath1, urlMap, "NotStem");
    Indexing.buildIndex(docsPath, indexPath2, urlMap, "Stem");
  }

  /**
   * Build index.
   *
   * @param docsPath path for html files
   * @param indexPath directory for index
   * @param urlMap (docId, url) map
   * @throws Exception
   */
  public static void buildIndex(
      File docsPath, File indexPath, Map<String, String> urlMap, String isStem) throws Exception {
    directory = new SimpleFSDirectory(indexPath);
    IndexWriter writer;
    if (isStem.contentEquals("Stem")) {
      writer =
          new IndexWriter(
              directory,
              new PositionalPorterStopAnalyzer(), // new StandardAnalyzer(Version.LUCENE_30),
              IndexWriter.MaxFieldLength.UNLIMITED);
    } else {
      writer =
          new IndexWriter(
              directory,
              new StandardAnalyzer(Version.LUCENE_30), // new StandardAnalyzer(Version.LUCENE_30),
              IndexWriter.MaxFieldLength.UNLIMITED);
    }

    String docId = "";
    String title = "";
    String content = "";
    String url = "";
    for (File file : docsPath.listFiles()) {
      org.jsoup.nodes.Document htmlDoc = ParseDoc.parseHtml(file);
      docId = ParseDoc.getDocId(file);
      title = ParseDoc.parseTitle(htmlDoc);
      url = ParseDoc.getUrl(docId, urlMap);
      content = ParseDoc.parseContent(htmlDoc);
      if (url != null && title != null && content != null) {
        insertDocument(indexPath, writer, docId, title, url, content);
      }
    }

    writer.close();
    System.out.println("Building index complete!");
  }

  /**
   * Add term to index for each document. Index has four fields.
   *
   * @param indexPath index directory
   * @param writer index writer
   * @param docId field "id"
   * @param title field "title"
   * @param url field "url"
   * @param content field "content"
   * @throws Exception
   */
  public static void insertDocument(
      File indexPath, IndexWriter writer, String docId, String title, String url, String content)
      throws Exception {
    org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

    doc.add(new Field("id", docId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("title", title, Field.Store.YES, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("content", content, Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
  }

  public static Directory getDir() {
    return directory;
  }
}
