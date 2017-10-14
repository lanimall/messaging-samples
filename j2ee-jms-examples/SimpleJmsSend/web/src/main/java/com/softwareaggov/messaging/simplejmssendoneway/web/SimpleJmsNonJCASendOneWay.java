package com.softwareaggov.messaging.simplejmssendoneway.web;

import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>
 * A simplistic (on purpose) servlet that sends JMS messages without  JCA construct (no connection pooling and the likes...)
 * </p>
 *
 * @author Fabien Sanglier
 */
@WebServlet("/SimpleJmsNonJCASendOneWay")
public class SimpleJmsNonJCASendOneWay extends BaseMessageProducer {
    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(SimpleJmsNonJCASendOneWay.class);

    @EJB(beanName = "JMSNonJCAOneWayPublisherBean")
    private JmsPublisherLocal jmsSimplePublisher;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        boolean useCached = Boolean.parseBoolean(req.getParameter("useCachedConnection"));

        out.write("<h1>Sending JMS message To Queue</h1>");
        try {
            String response = jmsSimplePublisher.sendTextMessage(messagePayload, messageProperties);

            out.write("<p><b>messages sent successfully</b></p>");
            out.write(String.format("<div><p>Response:</p><p>%s</p></div>", ((null != response) ? response : "null")));
            out.close();
        } catch (Throwable exc) {
            log.error("Error Occurred", exc);
            throw new ServletException(exc);
        }
    }
}
