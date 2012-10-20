final vaultName = "family_media";
final archiveToUpload = "/Users/cbrown/Pictures/Thalia's note to Owen.jpeg";
final endpoint = "https://glacier.us-east-1.amazonaws.com/"
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;


AWSCredentials credentials = new PropertiesCredentials(
        AmazonGlacierClient.class.getResourceAsStream("AwsCredentials.properties"))
client = new com.amazonaws.services.glacier.AmazonGlacierClient(credentials)
client.setEndpoint(endpoint)
ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials)

UploadResult result = atm.upload(vaultName, "my archive " + (new Date()), new File(archiveToUpload))

print "Archive ID: ${result.getArchiveId()}"
