///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
///////////////////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.publishers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Class for mailing list publication.
 *
 * @author Vladislav Lisetskii
 */
public class MailingListPublisher {

    /** Recipient address. */
    private static final String RECIPIENT_ADDRESS = "checkstyle-announce@googlegroups.com";
    /** Smtp host. */
    private static final String SMTP_HOST = "smtp.gmail.com";
    /** Smtp port. */
    private static final String SMTP_PORT = "587";
    /** Template for a subject. */
    private static final String SUBJECT_TEMPLATE = "Checkstyle %s";
    /** True constant. */
    private static final String TRUE = "true";

    /** Sender username. */
    private final String username;
    /** Sender password. */
    private final String password;
    /** The name of the file to get post text from. */
    private final String postFilename;
    /** Release number. */
    private final String releaseNumber;

    /**
     * Default constructor.
     *
     * @param postFilename the name of the file to get post from.
     * @param username username for publishing.
     * @param password password for publishing.
     * @param releaseNumber release number.
     */
    public MailingListPublisher(String postFilename, String username, String password,
        String releaseNumber) {
        this.postFilename = postFilename;
        this.username = username;
        this.password = password;
        this.releaseNumber = releaseNumber;
    }

    /**
     * Publish post.
     *
     * @throws MessagingException if an error occurs while publishing.
     * @throws IOException if there are problems with reading file with the post text.
     */
    public void publish() throws MessagingException, IOException {
        final Properties props = System.getProperties();
        props.setProperty("mail.smtp.starttls.enable", TRUE);
        props.setProperty("mail.smtp.host", SMTP_HOST);
        props.setProperty("mail.smtp.user", username);
        props.setProperty("mail.smtp.password", password);
        props.setProperty("mail.smtp.port", SMTP_PORT);
        props.setProperty("mail.smtp.auth", TRUE);

        final Session session = Session.getInstance(props, null);
        final MimeMessage mimeMessage = new MimeMessage(session);

        mimeMessage.setSubject(String.format(SUBJECT_TEMPLATE, releaseNumber));
        mimeMessage.setFrom(new InternetAddress(username));
        mimeMessage.addRecipients(Message.RecipientType.TO,
                InternetAddress.parse(RECIPIENT_ADDRESS));

        final String post = Files.readString(Paths.get(postFilename), StandardCharsets.UTF_8);

        final BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(post, "text/plain");

        final Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(messageBodyPart);
        mimeMessage.setContent(multipart);

        final Transport transport = session.getTransport("smtp");
        transport.connect(SMTP_HOST, username, password);
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
    }
}
