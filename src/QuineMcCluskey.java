import java.util.*;
import javax.swing.JOptionPane;

/**
 * The QuineMcCluskey class provides all fields and methods necessary for solving
 * the simplification of Boolean functions using the Quine-McCluskey Tabular Method.
 *
 * @author  Marxel S. Abogado
 * @author  Carla Joy G. Haboc
 * @version 1.0
 * @since   2023-11-21
 */
public class QuineMcCluskey {

    /**
     * comparator for comparing terms based on the number of ones in their binary form
     * @see Comparator
     */
    private class OnesComparator implements Comparator<Term>{
        /**
         * compares two terms based on their number of ones
         * @param a the first term to be compared.
         * @param b the second term to be compared.
         * @return 0 if same number of one, positive int if a greater than b, negative int if a is less than b
         */
        @Override
        public int compare (Term a, Term b) {
            return a.getNumOnes() - b.getNumOnes();
        }
    }

    /**
     * array of terms to store terms necessary for solution
     */
    private Term[] terms;

    /**
     * array list storing minterms entered by user
     */
    private ArrayList<Integer> minterms;

    /**
     * int value for the maximum length possible for solution
     */
    private int maxLength;

    /**
     * array list array containing solutions accumulated throughout the program
     */
    private ArrayList<String>[] solution;

    /**
     * array list containing prime implicants accumulated throughout the program
     */
    private ArrayList<String> primeImplicants;

    /**
     * array list storing every term necessary for the second stage of solving
     */
    private ArrayList<Term> finalTerms;

    /**
     * array list of array lists storing terms gathered from the first step of solving
     */
    public ArrayList<ArrayList<Term>[]> firstStep;

    /**
     * array list of Hash sets storing checked terms gathered from the first step of solving
     */
    public ArrayList<HashSet<String>> checkedFirstStep;

    /**
     * array list storing simplified terms after using Petrick's method
     */
    public ArrayList<String> simplified;

    /**
     * constructor for the initialization of an object that implements the Quine-McCluskey method
     * @param mintermsStr a valid String containing the minterms to be solved
     */
    public QuineMcCluskey (String mintermsStr) {
        // converts minterms string input to int array
        int[] minterms = convertString(mintermsStr);

        if (!checkRepeats(minterms)) {
            JOptionPane.showMessageDialog(null, "Duplicates encountered. Please try again", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // sorts minterms array
        Arrays.sort(minterms);

        // calculate max. length of binary string
        maxLength = Integer.toBinaryString(minterms[minterms.length - 1]).length();

        this.minterms = new ArrayList<>();

        primeImplicants = new ArrayList<String>();
        firstStep = new ArrayList<ArrayList<Term>[]>();
        checkedFirstStep = new ArrayList<HashSet<String>>();
        simplified = new ArrayList<String>();

        // combine minterms in one array
        Term[] temp = new Term[minterms.length];
        int k = 0; // index in temp array
        for (int i = 0; i < minterms.length; i++) {
            temp[k++] = new Term(minterms[i], maxLength);
            this.minterms.add(minterms[i]);
        }

        // fill the terms array with terns
        terms = new Term[k];
        for (int i = 0; i < k; i++) {
            terms[i] = temp[i];
        }

        // sort terms according to number of ones
        Arrays.sort(terms, new OnesComparator());
    }

    /**
     * converts the minterms String input and checks if valid
     * @param s a valid String containing the minterms to be solved
     * @return int array with minterms parsed from String input
     */
    private int[] convertString(String s) {
        // replace commas with spaces, if commas were used
        s = s.replace(",", " ");

        // if string is empty
        if (s.trim().equals("")) {
            return new int[] {};
        }

        // split string delimited by spaces and store in an array
        String[] a = s.trim().split(" +");
        int[] t = new int[a.length]; // array of minterms

        // parse strings in the array to integers, throw error message if not digits, strings, or commas
        for (int i = 0; i < t.length; i++) {
            try {
                // until it reaches outside bounds
                int temp = Integer.parseInt(a[i]);
                t[i] = temp;
            } catch (Exception e) {
                if (s.matches("[\\d,\\s]+"))
                    JOptionPane.showMessageDialog(null, "Invalid input. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // check for duplicates using a hash set. if no duplicates add to hash set, repeat until end of array
        HashSet<Integer> dup = new HashSet<>();
        for (int i = 0; i < t.length; i++) {
            if (dup.contains(t[i])) {
                JOptionPane.showMessageDialog(null, "Duplicates encountered. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            dup.add(t[i]);
        }

        return t;
    }

    /**
     * groups an array of terms based on their number of ones
     * @param terms array of terms to be grouped
     * @return array of array lists of terms where each element represents a group of terms with the same number of ones
     */
    private ArrayList<Term>[] group(Term[] terms) {
        // create an array of array lists based on their number of ones, with size from the maximum number of ones
        ArrayList<Term>[] groups = new ArrayList[terms[terms.length - 1].getNumOnes() + 1];

        // initialize each array list in the groups array
        for (int i = 0; i < groups.length; i++) {
            groups[i] = new ArrayList<>();
        }

        // group the terms based on their number of ones, respective of their index in the groups array
        for (int i = 0; i < terms.length; i++) {
            int k = terms[i].getNumOnes();
            groups[k].add(terms[i]);
        }

        return groups;
    }

    /**
     * first stage of solution using Quine-McCluskey method
     * main solver method of the class to be called
     */
    public void solve(){
        // keep track of the unchecked terms
        ArrayList<Term> unchecked = new ArrayList<>();

        // gather the first list of grouped terms
        ArrayList<Term>[] list = group(this.terms);

        // store resulting terms of each iteration
        ArrayList<Term>[] result;

        // add the current list to the firstStep array list array
        firstStep.add(list);

        // loop as long as result array is not empty and length > 1
        boolean insert = true;

        do {
            // store checked terms next
            HashSet<String> checked= new HashSet<>();

            // set result array to a new empty array
            result = new ArrayList[list.length - 1];

            ArrayList<String> temp;
            insert = false;

            // loop over
            for (int i = 0; i < list.length - 1; i++){
                result[i] = new ArrayList<>();
                // keep track of added terms in results to avoid duplicates
                temp = new ArrayList<>();

                // loop over each element in first group with all elements of second
                for (int j = 0; j < list[i].size(); j++){
                    // loop over each element in the second group
                    for (int k = 0; k < list[i + 1].size(); k++){
                        // check first if is a valid combination
                        if (checkValidity(list[i].get(j), list[i + 1].get(k))) {
                            // append the terms to be checked
                            checked.add(list[i].get(j).getString());
                            checked.add(list[i+1].get(k).getString());

                            Term n = new Term(list[i].get(j), list[i+1].get(k));

                            // check if resulting term is already in the results, don't add them
                            if (!temp.contains(n.getString())) {
                                result[i].add(n);
                                insert = true;
                            }
                            temp.add(n.getString());

                        }
                    }
                }
            }

            // if result is not empty and new terms generated, update unchecked
            if (insert) {
                for (int i = 0; i < list.length; i++) {
                    for (int j = 0; j < list[i].size(); j++) {
                        if (!checked.contains(list[i].get(j).getString())) {
                            // add the unchecked terms to the unchecked array list
                            unchecked.add(list[i].get(j));
                        }
                    }
                }
                list = result;

                // add result and checked to firstStep and checkedFirstStep array lists
                firstStep.add(list);
                checkedFirstStep.add(checked);
            }
        } while (insert && list.length > 1);

        // copy resulting minterms into new array list along with unchecked terms
        finalTerms = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            for (int j = 0; j < list[i].size(); j++) {
                finalTerms.add(list[i].get(j));
            }
        }
        for (int i = 0; i < unchecked.size(); i++) {
            finalTerms.add(unchecked.get(i));
        }

        solveSecond();
    }

    /**
     * second stage of solution using Quine-McCluskey method
     * recursively calls itself if there are still remaining minterms
     */
    public void solveSecond(){
        // identify prime implicants, first and check for row dominance then column dominance
        if (!identifyPrimeImplicants()) {
            if (!rowDominance()) {
                if (!columnDominance()) {
                    // if none succeeds, go to simplify method
                    simplify();
                    return;
                }
            }
        }

        // if there are still minterms to be taken call this function again
        if (minterms.size() != 0)
            solveSecond();
        // if all minterms taken, add to solution
        else {
            solution = new ArrayList[1];
            solution[0] = primeImplicants;
        }
    }

    /**
     * check if there are repeated elements in the minterms int array
     * @param m the int array of minterms
     * @return true if no duplicates, else false
     */
    boolean checkRepeats(int[] m){
        HashSet<Integer> temp =new HashSet<>();
        for (int i = 0; i < m.length; i++) {
            if (!temp.add(m[i])) {
                // duplicate found, return false
                return false;
            }
        }
        // no duplicates found, return true
        return true;
    }

    /**
     * check if two terms are valid for grouping
     * @param term1 the first term to be checked
     * @param term2 the second term to be checked
     * @return true if grouping is possible, else false if not
     */
    boolean checkValidity (Term term1, Term term2) {
        // check if both terms have the same length
        if (term1.getString().length() != term2.getString().length())
            return false;

        // count differing positions, return false immediately if '-' is paired with 0 or 1
        int k = 0;
        for (int i = 0; i < term1.getString().length(); i++) {
            if (term1.getString().charAt(i) == '-' && term2.getString().charAt(i) != '-')
                return false;
            else if (term1.getString().charAt(i) != '-' && term2.getString().charAt(i) == '-')
                return false;
            else if (term1.getString().charAt(i) != term2.getString().charAt(i))
                k++;
        }

        // only return true if there is exactly one differing position from both terms
        if (k != 1)
            return false;
        else
            return true;
    }

    /**
     * check if two terms have all its numbers present in another term
     * @param term1 the first term to be the basis of checking
     * @param term2 the second term to be checked for presence of numbers in term1
     * @return true if term1 contains all its numbers present in term2, else false
     */
    boolean contains(Term term1, Term term2) {
        // return false if the number of minterms grouped in term1 is less than or equal than that of term2's
        if (term1.getNums().size() <= term2.getNums().size()) {
            return false;
        }

        // else, gather all numbers associated with term1 and term2 in array lists
        ArrayList<Integer> a = term1.getNums();
        ArrayList<Integer> b = term2.getNums();

        // return true if all numbers in b are in a, else false
        if (a.containsAll(b))
            return true;
        else
            return false;
    }

    /**
     * simplifies the solution done on the object using Petrick's method
     * @see <a href="https://www.allaboutcircuits.com/technical-articles/prime-implicant-simplification-using-petricks-method/">Petrick's method</a>
     */
    void simplify(){
        HashSet<String>[] temp = new HashSet[minterms.size()];

        //construct temp array containing sets of associated minterms in finalTerms
        for (int i = 0; i < minterms.size(); i++) {
            temp[i] = new HashSet<>();
            for (int j = 0; j < finalTerms.size(); j++) {
                if (finalTerms.get(j).getNums().contains(minterms.get(i))) {
                    char t = (char) ('a' + j);
                    simplified.add(t + ": " + finalTerms.get(j).getString());
                    temp[i].add(t + "");
                }
            }
        }

        // multiply sets in temp for simplification
        HashSet<String> finalResult = multiply(temp, 0);

        // identify minimum length terms in finalResult and count occurences
        int min = -1;
        int count = 0;
        for (Iterator<String> t = finalResult.iterator(); t.hasNext();) {
            String m = t.next();
            if (min == -1 || m.length() < min) {
                min = m.length();
                count = 1;
            } else if (min == m.length()) {
                count++;
            }
        }

        // add the simplified minimum terms to solutions
        solution = new ArrayList[count];
        int k = 0;
        for (Iterator<String> t = finalResult.iterator(); t.hasNext();) {
            String c = t.next();
            if (c.length() == min) {
                solution[k] = new ArrayList<>();
                for (int i = 0; i < c.length(); i++) {
                    solution[k].add(finalTerms.get((int) c.charAt(i) - 'a').getString());
                }
                for (int i = 0; i < primeImplicants.size(); i++) {
                    solution[k].add(primeImplicants.get(i));
                }
                k++;
            }
        }
    }

    /**
     * multiplies elements from sets at indices adjacent to each other in the Hash set array and recurvisely computes for the product
     * @param p an array of Hash sets containing elements to be multiplied
     * @param k the index pointing to the first set to be multiplied
     * @return a Hash set resulting from the multiplication of adjacent sets in a Hash set array
     */
    HashSet<String> multiply(HashSet<String>[] p, int k){
        // check if k is greater than or equal to p.length - 1
        if (k >= p.length - 1)
            return p[k];

        // initialize resulting Hash set
        HashSet<String> s = new HashSet<>();

        // iterate through elements of p[k]
        for (Iterator<String> t = p[k].iterator(); t.hasNext();) {
            String temp2 = t.next();
            // iterate through elements of p[k +1]
            for (Iterator<String> g = p[k + 1].iterator(); g.hasNext();) {
                String temp3 = g.next();
                // add mixed elements to resulting Hash set
                s.add(mix(temp2, temp3));
            }
        }
        p[k + 1] = s; // update element from Hash set array at index k+1 with the resulting set
        return multiply(p, k + 1); // recursion to multiply the following sets until the end index
    }

    /**
     * mixes terms and simplifies those that are duplicated with respect to properties of boolean expressions
     * @param str1 the first string to be multiplied
     * @param str2 the second string to be multiplied
     * @return a string containing the simplified boolean product of both input strings
     */
    String mix (String str1, String str2){
        // Hash set to immediately remove duplicates
        HashSet<Character> r = new HashSet<>();

        // add characters from str1 to Hash set r
        for (int i = 0; i < str1.length(); i++)
            r.add(str1.charAt(i));

        // add characters from str2 to Hash set r
        for (int i = 0; i < str2.length(); i++)
            r.add(str2.charAt(i));

        // construct resulting string by concatenating characters from Hash set r
        String result = "";
        for (Iterator<Character> i = r.iterator(); i.hasNext();)
            result += i.next();

        return result;
    }

    /**
     * identify prime implicants, add them to primeImplicants array list, and remove from minterms and finalTerms array lists
     * @return true if prime implicants are identified, else false
     */
    private boolean identifyPrimeImplicants(){
        // initialize columns array to store indeces of final terms matching each minterm
        ArrayList<Integer>[] columns = new ArrayList[minterms.size()];

        // fill columns with indeces of final terms that match each minterm
        for (int i = 0; i < minterms.size(); i++) {
            columns[i] = new ArrayList();
            for (int j = 0; j < finalTerms.size(); j++) {
                if (finalTerms.get(j).getNums().contains(minterms.get(i))) {
                    columns[i].add(j);
                }
            }
        }
        boolean isPrimeImplicant = false;

        // check each minterm's matched final terms for single matches
        for (int i = 0; i < minterms.size(); i++) {
            if (columns[i].size() == 1) {
                isPrimeImplicant = true;

                // gather numbers of associated minterms with the prime implicant
                ArrayList<Integer> del = finalTerms.get(columns[i].get(0)).getNums();

                // remove associated minterms from object's array of minterms
                for (int j = 0; j < minterms.size(); j++) {
                    if (del.contains(minterms.get(j))) {
                        minterms.remove(j);
                        j--;
                    }
                }

                // add the identified prime implicant to the primeImplicant array list
                primeImplicants.add(finalTerms.get(columns[i].get(0)).getString());
                // remove identified prime implicant from finalTerms array list
                finalTerms.remove(columns[i].get(0).intValue());
                break;
            }
        }
        return isPrimeImplicant;
    }

    /**
     * identify dominating columns and removes them from the minterms and finalTerms array lists
     * @return true if there are dominating columns and were identified and removed, else false
     */
    private boolean columnDominance(){
        boolean flag = false;

        // create a table
        ArrayList<ArrayList<Integer>> columns = new ArrayList<>();

        // fill columns with indeces of final terms that match each minterm
        for (int i = 0; i < minterms.size(); i++){
            columns.add(new ArrayList<Integer>());
            for (int j = 0; j < finalTerms.size(); j++){
                if (finalTerms.get(j).getNums().contains(minterms.get(i)))
                    columns.get(i).add(j);
            }
        }

        // identify dominating columns, where a column has its all its checks present in the other dominating columns, and remove them
        for (int i = 0; i < columns.size(); i++) {
            for (int j = i + 1; j < columns.size(); j++) {
                if (columns.get(j).containsAll(columns.get(i)) && columns.get(j).size() > columns.get(i).size()) {
                    columns.remove(j);
                    minterms.remove(j);
                    j--;
                    flag = true;
                } else if (columns.get(i).containsAll(columns.get(j)) && columns.get(i).size() > columns.get(j).size()) {
                    columns.remove(i);
                    minterms.remove(i);
                    i--;
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * identify dominating rows and removes them from the minterms and finalTerms array lists
     * @return true if there are dominating rows and were identified and removed, else false
     */
    private boolean rowDominance(){
        boolean flag = false;

        // identify dominating rows, where a row has all its checks present in the other dominating columns, and remove them
        for (int i = 0; i < finalTerms.size() - 1; i++) {
            for (int j = i + 1; j < finalTerms.size(); j++) {
                if (contains(finalTerms.get(i), finalTerms.get(j))) {
                    finalTerms.remove(j);
                    j--;
                    flag = true;
                } else if (contains(finalTerms.get(j), finalTerms.get(i))) {
                    finalTerms.remove(i);
                    i--;
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * converts a term in binary form to standard form
     * @param s a term in binary form
     * @return the String of the term in its standard form
     */
    String toStandardForm(String s) {
        StringBuilder r = new StringBuilder();

        // i keeps track of variables starting from 'A'
        for (int i = 0; i < s.length(); i++) {

            // ignores '-' and proceeds to next character
            if (s.charAt(i) == '-') {
                continue;
            }

            // unprimed variable if 1
            else if (s.charAt(i) == '1') {
                r.append((char) ('A' + i));
            }

            // primed variable if 0
            else {
                r.append((char) ('A' + i));
                r.append('\'');
            }
        }

        // if the resulting string is empty, append 1 to represent a constant
        if (r.toString().length() == 0) {
            r.append("1");
        }
        return r.toString();
    }

    /**
     * build a String for the final resulting solutions to be presented to the user
     * @param variables list of variables that contains corresponding variable names
     * @return the String build-up of the final resulting solutions
     */
    public String printResults(String[] variables) {
        StringBuilder printedAnswer = new StringBuilder();
        for (int i = 0; i < solution.length; i++) {

            if (solution.length == 1)
                printedAnswer.append("Solution:").append("\n");
            else
                printedAnswer.append("Solution #").append(i+1).append(":").append("\n");

            // convert solution to standard form first, separate sum of products with '+'
            StringBuilder finalAnswer = new StringBuilder();
            for (int j = 0; j < solution[i].size(); j++) {
                finalAnswer.append(toStandardForm(solution[i].get(j)));
                if (j != solution[i].size() - 1) {
                    finalAnswer.append(" + ");
                }
            }

            // replace characters from converted standard form with their respective variable entered by the user
            for (int j = 0; j < finalAnswer.toString().length(); j++){
                if(finalAnswer.charAt(j) == 'A')
                    printedAnswer.append(variables[0]);
                else if (finalAnswer.charAt(j) == 'B')
                    printedAnswer.append(variables[1]);
                else if (finalAnswer.charAt(j) == 'C')
                    printedAnswer.append(variables[2]);
                else if (finalAnswer.charAt(j) == 'D')
                    printedAnswer.append(variables[3]);
                else if (finalAnswer.charAt(j) == 'E')
                    printedAnswer.append(variables[4]);
                else if (finalAnswer.charAt(j) == 'F')
                    printedAnswer.append(variables[5]);
                else if (finalAnswer.charAt(j) == 'G')
                    printedAnswer.append(variables[6]);
                else if (finalAnswer.charAt(j) == 'H')
                    printedAnswer.append(variables[7]);
                else if (finalAnswer.charAt(j) == 'I')
                    printedAnswer.append(variables[8]);
                else if (finalAnswer.charAt(j) == 'J')
                    printedAnswer.append(variables[9]);
                else
                    printedAnswer.append(finalAnswer.toString().charAt(j));
            }
            printedAnswer.append("\n\n");
        }
        return printedAnswer.toString();
    }
}
