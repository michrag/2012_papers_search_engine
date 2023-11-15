package models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Collection;

import org.wikipedia.miner.annotation.Disambiguator;
import org.wikipedia.miner.annotation.Topic;
import org.wikipedia.miner.annotation.TopicDetector;
import org.wikipedia.miner.annotation.weighting.LinkDetector;
import org.wikipedia.miner.comparison.ArticleComparer;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.RelatednessCache;

public class Document
{

    private String sourcePath;
    private int groupDimension = 20;
    private static double minWeightForATopicsToBeConsidered = 0.1;
    private int ID;
    private double relatedness2Keywords;
    private ArrayList<Section> sections;
    private String title;
    private BufferedReader reader;

    public enum SectionParams
    {
        abst("abstract", 1.0);
        //intro("intro", 1.0), toooo long!!
        //conc("conclusion", 1.0);
        private String label;
        private double weight;
        SectionParams(String l, double w)
        {
            label = l;
            weight = w;
        }
        public String getLabel()
        {
            return label;
        }
        public double getWeight()
        {
            return weight;
        }
    }

    public Document(String path, int id)
    {
        ID = id;
        sourcePath = path + ID;
        sections = new ArrayList<Section>();
        constructFrom(sourcePath);
        title = readFrom(sourcePath + "/title.txt");
    }

    /**
     * @param path of root folder
     *
     * given a root folder containing a bunch of original doc sections,
     * turns them into its proper field
     * */
    private void constructFrom(String path)
    {

        String sectionName;
        double sectionWeight;

        for(SectionParams s : SectionParams.values())
        {
            sectionName = s.getLabel();
            sectionWeight = s.getWeight();
            //e.g. Document box/1/abstract/abstract.txt
            String currentFolderPath = path + "/" + sectionName;
            String currentSourcePath = currentFolderPath + "/" + sectionName + ".txt";

            if(!(new File(currentSourcePath).exists()))
            {
                //System.out.println("no "+sectionName+ " to process!");
            }
            else
            {
                Section sect = new Section(sectionName, sectionWeight);
                sect.setContent(readFrom(currentSourcePath));
                sect.setFolderPath(currentFolderPath);
                sections.add(sect);
            }
        }
    }

    private String readFrom(String path)
    {
        String text = new String("");

        try
        {
            reader = new BufferedReader(new FileReader(path));
            text += reader.readLine();
            reader.close();
        }
        catch(Exception e)
        {
            System.out.println("something wrong reading " + path + " ! exiting...");
            System.exit(0);
            e.getMessage();
        }

        return text;
    }

    public void extractTopics(Wikipedia wikipedia, RelatednessCache RC, Disambiguator D, TopicDetector TD, LinkDetector LD)
    {

        try
        {
            for(Section sect : sections)
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(sect.getFolderPath() + "/extracted topics.txt")));
                Collection<Topic> topics = TD.getTopics(sect.getContent(), RC);
                /*
                 * divides the minimum topics weight accepted for current section by a constant
                 * obtained by the number of topics extracted from it
                 */
                double minCurrentWeight = minWeightForATopicsToBeConsidered;
                int howManyTopics = topics.size();
                int howManyGroups = howManyTopics / groupDimension;
                minCurrentWeight /= howManyGroups; //the more topics there are the less is the min weight to them to be considered
                ArrayList<Topic> bestTopics = LD.getBestTopics(topics, minCurrentWeight);
                int id = 0;

                for(Topic t : bestTopics)
                {
                    id = t.getId();

                    if(wikipedia.getPageById(id).getClass().getName().equals("org.wikipedia.miner.model.Redirect"))
                    {
                        //System.out.println("page with id "+id+" is a redirect! skipping...");
                    }
                    else
                    {
                        sect.addTopic(t, t.getWeight());
                        writer.write(t.getTitle() + " " + t.getWeight() + "\n");
                    }
                }

                writer.close();
                writer = new BufferedWriter(new FileWriter(new File(sect.getFolderPath() + "/topics ID.txt")));

                for(Topic t : bestTopics)
                {
                    if(wikipedia.getPageById(id).getClass().getName().equals("org.wikipedia.miner.model.Redirect")) {}
                    else
                    {
                        writer.write(new Integer(t.getId()).toString() + "\n");
                        writer.write(t.getWeight() + "\n");
                    }
                }

                writer.close();
            }
        }
        catch(Exception e)
        {
            System.out.print("something wrong happened extracting topics from .txt sections!exiting...");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /*
     * iterates over its sections and weights relatedness from each section with a (Section-static) weight
     */
    public double computeRelatedness2(ArrayList<Article> keywords, ArticleComparer AC)
    {
        if(sections.size() > 0)
        {
            double sumOfSectionsWeight = 0;
            relatedness2Keywords = 0;

            for(Section s : sections)
            {
                relatedness2Keywords += s.getRelatedness2(keywords, AC)
                                        * s.getWeight();

                if(s.getTopics().size() > 0)
                {
                    sumOfSectionsWeight += s.getWeight();
                }
            }

            //rescales ret value wrt how many and which section this doc contains
            if(sumOfSectionsWeight > 0)
            {
                return relatedness2Keywords / sumOfSectionsWeight;
            }
            else
            {
                return relatedness2Keywords;
            }
        }
        else
        {
            return 0;
        }
    }

    public double getRelatedness2Keywords()
    {
        return relatedness2Keywords;
    }

    public int getID()
    {
        return ID;
    }

    public String getTitle()
    {
        return title;
    }

    public void readTopics(Wikipedia wikipedia)
    {

        String id = "";
        String weight;

        try
        {
            for(Section sect : sections)
            {
                reader = new BufferedReader(new FileReader(new File(sect.getFolderPath() + "/topics ID.txt")));

                do
                {
                    id = reader.readLine();
                    weight = reader.readLine();

                    if(id != null)
                    {
                        Article topic = (Article) wikipedia.getPageById(Integer.parseInt(id));
                        sect.addTopic(topic, Double.parseDouble(weight));
                    }
                }
                while(id != null);
            }
        }
        catch(ClassCastException e)
        {
            ///System.out.println("page with id "+id+" is a redirect! skipping...");
        }
        catch(Exception e)
        {
            System.out.println("something wrong happened reading topics from the box! exiting...");
            System.exit(0);
        }


    }
}

