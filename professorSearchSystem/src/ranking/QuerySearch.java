package ranking;

import java.io.IOException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class QuerySearch {
  /**
   * Phrase query.
   *
   * @param dir index directory
   * @param searcher
   * @param queryTerms
   * @return relevant documents
   * @throws ParseException
   * @throws IOException
   */
  public static TopDocs outputPhraseQueryResults(
      Directory dir, IndexSearcher searcher, String queryTerms) throws ParseException, IOException {
    PhraseQuery query = new PhraseQuery();
    query.setSlop(3); // distance = 3
    for (String term : queryTerms.split(" ")) {
      query.add(new Term("content", term));
    }
    TopDocs docs = searcher.search(query, 30);

    return docs;
  }

  /**
   * Combined boolean query. Boolean operator is AND.
   *
   * @param dir index directory
   * @param searcher
   * @param queryTerms
   * @return relevant documents
   * @throws ParseException
   * @throws IOException
   */
  public static TopDocs outputCombinedBooleanQueryResults(
      Directory dir, IndexSearcher searcher, String queryTerms) throws ParseException, IOException {
    BooleanQuery combinedBoolean = new BooleanQuery();
    for (String term : queryTerms.split(" ")) {
      TermQuery queryTerm = new TermQuery(new Term("content", term));
      //      combinedBoolean.add(queryTerm, BooleanClause.Occur.MUST);
      combinedBoolean.add(queryTerm, BooleanClause.Occur.SHOULD);
    }
    TopDocs docs = searcher.search(combinedBoolean, 30);

    return docs;
  }
}
