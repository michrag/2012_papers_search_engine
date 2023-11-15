package models;

import java.util.ArrayList;
import java.util.HashMap;

import org.wikipedia.miner.comparison.ArticleComparer;
import org.wikipedia.miner.model.Article;

public class Section
{

    private String name;
    private String content;
    private double weight;
    private String folderPath;
    private HashMap<Article, Double> topics;
    private static ArrayList<Article> keywords;

    public Section(String n, double sectionWeight)
    {
        this.name = n;
        weight = sectionWeight;
        topics = new HashMap<Article, Double>();
    }

    public void setContent(String text)
    {
        this.content = text.toLowerCase();
    }

    public String getContent()
    {
        return content;
    }

    public String getName()
    {
        return name;
    }

    public void setFolderPath(String p)
    {
        folderPath = p;
    }

    public String getFolderPath()
    {
        return folderPath;
    }

    public HashMap<Article, Double> getTopics()
    {
        return topics;
    }

    public void addTopic(Article topic, double weight)
    {
        topics.put(topic, weight);
    }

    public double getWeight()
    {
        return weight;
    }

    public double getRelatedness2(ArrayList<Article> kw, ArticleComparer ac)
    {

        keywords = kw;
        double meanRelatdnessOfATopic2Keywords = 0;

        if(topics != null && topics.size() > 0)
        {
            for(Article topic : topics.keySet())
            {
                meanRelatdnessOfATopic2Keywords += computeRelatednessMeanOfCurrentTopic2Keywords(
                                                       topic, ac) / topics.size();
            }

            return meanRelatdnessOfATopic2Keywords;
        }

        return 0;
    }

    private double computeRelatednessMeanOfCurrentTopic2Keywords(Article topic, ArticleComparer AC)
    {
        double mean = 0;

        try
        {
            for(Article kw : keywords)
            {
                mean += AC.getRelatedness(topic, kw) * (Math.pow(topics.get(topic), 2)) * 1000;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return mean / keywords.size();
    }

}
