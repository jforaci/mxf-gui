package org.foraci.mxf.mxfTool.mxf;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.parsers.Parser;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Returns a friendly <code>String</code> for an unknown value
 */
public class FriendlyUnknownValueParser extends Parser
{
    private static final int MAX_LENGTH = 1024;
    private static final BigInteger MAX_LENGTH_BI = BigInteger.valueOf(MAX_LENGTH); // maximum length of bytes of value that should be formatted for display

    public FriendlyUnknownValueParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    @Override
    public Object read() throws IOException {
        if (length.equals(count)) {
            return null;
        }
        int len = (length.compareTo(MAX_LENGTH_BI) > 0) ? MAX_LENGTH : length.intValue();
        StringBuilder sb = new StringBuilder("@0x" + Long.toHexString(in.getLastKeyOffset()) + ":");
        for (int i = 0; i < len; i++) {
            int byteValue = in.read();
            sb.append(String.format(" %02x", byteValue));
        }
        in.skip(length.subtract(BigInteger.valueOf(len)));
        count = length;
        return sb.toString();
    }
}
