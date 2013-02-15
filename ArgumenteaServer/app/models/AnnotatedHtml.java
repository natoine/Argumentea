package models;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.CssSelectorNodeFilter;
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
	
	public void highLight(Annotation annotation, String color) throws ParserException
	{
		this.addAnnotationSpan(annotation.getPointerBegin(), annotation.getPointerEnd(), color, annotation.getContent(), annotation.getId().toString());
	}
	

	public String[] xpointerSplit(String _xpointer)
	{
		String xpointer_tag = "#xpointer(" ;
		int begin_sub = _xpointer.indexOf(xpointer_tag) + xpointer_tag.length();
		String clean_xpointer = _xpointer.substring(begin_sub , _xpointer.length());
		clean_xpointer = clean_xpointer.substring(0, clean_xpointer.indexOf(','));
		return clean_xpointer.split("/");
	}

	//retourne l'indice textuel du xpointer
	private int getTextPositionXpointer(String _xpointer)
	{
		int coma_index = _xpointer.indexOf(',') ;
		if(coma_index > 0 && coma_index < _xpointer.length())
		{
			String position = _xpointer.substring(coma_index + 1, _xpointer.length()-1);
			return Integer.parseInt(position);
		}
		else return -1 ;
	}

	public boolean isChildXPointer(String _xpointer_father, String _xpointer_child) throws ParserException
	{
		//System.out.println("father : " + _xpointer_father);
		//System.out.println("child : " + _xpointer_child);
		if(_xpointer_father.contains(","))
		{
			String clean_xpointer_father = _xpointer_father.split(",")[0];
			if(_xpointer_child.startsWith(clean_xpointer_father)) return true ;
		}
		Parser parser = Parser.createParser(this.htmlContent , null);
		NodeList nl = parser.parse(null);
		Node father = this.getNodeXpointer(_xpointer_father, nl);
		Node child = this.getNodeXpointer(_xpointer_child, nl);
		//System.out.println("Father Node : " + father.toHtml());
		//System.out.println("Child Node : " + child.toHtml());
		return isChildNode(father , child);
	}

	public boolean isChildNode(Node _father , Node _child)
	{
		boolean to_return = false ;
		NodeList children = _father.getChildren();
		int children_length = 0 ;
		if(children!=null) children_length = children.size() ;
		int cpt_children = 0 ;
		while(!to_return && cpt_children < children_length)
		{
			Node to_test = children.elementAt(cpt_children);
			if(to_test.equals(_child)) return true ;
			else to_return = isChildNode(to_test , _child);
			cpt_children ++ ;
		}
		return to_return ;
	}

	//Renvoie le noeud relatif à un XPointer dans le body
	//!!!ATTENTION un saut de ligne entre deux divs est compté comme un textNode ... La merde htmlParser je vous jure.
	//!!!Attention, devrait ignorer les SPAN annotation déjà ajoutées ... TODO
	public Node getNodeXpointer(String _xpointer , NodeList _nl) throws ParserException
	{
		//toujours vérifier que l'on ne considére pas une balise span de classe annotation
		Node current = null;
		String[] splited_xpointer = xpointerSplit(_xpointer);
		//créer la nodelist
		//Parser parser = Parser.createParser(body , null);
		//NodeList nl = parser.parse(null);
		int nb_selectors = splited_xpointer.length ;//nombre d'éléments dans le xpointe (séparations par des /)
		//premier élément : body ou id
		//if(splited_xpointer[0].contains("body"))
		if(splited_xpointer[0].contains("article"))
		{
			//se placer sur le premier noeud
			current = _nl.elementAt(0);
			//System.out.println("Node value : " + current.toHtml());
		}
		else if(splited_xpointer[0].contains("id"))
		{
			//récupérer le véritable id
			String id = splited_xpointer[0].substring(splited_xpointer[0].indexOf("id") + 4 , splited_xpointer[0].length() - 2);
			//System.out.println("id : " + id );
			//se placer sur le noeud correspondant
			NodeList nlId = _nl.extractAllNodesThatMatch(new CssSelectorNodeFilter("#" + id), true) ;
			if(nlId.size() > 0 ) current = nlId.elementAt(0);
			//System.out.println("Node value : " + current.toHtml());
		}
		if(nb_selectors == 1) return current ; // il n'y a qu'un élément dans le xpointer donc on a fini le travail.
		else
		{
			int cpt_node_selector = 1 ;//pour compter les éléments du xpointer parcourus.
			while(cpt_node_selector < nb_selectors && current != null)
			{
				int indice_child_node = Integer.parseInt(splited_xpointer[cpt_node_selector]);
				//System.out.println("indice_child_node : " + indice_child_node);
				NodeList children = current.getChildren() ;
				//System.out.println("nb children :" + children.size());
				int nb_children = 0 ; //va compter le nombre de fils parcourus
				int true_nb_children = 0 ; // va compter le nombre de fils parcourus hors span d'annotations.
				int children_size = 0 ;
				if(children != null) children_size = children.size() ;
				while(nb_children < children_size && true_nb_children < indice_child_node)//parcours des fils du noeud courant jusqu'à trouver celui d'indice indice_child_node
				{
					Node current_child = children.elementAt(nb_children);
					//System.out.println("HTMLPage getNodeXpointer current_child nb " + nb_children + " : " + current_child.toPlainTextString() + " length : " + current_child.toPlainTextString().length());
					//tester si le noeud est une span d'annotation
					//if(current_child instanceof TagNode && ((TagNode)current_child).getTagName().equalsIgnoreCase("span") && ((TagNode)current_child).getAttribute("class").equals("annotation"))
					//if(current_child instanceof Span) System.out.println("SPAAAAAAAAAAAN !!!!!!!!!!!!!!!!!!!!!!");
					if(current_child instanceof Span && ((Span)current_child).getAttribute("class") != null && ((Span)current_child).getAttribute("class").equals("annotation"))
					{
						//System.out.println("HTMLPage getNodeXpointer considére bien que c'est un SPAN d'annotation");
						nb_children ++ ;
					}
					else if(current_child instanceof TextNode )
						//il faut ignorer les sauts de ligne dans le HTML qui ne servent qu'à structurer le code source, les navigateurs ne les prennent pas en compte eux
					{
						nb_children ++ ;
						/*String content = current_child.getText() ;
content = content.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
if(content.length() > 0) true_nb_children ++ ;
else System.out.println("Noeud ignoré !!!");*/
					}
					else
					{
						nb_children ++ ;
						true_nb_children ++ ;
					}
				}
				//System.out.println("fin du while, nb_children : " + nb_children + " true_nb_children : " + true_nb_children );
				if(true_nb_children == indice_child_node && children!= null && children.size() > 0)
				{
					if(nb_children > 0) current = children.elementAt(nb_children -1);
					else current = children.elementAt(0);
					//System.out.println("on change de current, nouveau current : " + current);
				}
				cpt_node_selector ++ ;
			}
		}
		//System.out.println("[getNodeXpointer] current : " + current.toHtml());
		//Attention au cas ou le noeud sur lequel on s'arréte est une SPAN d'annotation
		if(current instanceof Span && ((Span)current).getAttribute("class") != null && ((Span)current).getAttribute("class").equals("annotation"))
		{//dans ce cas, prendre le noeud suivant tant qu'il n'est pas une annotation. Si on a une abération, il vaut mieux renvoyer current quand même
			//System.out.println("[getNodeXpointer] parcours des nextSibling");
			Node next_sibling = current.getNextSibling();
			while(next_sibling!=null && next_sibling instanceof Span && ((Span)next_sibling).getAttribute("class")!=null && ((Span)next_sibling).getAttribute("class").equals("annotation"))
			{
				next_sibling = next_sibling.getNextSibling();
			}
			if(next_sibling!=null) current = next_sibling ;
		}
		return current ;
	}

	//Renvoie vrai si deux xpointer référent un même noeud
	private boolean testSameNodeXpointer(String _xpointer1 , String _xpointer2) throws ParserException
	{
		//System.out.println("[testSameNodeXpointer]");
		if(! _xpointer1.substring(0, _xpointer1.indexOf("#")).equalsIgnoreCase(_xpointer2.substring(0, _xpointer2.indexOf("#")))) return false ;
		else if(_xpointer1.substring(0, _xpointer1.indexOf(',')).equalsIgnoreCase(_xpointer2.substring(0, _xpointer2.indexOf(',')))) return true ;
		else
		{
			Parser parser = Parser.createParser(this.htmlContent , null);
			NodeList nl = parser.parse(null);
			Node node1 = getNodeXpointer(_xpointer1 , nl);
			Node node2 = getNodeXpointer(_xpointer2 , nl);
			return node1 == node2 ;
		}
	}

	private void createSpanAndBefore(String _toModify , int _indice_start, int _indice_end, NodeList _newChildrenList, String _span_style , String _annotation_content, String _annotation_id , TagNode _endSpan)
	{
		String beforeSpan = _toModify.substring(0, _indice_start);
		String insideSpan = _toModify.substring(_indice_start, _indice_end);
		TextNode before_node = new TextNode(beforeSpan);
		_newChildrenList.add(before_node);
		Span span = createAnnotation(_span_style , _annotation_content , _annotation_id , insideSpan, _endSpan);
		_newChildrenList.add(span);
	}

	private void createSpanAndSurrounding(String _toModify , int _indice_start, int _indice_end, int _to_modify_content_length, NodeList _newChildrenList, String _span_style , String _annotation_content, String _annotation_id , TagNode _endSpan)
	{
		this.createSpanAndBefore(_toModify, _indice_start, _indice_end, _newChildrenList, _span_style, _annotation_content, _annotation_id, _endSpan);
		String afterSpan = _toModify.substring(_indice_end, _to_modify_content_length);
		TextNode after_node = new TextNode(afterSpan);
		_newChildrenList.add(after_node);
	}

	private Span createAnnotation(String _span_style , String _annotation_content , String _annotation_id , String _text_inside_span, TagNode _endSpan)
	{
		Span span = new Span();
		span.setAttribute("class" , "annotation" , '\"');
		span.setAttribute("style" , _span_style , '\"');
		span.setAttribute("title" , _annotation_content , '\"');
		span.setAttribute("id", "annotation_" + _annotation_id , '\'');
		NodeList newSpanChildrenList = new NodeList();
		TextNode inside_span_node = new TextNode(_text_inside_span);
		newSpanChildrenList.add(inside_span_node);
		span.setChildren(newSpanChildrenList);
		span.setEndPosition(_text_inside_span.length());
		span.setEndTag(_endSpan);
		return span ;
	}


	//Modifie le body en ajoutant les balises span nécessaires pour colorer une annotation dans la page
	public void addAnnotationSpan(String _xpointer_start, String _xpointer_end, String _span_style, String _annotation_content, String _annotation_id) throws ParserException
	{
		//System.out.println("[addAnnotationSpan] xpointerStart : " + _xpointer_start + "xpointerEnd : " + _xpointer_end);
		//Les balises span à placer sont de classe annotation
		TagNode endSpan = new TagNode();
		endSpan.setTagName("/SPAN");
		Parser parser = Parser.createParser(this.htmlContent , null);
		NodeList nl = parser.parse(null);
		int indice_start = getTextPositionXpointer(_xpointer_start);
		int indice_end = getTextPositionXpointer(_xpointer_end);
		int nb_span_annotation = 0 ;
		//Si les xpointers renvoient à un même noeud (l'id des balises span seront annotation_[id])
		if(testSameNodeXpointer(_xpointer_start, _xpointer_end))
		{
			//System.out.println("same node");
			Node nodeToModify = getNodeXpointer(_xpointer_start, nl);
			//System.out.println("addAnnotationSpan to modify node : " + nodeToModify.toHtml());
			if(nodeToModify != null)
			{
				if(indice_start < indice_end) addSpans(nodeToModify, indice_start, indice_end, nb_span_annotation, _span_style, _annotation_content, ""+_annotation_id, endSpan);
				else if(indice_start > indice_end) addSpans(nodeToModify, indice_end, indice_start, nb_span_annotation, _span_style, _annotation_content, ""+_annotation_id, endSpan);
				//on gére les cas ou les indices ne sont pas dans le bon ordre et on exclue les cas ou les indices sont identiques et qu'il n'y a rien à faire
			}
		}
		else
		{
			//System.out.println("not same node");
			//Si les xpointer ne renvoient pas à un même noeud (l'id des balises span seront annotation_[id]-[indice] ou indice est le nombre de spans précédentes pour cette annotation)
			boolean isChild = isChildXPointer(_xpointer_start, _xpointer_end);
			Node startNode = getNodeXpointer(_xpointer_start, nl);
			Node endNode = getNodeXpointer(_xpointer_end, nl);
			if(startNode != null && endNode != null)
			{
				int[] actual_state ;
				//Soit la deuxième balise est un fils de la première
				if(isChild)
				{
					//System.out.println("isChild !!! xpointerStart : " + _xpointer_start + " xpointerEnd : " + _xpointer_end);
					//Colorer tout le texte allant du point de départ du xpointer jusqu'à la balise fils
					actual_state = addSpansAllNodeUntilSpecificNode(startNode, endNode, indice_start, nb_span_annotation, _span_style, _annotation_content, ""+_annotation_id, endSpan);
					indice_start = 0 ;
					nb_span_annotation = actual_state[1];
					//Colorer le texte de la balise fils/finale du début de cette balise jusqu'à son indice
					addSpans(endNode, 0, indice_end, nb_span_annotation, _span_style, _annotation_content, ""+ _annotation_id, endSpan);
				}
				else
				{
					//Soit la deuxième balise est à un autre niveau dans le DOM (pas fils)
					//System.out.println("is not Child !!! xpointerStart : " + _xpointer_start + " xpointerEnd : " + _xpointer_end);
					//pour noeud de départ
					actual_state = addSpansAllChildren(startNode, indice_start, 0, _span_style, _annotation_content, "" +_annotation_id, endSpan);
					nb_span_annotation = actual_state[1];
					//pour noeud entre départ et arrivé
					//Problem, si le noeud d'arrivée n'est pas au même niveau dans le dom ?
					//Solution : Parcourir tous les noeuds à la même hauteur que le noeud de départ et tester si le noeud d'arrivée est l'un de leurs fils
					Node next_sibling = startNode.getNextSibling();
					NodeList toHighlight = new NodeList();
					while(next_sibling!=null && !isChildNode(next_sibling, endNode))
					{
						toHighlight.add(next_sibling);
						next_sibling = next_sibling.getNextSibling();
					}
					if(next_sibling != null)
					{
						//next_sibling est donc un père du noeud d'arrivée
						//Colorer tout ce qui est dans toHighlight
						for(int i =0 ; i< toHighlight.size() ; i++)
						{
							actual_state = addSpansProcessChildrenNoEndLimit(toHighlight.elementAt(i), 0, nb_span_annotation, _span_style, _annotation_content, ""+_annotation_id, endSpan);
							nb_span_annotation = actual_state[1];
						}
						//colorer tous les noeuds avant le endNode
						actual_state = addSpansAllNodeUntilSpecificNode(next_sibling, endNode, 0, nb_span_annotation, _span_style, _annotation_content,""+ _annotation_id, endSpan);
						nb_span_annotation = actual_state[1];
					}
					//pour noeud d'arrivé
					addSpans(endNode, 0, indice_end, nb_span_annotation, _span_style, _annotation_content, ""+ _annotation_id, endSpan);	
				}
			}
		}
		if(nl != null)
		{
			String new_html = nl.toHtml();
			if(new_html != null && new_html.length() > 0) this.htmlContent = new_html ;
		}
	}

	private int[] addSpansAllNodeUntilSpecificNode(Node startNode , Node endNode , int indice_start, int nb_span_annotation, String _span_style, String _annotation_content, String _annotation_id, TagNode endSpan)
	{
		NodeList startNodeChildren = startNode.getChildren();
		if(startNodeChildren != null)
		{
			NodeList newStartNodeList = new NodeList();
			int cpt_children = 0 ;
			Node current_child = startNodeChildren.elementAt(cpt_children);
			while(!current_child.equals(endNode) && !isChildNode(current_child , endNode))
			{
				//System.out.println("current_child : " + current_child.toHtml());
				if(current_child instanceof TextNode)
				{
					String toModifyContent = ((TextNode)current_child).getText();
					int to_modify_content_length = toModifyContent.length();
					//vérification que les indices sont compatibles, sinon on met 0 et length en valeurs
					if(indice_start > to_modify_content_length) indice_start = 0 ;
					if(nb_span_annotation >0 ) createSpanAndBefore(toModifyContent , indice_start, to_modify_content_length, newStartNodeList, _span_style , _annotation_content, ""+ _annotation_id +"-"+nb_span_annotation , endSpan);
					else createSpanAndBefore(toModifyContent , indice_start, to_modify_content_length, newStartNodeList, _span_style , _annotation_content, ""+ _annotation_id , endSpan);
					nb_span_annotation ++ ;
				}
				else
				{
					int[] actual_state = addSpansNoEndLimit(current_child, indice_start, nb_span_annotation, _span_style, _annotation_content, ""+_annotation_id, endSpan);
					newStartNodeList.add(current_child);
					indice_start = 0;
					nb_span_annotation = actual_state[1];
				}
				cpt_children ++ ;
				current_child = startNodeChildren.elementAt(cpt_children);
			}
			//on ajoute à la liste les noeuds non modifiés
			while(cpt_children < startNodeChildren.size())
			{
				newStartNodeList.add(startNodeChildren.elementAt(cpt_children));
				cpt_children ++ ;
			}
			//on sette la nouvelle liste de fils
			startNode.setChildren(newStartNodeList);
		}
		int[] to_return = {indice_start , nb_span_annotation};
		return to_return ;
	}

	private int[] addSpans(Node _nodeToModify, int _indice_start, int _indice_end, int _nb_span_annotation, String _span_style , String _annotation_content, String _annotation_id, TagNode _endSpan)
	{
		//récupération des noeuds fils du noeud à modifier
		NodeList childrenOfNodeToModify = _nodeToModify.getChildren() ;
		if(childrenOfNodeToModify!=null)
		{
			int nb_children = childrenOfNodeToModify.size() ;
			if(nb_children > 0)//Si le noeud n'a pas au moins un fils TextNode, il n'y a rien à faire.
			{
				if(childrenOfNodeToModify.size() == 1 && childrenOfNodeToModify.elementAt(0) instanceof TextNode)//S'il n'y a que du texte dans le noeud, le traitement est simple
				{
					//System.out.println("addSpans only textNode");
					TextNode content_textnode = (TextNode)childrenOfNodeToModify.elementAt(0) ;
					String toModifyContent = content_textnode.getText() ;	
					int to_modify_content_length = toModifyContent.length();
					//vérification que les indices sont compatibles, sinon on met 0 et length en valeurs
					if(_indice_start > to_modify_content_length) _indice_start = 0 ;
					if(_indice_end > to_modify_content_length) _indice_end = to_modify_content_length ;
					NodeList newChildrenList = new NodeList();
					if(_nb_span_annotation >0 ) createSpanAndSurrounding(toModifyContent , _indice_start, _indice_end, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id +"-"+_nb_span_annotation , _endSpan);
					else createSpanAndSurrounding(toModifyContent , _indice_start, _indice_end, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id , _endSpan);
					_nodeToModify.setChildren(newChildrenList);
					_nb_span_annotation ++ ;
				}
				else
				{//il y a d'autres fils, pas qu'un noeud texte.
					//parcours de tous les fils
					int[] actual_state = addSpansProcessChildren(_nodeToModify, _indice_start, _indice_end, _nb_span_annotation, _span_style, _annotation_content, _annotation_id, _endSpan);
					_indice_start = actual_state[0];
					_indice_end = actual_state[1];
					_nb_span_annotation = actual_state[2];
				}
			}
		}
		else if(_nodeToModify instanceof TextNode)//en fait il n'y a que ce noeud à modifier
		{
			String toModifyContent = ((TextNode)_nodeToModify).getText();
			int to_modify_content_length = toModifyContent.length();
			//vérification que les indices sont compatibles, sinon on met 0 et length en valeurs
			if(_indice_start > to_modify_content_length) _indice_start = 0 ;
			if(_indice_end > to_modify_content_length) _indice_end = to_modify_content_length ;
			NodeList newChildrenList = new NodeList();
			if(_nb_span_annotation >0 ) createSpanAndSurrounding(toModifyContent , _indice_start, _indice_end, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id +"-"+_nb_span_annotation , _endSpan);
			else createSpanAndSurrounding(toModifyContent , _indice_start, _indice_end, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id , _endSpan);
			_nodeToModify.setChildren(newChildrenList);
			_nb_span_annotation ++ ;
		}
		int[] to_return = {_indice_start , _indice_end, _nb_span_annotation};
		return to_return;
	}

	private int[] addSpansNoEndLimit(Node _nodeToModify, int _indice_start, int _nb_span_annotation, String _span_style , String _annotation_content, String _annotation_id, TagNode _endSpan)
	{
		//System.out.println("[addSpansNoEndLimit]");
		//récupération des noeuds fils du noeud à modifier
		NodeList childrenOfNodeToModify = _nodeToModify.getChildren() ;
		if(childrenOfNodeToModify!=null)
		{
			//System.out.println("[addSpansNoEndLimit] has children");
			int nb_children = childrenOfNodeToModify.size() ;
			if(nb_children > 0)//Si le noeud n'a pas au moins un fils TextNode, il n'y a rien à faire.
			{
				if(childrenOfNodeToModify.size() == 1 && childrenOfNodeToModify.elementAt(0) instanceof TextNode)//S'il n'y a que du texte dans le noeud, le traitement est simple
				{
					//System.out.println("addSpansNoEndLimit : only textNode");
					TextNode content_textnode = (TextNode)childrenOfNodeToModify.elementAt(0) ;
					String toModifyContent = content_textnode.getText() ;	
					int to_modify_content_length = toModifyContent.length();
					//vérification que les indices sont compatibles, sinon on met 0 et length en valeurs
					if(_indice_start > to_modify_content_length) _indice_start = 0 ;
					NodeList newChildrenList = new NodeList();
					if(_nb_span_annotation >0 ) createSpanAndBefore(toModifyContent , _indice_start, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id +"-"+_nb_span_annotation , _endSpan);
					else createSpanAndBefore(toModifyContent , _indice_start, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id , _endSpan);
					_nodeToModify.setChildren(newChildrenList);
					//System.out.println("addSpansNoEndLimit new content : " + _nodeToModify.toHtml());
					_nb_span_annotation ++ ;
				}
				else
				{//il y a d'autres fils, pas qu'un noeud texte.
					//parcours de tous les fils
					int[] actual_state = this.addSpansProcessChildrenNoEndLimit(_nodeToModify, _indice_start, _nb_span_annotation, _span_style, _annotation_content, _annotation_id, _endSpan);
					_indice_start = actual_state[0];
					_nb_span_annotation = actual_state[1];
				}
			}
		}
		else if(_nodeToModify instanceof TextNode)//en fait il n'y a que ce noeud à modifier
		{
			//System.out.println("[addSpansNoEndLimit] is TextNode");
			String toModifyContent = ((TextNode)_nodeToModify).getText();
			int to_modify_content_length = toModifyContent.length();
			//vérification que les indices sont compatibles, sinon on met 0 et length en valeurs
			if(_indice_start > to_modify_content_length) _indice_start = 0 ;
			NodeList newChildrenList = new NodeList();
			if(_nb_span_annotation >0 ) createSpanAndBefore(toModifyContent , _indice_start, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id +"-"+_nb_span_annotation , _endSpan);
			else createSpanAndBefore(toModifyContent , _indice_start, to_modify_content_length, newChildrenList, _span_style , _annotation_content, ""+ _annotation_id , _endSpan);
			_nodeToModify.setChildren(newChildrenList);
			_nb_span_annotation ++ ;
		}
		int[] to_return = {_indice_start , _nb_span_annotation};
		return to_return;
	}

	private int[] addSpansAllChildren(Node _nodeToModify , int _start_indice, int _nb_span_annotation, String _span_style, String _annotation_content, String _annotation_id, TagNode _endSpan)
	{
		NodeList childrenOfNodeToModify = _nodeToModify.getChildren();
		if(childrenOfNodeToModify != null)
		{
			NodeList toModifyNewChildren = new NodeList();
			for(int cptchildren = 0 ; cptchildren < childrenOfNodeToModify.size() ; cptchildren ++)
			{
				Node current_child = childrenOfNodeToModify.elementAt(cptchildren);
				//SI c'est un textNode, on colore
				if(current_child instanceof TextNode)
				{
					int[] actual_state = createSpanInTextNode((TextNode)current_child, cptchildren, childrenOfNodeToModify, toModifyNewChildren, _start_indice, ((TextNode)current_child).getText().length(), _nb_span_annotation, _span_style, _annotation_content, _annotation_id, _endSpan);
					_start_indice = 0 ; //on colore l'intégralité pour les autres noeuds
					_nb_span_annotation = actual_state[2] ;
				}
				//TODO modifier ce comportement, descendre jusqu'aux Textnode et les encadrer de span annotation
				//SI c'est autre chose, rien à modifier, on ajoute le noeud tel quel, du coup quand on annote avec un lien dans l'annotation, le lien n'est pas surligné...
				else toModifyNewChildren.add(current_child);// ne pas oublier de conserver les noeuds non annotés
			}
			_nodeToModify.setChildren(toModifyNewChildren);
		}
		/*else
{
if(_nodeToModify instanceof TextNode)
{
String content = _nodeToModify.getText();
NodeList newChildrenList = new NodeList();
createSpanAndBefore(content, _start_indice, content.length(), newChildrenList, _span_style, _annotation_content, ""+_annotation_id, _endSpan);
_nodeToModify.setChildren(newChildrenList);
_nb_span_annotation ++ ;
}
}*/
		int[] to_return = {_start_indice , _nb_span_annotation};
		return to_return ;
	}

	private int[] addSpansProcessChildren( Node _nodeToModify, int _start_indice, int _end_indice, int _nb_span_annotation, String _span_style, String _annotation_content, String _annotation_id, TagNode _endSpan)
	{
		int already_ended = 0 ;
		NodeList childrenOfNodeToModify = _nodeToModify.getChildren();
		if(childrenOfNodeToModify != null)
		{
			NodeList toModifyNewChildren = new NodeList();
			for(int cptchildren = 0 ; cptchildren < childrenOfNodeToModify.size() ; cptchildren ++)
			{
				Node current_child = childrenOfNodeToModify.elementAt(cptchildren);
				//SI c'est un textNode, on colore
				if(current_child instanceof TextNode)
				{
					int[] actual_state = createSpanInTextNode((TextNode)current_child, cptchildren, childrenOfNodeToModify, toModifyNewChildren, _start_indice, _end_indice, _nb_span_annotation, _span_style, _annotation_content, _annotation_id, _endSpan);
					_start_indice = actual_state[0];
					_end_indice = actual_state[1];
					_nb_span_annotation = actual_state[2];
					already_ended = actual_state[3];
					if(already_ended == -1) cptchildren = childrenOfNodeToModify.size();
				}
				//SI c'est une SPAN annotation on va devoir faire une récursion sur son contenu
				else if(current_child instanceof Span && ((Span)current_child).getAttribute("class") != null && ((Span)current_child).getAttribute("class").equalsIgnoreCase("annotation"))
				{
					int[] actual_state = addSpansProcessChildren(current_child, _start_indice, _end_indice, _nb_span_annotation, _span_style, _annotation_content, _annotation_id, _endSpan);
					_start_indice = actual_state[0];
					_end_indice = actual_state[1];
					_nb_span_annotation = actual_state[2];
					already_ended = actual_state[3];
					toModifyNewChildren.add(current_child);
					if(already_ended == -1)
					{
						//ajouter tous les noeuds restant
						for(int i = cptchildren + 1 ; i < childrenOfNodeToModify.size() ; i ++)
						{
							toModifyNewChildren.add(childrenOfNodeToModify.elementAt(i));
						}
						//mettre fin au parcours
						cptchildren = childrenOfNodeToModify.size();
					}
				}
				//TODO modifier ce comportement, descendre jusqu'aux Textnode et les encadrer de span annotation
				//SI c'est autre chose, rien à modifier, on ajoute le noeud tel quel, du coup quand on annote avec un lien dans l'annotation, le lien n'est pas surligné...
				else toModifyNewChildren.add(current_child);// ne pas oublier de conserver les noeuds non annotés
			}
			_nodeToModify.setChildren(toModifyNewChildren);
		}
		int[] to_return = {_start_indice , _end_indice , _nb_span_annotation , already_ended};
		return to_return ;
	}

	private int[] addSpansProcessChildrenNoEndLimit( Node _nodeToModify, int _start_indice, int _nb_span_annotation, String _span_style, String _annotation_content, String _annotation_id, TagNode _endSpan)
	{
		//int already_ended = 0 ;
		NodeList childrenOfNodeToModify = _nodeToModify.getChildren();
		if(childrenOfNodeToModify != null)
		{
			NodeList toModifyNewChildren = new NodeList();
			for(int cptchildren = 0 ; cptchildren < childrenOfNodeToModify.size() ; cptchildren ++)
			{
				Node current_child = childrenOfNodeToModify.elementAt(cptchildren);
				//SI c'est un textNode, on colore
				if(current_child instanceof TextNode)
				{
					int[] actual_state = createSpanInTextNode((TextNode)current_child, cptchildren, childrenOfNodeToModify, toModifyNewChildren, _start_indice, ((TextNode)current_child).getText().length(), _nb_span_annotation, _span_style, _annotation_content, _annotation_id, _endSpan);
					_start_indice = actual_state[0];
					//_end_indice = actual_state[1];
					_nb_span_annotation = actual_state[2];
					//already_ended = actual_state[3];
					//if(already_ended == -1) cptchildren = childrenOfNodeToModify.size();
				}
				//SI c'est une SPAN annotation on va devoir faire une récursion sur son contenu
				/*else if(current_child instanceof Span && ((Span)current_child).getAttribute("class").equalsIgnoreCase("annotation"))
{
int[] actual_state = addSpansProcessChildren(current_child, _start_indice, _end_indice, _nb_span_annotation, _span_style, _annotation_content, _annotation_id, _endSpan);
_start_indice = actual_state[0];
_end_indice = actual_state[1];
_nb_span_annotation = actual_state[2];
already_ended = actual_state[3];
toModifyNewChildren.add(current_child);
if(already_ended == -1)
{
//ajouter tous les noeuds restant
for(int i = cptchildren + 1 ; i < childrenOfNodeToModify.size() ; i ++)
{
toModifyNewChildren.add(childrenOfNodeToModify.elementAt(i));
}
//mettre fin au parcours
cptchildren = childrenOfNodeToModify.size();
}
}*/
				//TODO modifier ce comportement, descendre jusqu'aux Textnode et les encadrer de span annotation
				//SI c'est autre chose, rien à modifier, on ajoute le noeud tel quel, du coup quand on annote avec un lien dans l'annotation, le lien n'est pas surligné...
				else toModifyNewChildren.add(current_child);// ne pas oublier de conserver les noeuds non annotés
			}
			_nodeToModify.setChildren(toModifyNewChildren);
		}
		//int[] to_return = {_start_indice , _end_indice , _nb_span_annotation , already_ended};
		int[] to_return = {_start_indice , _nb_span_annotation };
		return to_return ;
	}

	private int[] createSpanInTextNode(
			TextNode _textNodeToProcess,
			int _cptchildrenOfNodeToModifyAlreadyProcessed,
			NodeList _childrenOfNodeToModify,
			NodeList _newChildrenOfNodeToModify,
			int _start_indice, int _end_indice, int _nb_span_annotation,
			String _span_style, String _annotation_content, String _annotation_id, TagNode _endSpan)
	{
		int end = 0 ;
		String text_content = _textNodeToProcess.getText() ;
		int length_text_content = text_content.length() ;
		if(length_text_content < _start_indice) //on est avant l'annotation
		{
			_start_indice = _start_indice - length_text_content ;
			_end_indice = _end_indice - length_text_content ;
			_newChildrenOfNodeToModify.add(_textNodeToProcess);
		}
		else if(length_text_content >= _start_indice && length_text_content > _end_indice)//toute l'annotation est dans ce noeud texte
		{
			//créer l'annotation
			if(_nb_span_annotation > 0)
			{
				this.createSpanAndSurrounding(text_content, _start_indice, _end_indice, text_content.length(), _newChildrenOfNodeToModify, _span_style, _annotation_content, _annotation_id + "-" + _nb_span_annotation, _endSpan);
			}
			else this.createSpanAndSurrounding(text_content, _start_indice, _end_indice, text_content.length(), _newChildrenOfNodeToModify, _span_style, _annotation_content, ""+ _annotation_id, _endSpan);
			//mettre fin au parcours de fils puisque l'annotation a été créée, mais ne pas oublier d'ajouter tout le reste des noeuds non parcourus
			for(int children_unchecked = _cptchildrenOfNodeToModifyAlreadyProcessed + 1 ; children_unchecked < _childrenOfNodeToModify.size() ; children_unchecked ++)
			{
				_newChildrenOfNodeToModify.add(_childrenOfNodeToModify.elementAt(children_unchecked));
			}
			_cptchildrenOfNodeToModifyAlreadyProcessed = _childrenOfNodeToModify.size() ;
			end = -1 ;
		}
		else if(length_text_content >= _start_indice)//le début de l'annotation est dans ce noeud texte mais la fin est dans un autre noeud
		{
			//il va falloir créer plusieurs annotations
			createSpanAndBefore(text_content, _start_indice, text_content.length(), _newChildrenOfNodeToModify, _span_style, _annotation_content, ""+_annotation_id + "-" + _nb_span_annotation, _endSpan);
			_nb_span_annotation ++ ;
			_start_indice = 0 ;
			_end_indice = _end_indice - length_text_content ;
		}
		int[] to_return = {_start_indice , _end_indice, _nb_span_annotation, end};
		return to_return ;
	}
}