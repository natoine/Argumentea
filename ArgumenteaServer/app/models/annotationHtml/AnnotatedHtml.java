package models.annotationHtml;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Span;
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

	/**
	 * Returns true if node1 is before node2 in the dom
	 * @param node1
	 * @param node2
	 * @return
	 */
	private boolean isPreceedingNode(Node node1, Node node2)
	{
		Node currentNode = node1 ;
		while(currentNode != null)
		{
			if(currentNode.equals(node2)) return false;
			if(currentNode.getPreviousSibling() != null) currentNode = currentNode.getPreviousSibling() ;
			else currentNode = currentNode.getParent();
		}
		return true ;
	}
	
	public void highLight(SplitedXpointer start , SplitedXpointer end, String color, String annotationId, String onHover) throws ParserException //throws ParserException
	{
		Parser parser = Parser.createParser(htmlContent , null);
		NodeList nl = parser.parse(null);
		Node announcedStartNode = findNode(nl, start);
		Node announcedEndNode = findNode(nl, end);
		//vérifier l'ordre des noeuds dans l'arbre
		if(announcedStartNode.equals(announcedEndNode))
		{
			if(start.getIndice() < end.getIndice()) addSpans(nl, announcedStartNode, announcedEndNode, start.getIndice(), end.getIndice(), color, annotationId, onHover);
			else addSpans(nl, announcedStartNode, announcedEndNode, end.getIndice(), start.getIndice(), color, annotationId, onHover);
		}
		else if(isPreceedingNode(announcedStartNode , announcedEndNode)) addSpans(nl, announcedStartNode, announcedEndNode, start.getIndice(), end.getIndice(), color, annotationId, onHover);
		else addSpans(nl, announcedEndNode, announcedStartNode, end.getIndice(), start.getIndice(), color, annotationId, onHover);
		htmlContent = nl.toHtml();
	}
	
	private static NodeList addSpanInTextNode(TextNode node, int indiceStart, int indiceEnd, String color, String annotationId, String onHover)
	{
		NodeList toreturn = new NodeList();
		
		String originalContent = node.getText() ;
		String beforeSpan ;
		String contentAnnotated ;
		String afterSpan ;
		if(indiceStart + 1 < originalContent.length()) 
		{
			beforeSpan = originalContent.substring(0, indiceStart + 1);
			if(indiceEnd + 1 < originalContent.length())
			{
				contentAnnotated = originalContent.substring(indiceStart + 1, indiceEnd + 1);
				afterSpan = originalContent.substring(indiceEnd + 1);
			}
			else 
			{
				contentAnnotated = originalContent.substring(indiceStart +1);
				afterSpan = "";
			}
		}
		else
		{
			beforeSpan = originalContent ;
			contentAnnotated = "";
			afterSpan = "" ;
		}
		
		Span annotationSpan = new Span();
		TagNode endSpan = new TagNode();
		endSpan.setTagName("/SPAN");
		annotationSpan.setEndTag(endSpan);
		annotationSpan.setAttribute("id", "'annotationSpan-" + annotationId + "'");
		annotationSpan.setAttribute("style", "'background-color:" + color + "'");
		annotationSpan.setAttribute("class", "'annotated-coloration'");
		annotationSpan.setAttribute("title", onHover);
		NodeList toAdd = new NodeList();
		toAdd.add(new TextNode(contentAnnotated));
		annotationSpan.setChildren(toAdd);
		//System.out.println("[AnnotatedHtml.addSpanInTextNode] Span : " + annotationSpan.toHtml());
		
		toreturn.add(new TextNode(beforeSpan));
		toreturn.add(annotationSpan);
		toreturn.add(new TextNode(afterSpan));
		
		return toreturn ;
	}
	
	private static void addSpanInANode(Node node, int indiceStart, int indiceEnd, String color, String annotationId, String onHover)
	{
		NodeList children = node.getChildren();
		node.setChildren(null);
		NodeList newChildren = new NodeList();
		if(children != null)
		{
			int currentIndiceStart = indiceStart ;
			int currentIndiceEnd = indiceEnd ;
			int nbNodes = children.size() ;
			int cptNode = 0 ;
			boolean done = false ;
			Node currentNode ;
			while(cptNode < nbNodes && !done )
			{
				currentNode = children.elementAt(cptNode) ;
				if(currentNode instanceof TextNode)
				{
					int contentLength = currentNode.getText().length() ;
					// dans ce cas on n'annote pas mais on met à jour l'indice de début et l'indice de fin
					if(contentLength < currentIndiceStart) 
					{
						currentIndiceStart = currentIndiceStart - contentLength ;
						currentIndiceEnd = currentIndiceEnd - contentLength ;
						newChildren.add(currentNode);
					}
					else //là va falloir annoter
					{
						//toute l'annotation est dans ce TextNode
						if(contentLength > currentIndiceEnd) 
						{
							NodeList newNodes = addSpanInTextNode((TextNode)currentNode , currentIndiceStart, currentIndiceEnd, color, annotationId, onHover) ;
							newChildren.add(newNodes);
							done = true ;
						}
						else //là il va falloir continuer à annoter
						{
							NodeList newNodes = addSpanInTextNode((TextNode)currentNode , currentIndiceStart, contentLength, color, annotationId, onHover);
							newChildren.add(newNodes);
							currentIndiceStart = 0 ;
							currentIndiceEnd = currentIndiceEnd - contentLength ;
							if(currentIndiceEnd <= 0) 
							{
								done = true ;
							}
						} 
					}
				}
				else
				{
					//penser à gérer les annotations déjà faites
					if(currentNode instanceof Span)
					{
						Span currentSpan = (Span)currentNode ;
						if(currentSpan.getAttribute("class").equalsIgnoreCase("annotated-coloration")) // on s'occupe que des Span d'annotations
						{
							NodeList spanChildren = currentSpan.getChildren();
							int nbChlidrenSpan = spanChildren.size() ;
							for(int cptChildrenSpan = 0 ; cptChildrenSpan < nbChlidrenSpan ; cptChildrenSpan ++)
							{
								int toSubstract = getRecursiveLengthAnnotatedSpan(spanChildren.elementAt(cptChildrenSpan) , 0) ;
								if(toSubstract < currentIndiceStart)//dans ce cas l'annotation est avant ce qui nous intéresse
								{
									currentIndiceStart = currentIndiceStart - toSubstract ;
									currentIndiceEnd = currentIndiceEnd - toSubstract ;
									//System.out.println("[AnnotatedHtml.addSpanInANode] span d'annotation prise en compte");
								}
								else
								{
									//toute l'annotation est dans cette Span et ses fils ...
									if(toSubstract > currentIndiceEnd) 
									{
										//TODO
									}
									else // l'annotation est à cheval ...
									{
										//TODO
									}
								}
							}
						}
					}
					newChildren.add(currentNode);
				}
				cptNode ++ ;
			}
			if(cptNode < nbNodes) // il faut traiter les autres noeuds
			{
				while(cptNode < nbNodes)
				{
					currentNode = children.elementAt(cptNode) ;
					newChildren.add(currentNode);
					cptNode ++ ;
				}
			}
		}
		else
		{
			if(node instanceof TextNode)
			{
				NodeList modifieds = addSpanInTextNode((TextNode)node, indiceStart, indiceEnd, color, annotationId, onHover);
				//TODO needs to be tested ...
				node.setChildren(modifieds);
			}
		}
		node.setChildren(newChildren);
		//System.out.println("[AnnotatedHtml.addSpanInANode] new Node content : " + node.toHtml());
	}
	
	//renvoie la longueur de la somme de tous les textnodes des noeuds fils
	public static int getRecursiveLengthAnnotatedSpan(Node node, int length)
	{
		int _length = length ;
		if(node instanceof TextNode) _length += ((TextNode)node).getText().length();
		else
		{
			NodeList children = node.getChildren();
			int nbChild = children.size() ;
			for(int cptChild = 0 ; cptChild < nbChild ; cptChild ++)
			{
				_length += getRecursiveLengthAnnotatedSpan(children.elementAt(cptChild) , _length);
			}
		}
		return _length ;
	}
	
	//TODO ajouter les balises span
	private static void addSpans(NodeList nl, Node node1, Node node2, int indiceStart, int indiceEnd, String color, String annotationId, String onHover)
	{
		//soit node1 == node2
		if(node1.equals(node2))
		{
			//System.out.println("[AnnotatedHtml.addSpans] same node");
			addSpanInANode(node1, indiceStart, indiceEnd, color, annotationId, onHover);
			//System.out.println("[AnnotatedHtml.addSpans] same node, new node content : " + node1.toHtml());
		}
		else 
		{
			//soit node1 et node2 de même niveau
			if(sameLevel(nl, node1, node2))
			{
				//System.out.println("[AnnotatedHtml.addSpans] node of same level in the html tree and node1 before node2");
				
			}
			else
			{
				if(sameLevel(nl, node2, node1))
				{
					//System.out.println("[AnnotatedHtml.addSpans] node of same level in the html tree and node2 before node1");
				}
				else
				{
					//soit node1 fils de node2
					if(hasChild(node1 , node2))
					{
						//System.out.println("[AnnotatedHtml.addSpans] node2 child of node1");
					}
					else
					{
						//soit node2 fils de node1
						if(hasChild(node2 , node1))
						{
							//System.out.println("[AnnotatedHtml.addSpans] node1 child of node2");
						}
						else
						{
							//soit node1 et node2 pas de même niveau et pas fils l'un de l'autre
							//System.out.println("[AnnotatedHtml.addSpans] not child one of another");
							//dans ce cas il faut trouver l'ensemble des noeuds entre les deux noeuds et il faut l'ordre entre start et end
						}
					}
				}
			}
		}
	}
	/**
	 * Tests if node2 is in the same level than node1 but after him in the DOM.
	 * @param node1
	 * @param node2
	 * @return boolean
	 */
	public static boolean sameLevel(NodeList nl, Node node1, Node node2)
	{
		//boolean found = false ;
		//	Node currentNode = node1 ;
		int indexnode1 = nl.indexOf(node1);
		//System.out.println("[AnnotatedHtml.sameLevel] index of node1 : " + i);
		int nbNode = nl.size();
		for(int i = indexnode1 ; i < nbNode ; i++)
		{
			if(nl.elementAt(i).equals(node2)) return true ;
		}
		/*Node nextSibling = currentNode.getNextSibling() ;
		while(nextSibling != null && !found)
		{
			if(nextSibling.equals(node2)) found = true ;
			nextSibling = nextSibling.getNextSibling();
			System.out.println("[AnnotatedHtml.sameLevel] next sibling : " + nextSibling);
		}*/
		//return found ;
		return false ;
	}
	/**
	 * Tests if node1 has node2 as a child. Recursive method.
	 * @param node1
	 * @param node2
	 * @return boolean
	 */
	public static boolean hasChild(Node node1, Node node2)
	{
		boolean found = false ;
		NodeList children = node1.getChildren() ;
		if(children != null)
		{
			int nbChild = children.size();
			Node currentChild ;
			int cptChild = 0 ;
			while(cptChild < nbChild)
			{
				currentChild = children.elementAt(cptChild);
				if(currentChild.equals(node2)) found = true ;
				else
				{
					found = hasChild(currentChild , node2);
				}
				cptChild ++ ;
			}
			
		}
		return found ;
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
			//System.out.println("level : " + level + " elt nb : " + i);
			Node node = nl.elementAt(i);
			//System.out.println(node.toHtml());
			seeThroughNodeList(level + 1 , node.getChildren());
		}
	}
	
	private Node findNode(NodeList nl , SplitedXpointer splitedXpointer)
	{
		int nbNode = splitedXpointer.getXpointerTree().length ;
		
		System.out.println("Nb nodes : " + nbNode);
		
		System.out.println(splitedXpointer);
		
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
			//System.out.println("[AnnotatedHtml.findNode] cptNode : " + cptNode);
			String info = splitedXpointer.getXpointerTree()[cptNode];
			try{// Pour l'instant on ne fabrique que des Xpointer avec des int après l'id de la ressource
				Integer nextSibling = Integer.parseInt(info);
				//System.out.println("[AnnotatedHtml.findNode] nextSibling : " + nextSibling);
				//Attention, on ne peut pas juste aller chercher l'élément à nextSibling car la lib htmlParser compte les TextNode
				//Donc il faut itérer et ignorer les TextNode
				int cptGlobal = 0;
				int cptWhithoutTextNode = 0 ;
				while(cptWhithoutTextNode < nextSibling )
				{
					//System.out.println("[AnnotatedHtml.findNode] in while next node : " + nl.elementAt(cptGlobal).toHtml());
					if(! (nl.elementAt(cptGlobal) instanceof TextNode)) cptWhithoutTextNode ++ ;
					cptGlobal ++ ;
					//System.out.println("[AnnotatedHtml.findNode] cptWithoutTexteNode : " + cptWhithoutTextNode + " cptGlobal : " + cptGlobal);
				}
				Node testTextNode = currentNodeList.elementAt(cptGlobal) ;
				while(testTextNode instanceof TextNode) 
				{
					cptGlobal ++ ;
					testTextNode = currentNodeList.elementAt(cptGlobal) ;
				}
				//System.out.println("[AnnotatedHtml.findNode] cptGlobal : " + cptGlobal);
				currentNodeList = testTextNode.getChildren();
			}
			catch (NumberFormatException exception) // La syntaxe Xpointer permet d'autres cibles que des int
			{
				exception.printStackTrace();
				//System.out.println("[AnnotatedHtml.findNode] nextSibling is not an integer !!!");
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
			//System.out.println("[AnnotatedHtml.findNode] nextSibling is not an integer !!!");
			//TODO gérer les autres syntaxes XPointer
		}
		//return currentNodeList.elementAt(0) ;
		return currentNode ;
	}
}