final projectName = 'crevasse'
final endpoint = "https://glacier.us-east-1.amazonaws.com/"

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.services.glacier.model.InitiateJobRequest
import com.amazonaws.services.glacier.transfer.UploadResult
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager
import org.slf4j.*
import org.apache.commons.cli.ParseException
import com.amazonaws.AmazonServiceException


ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(projectName)

def cli = new CliBuilder(
        usage: "${projectName} [options] --from [path] --to [vault] --credentials [file with your AWS credentials]",
        header: 'Options:')
cli.with {
    h(longOpt: 'help', "this help")
    d(longOpt: 'debug', "enable debug logging")
    dryrun(longOpt: 'dryrun', "don't actually delete")
    vault(longOpt: 'vault', "the vault to delete")
    credentials(longOpt: 'credentials', args: 1, argName: 'credentials', "file containing AWS credentials", required: true)
}
def options = cli.parse(args)
if (!options) System.exit(0)

if (options.h) {
    cli.usage()
    System.exit(0)
}

logger.setLevel(options.d ? ch.qos.logback.classic.Level.DEBUG : ch.qos.logback.classic.Level.INFO)

AWSCredentials credentials = new PropertiesCredentials(new File(options.credentials))
client = new com.amazonaws.services.glacier.AmazonGlacierClient(credentials)
client.setEndpoint(endpoint)
ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials)
logger.info("deleting from vault=${credentials.AWSAccessKeyId}.${options.vault}")


dir.traverse { fileToUpload ->
    logger.debug("Processing ${fileToUpload}")
    final description = fileToUpload
    if (!options.dryrun) {
        try {

            UploadResult result = atm.upload(options.to, description.name, fileToUpload)
        } catch (AmazonServiceException ase) {
            if (ase.getStatusCode() == 408) {  // angry ISP?
                logger.info("Sleeping, will retry")
                sleep(1000 * 60 * 5);
                UploadResult result = atm.upload(options.to, description.name, fileToUpload)
            }
        }
        logger.info("Deleted archive=${result.getArchiveId()} in vault=${options.vault}")
    }
}
