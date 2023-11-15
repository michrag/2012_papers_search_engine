package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.wikipedia.miner.annotation.ArticleCleaner;
import org.wikipedia.miner.annotation.Disambiguator;
import org.wikipedia.miner.annotation.TopicDetector;
import org.wikipedia.miner.annotation.weighting.LinkDetector;
import org.wikipedia.miner.comparison.ArticleComparer;
import org.wikipedia.miner.db.WDatabase.DatabaseType;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.ArticleSet;
import org.wikipedia.miner.util.RelatednessCache;
import org.wikipedia.miner.util.WikipediaConfiguration;

public class TrainingSetMaker
{

    private static Wikipedia w;
    private static ArticleComparer comparer;
    private static int setSize;
    private static int k, i = 0;
    private static double minLinkRelatedness = 0.35;
    private static ArticleSet SET;
    private static ArrayList<Article> keywords;

    public TrainingSetMaker(ArrayList<Article> kw, int size)
    {

        setSize = size;
        keywords = new ArrayList<Article>();
        System.out.println("constructing training set maker; keywords to expand: ");

        for(Article a : kw)
        {
            if(a != null)
            {
                System.out.println(a.getTitle());
                keywords.add(a);
            }
        }

        if(keywords.isEmpty())
        {
            System.out.println("nothing to expand!exiting...");
            System.exit(0);
        }

        System.out.println();

        try
        {
            WikipediaConfiguration conf = new WikipediaConfiguration((new File("configurations/config.xml")));
            conf.addDatabaseToCache(DatabaseType.pageLinksIn);
            conf.addDatabaseToCache(DatabaseType.label);
            //conf.addDatabaseToCache(DatabaseType.pageLinksOut);
            w = new Wikipedia(conf, false);
            SET = new ArticleSet();
            comparer = new ArticleComparer(w);
        }
        catch(Exception e)
        {
            System.out.println("something wrong constructing the training set maker! exiting...");
            System.exit(0);
        }
    }

    public void run()
    {
        // specifyParams();
        System.out.println();
        System.out.println();
        System.out.println("gathering step starting...");
        System.out.println();
        /* lista di iterazione */
        ArrayList<Article> tmpList = new ArrayList<Article>();

        for(Article a : keywords)
        {
            tmpList.add(a);
        }

        while(!tmpList.isEmpty() && SET.size() < setSize)
        {
            /*
             * estraggo il primo elemento - quello che sarà espanso - e lo metto
             * nella lista globale
             */
            Article toExp = tmpList.get(0);
            System.out.println(i + ") adding article '" + tmpList.get(0) + "'");
            i++;
            SET.add(tmpList.remove(0));
            /*
             * passo l'articolo da espandare al modulo di espansione : fondo a
             * lista di iterazione
             */
            System.out.println("expanding keyword '" + toExp.getTitle() + "'...");
            tmpList = merge(tmpList, gatherInOut(toExp, tmpList));
        }

        saveTrainedClassifiers();
    }

    private static ArrayList<Article> gatherInOut(Article toExp,
            ArrayList<Article> tmpList)
    {
        ArrayList<Article> retList = new ArrayList<Article>();

        /* raccolgo tutti i link entranti che superano la soglia */
        try
        {
            Article[] links = toExp.getLinksIn();
            ArrayList<Article> linkList = new ArrayList<Article>();

            for(k = 0; k < links.length; k++)
            {
                linkList.add(links[k]);
            }

            Iterator<Article> iter = linkList.iterator();

            while(iter.hasNext())
            {
                Article a = iter.next();

                /*
                 * SET contiene tutti gli articoli già espansi; tmpList contiene
                 * tutti gli articoli in attesa di essere espansi; si evita di
                 * riespandere o rimettere in coda articoli già visti; si
                 * evitano redirects e anchors
                 */
                if(a.getClass().getName()
                        .equals("org.wikipedia.miner.model.Article")
                        && !containsTitle(a, tmpList, retList))
                {
                    double rel = computeRelatednessMean(a);

                    if(rel > minLinkRelatedness)
                    {
                        retList.add(a);
                    }
                }
            }

            /* raccolgo tutti i link uscenti che superano la soglia */
            links = toExp.getLinksOut();
            linkList = new ArrayList<Article>();

            for(k = 0; k < links.length; k++)
            {
                linkList.add(links[k]);
            }

            iter = linkList.iterator();

            while(iter.hasNext())
            {
                Article a = iter.next();

                if(a.getClass().getName()
                        .equals("org.wikipedia.miner.model.Article")
                        && !containsTitle(a, tmpList, retList))
                {
                    double rel = computeRelatednessMean(a);

                    if(rel > minLinkRelatedness)
                    {
                        retList.add(a);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.getCause();
            return retList;
        }

        return retList;
    }

    private static double computeRelatednessMean(Article a)
    {
        double mean = 0;

        try
        {
            for(Article kw : keywords)
            {
                mean += comparer.getRelatedness(a, kw);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return mean / keywords.size();
    }

    /* appende B in coda ad A senza ripetizioni */
    private static ArrayList<Article> merge(ArrayList<Article> A,
                                            ArrayList<Article> B)
    {
        ArrayList<Article> C = A;
        Iterator<Article> iter = B.iterator();

        while(iter.hasNext())
        {
            Article o = iter.next();

            if(!C.contains(o))
            {
                C.add(o);
            }
        }

        return C;
    }

    /* evita ripetizioni dovute ad ominimia di articoli */
    private static boolean containsTitle(Article a, ArrayList<Article> tmpList,
                                         ArrayList<Article> retList)
    {

        String targetTitle = a.getTitle();
        Iterator<Article> iter = SET.iterator();

        /* non ancora espanso */
        while(iter.hasNext())
        {
            String title = iter.next().getTitle();

            if(targetTitle.equals(title))
            {
                return true;
            }
        }

        /* non ancora in lista di espansione */
        iter = tmpList.iterator();

        while(iter.hasNext())
        {
            String title = iter.next().getTitle();

            if(targetTitle.equals(title))
            {
                return true;
            }
        }

        /* non ancora considerato in questa espansione */
        iter = retList.iterator();

        while(iter.hasNext())
        {
            String title = iter.next().getTitle();

            if(targetTitle.equals(title))
            {
                return true;
            }
        }

        return false;
    }

    private static void saveTrainedClassifiers()
    {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        Disambiguator D;
        LinkDetector LD;
        boolean wrote = false;
        String destinationD = "disambiguation models/";
        String destinationLD = "detection models/";

        try
        {
            ArticleComparer AC = new ArticleComparer(w);
            RelatednessCache RC = new RelatednessCache(AC);
            D = new Disambiguator(w);
            D.train(SET, ArticleCleaner.SnippetLength.firstParagraph, "disambiguationFP", RC);
            D.buildDefaultClassifier();

            while(!wrote)
            {
                System.out.println("disambiguator training complete! chose a name for the .model file!");
                input = br.readLine();
                D.saveClassifier(new File(destinationD + input + ".model"));
                System.out.println("your disambiguator has been saved into '" + destinationD + input + ".model'" + " :");
                System.out.println("to use it instead of current disambiguation model just " +
                                   "rename it as 'current.model'!");
                wrote = true;
            }

            TopicDetector TD = new TopicDetector(w, D, true, true);
            LD = new LinkDetector(w);
            LD.train(SET, ArticleCleaner.SnippetLength.firstParagraph, "detectionFP", TD, RC);
            LD.buildDefaultClassifier();
            wrote = false;

            while(!wrote)
            {
                System.out.println("detector training complete! chose a name for the .model file!");
                input = br.readLine();
                LD.saveClassifier(new File(destinationLD + input + ".model"));
                System.out.println("your detector has been saved into '" + destinationLD + input + ".model'" + " :");
                System.out.println("to use it instead of current detection model just " +
                                   "rename it as 'current.model'!");
                wrote = true;
            }

        }
        catch(IOException e)
        {
            System.out.println("invalid file name!");
        }
        catch(Exception e)
        {
            System.out.println("something wrong training the disambiguation classifier! exiting...");
            System.exit(0);
        }

        w.close();
        System.out.println("goodbye!");
        System.exit(0);
    }

}
