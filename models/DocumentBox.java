package models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.wikipedia.miner.annotation.Disambiguator;
import org.wikipedia.miner.annotation.TopicDetector;
import org.wikipedia.miner.annotation.weighting.LinkDetector;
import org.wikipedia.miner.comparison.ArticleComparer;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.RelatednessCache;
import org.wikipedia.miner.util.WikipediaConfiguration;

import utilities.DocumentBoxInitializer;

public class DocumentBox
{

    private ArrayList<Document> docs;
    private int i;
    private int howManyDocs;
    private Wikipedia wikipedia;
    private ArrayList<Document> bestDocsOnTop;
    private BufferedWriter writer;
    private ArticleComparer AC;
    private RelatednessCache RC;
    private Disambiguator D;
    private TopicDetector TD;
    private LinkDetector LD;
    private boolean exists;
    private static String documentBoxPath = "Document box(testing)/";
    private static String pdfsSourcePath = "pdfs/";

    public DocumentBox(WikipediaConfiguration conf, boolean ex)
    {

        try
        {

            exists = ex;

            if(!exists)
            {
                DocumentBoxInitializer.initialize(pdfsSourcePath, documentBoxPath, howManyDocs);
                File pdfFolder = new File(pdfsSourcePath);
                File[] pdfs = pdfFolder.listFiles();
                howManyDocs = pdfs.length;
            }
            else
            {
                howManyDocs = new File(documentBoxPath).listFiles().length;
            }

            wikipedia = new Wikipedia(conf, false);
            AC = new ArticleComparer(wikipedia);
            RC = new RelatednessCache(AC);
            D = new Disambiguator(wikipedia);
            TD = new TopicDetector(wikipedia, D, false, true);
            LD = new LinkDetector(wikipedia);

            docs = new ArrayList<Document>();

            for(i = 0; i < howManyDocs; i++)
            {
                //e.g. Document box/1
                Document doc = new Document(documentBoxPath, i);
                docs.add(i, doc);
            }
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println(fnfe.getMessage());
            System.out.println("something wrong happened reading from pdfs folder! exiting...");
            System.exit(0);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("something wrong happened reading wikipedia configuration! exiting...");
            System.exit(0);
        }
    }

    public void extractTopics()
    {

        if(!exists)
        {
            System.out.println("extracting topics from each document...");

            for(i = 0; i < howManyDocs; i++)
            {
                docs.get(i).extractTopics(wikipedia, RC, D, TD, LD);
            }
        }
        else
        {
            System.out.println("reading topics from already existing box...");

            for(i = 0; i < howManyDocs; i++)
            {
                docs.get(i).readTopics(wikipedia);
            }
        }
    }

    public void showSortedDocs(ArrayList<Article> keywords)
    {

        System.out.println("sorting documents...");

        try
        {
            bestDocsOnTop = new ArrayList<Document>();

            for(Document doc : docs)
            {
                sortByRelatedness(doc, doc.computeRelatedness2(keywords, AC));
            }

            File bestDocsFile = new File("results/" + "sorted by relatedness to " + keywords.toString() + ".txt");
            writer = new BufferedWriter(new FileWriter(bestDocsFile));
            System.out.println("here are the documents in the box, sorted by the relatedness to your keywords:");

            for(i = 1; i <= howManyDocs; i++)
            {
                Document currentDoc = bestDocsOnTop.get(i - 1);
                System.out.println(i + ") ID: " + currentDoc.getID() + ", title: " + currentDoc.getTitle() +
                                   "; relatedness to keywords: " + currentDoc.getRelatedness2Keywords());
                writer.write(i + ") ID: " + currentDoc.getID() + ", title: " + currentDoc.getTitle() +
                             "; relatedness: " + currentDoc.getRelatedness2Keywords() + "\n");
            }

            writer.close();
            wikipedia.close();
        }
        catch(Exception e)
        {
            e.getMessage();
            System.out.println("something wrong writing best docs on file support! exiting...");
            System.exit(0);
        }
    }

    private void sortByRelatedness(Document doc, double distance)
    {
        int position = 0;
        Iterator<Document> iter = bestDocsOnTop.iterator();

        while(iter.hasNext() && bestDocsOnTop.get(position).getRelatedness2Keywords() > doc.getRelatedness2Keywords())
        {
            iter.next();
            position++;
        }

        bestDocsOnTop.add(position, doc);
    }

}
