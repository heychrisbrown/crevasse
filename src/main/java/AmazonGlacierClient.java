import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;


public class AmazonGlacierClient {
    public static String vaultName = "family_media";
    public static String archiveToUpload = "/Users/cbrown/Pictures/Thalia's note to Owen.jpeg";

    public static com.amazonaws.services.glacier.AmazonGlacierClient client;

    public static void main(String[] args) throws IOException {

        AWSCredentials credentials = new PropertiesCredentials(
                AmazonGlacierClient.class.getResourceAsStream("AwsCredentials.properties"));
        client = new com.amazonaws.services.glacier.AmazonGlacierClient(credentials);
        client.setEndpoint("https://glacier.us-east-1.amazonaws.com/");

        try {
            ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);

            UploadResult result = atm.upload(vaultName, "my archive " + (new Date()), new File(archiveToUpload));
            System.out.println("Archive ID: " + result.getArchiveId());

        } catch (Exception e)
        {
            System.err.println(e);
        }
    }

}