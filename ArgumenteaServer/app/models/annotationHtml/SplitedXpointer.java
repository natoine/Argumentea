package models.annotationHtml;

public class SplitedXpointer 
{
	private String[] xpointerTree ;
	private int indice ;
	private String startingHtmlNodeName ;
	
	public SplitedXpointer()
	{
		xpointerTree = null ;
		indice = 0 ;
		startingHtmlNodeName = "undefined";
	}
	
	public String[] getXpointerTree() {
		return xpointerTree;
	}
	public void setXpointerTree(String[] xpointerTree) {
		this.xpointerTree = xpointerTree;
	}
	public int getIndice() {
		return indice;
	}
	public void setIndice(int indice) {
		this.indice = indice;
	}
	public String getStartingHtmlNodeName() {
		return startingHtmlNodeName;
	}
	public void setStartingHtmlNodeName(String startingHtmlNodeName) {
		this.startingHtmlNodeName = startingHtmlNodeName;
	}
	
	public static SplitedXpointer createXpointer( String xpointer , SplitedXpointer splitedXpointer)
	{
		//System.out.println("[SplitedXpointer.createNode] raw xpointer : " + xpointer);
		splitedXpointer.setXpointerTree( xpointerSplit(xpointer) );
		splitedXpointer.setIndice(getTextPositionXpointer(xpointer)); 
		//System.out.println("[SplitedXpointer.createNode] indiceStart : " + splitedXpointer.indice);
		//System.out.println("[SplitedXpointer.createNode] SplitedXpointerTree : ");
		for(int i = 0 ; i < splitedXpointer.xpointerTree.length ; i++)
		{
			//System.out.println("" + splitedXpointer.xpointerTree[i]);
		}
		return splitedXpointer ;
	}

	public static String[] xpointerSplit(String xpointer)
	{
		String xpointer_tag = "#xpointer(" ;
		int begin_sub = xpointer.indexOf(xpointer_tag) + xpointer_tag.length();
		String clean_xpointer = xpointer.substring(begin_sub , xpointer.length());
		clean_xpointer = clean_xpointer.substring(0, clean_xpointer.indexOf(','));
		return clean_xpointer.split("/");
	}

	//retourne l'indice textuel du xpointer
	public static int getTextPositionXpointer(String xpointer)
	{
		int coma_index = xpointer.indexOf(',') ;
		if(coma_index > 0 && coma_index < xpointer.length())
		{
			String position = xpointer.substring(coma_index + 1, xpointer.length()-1);
			return Integer.parseInt(position);
		}
		else return -1 ;
	}
	
}