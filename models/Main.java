package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;


import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.WikipediaConfiguration;

import utilities.TrainingSetMaker;

public class Main
{

    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private static String input;
    private static int choice;
    private static ArrayList<Article> keywords;
    private static WikipediaConfiguration conf;
    private static Wikipedia wiki;
    private static boolean newBox = false;

    /**
     * @param arg [0] 'true' to create a new box OR 'a number' to set the training set size
     */
    public static void main(String[] args)
    {

        //args[0] = "false";
        //      args[0] = "5";

        System.out.println("hello!");

        try
        {
            conf = new WikipediaConfiguration(new File("configurations/config.xml"));
            wiki = new Wikipedia(conf, false);
            keywords = new ArrayList<Article>();

            // acquire keywords
            while(true)
            {
                System.out.println("insert a keyword(1)");
                System.out.println("proceed(2)");
                System.out.println("exit(0)");
                Article art;

                do
                {
                    input = br.readLine();

                }
                while(!input.equals("0") && !input.equals("1") && !input.equals("2"));

                choice = Integer.parseInt(input);

                if(choice == 0)
                {
                    System.out.println("goodbye!");
                    wiki.close();
                    System.exit(0);

                }
                else if(choice == 2)
                {
                    if(keywords.size() == 0)
                    {
                        System.out.println("empty keyword list!");
                    }
                    else
                    {
                        wiki.close();

                        try
                        {
                            //case: args[0] = training set size
                            new TrainingSetMaker(keywords, Integer.parseInt(args[0])).run();
                        }
                        catch(NumberFormatException nfe)
                        {
                            //case: args[0] = newBox
                            newBox = Boolean.parseBoolean(args[0]);
                            runExtraction();
                        }
                    }

                }
                else
                {
                    boolean added = false;
                    input = "";

                    while(!added)
                    {

                        System.out.println("insert a keyword ");
                        input = br.readLine();
                        art = wiki.getMostLikelyArticle(input, null);

                        if(art == null)
                            System.out
                            .println("ouch! no wikipedia articles found...");
                        else
                        {
                            System.out.println("found: " + art.getTitle()
                                               + "; (y) to validate, (n) to retry");

                            do
                            {
                                input = br.readLine();

                            }
                            while(!input.equals("y") && !input.equals("n"));

                            if(input.equals("n"))
                            {
                            }
                            else
                            {
                                keywords.add(art);
                                System.out.println("added!");
                                System.out.println("");
                                System.out
                                .println("************************************");
                                System.out.println("keywords summary: ");
                                System.out.println("");
                                System.out.println("");
                                int k = 1;

                                for(Article a : keywords)
                                {
                                    System.out.println(k + ")" + a.getTitle());
                                    k++;
                                }

                                System.out
                                .println("************************************");
                                System.out.println("");
                                System.out.println("");
                                added = true;

                            }// decided to add
                        }// something found
                    }// while(added)
                }// choice=1
            }// while(true)
        }// try
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("something wrong! exiting...");
        }
    }

    private static void runExtraction()
    {

        //loads default classifiers
        conf.setTopicDisambiguationModel(new File("disambiguation models/current.model"));
        conf.setLinkDetectionModel(new File("detection models/current.model"));
        DocumentBox box = new DocumentBox(conf, !newBox);
        box.extractTopics();
        box.showSortedDocs(keywords);

        //cleans the interface
        try
        {
            keywords.clear();
            conf = new WikipediaConfiguration(new File("configurations/config.xml"));
            wiki = new Wikipedia(conf, false);
        }
        catch(Exception e)
        {
            System.out.println("something wrong restarting the interface! exiting...");
        }

    }

}
