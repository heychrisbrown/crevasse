final projectName = 'crevasse'
final vaultName = "family_media";
final endpoint = "https://glacier.us-east-1.amazonaws.com/"

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager
import com.amazonaws.services.glacier.transfer.UploadResult
import org.slf4j.*
import org.apache.commons.cli.ParseException

final dryrun = true
ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("AGC");

def cli = new CliBuilder(
        usage: "${projectName} [options] --from [path] --to [vault]",
        header: 'Options (or ask for --help):')
cli.with {
    h(longOpt: 'help', "this help")
    d(longOpt: 'debug', "enable debug logging")
    from(longOpt: 'from', "the path to copy from")
    to(longOpt: 'to', "the already-existing vault to copy to")
}
def options = cli.parse(args)
logger.setLevel(options.d ? ch.qos.logback.classic.Level.DEBUG : ch.qos.logback.classic.Level.INFO)
if (options.h) cli.usage()

AWSCredentials credentials = new PropertiesCredentials(
        AmazonGlacierClient.class.getResourceAsStream("AwsCredentials.properties"))
client = new com.amazonaws.services.glacier.AmazonGlacierClient(credentials)
client.setEndpoint(endpoint)
ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials)

dir = new File("/Users/cbrown/Pictures")
if (!(dir.isDirectory())) {
    println "${dir.canonicalPath} is not a directory."
    System.exit(1)
}

dir.traverse { archiveToUpload ->
    logger.debug("Processing ${archiveToUpload}")
    final description = "my archive " + (new Date())
    if (!dryrun) {
        UploadResult result = atm.upload(vaultName, description, new File(archiveToUpload))
        print "Archive ID: ${result.getArchiveId()}"
    }
}
