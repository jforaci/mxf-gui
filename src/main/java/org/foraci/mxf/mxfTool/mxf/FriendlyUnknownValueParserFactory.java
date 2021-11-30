package org.foraci.mxf.mxfTool.mxf;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.parsers.Parser;
import org.foraci.mxf.mxfReader.parsers.factory.ParserFactory;

import java.math.BigInteger;

/**
 * Returns a friendly <code>String</code> for an unknown value
 */
public class FriendlyUnknownValueParserFactory implements ParserFactory
{
    public Parser createParser(BigInteger length, MxfInputStream in)
    {
        return new FriendlyUnknownValueParser(length, in);
    }
}
