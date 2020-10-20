package scriptutils;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HobbyFetcher {

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_hobbies").get();
        System.out.println(doc.title());
        Elements lis = doc.select("a");
        
        for (Element li : lis) {
            String title = li.attr("title");
            String wiki= li.attr("href");
            if(title!= null && !title.isEmpty()){
                if(title.contains("Edit section")){
                    System.out.println("######"+title);
                }
                else
                  System.out.println(title+"##-##https://en.wikipedia.org"+wiki);
            }
        }
//        Document doc = Jsoup.connect("https://en.wikipedia.org/").get();
//        System.out.println(doc.title());
//        Elements newsHeadlines = doc.select("#mp-itn b a");
//        for (Element headline : newsHeadlines) {
//            System.out.println(headline.attr("title")+", "+ headline.absUrl("href"));
//        }
    }
}
