/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softwareaggov.messaging.simplejmssendoneway.web;

import com.softwareaggov.messaging.libs.utils.AppConfig;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.MessageInteropLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * Base Servlet
 * </p>
 * <p/>
 *
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
public abstract class BaseMessageProducer extends HttpServlet {
    private static final long serialVersionUID = -8314035702649252239L;

    private static final String MSG_TEXT = "Some text message";

    private static Logger log = LoggerFactory.getLogger(BaseMessageProducer.class);

    protected Random rdm;
    protected String messagePayload;
    protected Map<String, Object> messageProperties;

    protected abstract MessageInteropLocal getJmsPublisherLocal();

    @Override
    public void init() throws ServletException {
        super.init();

        rdm = new Random(System.currentTimeMillis());

        int payloadSize = AppConfig.getInstance().getPropertyHelper().getPropertyAsInt("jms.message.size", 512);
        byte[] chars = new byte[payloadSize];
        rdm.nextBytes(chars);
        messagePayload = new String(chars);

        HashMap props = new HashMap<String, String>(4);
        props.put("property1", new Integer(rdm.nextInt()).toString());
        props.put("property2", new Integer(rdm.nextInt()).toString());
        props.put("property3", new Integer(rdm.nextInt()).toString());
        props.put("property4", new Integer(rdm.nextInt()).toString());

        messageProperties = Collections.unmodifiableMap(props);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>" + req.getContextPath() + " - Sending JMS message To Queue</h1>");
        try {
            MessageInteropLocal jmsPublisher = getJmsPublisherLocal();
            if (null == jmsPublisher)
                throw new IllegalArgumentException("jmsPublisher is null...should not be...check code or configs");

            if (jmsPublisher.isEnabled()) {
                String response = jmsPublisher.sendTextMessage(messagePayload, messageProperties);

                out.write("<p><b>messages sent successfully</b></p>");
                out.write(String.format("<div><p>Response:</p><p>%s</p></div>", ((null != response) ? response : "null")));
            } else {
                out.write("<p><b>Functionality not enabled by backend EJB!</b></p>");
            }

            out.close();
        } catch (Throwable exc) {
            log.error("Error Occurred", exc);
            throw new ServletException(exc);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
