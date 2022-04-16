import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.google.gson.Gson;

public class VerifyEmailEvent implements RequestHandler<SNSEvent, Object> {
    public Object handleRequest(SNSEvent request, Context context) {
        String record = request.getRecords().get(0).getSNS().getMessage();
        UserMessage m = new Gson().fromJson(record, UserMessage.class);

        String FROM = "kwokyan@prod.csye6225kwokyan.me";
        String TO = m.getUsername();
        String SUBJECT = "User Verification Email";
        String TOKEN = m.getOne_time_token();

        // The HTML body for the email.
//        static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
//                + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
//                + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>"
//                + "AWS SDK for Java</a>";

        // The email body for recipients with non-HTML email clients.
        String TEXTBODY = "Please verify your email address. Verification Email is "  + TO + " and Token is " + TOKEN;

        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            // Replace US_WEST_2 with the AWS Region you're using for
                            // Amazon SES.
                            .withRegion(Regions.US_EAST_1).build();
            SendEmailRequest emailRequest = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
//                                    .withHtml(new Content()
//                                            .withCharset("UTF-8").withData(HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);
            // Comment or remove the next line if you are not using a
            // configuration set
//                    .withConfigurationSetName(CONFIGSET);
            client.sendEmail(emailRequest);
            context.getLogger().log("Email sent!");
        } catch (Exception ex) {
            context.getLogger().log("The email was not sent. Error message: "
                    + ex.getMessage());
        }

        context.getLogger().log("Record Message:" + record);


        return null;
    }
}
