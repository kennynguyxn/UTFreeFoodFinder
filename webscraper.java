import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;
import java.util.Locale;


public class webscraper {
    public static void main(String[] args) {
        String[] keywords = { "food ", " snacks ", " coffee ", " donut ", " pizza ", "ice cream", "lunch",
                " dinner ", " brunch ", " dine ", "freeze", "cookies", "chick-fil-a"};
        String[] allowed = { "/calendar/rooms/", "/calendar/events/" };

        WebClient webClient = new WebClient(BrowserVersion.CHROME);

        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        try {
            HtmlPage page = webClient.getPage("https://apps.cs.utexas.edu/calendar/");

            FileWriter foodFile = new FileWriter("food.csv", false);

            webClient.getCurrentWindow().getJobManager().removeAllJobs();

            String title = page.getTitleText();
            System.out.println("Page Title: " + title);

            List<HtmlAnchor> links = page.getAnchors();
            ArrayList<String> validPages = new ArrayList<>();
            for (HtmlAnchor link : links) {
                String href = link.getHrefAttribute();
                String eventTitle = link.getAttribute("title").replace(',', ';');
                int eventNum = -1;
                if (href.length() < 17) {
                    continue;
                }
                if (href.substring(0, 16).equals(allowed[0])) {
                    try {
                        eventNum = Integer.parseInt(href.substring(16));
                    } catch (NumberFormatException ex) { }
                } else if (href.substring(0, 17).equals(allowed[1])) {
                    try {
                        eventNum = Integer.parseInt(href.substring(17));
                    } catch (NumberFormatException ex) { }
                }
                if (eventNum != -1) {
                    validPages.add(href);
                    System.out.println("Link: " + href);
                }
            }
            webClient.close();

            for (String s : validPages) {
                String url = String.format("https://apps.cs.utexas.edu%s", s);
                System.out.println(url);
                HtmlPage contentPage = webClient.getPage(url);
                StringBuffer buff = new StringBuffer();
                NodeList list = contentPage.getHtmlElementById("content").getChildNodes();
                getPageText(list, buff);
                webClient.close();
                String buffer = buff.toString().toLowerCase();
                for (int i = 0; i < keywords.length; i++) {
                    if (buffer.indexOf(keywords[i]) != -1) {
                        System.out.println(buff.toString());
                        foodFile.write(buff.toString());
                    }
                }
            }


            foodFile.close();

        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }

    }

    public static void getPageText(NodeList list, StringBuffer sb) {
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.TEXT_NODE && !node.getTextContent().isEmpty()) {
                sb.append(node.getNodeValue());
            }
            if (node.getChildNodes().getLength() > 0) {
                getPageText(node.getChildNodes(), sb);
            }
        }
    }
}
