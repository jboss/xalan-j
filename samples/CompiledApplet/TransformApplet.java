/*
 * @(#)$Id$
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * @author Morten Jorgensen
 * @author Jacek Ambroziak
 *
 */

import java.io.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import org.apache.xalan.xsltc.*;
import org.apache.xalan.xsltc.runtime.AbstractTranslet;
import org.apache.xalan.xsltc.runtime.MessageHandler;
import org.apache.xalan.xsltc.runtime.output.*;
import org.apache.xalan.xsltc.dom.*;

/**
 * This applet demonstrates how XSL transformations can be made run in
 * browsers without native XSLT support.
 *
 * Note that the XSLTC transformation engine is invoked through its native
 * interface and not the javax.xml.transform (JAXP) interface. This because
 * XSLTC still does not offer precompiled transformations through JAXP.
 */
public final class TransformApplet extends Applet {

    // Single document cache
    private String _documentUrl = "";
    private DOMImpl _dom = null;
    private DTDMonitor _dtdMonitor = null;

    private static final String NAMESPACE_FEATURE =
	"http://xml.org/sax/features/namespaces";

    /**
     * This class implements a dialog box used for XSL messages/comments
     */
    public class MessageFrame extends Frame {

	public Frame frame;

        public class ButtonHandler implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	        frame.setVisible(false);
	    }
	}

	/**
	 * This method handles xml:message and xsl:comment by displaying
	 * the message/comment in a dialog box.
	 */
	public MessageFrame(String title, String message) {
	    super(title);
	    frame = this; // Make visible to ButtonHandler
	    setSize(320,200);

	    // Create a panel for the message itself
	    Panel center = new Panel();
	    center.add(new Label(message));

	    // Create a panel for the 'OK' button
	    Panel bottom = new Panel();
	    Button okButton = new Button("   OK   ");
	    okButton.addActionListener(new ButtonHandler());
	    bottom.add(okButton);
	    
	    // Add the two panels to the window/frame
	    add(center, BorderLayout.CENTER);
	    add(bottom,BorderLayout.SOUTH);

	    // Show the fecking thing
	    setVisible(true);
	}

    }

    /**
     * The appled uses this method to display messages and comments
     * generated by xsl:message and xsl:comment elements.
     */
    public class AppletMessageHandler extends MessageHandler {
	public void displayMessage(String msg) {
	    MessageFrame z = new MessageFrame("XSL transformation alert",msg);
	}
    }

    /**
     * Reads the input document from the supplied URL and builds the
     * internal "DOM" tree.
     */
    private DOM getDOM(String url) throws Exception {
	// Check if the document is already in the 1-document cache
	if (url.equals(_documentUrl) == false) {

	    // Create a SAX parser and get the XMLReader object it uses
	    final SAXParserFactory factory = SAXParserFactory.newInstance();
	    try {
		factory.setFeature(NAMESPACE_FEATURE,true);
	    }
	    catch (Exception e) {
		factory.setNamespaceAware(true);
	    }
	    final SAXParser parser = factory.newSAXParser();
	    final XMLReader reader = parser.getXMLReader();

	    // Set the DOM's builder as the XMLReader's SAX2 content handler
	    _dom = new DOMImpl();
	    reader.setContentHandler(_dom.getBuilder());

	    // Create a DTD monitor and pass it to the XMLReader object
	    _dtdMonitor = new DTDMonitor();
	    _dtdMonitor.handleDTD(reader);

	    // Parse the input document
	    reader.parse(url);

	    // Update the 1-document cahce with this DOM
	    _documentUrl = url;
	}
	return _dom;
    }

    /**
     * This method is the main body of the applet. The method is called
     * by some JavaScript code in an HTML document.
     */ 
    public String transform(Object arg1, Object arg2) {

	// Convert the two arguments to strings.
	final String transletName = (String)arg1;
	final String documentUrl = (String)arg2;

	// Initialise the output stream
	final StringWriter sout = new StringWriter();
	final PrintWriter out = new PrintWriter(sout);

	try {
	    // Check that the parameters are valid
	    if (transletName == null || documentUrl == null) {
		out.println("<h1>Transformation error</h1>");
		out.println("The parameters <b><tt>class</tt></b> "+
			    "and <b><tt>source</tt></b> must be specified");
	    }
	    else {
		// Instanciate a message handler for xsl:message/xsl:comment
		AppletMessageHandler msgHandler = new AppletMessageHandler();

		// Get a refenrence to the translet class
		final Class tc = Class.forName(transletName);

		// Instanciate and initialise the tranlet object
		AbstractTranslet translet = (AbstractTranslet)tc.newInstance();
		((AbstractTranslet)translet).setMessageHandler(msgHandler);

		// Create output handler
		TransletOutputHandlerFactory tohFactory = 
		    TransletOutputHandlerFactory.newInstance();
		tohFactory.setOutputType(TransletOutputHandlerFactory.STREAM);
		tohFactory.setEncoding(translet._encoding);
		tohFactory.setOutputMethod(translet._method);
		tohFactory.setWriter(out);

		getDOM(documentUrl);

		final long start = System.currentTimeMillis();

		// Set size of key/id indices
		translet.setIndexSize(_dom.getSize());
		// If there are any elements with ID attributes, build an index

		_dtdMonitor.buildIdIndex(_dom, 0, translet);
		// Pass unparsed entities to translet
		translet.setUnparsedEntityURIs(_dtdMonitor.
					       getUnparsedEntityURIs());
		// Do the actual transformation
		translet.transform(_dom, tohFactory.getTransletOutputHandler());

		final long done = System.currentTimeMillis() - start;
		out.println("<!-- transformed by XSLTC in "+done+"msecs -->");
	    }
	    // Now close up the sink, and return the HTML output in the
	    // StringWrite object as a string.
	    out.close();
	    System.err.println("Transformation complete!");
	    System.err.println(sout.toString());
	    return sout.toString();
	}
	catch (RuntimeException e) {
	    out.close();
	    return sout.toString();
	}
	catch (Exception e) {
	    out.println("<h1>exception</h1>");
	    out.println(e.toString());
	    out.close();
	    return sout.toString();
	}
    }
}