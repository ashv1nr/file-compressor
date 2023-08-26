
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;

public class HuffUncompressor implements IHuffConstants
{
	private HuffmanCodeTree huffer;
	
	public HuffUncompressor()
	{
		huffer = new HuffmanCodeTree();
	}
	
	/**
	 * uncompress the given file based on the 
	 * header format it uses
	 */
	public int uncompress(BitInputStream inStream, BitOutputStream outStream)
	{
        reset();
		checkMagicNumber(inStream);
		handleHeader(inStream);
		int bitsWritten = writeOutOriginalData(inStream, outStream);
		inStream.close();
		outStream.close();
		return bitsWritten;
	}
	
	/**
	 * resets instance varibale before uncompressing	
	 */
	private void reset()
	{
		huffer.reset();
	}
	
	/**
	 * makes sure the file we are reading
	 * is a huffman file	
	 */
	private void checkMagicNumber(BitInputStream inStream)
	{
		int magicBits = 0;
		try {
				magicBits = inStream.readBits(BITS_PER_INT);
        } catch (IOException e) {
            System.out.println("Error / Exception while reading file for uncompressing.");
        }
		if(magicBits != MAGIC_NUMBER)
		{
			throw new IllegalArgumentException("file given not a huffman file.");
		}
	}
	
	/**
	 * processes the compressed header info before
	 * uncompressing the actual data
	 */
	private void handleHeader(BitInputStream inStream)
	{
		int headerBits = 0;
		try {
				headerBits = inStream.readBits(BITS_PER_INT);
        } catch (IOException e) {
            System.out.println("Error / Exception while reading file for uncompressing.");
        }
		if(headerBits == STORE_COUNTS)
		{
			huffer.buildHuffmanCodeTree(getFreqArr(inStream));
		}
		else if(headerBits == STORE_TREE)
		{
			readStfKickoff(inStream);
		}
		else
		{
			throw new IllegalArgumentException("file has invalid header type.");
		}
	}
	
	/**
	 * reads the frequencies given by the scf
	 * header and puts them into an array
	 */
	private int[] getFreqArr(BitInputStream inStream)
	{
		int index = 0;
		int curBits = 0;
		// the plus one is to hold pseudo eof in index 256
		int[] asciiVals = new int[ALPH_SIZE + 1];
		while(index < ALPH_SIZE)
		{
			try {
					curBits = inStream.readBits(BITS_PER_INT);
	        } catch (IOException e) {
	            System.out.println("Error / Exception while reading file for uncompressing.");
	        }
			if(curBits == -1)
			{
				throw new IllegalStateException("unexpected end of file");
			}
			if(curBits != 0)
			{
				asciiVals[index] += curBits;
			}
			index++;
		}
		asciiVals[PSEUDO_EOF]++;
		return asciiVals;
	}
	
	/**
	 * a kickoff method for the actual recursive
	 * method tgat will reconstruct the huffmam
	 * tree from the info in the file	
	 */
	private void readStfKickoff(BitInputStream inStream)
	{
		int treeBitLength = 0;
		try {
				treeBitLength = inStream.readBits(BITS_PER_INT);
        } catch (IOException e) {
            System.out.println("Error / Exception while reading file for uncompressing.");
        }
		if(treeBitLength == -1)
		{
			throw new IllegalStateException("unexpected end of file.");
		}
		// 0th index for tracking how many we've read
		// 1st index for tracking how many bits we should read
		int[] bitsReadTracker = new int[2];
		bitsReadTracker[1] = treeBitLength;
		// using -1's because the value and frequnecy of an
		// internal node doesn't matter and -1 is good
		// debugging value
		TreeNode root = readStf(inStream, bitsReadTracker);
		huffer.setRoot(root);
	}
	
	/**
	 * uses a pre order traversal-like method
	 * to reconstrcut the huffman tree	
	 */
	private TreeNode readStf(BitInputStream inStream, int[] bitsReadTracker)
	{
		int internalNode = 0;
		int leafNode = 1;
		if(bitsReadTracker[0] < bitsReadTracker[1])
		{
			int curBits = 0;
			try {
					curBits = inStream.readBits(1);
		       } catch (IOException e) {
		           System.out.println("Error / Exception while reading file for uncompressing.");
		       }
			if(curBits == internalNode)
			{
				// using -1's because the value and frequnecy of an
				// internal node doesn't matter and -1 is good
				// debugging value
				bitsReadTracker[0]++;
				TreeNode newNode = new TreeNode(-1, -1);
				newNode.setLeft(readStf(inStream, bitsReadTracker));
				newNode.setRight(readStf(inStream, bitsReadTracker));
				return newNode;
			}
			if(curBits == leafNode)
			{
				int numBitsInLeaf = 9;
				try {
						curBits = inStream.readBits(numBitsInLeaf);
			       } catch (IOException e) {
			           System.out.println("Error / Exception while reading file for uncompressing.");
			       }
				int bitsReadForLeaf = numBitsInLeaf + leafNode;
				bitsReadTracker[0]+= bitsReadForLeaf;
				 // making frequency -1 because frequnecy doesn't matter
				 // at this point in the tree's construction, the node is
				 // already in the right place in the tree
				return new TreeNode(curBits, -1);
			}
		}
		throw new IllegalStateException("invalid stf header.");
	}
	
	/**
	 * "walks the tree" to go from the compressed
	 * codes back to the original data	
	 */
	private int writeOutOriginalData(BitInputStream inStream, 
		BitOutputStream outStream)
	{
		int bitCount = 0;
		boolean foundEof = false;
		while(!foundEof)
		{
			int left = 0;
			TreeNode node = huffer.getRoot();
			while(!node.isLeaf())
			{
				// didn't want to make -1, 0, or 1 because those hold meaning
				int curBit = -2;
				try {
						curBit = inStream.readBits(1);
		        } catch (IOException e) {
		            System.out.println("Error / Exception while reading file for uncompressing.");
		        }
				if(curBit == -1)
				{
					throw new IllegalStateException("no psuedo eof value present in file");
				}
				if(curBit == left)
				{
					node = node.getLeft();
				}
				else
				{
					// reached here means bit is a 0
					node = node.getRight();
				}
			}
			if(node.getValue() == PSEUDO_EOF)
			{
				foundEof = true;
			}
			else
			{
				outStream.writeBits(BITS_PER_WORD, node.getValue());
				bitCount += BITS_PER_WORD;	
			}
		}
		return bitCount;
	}
}