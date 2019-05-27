/*
 * KeyFile.java
 *
 * Reads in the given files and parses for keys
 *
 * @author Connor Henley, cxh1451@rit.edu
 */
package xfer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.io.BufferedReader;

/**
 * Represents a parsed private or public key file
 */
public class KeyFile {

    private BigInteger exp;
    private BigInteger mod;

    public KeyFile(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        // Read the file
        String line;
        while ((line = br.readLine()) != null) {
            // First, read the exponent
            if (this.exp == null) {
                try {
                    this.exp = new BigInteger(line);
                } catch (NumberFormatException e) {
                    System.err.println("Couldn't read exponent value in key file");
                    System.exit(1);
                }
            }

            // Next, read the modulus
            else if (this.mod == null) {
                try {
                    this.mod = new BigInteger(line);
                } catch (NumberFormatException e) {
                    System.err.println("Couldn't read modulus value in key file");
                    System.exit(1);
                }

                break;
            }
        }

        br.close();
    }

    /**
     * @return the exponent from the parsed file
     */
    public BigInteger getExponent() {
        return this.exp;
    }

    /**
     * @return the modulus from the parsed file
     */
    public BigInteger getModulus() {
        return this.mod;
    }
}
