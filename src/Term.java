import java.util.*;

/**
 * The Term class provides all fields and methods necessary for the representation
 * of terms for the convenience of the QuineMcCluskey class to solve.
 *
 * @author  Marxel S. Abogado
 * @author  Carla Joy G. Haboc
 * @version 1.0
 * @since   2023-11-21
 */
public class Term {
    /**
     * String representation of a boolean function Term in binary form. 
     */
    private String term;
    
    /**
     * number of ones in the current binary form of Term
     */
    private int ones;
    
    /**
     * grouped numbers in the representation of the Term
     */
    private ArrayList<Integer> nums;
    
    /**
     * constructor for the initialization of new term from integer minterm value
     * @param value the integer value of minterm
     * @param length the length of the binary string to pad leading zeroes to match the maximum minterm
     * 
     */
    public Term (int value, int length){
        //convert minterm to binary String
        String binary = Integer.toBinaryString(value);
        
        // left-pads zeroes if binary String does not match desired length from max minterm
        StringBuffer temp = new StringBuffer(binary);
        while (temp.length() != length){
            temp.insert(0, 0);
        }
        this.term = temp.toString();

        // initialize array list for the minterm value for groupings later on
        nums = new ArrayList<Integer>();
        nums.add(value);

        // count number of ones in binary
        ones = 0;
        for (int i = 0; i < term.length(); i++){
            if(term.charAt(i) == '1')
                ones++;
        }
    }

    /**
     * constructor for term when two minterms are grouped
     * @param term1 the first term grouped
     * @param term2 the second term grouped
     *
     */
    public Term (Term term1, Term term2){
        // scans both terms and replace non-matching character to '-'
        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < term1.getString().length(); i++){
            if (term1.getString().charAt(i) != term2.getString().charAt(i))
                temp.append("-");
            else
                temp.append(term1.getString().charAt(i));
        }
        this.term = temp.toString();

        // count new number of ones
        ones = 0;
        for (int i = 0; i < term.length(); i++){
            if (this.term.charAt(i) == '1') 
                ones++;
        }

        // adds both int minterms to the nums array list
        nums = new ArrayList<Integer>();
        for (int i = 0; i < term1.getNums().size(); i++){
            nums.add(term1.getNums().get(i));
        }
        for (int i = 0; i < term2.getNums().size(); i++){
            nums.add(term2.getNums().get(i));
        }
    }

    /**
     * gets a term's binary value in String form.
     * @return String of term's current binary value
     */
    String getString() {
        return term;
    }
    
    /**
     * gets the list of integer minterms grouped to create the term.
     * @return ArrayList containing current grouped minterms of term
     */
    ArrayList<Integer> getNums(){
        return nums;
    }
    
    /**
     * gets the number of ones present in the term's binary form.
     * @return integer value of counted ones in term
     */
    int getNumOnes(){
        return ones;
    }
}
