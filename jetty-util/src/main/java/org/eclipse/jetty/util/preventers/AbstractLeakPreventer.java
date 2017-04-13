//
//  ========================================================================
//  Copyright (c) 1995-2017 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//


package org.eclipse.jetty.util.preventers;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * AbstractLeakPreventer
 *
 * Abstract base class for code that seeks to avoid pinning of webapp classloaders by using the jetty classloader to
 * proactively call the code that pins them (generally pinned as static data members, or as static
 * data members that are daemon threads (which use the context classloader)).
 * 
 * Instances of subclasses of this class should be set with Server.addBean(), which will
 * ensure that they are called when the Server instance starts up, which will have the jetty
 * classloader in scope.
 *
 * 查找防止webapp类加载器固定住(pinning)的基类，使用jetty的类加载器积极调用
 * 会固定住加载器（通常来说会像静态成员变量或作为daemon线程的，会使用context加载器的静态成员变量）的代码
 *
 * 子类实例应使用Server.addBean()进行设置，以保证会在Server实例启动时（拥有jetty类加载器范围）被调用。
 *
 * 注：系统类加载器会加载JETTY_HOME/lib下的内容，Jetty的WebAppClassLoader用于加载WEB-INF/lib、WEB-INF/classes
 * 以防止多个WEBAPP互相影响。可参考http://blog.csdn.net/lovingprince/article/details/6314309
 *
 */
public abstract class AbstractLeakPreventer extends AbstractLifeCycle
{
    protected static final Logger LOG = Log.getLogger(AbstractLeakPreventer.class);
    
    /* ------------------------------------------------------------ */
    abstract public void prevent(ClassLoader loader);
    
    
    /* ------------------------------------------------------------ */
    @Override
    protected void doStart() throws Exception
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            prevent(getClass().getClassLoader());
            super.doStart();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }
}
