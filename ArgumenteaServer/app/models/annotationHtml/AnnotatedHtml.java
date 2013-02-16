package models.annotationHtml;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class AnnotatedHtml 
{
	private String htmlContent ;

	public AnnotatedHtml(String newHtmlContent)
	{
		htmlContent = newHtmlContent ;
	}
	
	public String getHtmlContent() {
		return htmlContent;
	}

	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}

	public void highLight(SplitedXpointer start , SplitedXpointer end, String color) throws ParserException //throws ParserException
	{
		Parser parser = Parser.createParser(htmlContent , null);
		NodeList nl = parser.parse(null);
		//seeThroughNodeList(0, nl);
		System.out.println("[AnnotatedHtml.highLight] nl size : " + nl.size());
		Node startNode = findNode(nl, start);
		System.out.println("[AnnotatedHtml.highLight] startNode content : " + startNode.toHtml());
		Node endNode = findNode(nl, end);
		System.out.println("[AnnotatedHtml.highLight] endNode content : " + endNode.toHtml());
		//TODO ajouter les balises span
		
	}
	/**
	 * Usefull for debuging HTMLTreeNode
	 * @param level recursive param, level in the tree
	 * @param nl one level NodeList
	 */
	public static void seeThroughNodeList(int level, NodeList nl)
	{
		int size = 0;
		if(nl != null) size = nl.size();
		for(int i= 0 ; i < size ; i++)
		{
			System.out.println("level : " + level + " elt nb : " + i);
			Node node = nl.elementAt(i);
			System.out.println(node.toHtml());
			seeThroughNodeList(level + 1 , node.getChildren());
		}
	}
	
	private Node findNode(NodeList nl , SplitedXpointer splitedXpointer)
	{
		int nbNode = splitedXpointer.getXpointerTree().length ;
		NodeList currentNodeList = nl ;
		//TODO changer ce comportement
		//dans notre contexte le contenu d'un article ou d'une annotation est encadré par une balise div d'id commençant par article- ou annotation-
		int cptNode = 0 ;
		if(splitedXpointer.getXpointerTree()[0].contains("article-")||splitedXpointer.getXpointerTree()[0].contains("annotation-"))
		{
			cptNode ++ ;
		}
		//fin du comportement propre à notre contexte
		
		while(cptNode < nbNode -1)
		{
			System.out.println("[AnnotatedHtml.findNode] cptNode : " + cptNode);
			String info = splitedXpointer.getXpointerTree()[cptNode];
			try{// Pour l'instant on ne fabrique que des Xpointer avec des int après l'id de la ressource
				Integer nextSibling = Integer.parseInt(info);
				System.out.println("[AnnotatedHtml.findNode] nextSibling : " + nextSibling);
				//Attention, on ne peut pas juste aller chercher l'élément à nextSibling car la lib htmlParser compte les TextNode
				//Donc il faut itérer et ignorer les TextNode
				int cptGlobal = 0;
				int cptWhithoutTextNode = 0 ;
				while(cptWhithoutTextNode < nextSibling )
				{
					System.out.println("[AnnotatedHtml.findNode] in while next node : " + nl.elementAt(cptGlobal).toHtml());
					if(! (nl.elementAt(cptGlobal) instanceof TextNode)) cptWhithoutTextNode ++ ;
					cptGlobal ++ ;
					System.out.println("[AnnotatedHtml.findNode] cptWithoutTexteNode : " + cptWhithoutTextNode + " cptGlobal : " + cptGlobal);
				}
				Node testTextNode = currentNodeList.elementAt(cptGlobal) ;
				while(testTextNode instanceof TextNode) 
				{
					cptGlobal ++ ;
					testTextNode = currentNodeList.elementAt(cptGlobal) ;
				}
				System.out.println("[AnnotatedHtml.findNode] cptGlobal : " + cptGlobal);
				currentNodeList = testTextNode.getChildren();
			}
			catch (NumberFormatException exception) // La syntaxe Xpointer permet d'autres cibles que des int
			{
				exception.printStackTrace();
				System.out.println("[AnnotatedHtml.findNode] nextSibling is not an integer !!!");
				//TODO gérer les autres syntaxes XPointer
			}
			cptNode ++ ;
		}
		//il nous reste le dernier à parcourir pour récupérer le noeud
		Node currentNode = currentNodeList.elementAt(0) ;
		String info = splitedXpointer.getXpointerTree()[cptNode];
		try{// Pour l'instant on ne fabrique que des Xpointer avec des int après l'id de la ressource
			Integer nextSibling = Integer.parseInt(info);
			int cptGlobal = 0;
			int cptWhithoutTextNode = 0 ;
			while(cptWhithoutTextNode < nextSibling )
			{
				if(! (nl.elementAt(cptGlobal) instanceof TextNode)) cptWhithoutTextNode ++ ;
				cptGlobal ++ ;
			}
			currentNode = currentNodeList.elementAt(cptGlobal) ;
		}
		catch (NumberFormatException exception) // La syntaxe Xpointer permet d'autres cibles que des int
		{
			exception.printStackTrace();
			System.out.println("[AnnotatedHtml.findNode] nextSibling is not an integer !!!");
			//TODO gérer les autres syntaxes XPointer
		}
		//return currentNodeList.elementAt(0) ;
		return currentNode ;
	}
}