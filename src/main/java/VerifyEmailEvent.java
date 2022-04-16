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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;

import com.google.gson.Gson;


public class VerifyEmailEvent implements RequestHandler<SNSEvent, Object> {
    public Object handleRequest(SNSEvent request, Context context) {
        String record = request.getRecords().get(0).getSNS().getMessage();
        UserMessage m = new Gson().fromJson(record, UserMessage.class);

        String FROM = "j@prod.csye6225jinshuang.me";
        String TO = m.getUsername();
        String SUBJECT = "User Verification Email";
        String FIRST_NAME = m.getFirst_name();
        String LINK = m.getLink();

        String TEXTBODY = "please verify your account with the following link: " + LINK;

        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1).build();
            SendEmailRequest emailRequest = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);

            client.sendEmail(emailRequest);
            context.getLogger().log("Email sent!");
        } catch (Exception ex) {
            context.getLogger().log("The email was not sent. Error message: "
                    + ex.getMessage());
        }

        context.getLogger().log("Record Message:" + record);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        String tableName = "StatusTable";

        try {
            // Add a new item to Forum
            TableWriteItems tableWriteItems = new TableWriteItems(tableName) // Forum
                    .withItemsToPut(new Item().withPrimaryKey("Email", m.getUsername())
                            .withString("Status", "Sent"));
            BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);
            context.getLogger().log("item added to Status table");
        }
        catch (Exception e) {
            context.getLogger().log("Fail to add item. Error message: "
                    + e.getMessage());
        }
        return null;
    }
}
