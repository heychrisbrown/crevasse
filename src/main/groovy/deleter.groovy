final projectName = 'deleter'
final endpoint = "https://glacier.us-east-1.amazonaws.com/"

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.services.glacier.model.DeleteArchiveRequest
import com.amazonaws.services.glacier.model.InitiateJobRequest
import com.amazonaws.services.glacier.model.InitiateJobResult
import com.amazonaws.services.glacier.model.JobParameters
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
    vault(longOpt: 'vault', "the vault to delete", args: 1, argName: 'vault')
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

InitiateJobRequest initJobRequest = new InitiateJobRequest().
        withVaultName(options.vault).
        withJobParameters(new JobParameters().withType("inventory-retrieval"))
;
InitiateJobResult initJobResult = client.initiateJob(initJobRequest);
String jobId = initJobResult.getJobId();
logger.info("created job=${jobId} for vault=${options.vault}")

