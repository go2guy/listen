package com.interact.listen.gui;

import com.interact.listen.EmailerService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestNotificationSettingsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_TEST_NOTIFICATION_SETTINGS);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        String messageType = ServletUtil.getNotNullNotEmptyString("messageType", request, "Message Type");
        String address = ServletUtil.getNotNullNotEmptyString("address", request, "Address");

        EmailerService emailService = new EmailerService();

        if(!emailService.sendTestNotificationSettingsMessage(messageType, address))
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                             "An error occurred sending the test message", "text/plain");
        }
    }
}
