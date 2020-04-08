package indexing;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

public class AnalyzerDemo {
  //  private static String[] examples;
  private static final Analyzer[] analyzers =
      new Analyzer[] {
        new WhitespaceAnalyzer(),
        new SimpleAnalyzer(),
        new StopAnalyzer(Version.LUCENE_30),
        new StandardAnalyzer(Version.LUCENE_30)
      };

  public static void main(String[] args) throws IOException {
    String string =
        "menu Research Areas Labs Lectures and Seminars Research Experiences for Undergraduates "
            + "Faculty and Staff Expand Faculty and Staff menu Department Head Faculty Emeritus "
            + "Faculty Adjunct Faculty Staff Faculty Resources Open Positions News Expand News menu "
            + "News Stories Press Mentions Events Expand Events menu CS calendar Student Opportunities"
            + "Eyebrow menu Break Through Tech Giving Alumni UIC Engineering Home UIC menu UIC.edu ";

    analyze(string);
  }

  private static void analyze(String text) throws IOException {
    System.out.println("Analyzing \"" + text + "\"");
    for (Analyzer analyzer : analyzers) {
      String name = analyzer.getClass().getSimpleName();
      System.out.println(" " + name + ":");
      System.out.print(" ");
      displayTokens(analyzer, text);
      System.out.println("\n");
    }
  }

  public static void displayTokens(Analyzer analyzer, String text) throws IOException {
    displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
  }

  public static void displayTokens(TokenStream stream) throws IOException {
    TermAttribute term = stream.addAttribute(TermAttribute.class);
    while (stream.incrementToken()) {
      System.out.print("[" + term.term() + "] ");
    }
  }
}
