/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.core;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.apache.tomcat.util.res.StringManager;

/**
 * Valve that implements the default basic behavior for the
 * <code>StandardWrapper</code> container implementation.
 *
 * @author Craig R. McClanahan
 */
final class StandardWrapperValve extends ValveBase {

    //------------------------------------------------------ Constructor
    public StandardWrapperValve() {
        super(true);
    }

    // ----------------------------------------------------- Instance Variables


    // Some JMX statistics. This valve is associated with a StandardWrapper.
    // We expose the StandardWrapper as JMX ( j2eeType=Servlet ). The fields
    // are here for performance.
    private volatile long processingTime;
    private volatile long maxTime;
    private volatile long minTime = Long.MAX_VALUE;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // --------------------------------------------------------- Public Methods


    /**
     * Invoke the servlet we are managing, respecting the rules regarding
     * servlet lifecycle and SingleThreadModel support.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    @Override
    public final void invoke(Request request, Response response) throws IOException, ServletException {
        // ...
        StandardWrapper wrapper = (StandardWrapper) getContainer();
        Servlet servlet = null;
        Context context = (Context) wrapper.getParent();

        // ...
        // 通过StandardWrapper获取对应的servlet
        servlet = wrapper.allocate();

        // 创建针对该请求的调用链
        ApplicationFilterChain filterChain = ApplicationFilterFactory.createFilterChain(request, wrapper, servlet);
        filterChain.doFilter(request.getRequest(), response.getResponse());
    }


    // -------------------------------------------------------- Private Methods

    /**
     * Handle the specified ServletException encountered while processing
     * the specified Request to produce the specified Response.  Any
     * exceptions that occur during generation of the exception report are
     * logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param exception The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    private void exception(Request request, Response response,
                           Throwable exception) {
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, exception);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setError();
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public long getMinTime() {
        return minTime;
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public int getErrorCount() {
        return errorCount.get();
    }

    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }

    @Override
    protected void initInternal() throws LifecycleException {
        // NOOP - Don't register this Valve in JMX
    }
}
