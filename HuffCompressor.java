
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class HuffCompressor implements IHuffConstants
{
	private final int NUM_BITS_IN_LEAF = 9;
	// orig short for original
	private int origAmtOfBits;
	private int newAmtOfBits;
	// will hold either the vaule of scf or stf
	private int headerInt;
	private HuffmanCodeTree huffer;
	
	/**
	 * intialzies the instance variables	
	 */
	public HuffCompressor()
	{
		origAmtOfBits = 0;
		// setting intially to bits_per_int * 2 because every
		// huffman file will start with the magic number and
		// a header format
		newAmtOfBits = BITS_PER_INT + BITS_PER_INT;
		// setting initally to -1 since its a good debugging value and 
		// since neither header formats' value are -1
		headerInt = -1;
		huffer = new HuffmanCodeTree();
	}
	
    /**
	 * sets up all the info the program needs
	 * to the compress the file	
	 */
	public int preprocessCompress(BitInputStream inStream, int headerFormat) throws IOException 
	{
		reset(headerFormat);
		huffer.buildHuffmanCodeTree(getFreqArr(inStream));
		setNewAmtOfBits(headerFormat);
        return origAmtOfBits - newAmtOfBits;
    }
	
	/**
	 * resets instance varibales if a new file is being compressed	
	 */
	private void reset(int headerFormat)
	{
		origAmtOfBits = 0;
		newAmtOfBits = BITS_PER_INT + BITS_PER_INT;
		headerInt = headerFormat;
		huffer.reset();
	}
	
	/**
	 * tallies up frequecnies of every charcter in file
	 */
	private int[] getFreqArr(InputStream in)
	{
		int curBits = 0;
		BitInputStream inStream = new BitInputStream(in);
		//ArrayList<Integer> asciiVals = new ArrayList<Integer>();
		// the plus one is to hold pseudo eof in index 256
		int[] asciiVals = new int[ALPH_SIZE + 1];
		while(curBits != -1)
		{
	        try {
					curBits = inStream.readBits(BITS_PER_WORD);
	        } catch (IOException e) {
	            System.out.println("Error / Exception while reading file for preprocessing.");
	        }
			if(curBits != -1)
			{
				asciiVals[curBits]++;
				origAmtOfBits += BITS_PER_WORD;
			}
		}
		inStream.close();
		//adding psuedo eof to map
		asciiVals[PSEUDO_EOF]++;
		return asciiVals;
	}
	
	/**
	 * sets new amount of bits writen to the output
	 * file based on the given header tyep and the
	 * lengths of the new codes generated
	 */
	private void setNewAmtOfBits(int headerFormat)
	{
		addBitsForHeaderFormat(headerFormat);
		addBitsForNewCodeLengths(huffer.getRoot(), huffer.getCodeMap());
	}
		
	/**
	 * calculates the amount of bits the given header
	 * type will add to the compressed file and adjusts
	 * the bit counter accordingly	
	 */	
	private void addBitsForHeaderFormat(int headerFormat)
	{
		if(headerInt == STORE_COUNTS)
		{
			newAmtOfBits += (BITS_PER_INT * ALPH_SIZE);
		}
		else
		{
			newAmtOfBits += ( BITS_PER_INT + ((huffer.getNumLeaves() * NUM_BITS_IN_LEAF)
				 + huffer.getTotalNodes()) );
		}
	}
	
	/**
	 * adjusts the bit counter based on the lengths of
	 * the new codes	
	 */
	private void addBitsForNewCodeLengths(TreeNode node, HashMap<Integer, String> codeMap)
	{
		if(node != null)
		{
			if(node.isLeaf())
			{
				newAmtOfBits += (node.getFrequency() * codeMap.get(node.getValue()).length());
			}
			else
			{
				addBitsForNewCodeLengths(node.getLeft(), codeMap);
				addBitsForNewCodeLengths(node.getRight(), codeMap);
			}
		}
	}
	
	/**
	 * compresses the file using the previously
	 * given header format	
	 */
	public int compress(BitInputStream inStream, BitOutputStream outStream, 
		boolean force) throws IOException 
	{
		if( (!force) && (newAmtOfBits > origAmtOfBits) )
		{
			return 0;
		}
		outStream.writeBits(BITS_PER_INT, MAGIC_NUMBER);
		outStream.writeBits(BITS_PER_INT, headerInt);
		if(headerInt == STORE_COUNTS)
		{
			writeOutSCF(outStream);
		}
		else
		{
			writeOutSTF(outStream);
		}
		writeOutNewCodes(inStream, outStream);
		inStream.close();
		outStream.close();
        return newAmtOfBits;
    }
	
	/**
	 * writes out the frequencies of all 256 possible values	
	 */
	private void writeOutSCF(BitOutputStream outStream)
	{
		int index = 0;
		TreeMap<Integer, Integer> freqMap = huffer.getFreqMap();
		Iterator it = freqMap.keySet().iterator();
		while(it.hasNext())
		{
			int key = ((Integer)it.next()).intValue();
			while(index < key)
			{
				// zero for zero frequency
				outStream.writeBits(BITS_PER_INT, 0);
				index++;
			}
			if(key != PSEUDO_EOF)
			{
				outStream.writeBits(BITS_PER_INT, freqMap.get(key));
				index++;
			}
		}
		while(index < ALPH_SIZE)
		{
			// zero for zero frequency
			outStream.writeBits(BITS_PER_INT, 0);
		}
	}
	
	/**
	 * writes out the values of the characters
	 * in the tree	
	 */
	private void writeOutSTF(BitOutputStream outStream)
	{
		outStream.writeBits(BITS_PER_INT, (huffer.getNumLeaves() * NUM_BITS_IN_LEAF)
			 + huffer.getTotalNodes());
		TreeNode root = huffer.getRoot();
		stfTreeTraverse(root, outStream);
	}
	
	/**
	 * conducts a pre order traversal of the tree
	 * to write out the appropriate bits	
	 */
	private void stfTreeTraverse(TreeNode node, BitOutputStream outStream)
	{
		if(node != null)
		{
			if(node.isLeaf())
			{
				outStream.writeBits(1, 1);
				outStream.writeBits(NUM_BITS_IN_LEAF, node.getValue());
			}
			else
			{
				outStream.writeBits(1, 0);
				stfTreeTraverse(node.getLeft(), outStream);
				stfTreeTraverse(node.getRight(), outStream);
			}
		}
	}
	
	/**
	 * goes through the orignal file to read in the 
	 * chracters and maps them to the new codes to
	 * then write out	
	 */
	private void writeOutNewCodes(BitInputStream inStream, BitOutputStream outStream)
	{
		int curBits = 0;
		StringBuilder code = new StringBuilder();
		HashMap<Integer, String> codeMap = huffer.getCodeMap();
		while(curBits != -1)
		{
	        try {
					curBits = inStream.readBits(BITS_PER_WORD);
	        } catch (IOException e) {
	            System.out.println("Error / Exception while reading file for compressing.");
	        }
			if(curBits != -1)
			{
				code.append(codeMap.get(curBits));
				for(int i = 0; i < code.length(); i++)
				{
					outStream.writeBits(1, Integer.parseInt(code.substring(i, i + 1)));
				}
				code.delete(0, code.length());
			}
		}
		code.append(codeMap.get(PSEUDO_EOF));
		for(int i = 0; i < code.length(); i++)
		{
			outStream.writeBits(1, Integer.parseInt(code.substring(i, i + 1)));
		}
	}
}