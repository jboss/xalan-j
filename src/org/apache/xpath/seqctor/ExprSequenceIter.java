/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xpath.seqctor;

import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XSequence;
import org.apache.xpath.objects.XSequenceCachedBase;

/**
 * The responsibility of enclosing_type is to .
 * 
 * Created Jul 20, 2002
 * @author sboag
 */
public class ExprSequenceIter extends XSequenceCachedBase
{
  protected Vector m_exprs;
  // XSequence[] m_xsequences;
  int m_exprsIndex = 0;
  private XSequence m_containedIterator;
  private XObject m_contextItem;
  
  /**
   * Constructor for ExprSequenceIter.
   */
  public ExprSequenceIter(Vector exprs, XPathContext xctxt)
  {
    super(xctxt);
    this.setShouldCache(true); // for now!
    m_exprs = exprs;
    m_contextItem = xctxt.getCurrentItem();
  }

  /**
   * @see org.apache.xpath.objects.XSequence#getNext()
   */
  public XObject getNext()
  {
      XObject xobj;
      try
      {
        m_xctxt.pushCurrentItem(m_contextItem);
        if (null != m_containedIterator)
        {
          xobj = m_containedIterator.next();
          if (null != xobj)
          {
            return xobj;
          }
          else
          {
            m_containedIterator = null;
          }
        }

        if (m_exprsIndex >= m_exprs.size())
          return null;
        Expression expr = (Expression) m_exprs.elementAt(m_exprsIndex);
        m_exprsIndex++;
        xobj = expr.execute(m_xctxt);
        if (xobj.isSequenceProper())
        {
          m_containedIterator = (XSequence) xobj;
          return getNext();
        }
        else
          return xobj;
      }
      catch (TransformerException e)
      {
        throw new WrappedRuntimeException(e);
      }
      finally
      {
        m_xctxt.popCurrentItem();
      }
  }

  /**
   * @see org.apache.xpath.objects.XSequence#getPrevious()
   */
  public XObject getPrevious()
  {
    throw new RuntimeException("ExprSequence can not go backwards without cacheing!");
  }

  /**
   * @see org.apache.xpath.objects.XObject#reset()
   */
  public void reset()
  {
    super.reset();
    m_exprsIndex = 0;
  }

}