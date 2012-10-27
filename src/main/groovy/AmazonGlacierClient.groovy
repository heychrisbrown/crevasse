final projectName = 'crevasse'
final endpoint = "https://glacier.us-east-1.amazonaws.com/"

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager
import com.amazonaws.services.glacier.transfer.UploadResult
import org.slf4j.*
import org.apache.commons.cli.ParseException

final dryrun = true
ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("AGC")

def cli = new CliBuilder(
        usage: "${projectName} [options] --from [path] --to [vault]",
        header: 'Options:')
cli.with {
    h(longOpt: 'help', "this help")
    d(longOpt: 'debug', "enable debug logging")
    from(longOpt: 'from', args:1, argName:'path', 'the path to copy from')
    to(longOpt: 'to', args:1, argName:'vault', "the already-existing vault to copy to")
}
def options = cli.parse(args)
logger.setLevel(options.d ? ch.qos.logback.classic.Level.DEBUG : ch.qos.logback.classic.Level.INFO)
if (options.h) {
    cli.usage()
    System.exit(0)
}

if (!(options.from && options.to)) {
    cli.usage()
    System.exit(1)
}


AWSCredentials credentials = new PropertiesCredentials(
        AmazonGlacierClient.class.getResourceAsStream("AwsCredentials.properties"))
client = new com.amazonaws.services.glacier.AmazonGlacierClient(credentials)
client.setEndpoint(endpoint)
ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials)
logger.info("copying from directory=${options.from} to vault=${credentials.AWSAccessKeyId}.${options.to}")

dir = new File(options.from)
if (!(dir.isDirectory())) {
    logger.fatal "not a directory, directory=${dir.canonicalPath}"
    System.exit(1)
}

dir.traverse { archiveToUpload ->
    logger.debug("Processing ${archiveToUpload}")
    final description = archiveToUpload
    if (!dryrun) {
        UploadResult result = atm.upload(options.to, description, archiveToUpload)
        logger.info("Uploaded from path=${archiveToUpload} to archive=${result.getArchiveId()} in vault=${options.to}")
    }
}
