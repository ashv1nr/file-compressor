
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class HuffmanCodeTree
{
	final String LEFT = "0";
	final String RIGHT = "1";
	private int numLeaves;
	private int totalNodes;
	private TreeNode root;
	// pq short for priority queue
	private PriorityQ<E> pq;
	private TreeMap<Integer, Integer> freqMap;
	private HashMap<Integer, String> codeMap;
	
	/**
	 * intialzies the instance variables	
	 */
	public HuffmanCodeTree()
	{
		numLeaves = 0;
		totalNodes = 0;
		root = null;
		pq = new PriorityQ<E>();
		freqMap = new TreeMap<Integer, Integer>();
		codeMap = new HashMap<Integer, String>();
	}
	
	/**
	 * calls the methods in proper order to correctly
	 * build a huffmna code tree	
	 */
	//public void buildHuffmanCodeTree(ArrayList<Integer> asciiVals)
	public void buildHuffmanCodeTree(int[] asciiVals)
	{
		makeFreqMap(asciiVals);
		enqInitialNodes();
		numLeaves = pq.size();
		totalNodes += numLeaves;
		buildTree();
		root = (TreeNode)pq.first();
		makeCodeMapKickoff();
	}

	/**
	 * creates the map containing the frequencies of all elements
	 */
	private void makeFreqMap(int[] asciiVals)
	{
		for(int i = 0; i < asciiVals.length; i++)
		{
			if(asciiVals[i] != 0)
			{
				freqMap.put(i, asciiVals[i]);
			}
		}
	}
	
	/**
	 * enq short for enque, this method adds 
	 * all elements in frequency map to queue
	 * before building tree
	 */
	private void enqInitialNodes()
	{
		for(Integer asciiVal: freqMap.keySet())
		{
			pq.enq(new TreeNode(asciiVal, freqMap.get(asciiVal)), freqMap.get(asciiVal));
		}
	}
	
	/**
	 * constructs the tree out of the
	 * priority queue	
	 */
	private void buildTree()
	{
		while(pq.size() > 1)
		{
			TreeNode item = (TreeNode)pq.deq();
			TreeNode item2 = (TreeNode)pq.deq();
			// -1 a placeholder value since no node can ever have a negative value
			TreeNode node = new TreeNode(-1, item.getFrequency() + item2.getFrequency());
			totalNodes++;
			node.setLeft(item);
			node.setRight(item2);
			pq.enq(node, node.getFrequency());
		}
	}
	
	/**
	 * a kickoff method for the actual recursive
	 * backtracking method that will generate
	 * the new codes	
	 */
	private void makeCodeMapKickoff()
	{
		TreeNode node = root;
		StringBuilder emptyCode = new StringBuilder();
		makeCodeMap(node, emptyCode);
	}
	
	/**
	 * travereses the tree to get the new codes
	 * for each character in the input ile	
	 */
	private void makeCodeMap(TreeNode node, StringBuilder code)
	{
		if(node != null)
		{
			if(node.isLeaf())
			{
				codeMap.put(node.getValue(), code.toString());
			}
			else
			{
				code.append(LEFT);
				makeCodeMap(node.getLeft(), code);
				code.deleteCharAt(code.length() - 1);
				code.append(RIGHT);
				makeCodeMap(node.getRight(), code);
				code.deleteCharAt(code.length() - 1);
			}
		}
	}
	
	/**
	 * returns the number of leaves in this huffman tree	
	 */
	public int getNumLeaves()
	{
		return numLeaves;
	}
	
	/**
	 * returns the total number of nodes in
	 * this huffman tree	
	 */
	public int getTotalNodes()
	{
		return totalNodes;
	}
	
	/**
	 * returns the root node in the huffman tree	
	 */
	public TreeNode getRoot()
	{
		return root;
	}
	
	/**
	 * returns the map with the frequencies
	 * of the elements in the huffman tree	
	 */
	public TreeMap<Integer, Integer> getFreqMap()
	{
		return freqMap;
	}
	
	/**
	 * returns the map with the new codes
	 * for the elements in the huffman tree	
	 */
	public HashMap<Integer, String> getCodeMap()
	{
		return codeMap;
	}
	
	/**
	 * sets the root of this huffman tree
	 * to the given node	
	 */
	public void setRoot(TreeNode node)
	{
		root = node;
	}
	
	/**
	 * resets instance variables if a new 
	 * tree nees to be made	
	 */
	public void reset()
	{
		numLeaves = 0;
		totalNodes = 0;
		root = null;
		pq.clear();
		freqMap.clear();
		codeMap.clear();
	}
}